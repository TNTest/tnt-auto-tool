package com.tntest.automation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/*
 * 		// http://tc.hd.xiaomi.com/hdget?callback=hdcontrol&_=1381810323809

 */
public class MainProcess {

	public final FileConfiguration CONFS;
	public final Properties urls;

	private static Logger log = Logger.getLogger(MainProcess.class);
	
	public MainProcess() {
		CONFS = FileConfiguration.generateInstance();
		checkPreCondition();
		urls = new Properties();
		urlFIleInit();
		FireFoxThread.setUrls(urls);
	}
	
	public Properties getUrls() {
		return urls;
	}

	public static void main(String[] args) {
		MainProcess m = new MainProcess();
		m.startThreads();
	}

	public void startThreads() {
		// int stn = CONFS.getSimThreadNum();
		int dtn = CONFS.getDrctThreadNum();
		List<Account> acs = CONFS.getAccounts();
		// simulate user action mode
		/*
		 * for (int i = 0; i < stn; i++) { Entry<String, String> ac = acs[i %
		 * acs.length]; FireFoxThread t = new FireFoxThread();
		 * t.setAc_name(ac.getKey()); t.setAc_pwd(ac.getValue()); t.setMode(1);
		 * t.start(); log.info("simulate browser thread[" + (i + 1) +
		 * "](account: " + t.getAc_name() + ") starting...");
		 * 
		 * }
		 */
		// direct call ajax mode
		for (int i = 0; i < dtn; i++) {
			Account ac = acs.get(i % acs.size());
			FireFoxThread t = new FireFoxThread(CONFS);
			t.setAc(ac);
			t.setMode(2);
			t.start();
			log.info("direct call browser thread[" + (i + 1) + "](account: " + t.getAc().getId() + ") starting...");

		}
	}

	private void urlFIleInit() {
		try {
			File urlFile = new File("url.properties");
			if (!urlFile.exists()) {
				urlFile.createNewFile();
			}
			urls.load(new InputStreamReader(new FileInputStream(urlFile), "UTF-8"));
		} catch (Exception e) {
			log.error("load url list file failed!", e);
		}
	}

	private void checkPreCondition() {
		long now = System.currentTimeMillis();
		Date endTime = CONFS.getRushEndTime();
		if (now - endTime.getTime() > 0) {
			log.warn("End time[" + endTime + "] is up. Exiting JVM!");
			System.exit(0);
		}
	}

}

class FireFoxThread extends Thread {

	private FileConfiguration CONFS;
	private WebDriver driver = new FirefoxDriver();
	private static Logger log = Logger.getLogger(FireFoxThread.class);
	//multi-instance sharing
	private static Properties urls = new Properties();

	private Account ac;
	private int mode;
	
	public FireFoxThread(FileConfiguration conf){
		CONFS = conf;
	}

	public void run() {
		int action_timeout = CONFS.getActionTimeOut();
		driver.manage().timeouts().implicitlyWait(action_timeout, TimeUnit.SECONDS);

		login();
		if (getMode() == 1) {
			rushBuyBySimulate();
		} else if (getMode() == 2) {
			rushBuyByDirect();
		}

	}

	public void login() {

		driver.get(CONFS.getLoginUrl());
		driver.findElement(By.id("user")).clear();
		driver.findElement(By.id("user")).sendKeys(ac.getId());
		driver.findElement(By.id("pwd")).clear();
		driver.findElement(By.id("pwd")).sendKeys(ac.getPwd());
		driver.findElement(By.cssSelector("input.no_bg")).click();

	}

