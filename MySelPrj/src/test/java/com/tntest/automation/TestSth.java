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
		
		sth2();
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

}
