package com.zstreaming.browser.http;

import java.util.List;

import com.zstreaming.browser.WebBrowser;
import com.zstreaming.browser.http.cookies.Cookie;

public class HttpResponse extends Session {	

	private static final long serialVersionUID = 1L;
	private StringBuilder content;
	private List<Cookie> cookies;
		
	public HttpResponse(long startSession) {
		super(startSession);
		this.content = new StringBuilder();
	}
	
	public StringBuilder getContent() {
		return content;
	}

	public void setContent(StringBuilder content) {
		this.content = content;
	}

	public List<Cookie> getCookies() {
		return cookies;
	}
	
	public void setCookies(List<Cookie> cookies) {
		this.cookies = cookies;
		WebBrowser.cookieManager.storeInFileSystem();
	}
}
