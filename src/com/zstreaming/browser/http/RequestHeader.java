package com.zstreaming.browser.http;

import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

public class RequestHeader {
	
	protected Map<String, String> header;

	public RequestHeader() {
		this.header = new HashMap<>();
	}
		
	public RequestHeader(Map<String, String> header) {
		this.header = header;
	}
	
	public void addRequestProperty(String key, String value) {
		this.header.put(key, value);
	}
	
	public void removeRequestProperty(String key) {
		this.header.remove(key);
	}
	
	public void set(URLConnection connection) {
		for(java.util.Map.Entry<String, String> keyValue : this.header.entrySet()) {
			connection.addRequestProperty(keyValue.getKey(), keyValue.getValue());
		}
	}
	
	public Map<String, String> getRequestHeader(){
		return header;
	}

}