	public void rushBuyBySimulate() {
		sleepToStartTime();
		driver.get(CONFS.getRushBuyEntryUrl());
		WebDriverWait wait = new WebDriverWait(driver, CONFS.getActionTimeOut());
		JavascriptExecutor jsRun = (JavascriptExecutor) driver;

		// loop until find next buy button
		boolean success = false;
		boolean timeEnd = false;
		boolean boxEnable = false;
		int count = 1;
		while (!timeEnd && !success) {
			try {

				String originUrl = driver.getCurrentUrl();
				String startJsStr = CONFS.getStartJSStr();
				String showBoxJsStr = CONFS.getShowBoxJSStr();

				// show box
				if (!boxEnable) {
					jsRun.executeScript(showBoxJsStr);
					wait.until(ExpectedConditions.visibilityOf(driver.findElement(By.xpath("//body"))));
					boxEnable = true;
				} else {
					// make button available
					jsRun.executeScript(startJsStr);
					wait.until(ExpectedConditions.visibilityOf(driver.findElement(By.xpath("//body"))));
					// click button
					driver.findElement(By.id(CONFS.getRebackId())).click();
					wait.until(ExpectedConditions.visibilityOf(driver.findElement(By.xpath("//body"))));
					log.info("[" + count + "](account: " + ac.getId() + ")call: " + startJsStr
							+ " and reback.click() via webdriver.");

					String currentUrl = driver.getCurrentUrl();
					log.info("Last url:" + originUrl + ", current url: " + currentUrl);
					if (currentUrl != null && !currentUrl.equals(originUrl)) {
						success = true;
						log.info("Maybe you[" + ac.getId() + "] success!");
					}
					Thread.sleep(CONFS.getLoopCallInterval());
				}
			} catch (TimeoutException e) {
				log.info("finding element timeout. \n" + e.getMessage().substring(0, e.getMessage().indexOf("\n")));
			} catch (WebDriverException e) {
				log.error("web driver exception: " + e.getMessage().substring(0, e.getMessage().indexOf("\n")));
				if (!success) {
					driver.navigate().refresh();
				}
			} catch (Exception e) {
				log.error("error", e);
				if (!success) {
					driver.navigate().refresh();
				}
			}
			long now = System.currentTimeMillis();
			timeEnd = now - CONFS.getRushEndTime().getTime() > 0 ? true : false;
			count++;
		}
		if (timeEnd)
			log.info("End time[" + CONFS.getRushEndTime() + "] is up. ");

	}

