package com.tntest.automation;

import java.io.StringReader;
import java.util.Date;
import java.util.Map.Entry;
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
		driver.get(CONFS.getRushBuyEntryUrl());

		// loop until find rush buy button
		boolean success = false;
		boolean timeEnd = false;
		int count = 1;
		while (!timeEnd && !success) {
			try {

				String originUrl = driver.getCurrentUrl();
				JavascriptExecutor jsRun = (JavascriptExecutor) driver;
				WebDriverWait wait = new WebDriverWait(driver, CONFS.getActionTimeOut());

				String jsStr = CONFS.getRunJSStr();
				// make button available
				jsRun.executeScript(jsStr);
				wait.until(ExpectedConditions.visibilityOf(driver.findElement(By.xpath("//body"))));
				// click button
				driver.findElement(By.id(CONFS.getRebackId())).click();
				wait.until(ExpectedConditions.visibilityOf(driver.findElement(By.xpath("//body"))));
				log.info("[" + count + "](account: " + this.ac_name + ")call: " + jsStr
						+ " and reback.click() via webdriver.");

				String currentUrl = driver.getCurrentUrl();
				log.info("Last url:" + originUrl + ", current url: " + currentUrl);
				if (currentUrl != null && !currentUrl.equals(originUrl)) {
					success = true;
					log.info("Maybe you[" + this.ac_name + "] win!");
				}
			} catch (TimeoutException e) {
				log.info("finding element timeout. \n" +   e.getMessage().substring(0, e.getMessage().indexOf("\n")));
			} catch (WebDriverException e) {
				log.error("web driver exception: " + e.getMessage().substring(0, e.getMessage().indexOf("\n")));
				if (!success) { driver.navigate().refresh(); }
			} catch (Exception e) {
				log.error("error", e);
				if (!success) { driver.navigate().refresh(); }
			}
			long now = System.currentTimeMillis();
			timeEnd = now - CONFS.getRushEndTime().getTime() > 0 ? true : false;
			count++;
			/*
			 * if (!success) { driver.navigate().refresh(); }
			 */
		}
		if (timeEnd)
			log.info("End time["+CONFS.getRushEndTime()+"] is up. ");


	}

	public void rushBuyByDirect() {
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
		boolean success = false;
		int count = 1;
		boolean timeEnd = false;
		while (!timeEnd && !success) {
			String jsonStr = null;
			String jsSource = null;
			try {
				//implicit call, 5s timeout
				WebDriverWait normaWait = new WebDriverWait(driver, CONFS.getActionTimeOut());
				WebDriverWait quickWait = new WebDriverWait(driver, 1);
				Long tmstp = System.currentTimeMillis();
				log.info("current time (millis): " + tmstp);

				Long gap = (tmstp - rst.getTime()) / 1000; // second
				log.info("gap time: " + gap);
				// int max_gap = CONFS.getDirectMaxGap();
				String direct_call_url = CONFS.getDirectCallUrl();
				direct_call_url += tmstp;
				driver.get(direct_call_url);
				log.info("[" + count + "]request: " + direct_call_url);
				normaWait.until(ExpectedConditions.visibilityOf(driver.findElement(By.xpath("//body"))));
				log.info("page source: " + driver.getPageSource().substring(0, 1000));
				//quickWait.until(ExpectedConditions.visibilityOf(driver.findElement(By.xpath("//pre"))));
				jsSource = driver.findElement(By.xpath("//pre")).getText().trim();
				jsonStr = jsSource.substring("hdcontrol(".length(), jsSource.length() - ")".length());

				JSONObject jsonObj = new JSONObject(jsonStr);
				log.debug("json object: " + jsonObj);
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
			} catch (JSONException e) {
				log.error("Parsing json failed! json string: " + jsonStr, e);
			} catch (TimeoutException e) {
				log.info("finding element timeout. \n" +   e.getMessage().substring(0, e.getMessage().indexOf("\n")));
			} catch (WebDriverException e) {
				log.error("web driver exception: " + e.getMessage().substring(0, e.getMessage().indexOf("\n")));
			} catch (Exception e) {
				log.error("error! ", e);
			}
			long now = System.currentTimeMillis();
			timeEnd = now - CONFS.getRushEndTime().getTime() > 0 ? true : false;
			count++;
		}
		if (timeEnd){
			log.info("End time["+CONFS.getRushEndTime()+"] is up. ");
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