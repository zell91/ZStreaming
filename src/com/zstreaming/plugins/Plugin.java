package com.zstreaming.plugins;

import java.net.URL;

import com.zstreaming.browser.WebBrowser;
import com.zstreaming.browser.http.Session;

public abstract class Plugin {
	protected WebBrowser browser;
	protected boolean online;
	protected URL url;
	protected URL finalLink;
	protected String hoster;
	
	public void setBrowser(WebBrowser browser) {
		this.browser = browser;
	}
	
	public String getHoster() {
		return hoster;
	}
	
	public URL getFinalLink() {
		return finalLink;
	}
	
	public WebBrowser getBrowser() {
		return browser;
	}
		
	public boolean isOnline() {
		return online;
	}
		
	public void setURL(URL url) {
		this.url = url;
	}

	public URL getURL() {
		return url;
	}
	
	public abstract boolean hasCaptcha();
	
	protected void offlineCheck() throws InterruptedException{
		if(Thread.currentThread().isInterrupted()) throw new InterruptedException();
		Session session = this.browser.getSession();
		if(session != null) {
			int code = this.browser.getSession().getCode() ;
			this.online = !(code > 399);
		}else
			this.online = false;

	}

	public void init(WebBrowser browser, URL url) throws InterruptedException {
		this.url = url;
		this.browser = browser;
		this.offlineCheck();
	}

}
