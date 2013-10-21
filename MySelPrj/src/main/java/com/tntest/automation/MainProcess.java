package com.tntest.automation;

import java.util.Date;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
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

	private static final FileConfiguration CONFS = FileConfiguration.getInstance();
	private static Logger log = Logger.getLogger(MainProcess.class);

	public static void main(String[] args) {
		long now = System.currentTimeMillis();
		Date endTime = CONFS.getRushEndTime();
		if (now - endTime.getTime() > 0) {
			log.warn("End time[" + endTime + "] is up. Exiting JVM!");
			System.exit(0);
		}
		int stn = CONFS.getSimThreadNum();
		int dtn = CONFS.getDrctThreadNum();
		Entry<String, String>[] acs = CONFS.getAccounts();
		// simulate user action mode
		for (int i = 0; i < stn; i++) {
			Entry<String, String> ac = acs[i % acs.length];
			FireFoxThread t = new FireFoxThread();
			t.setAc_name(ac.getKey());
			t.setAc_pwd(ac.getValue());
			t.setMode(1);
			t.start();
			log.info("simulate browser thread[" + (i + 1) + "](account: " + t.getAc_name() + ") starting...");

		}
		// direct call ajax mode
		for (int i = 0; i < dtn; i++) {
			Entry<String, String> ac = acs[i % acs.length];
			FireFoxThread t = new FireFoxThread();
			t.setAc_name(ac.getKey());
			t.setAc_pwd(ac.getValue());
			t.setMode(2);
			t.start();
			log.info("direct call browser thread[" + (i + 1) + "](account: " + t.getAc_name() + ") starting...");

		}
	}

}

class FireFoxThread extends Thread {

	private static final FileConfiguration CONFS = FileConfiguration.getInstance();
	private WebDriver driver = new FirefoxDriver();
	private static Logger log = Logger.getLogger(FireFoxThread.class);

	private String ac_name;
	private String ac_pwd;
	private int mode;

	public void run() {
		int action_timeout = CONFS.getActionTimeOut();
		driver.manage().timeouts().implicitlyWait(action_timeout, TimeUnit.SECONDS);

		login();
		if (getMode() == 1)
			rushBuyBySimulate();
		else if (getMode() == 2) {
			rushBuyByDirect();
		}

	}

	public void login() {

		driver.get(CONFS.getLoginUrl());
		driver.findElement(By.id("user")).clear();
		driver.findElement(By.id("user")).sendKeys(ac_name);
		driver.findElement(By.id("pwd")).clear();
		driver.findElement(By.id("pwd")).sendKeys(ac_pwd);
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
					log.info("[" + count + "](account: " + this.ac_name + ")call: " + startJsStr
							+ " and reback.click() via webdriver.");

					String currentUrl = driver.getCurrentUrl();
					log.info("Last url:" + originUrl + ", current url: " + currentUrl);
					if (currentUrl != null && !currentUrl.equals(originUrl)) {
						success = true;
						log.info("Maybe you[" + this.ac_name + "] success!");
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
		// prepare cookie
		driver.manage().addCookie(
				new Cookie("xm_difft_hd ", "53", ".xiaomi.com", "/", new Date(System.currentTimeMillis() + 1)));
		boolean success = false;
		int count = 1;
		boolean timeEnd = false;
		while (!timeEnd && !success) {
			String jsonStr = null;
			String jsSource = "";
			try {
				// implicit call
				WebDriverWait normaWait = new WebDriverWait(driver, CONFS.getActionTimeOut());
				WebDriverWait quickWait = new WebDriverWait(driver, 1);
				Long now_tmstp = System.currentTimeMillis();
				log.info("current time (millis): " + now_tmstp);

				// int max_gap = CONFS.getDirectMaxGap();
				String direct_call_url = CONFS.getDirectCallUrl();
				direct_call_url += now_tmstp;
				driver.get(direct_call_url);
				log.info("[" + count + "]request: " + direct_call_url);
				normaWait.until(ExpectedConditions.visibilityOf(driver.findElement(By.xpath("//body"))));
				log.info("page source: " + driver.getPageSource().substring(0, 1000));
				// quickWait.until(ExpectedConditions.visibilityOf(driver.findElement(By.xpath("//pre"))));

				driver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);
				jsSource = driver.findElement(By.xpath("//pre")).getText().trim();
				jsonStr = jsSource.substring("hdcontrol(".length(), jsSource.length() - ")".length());
				driver.manage().timeouts().implicitlyWait(CONFS.getActionTimeOut(), TimeUnit.SECONDS);

				JSONObject jsonObj = new JSONObject(jsonStr);
				log.debug("json object: " + jsonObj);
				Long stime = jsonObj.getLong("stime");

				Long gap = (now_tmstp / 1000 - stime); // second
				log.info("gap between local and server(second): " + gap);
				driver.manage().deleteCookieNamed("xm_difft_hd");
				driver.manage().addCookie(
						new Cookie("xm_difft_hd ", gap.toString(), ".xiaomi.com", "/", new Date(System
								.currentTimeMillis() + 1)));

				JSONObject statusObj = jsonObj.getJSONObject("status");
				if (statusObj.getBoolean("allow")) {
					success = true;
					String directCallUrl = statusObj.getJSONObject("miphone").getString("hdurl");
					directCallUrl += CONFS.getDirectCallUrl();
					log.info("[" + this.ac_name + "]Succeed to get the next jump url: " + directCallUrl);
					driver.get(directCallUrl);
					normaWait.until(ExpectedConditions.visibilityOf(driver.findElement(By.xpath("//body"))));
				} else {
					log.info("Maybe you[" + this.ac_name + "] failed to jump to next step!");
				}
				Thread.sleep(CONFS.getLoopCallInterval());
			} catch (JSONException e) {
				log.error("Parsing json failed! json string: " + jsonStr, e);
			} catch (TimeoutException e) {
				log.info("finding element timeout. \n" + e.getMessage().substring(0, e.getMessage().indexOf("\n")));
				// log.info("finding element timeout. \n", e);

			} catch (WebDriverException e) {
				log.error("web driver exception: " + e.getMessage().substring(0, e.getMessage().indexOf("\n")));
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

	public String getAc_name() {
		return ac_name;
	}

	public void setAc_name(String ac_name) {
		this.ac_name = ac_name;
	}

	public String getAc_pwd() {
		return ac_pwd;
	}

	public void setAc_pwd(String ac_pwd) {
		this.ac_pwd = ac_pwd;
	}

	public int getMode() {
		return mode;
	}

	public void setMode(int mode) {
		this.mode = mode;
	}

}