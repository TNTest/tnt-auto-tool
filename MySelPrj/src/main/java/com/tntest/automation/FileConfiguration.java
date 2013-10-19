package com.tntest.automation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.apache.log4j.Logger;

/**
 * 
 * 
 * @author WYT
 * 
 */
public class FileConfiguration extends FileAlterationListenerAdaptor {

	private static final String PATH_SYS_CONF = "conf/conf.properties";
	private static Logger log = Logger.getLogger(FileConfiguration.class);
	private static Properties ps = new Properties();
	private static FileConfiguration _instance;
	
	public String getDirectCallUrl() {
		return ps.getProperty("direct.call.url");
	}
	
	public String getRebackId() {
		return ps.getProperty("reback.id");
	}
	
	public String getLoginUrl() {
		return ps.getProperty("login.url");
	}
	
	public String getRushBuyEntryUrl() {
		return ps.getProperty("rushbuy.entry.url");
	}
	
	public String getRunJSStr() {
		return ps.getProperty("simulate.js.string");
	}

	public int getActionTimeOut() {
		String numStr = ps.getProperty("action.timeout");
		return Integer.valueOf(numStr);
	}
	
	public Date getRushStartTime() {
		Date rstDate = null;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			rstDate = sdf.parse(ps.getProperty("rush.start.time"));
		} catch (ParseException e) {
			log.fatal("rush start time parsing failed!!!", e);
		}
		return rstDate;
	}
	
	public Date getRushEndTime() {
		Date retDate = null;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			retDate = sdf.parse(ps.getProperty("rush.end.time"));
		} catch (ParseException e) {
			log.fatal("rush end time parsing failed!!!", e);
		}
		return retDate;
	}
	
	public Entry<String, String>[] getAccounts() {
		Map <String, String> acMap = new HashMap<String, String>();
		String accountsStr = ps.getProperty("accounts");
		String[] accounts = accountsStr.split(",");
		if (accounts != null && accounts.length > 0) {
			for (int i = 0; i < accounts.length; i++) {
				String[] account = accounts[i].split(":");
				if (account != null && account.length == 2){
					acMap.put(account[0], account[1]);
				}
				
			}
		}
		log.info("load account: " + acMap.keySet().toString());
		return acMap.entrySet().toArray(new Entry[0]);
	}
	
	public int getSimThreadNum() {
		log.info(ps);
		String numStr = ps.getProperty("simulate.thread.num");
		return Integer.valueOf(numStr);
	}
	
	public int getDrctThreadNum() {
		String numStr = ps.getProperty("direct.thread.num");
		return Integer.valueOf(numStr);
	}

	/**
	 * 
	 */
	@Override
	public void onFileCreate(File file) {
	}

	/**
	 * 
	 */
	@Override
	public void onFileChange(File file) {
		loadConfs();
		log.info("Configuration changed, reloading configruation: " + ps);
	}

	/**
	 * 
	 */
	@Override
	public void onFileDelete(File file) {
		log.error("Configuration file is deleted!");
	}

	/**
	 * 
	 */
	@Override
	public void onDirectoryCreate(File directory) {
	}

	/**
	 * 
	 */
	@Override
	public void onDirectoryChange(File directory) {
	}

	/**
	 * 
	 */
	@Override
	public void onDirectoryDelete(File directory) {
	}

	@Override
	public void onStart(FileAlterationObserver observer) {
		super.onStart(observer);
	}

	public void loadConfs() {
		try {
			ps.load(new InputStreamReader( new FileInputStream(new File(PATH_SYS_CONF)), "UTF-8"));
		} catch (FileNotFoundException e) {
			log.error("sys.conf cannot be found!", e);
		} catch (IOException e) {
			log.error("cannot read sys.conf!", e);
		}
	}

	@Override
	public void onStop(FileAlterationObserver observer) {
		// TODO Auto-generated method stub
		super.onStop(observer);
	}

	synchronized public static FileConfiguration getInstance() {

		if (_instance == null) {
			long interval = TimeUnit.SECONDS.toMillis(1);
			FileAlterationObserver observer = new FileAlterationObserver(
					PATH_SYS_CONF);

			_instance = new FileConfiguration();
			_instance.loadConfs();
			log.info("First load configruation: " + ps);

			observer.addListener(_instance); 
			FileAlterationMonitor monitor = new FileAlterationMonitor(interval,
					observer);
			try {
				monitor.start();
			} catch (Exception e) {
				log.fatal("Init configuration failed! Exit JVM!!!", e);
				System.exit(1);
			}
		}

		return _instance;
	}

}