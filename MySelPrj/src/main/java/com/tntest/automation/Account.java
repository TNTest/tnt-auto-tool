package com.tntest.automation;

public class Account {
	
	private String id;
	private String pwd;
	private String buyType;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getPwd() {
		return pwd;
	}
	public void setPwd(String pwd) {
		this.pwd = pwd;
	}
	public String getBuyType() {
		return buyType;
	}
	public void setBuyType(String buyType) {
		this.buyType = buyType;
	}
	
	@Override
	public String toString() {
		return "Account [id=" + id + ", pwd=" + pwd + ", buyType=" + buyType + "]";
	}
	

}
