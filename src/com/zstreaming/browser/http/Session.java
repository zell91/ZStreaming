package com.zstreaming.browser.http;

import java.io.Serializable;
import java.net.URL;
import java.util.List;
import java.util.Map;

public class Session implements Serializable {
	
	protected static final long serialVersionUID = 1L;
	protected URL url;
	protected int code;
	protected Map<String, List<String>> headerFields;
	protected String responseMessage;
	protected boolean view;
	protected String contentType;	
	protected long startSession;
	protected long endSession;
	
	public Session(URL url, Map<String, List<String>> headerFields, int code, String responseMessage) {
		this(System.currentTimeMillis());		
		this.url = url;
		this.headerFields = headerFields;
		this.code = code;
		this.responseMessage = responseMessage;
		try {
			this.contentType = this.view ? null : this.headerFields.get("Content-Type").get(0);
		}catch(NullPointerException e) { this.contentType = ""; }
	}
	
	public Session(long startSession) {	
		this.startSession = startSession;
		this.endSession = -1;
	}

	public URL getURL() {
		return url;
	}

	public void setURL(URL url) {
		this.url = url;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getResponseMessage() {
		return responseMessage;
	}

	public void setResponseMessage(String responseMessage) {
		this.responseMessage = responseMessage;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}
	
	public void setResponseHeader(Map<String, List<String>> headerFields) {
		this.headerFields = headerFields;
	}
	
	public Map<String, List<String>> getResponseHeader() {
		return headerFields;
	}
	
	public void setEndSession(long endSession) {
		this.endSession = endSession;
	}
	
	public long getSessionDuration() {
		return (endSession == -1 ? System.currentTimeMillis() : this.endSession) - this.startSession; 
	}
	
	public boolean isViewBrowser() {
		return this.view;
	}
	
	public void setViewBrowser(boolean view) {
		this.view = view;
	}

}