	public void rushBuyByDirect() {
		sleepToStartTime();
		WebDriverWait normaWait = new WebDriverWait(driver, CONFS.getActionTimeOut());
		JavascriptExecutor jsRun = (JavascriptExecutor) driver;

		boolean success = false;
		int count = 1;
		boolean timeEnd = false;
		Long gap = 1L;

		while (!timeEnd && !success) {
			String jsonStr = null;
			String jsSource = "";
			String urlKey = CONFS.getRushStartTimeStr() + "-" + ac.getBuyType();
			try {
				// implicit call
				Long now_tmstp = System.currentTimeMillis();
				log.info("current time (millis): " + now_tmstp);

				/*
				 * driver.manage().addCookie( new Cookie("xm_difft_hd ", "5",
				 * ".xiaomi.com", "/", new Date(System.currentTimeMillis() +
				 * 1)));
				 */
				String setCookieStr = CONFS.getSetCookieJsByValue(gap.toString());
				log.debug("run set cookie js: " + setCookieStr);
				jsRun.executeScript(setCookieStr);
				normaWait.until(ExpectedConditions.visibilityOf(driver.findElement(By.xpath("//body"))));
				// int max_gap = CONFS.getDirectMaxGap();
				String direct_call_url = CONFS.getDirectCallUrl();
				direct_call_url += now_tmstp;
				driver.get(direct_call_url);
				log.info("[" + count + "]request: " + direct_call_url);
				normaWait.until(ExpectedConditions.visibilityOf(driver.findElement(By.xpath("//body"))));
				log.debug("page source: " + driver.getPageSource());
				// quickWait.until(ExpectedConditions.visibilityOf(driver.findElement(By.xpath("//pre"))));

				driver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);
				jsSource = driver.findElement(By.xpath("//pre")).getText().trim();
				jsonStr = jsSource.substring("hdcontrol(".length(), jsSource.length() - ")".length());
				driver.manage().timeouts().implicitlyWait(CONFS.getActionTimeOut(), TimeUnit.SECONDS);

				JSONObject jsonObj = new JSONObject(jsonStr);
				log.debug("json object: " + jsonObj);
				/*
				 * if (driver.manage().getCookieNamed("xm_difft_hd") != null)
				 * driver.manage().deleteCookieNamed("xm_difft_hd");
				 * driver.manage().addCookie( new Cookie("xm_difft_hd ",
				 * gap.toString(), ".xiaomi.com", "/", new Date(System
				 * .currentTimeMillis() + 1)));
				 */

				JSONObject statusObj = jsonObj.getJSONObject("status");
				String buyType = ac.getBuyType();
				String nextJumpSuffix = statusObj.getJSONObject(buyType).getString("hdurl");
				String nextBaseUrl = CONFS.getNextBaseUrl();
				String nextJumpUrl = "";
				//record the url
				if (nextJumpSuffix != null && !nextJumpSuffix.isEmpty()) {
					nextJumpUrl = urls.getProperty(urlKey);
					if (nextJumpUrl == null || nextJumpUrl.isEmpty()) {
						nextJumpUrl = nextBaseUrl+nextJumpSuffix;
						urls.setProperty(urlKey, nextJumpUrl);
						urls.store(new OutputStreamWriter(new FileOutputStream(new File("url.properties")),
								"UTF-8"), "");
						log.debug("[" + ac.getId() + "]Writed jump url to url.properties: " + CONFS.getRushStartTimeStr() + "="+ nextJumpUrl);

					}
				}

				if (statusObj.getBoolean("allow")) {
					log.info("Got allow=true! status object: " + statusObj);
					
					if (nextJumpSuffix == null || nextJumpSuffix.isEmpty()) {
						log.debug("[" + ac.getId() + "]failed to get url from json.");
						nextJumpUrl = urls.getProperty(urlKey);
						log.debug("[" + ac.getId() + "]Got the url from url.properties: " + nextJumpUrl);

					}
					
					if (nextJumpUrl != null && !nextJumpUrl.isEmpty()) {
						success = true;
						log.info("[" + ac.getId() + "] Finally got he next jump url: " + nextJumpUrl);
						driver.get(nextJumpUrl);
						normaWait.until(ExpectedConditions.visibilityOf(driver.findElement(By.xpath("//body"))));
						log.info("[" + ac.getId() + "] finishing rush buy.");
					} else {
						log.info("[" + ac.getId() + "]failed to get the next jump url, because of empty hdurl and not found in url.properties.");
					}
					

				} else {
					log.info("Maybe you[" + ac.getId() + "] failed to jump to next step!");
				}
				// calculate diff time
				Long stime = jsonObj.getLong("stime");
				gap = (now_tmstp / 1000 - stime); // second
				log.info("gap between local and server(second): " + gap);

				Thread.sleep(CONFS.getLoopCallInterval());

			} catch (JSONException e) {
				log.error("Parsing json failed! json string: " + jsonStr, e);
			} catch (TimeoutException e) {
				log.info("finding element timeout. \n" + e.getMessage().substring(0, e.getMessage().indexOf("\n")));
				// log.info("finding element timeout. \n", e);

			} catch (WebDriverException e) {
				log.error("web driver exception: " + e.getMessage().substring(0, e.getMessage().indexOf("\n")), e);
				// log.info("web driver exception: ", e);

			} catch (Exception e) {
				log.error("error! ", e);
			}
			long now = System.currentTimeMillis();
			timeEnd = now - CONFS.getRushEndTime().getTime() > 0 ? true : false;
			count++;
		}
		if (timeEnd) {
			log.info("End time[" + CONFS.getRushEndTime() + "] is up. ");
		}
	}

	private void sleepToStartTime() {
		Date rst = CONFS.getRushStartTime();
		log.info("rush start time (millis): " + rst);
		Long waitTime = (rst.getTime() - System.currentTimeMillis()); // millis
		try {// wait to start rush buy
			if (waitTime > 0) {
				log.info("Sleeping " + (waitTime / 1000) + "s to get to start time[" + rst + "]...");
				Thread.sleep(waitTime);
			}
		} catch (InterruptedException e) {
			log.error("thread exception: ", e);
		}
	}

	public int getMode() {
		return mode;
	}

	public void setMode(int mode) {
		this.mode = mode;
	}

	public Account getAc() {
		return ac;
	}

	public void setAc(Account ac) {
		this.ac = ac;
	}

	public static void setUrls(Properties urls) {
		FireFoxThread.urls = urls;
	}

	public FileConfiguration getCONFS() {
		return CONFS;
	}

	public void setCONFS(FileConfiguration cONFS) {
		CONFS = cONFS;
	}

}