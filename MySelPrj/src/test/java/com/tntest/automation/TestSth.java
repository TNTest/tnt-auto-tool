package com.tntest.automation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

public class TestSth {
	
	public static void main(String[] args) {
		
		testMultiConfs5();
	}
	
	public static void sth1() {
		String jsonStr ="{'stime':1382069086,'status':{'allow':false,'miphone':{'hdstart':true,'hdstop':false,'hdurl':'?_a=20131018_phone_0b77f6a0c0e3e&amp;_op=choose','duration':null, 'pmstart':false},'mibox':{'hdstart':true,'hdstop':false,'hdurl':'?_a=20131018_box_aeb5500c600b&amp;_op=choose','duration':null, 'pmstart':false}}}";
		JSONObject jsonObj;
		try {
			jsonObj = new JSONObject(jsonStr);
			System.out.println(jsonObj.getJSONObject("status").getBoolean("allow") == false);
			System.out.println(jsonObj.getJSONObject("status").getJSONObject("miphone").getString("hdurl"));

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void sth2() {
		String line = null;
		try {
			BufferedReader r = new BufferedReader(new FileReader(new File("other/data.txt")));
			int count = 0;
			while ((line = r.readLine())!=null) {
				System.out.println("["+count+"] "+Util.decode(line));
				count++;
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void sth3() {
		String line = null;
		try {
			BufferedReader r = new BufferedReader(new FileReader(new File("other/data.txt")));
			FileReader fr = new FileReader(new File("other/abc.js"));
			String fileContent = "";
			int ch = 0; 
			while((ch = fr.read())!=-1 ) 
			{ 
				fileContent += (char)ch; 
			}
			int count = 0;
			while ((line = r.readLine())!=null) {
				//System.out.println("["+count+"] "+Util.decode(line));
				fileContent = fileContent.replace("_$["+count+"]", "'"+Util.decode(line)+"'");
				count++;
			}
			System.out.println(fileContent);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void testMultiConfs() {
		MainProcess m = new MainProcess();
		m.CONFS.getPs().setProperty("direct.call.url", "http://localhost:8080/WebTest/notallowJsonWithHdurlPhone.html?a=");
		m.CONFS.getPs().setProperty("direct.thread.num", "1");
		m.startThreads();
		
		try {
			Thread.sleep(20000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		m.CONFS.getPs().setProperty("direct.call.url", "http://localhost:8080/WebTest/allowJsonNoHdurl.html?a=");
		//MainProcess.CONFS.getPs().setProperty("direct.thread.num", "1");
		m.startThreads();
		
		try {
			Thread.sleep(20000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		m.CONFS.getPs().setProperty("direct.call.url", "http://localhost:8080/WebTest/allowJsonWithHdurlPhone.html?a=");
		//MainProcess.CONFS.getPs().setProperty("direct.thread.num", "1");
		m.startThreads();


	}
	
	public static void testMultiConfs2() {
		MainProcess m = new MainProcess();
		m.CONFS.getPs().setProperty("direct.call.url", "http://localhost:8080/WebTest/notallowJsonWithHdurlPhone.html?a=");
		m.CONFS.getPs().setProperty("direct.thread.num", "2");
		m.startThreads();
		
		try {
			Thread.sleep(20000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		m.CONFS.getPs().setProperty("direct.call.url", "http://localhost:8080/WebTest/allowJsonNoHdurl.html?a=");
		

	}
	
	public static void testMultiConfs3() {
		MainProcess m = new MainProcess();
		m.CONFS.getPs().setProperty("direct.call.url", "http://localhost:8080/WebTest/notAllowJsonNoHdurl.html?a=");
		m.CONFS.getPs().setProperty("direct.thread.num", "5");
		m.startThreads();
		
		try {
			Thread.sleep(20000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		m.CONFS.getPs().setProperty("direct.call.url", "http://localhost:8080/WebTest/allowJsonWithHdurlPhone.html?a=");
		

	}
	
	public static void testMultiConfs4() {
		MainProcess m1 = new MainProcess();
		m1.CONFS.getPs().setProperty("direct.call.url", "http://localhost:8080/WebTest/allowJsonWithHdurlPhone.html?a=");
		m1.CONFS.getPs().setProperty("direct.thread.num", "1");
		m1.CONFS.getPs().setProperty("accounts", "13888307667:mask1216:miphone");
		m1.startThreads();
		
		MainProcess m2 = new MainProcess();
		m2.CONFS.getPs().setProperty("direct.call.url", "http://localhost:8080/WebTest/allowJsonWithHdurlBox.html?a=");
		m2.CONFS.getPs().setProperty("direct.thread.num", "1");
		m2.CONFS.getPs().setProperty("accounts", "13888830524:147258:mibox");
		m2.startThreads();
		
		MainProcess m3 = new MainProcess();
		m3.CONFS.getPs().setProperty("direct.call.url", "http://localhost:8080/WebTest/allowJsonWithHdurlTV.html?a=");
		m3.CONFS.getPs().setProperty("direct.thread.num", "1");
		m3.CONFS.getPs().setProperty("accounts", "18908892611:yun18tao:mitv");
		m3.startThreads();

	}
	
	public static void testMultiConfs5() {
		MainProcess m1 = new MainProcess();
		m1.CONFS.getPs().setProperty("direct.call.url", "http://localhost:8080/WebTest/allowJsonWithHdurlAll.html?a=");
		m1.CONFS.getPs().setProperty("direct.thread.num", "6");
		m1.CONFS.getPs().setProperty("accounts", "13888307667:mask1216:miphone,13888830524:147258:mibox,18908892611:yun18tao:mitv");
		m1.startThreads();
	}

}
