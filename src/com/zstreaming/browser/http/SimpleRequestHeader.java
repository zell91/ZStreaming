package com.zstreaming.browser.http;

public class SimpleRequestHeader extends RequestHeader {	
	
	protected boolean persistent;

	public SimpleRequestHeader() {
		this(true);
	}
	
	public SimpleRequestHeader(boolean persistent) {
		super();
		this.persistent = persistent;
		setProperties();
	}
		
	private void setProperties() {		
		this.header.put("Accept", "*/*");
		this.header.put("Accept-Language", "it-IT,it;q=0.8,en-US;q=0.5,en;q=0.3");
		//this.header.put("Accept-Encoding", "UTF-8");
		this.header.put("Connection", this.persistent ? "keep-alive" : "close");
		this.header.put("Upgrade-Insecure-Requests", "1");		
	}
		
	

}
