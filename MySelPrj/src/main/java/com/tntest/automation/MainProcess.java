package com.tntest.automation;

import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

/*
 * 		// http://tc.hd.xiaomi.com/hdget?callback=hdcontrol&_=1381810323809

 */
public class MainProcess {

	private static final FileConfiguration CONFS = FileConfiguration.getInstance();

	public static void main(String[] args) {
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
		}
		// direct call ajax mode
		for (int i = 0; i < dtn; i++) {
			Entry<String, String> ac = acs[i % acs.length];
			FireFoxThread t = new FireFoxThread();
			t.setAc_name(ac.getKey());
			t.setAc_pwd(ac.getValue());
			t.setMode(2);
			t.start();
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

		/*
		 * (new WebDriverWait(driver, 10)).until(new
		 * ExpectedCondition<Boolean>() { public Boolean apply(WebDriver d) {
		 * return driver.findElement(By.id(CONFS.getImdtBuyId())).; } });
		 */
		// Date now = new Date();
		// Date rst = CONFS.getRushStartTime();
		// long duration = (rst.getTime() - now.getTime()) / 1000; // seconds
		// driver.findElement(By.id(CONFS.getImdtBuyId())).click();
		// log.debug("body: " +
		// driver.findElement(By.cssSelector("BODY")).getText());

		// loop until find rush buy button
		boolean startRush = false;
		while (!startRush) {
			try {
				/*
				 * (new WebDriverWait(driver, 10)).until(ExpectedConditions
				 * .presenceOfAllElementsLocatedBy(By.id(CONFS
				 * .getImdtBuyId())));
				 */
				driver.findElement(By.id(CONFS.getImdtBuyId())).click();
				startRush = true;
			} catch (TimeoutException e) {
				log.info("finding element timeout.", e);
			} catch (Exception e) {
				log.error("error", e);
			}
			if (!startRush) {
				driver.navigate().refresh();
			}
		}

		// driver.findElement(By.id("reback")).click();
		// redo queue otherwise finished.
		boolean canQueue = true;
		boolean win = false;
		while (canQueue) {
			try {
				new WebDriverWait(driver, 15).until(new ExpectedCondition<Boolean>() {
					public Boolean apply(WebDriver d) {
						return d.findElement(By.id(CONFS.getRebackId())).getText().endsWith(CONFS.getRebackText());
					}
				});
				driver.findElement(By.id(CONFS.getRebackId())).click();
			} catch (TimeoutException e) {
				log.info("finding element timeout. ", e);
				canQueue = false;
				String bodyStr = driver.findElement(By.cssSelector("BODY")).getText();
				if ( bodyStr.contains(CONFS.getSimBuySuccessText()) ) {
					win = true;
					log.info("Maybe you win! detail: \n" + bodyStr);
				} else if (bodyStr.contains(CONFS.getSimBuyFailText()) ) {
					log.info("Maybe you fail! detail: \n" + bodyStr);

				}else {
					log.info("Unknown...  detail: \n" + bodyStr);
				}
			} catch (Exception e) {
				log.error("error", e);
			}
		}

		// driver.findElement(By.linkText("重新进入")).click();

	}

	public void rushBuyByDirect() {
		Long rst = CONFS.getRushStartTime().getTime();
		log.info("rush start time (millis): " + rst);
		Long now = System.currentTimeMillis();
		Long waitTime = (rst - now); // millis
		try {//wait to start rush buy
			if (waitTime > 0)
				Thread.sleep(waitTime);
		} catch (InterruptedException e) {
			log.error("thread exception: ", e);
		}
		boolean win = false;
		int max_count = CONFS.getDirectMaxCount();
		int count = 0;
		while (count <= max_count && !win) {
			Long tmstp = System.currentTimeMillis();
			log.info("current time (millis): " + tmstp);

			Long gap = (tmstp - rst) / 1000; // second
			int max_gap = CONFS.getDirectMaxGap();
			String direct_call_url = CONFS.getDirectCallUrl();
			if (gap > 0 && gap <= max_gap) // <= max_gap seconds
				driver.get(direct_call_url + tmstp);
			else {
				tmstp = rst + (int) (Math.random() * 1000 * max_gap);
				driver.get(direct_call_url + tmstp); // <=max gap seconds
																							
			}
			String pageSource = driver.getPageSource();
			String directWinStr =CONFS.getDirectWinStr();
			if (pageSource.contains(directWinStr)) {
				log.info("Maybe you win! response: \n" + pageSource.substring(0, pageSource.length() > 1000?1000:pageSource.length()) + "...\n --account: "
						+ getAc_name() + ", --timestamp: " + tmstp + ", count:" + count);
				win = true;
			} else {
				log.info("Maybe you fail! response: \n" + pageSource.substring(0, pageSource.length() > 1000?1000:pageSource.length())  + "...\n --account: "
						+ getAc_name() + ", --timestamp: " + tmstp + ", count:" + count);
			}
			count++;
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