package com.zstreaming.browser.http.cookies;

import java.io.Serializable;
import java.net.URL;

public class Cookie implements Serializable{

	private static final long serialVersionUID = 1L;
	
	private URL url;
	private String name;
	private String value;
	private long expired;
	
	public Cookie(URL url, String name, String value, long expired) {
		this.url = url;
		this.name = name;
		this.value = value;
		this.expired = (System.currentTimeMillis()/1000) + expired;
	}
	
	public String getName() {
		return name;
	}
	
	public String getValue() {
		return value;		
	}
	
	public long getExpired() {
		return (this.expired - (System.currentTimeMillis() / 1000));
	}
	
	public URL getURL() {
		return url;
	}
	
	@Override
	public String toString() {
		return "URL: " + this.url + "    Cookie: " + this.name + "=" + value  + "    Max-Age: " + this.getExpired();
	}
		
}
