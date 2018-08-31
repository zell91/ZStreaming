package com.zstreaming.plugins.hostLink;

import java.net.URL;

import com.zstreaming.browser.WebBrowser;
import com.zstreaming.media.Media;
import com.zstreaming.plugins.Plugin;
import com.zstreaming.plugins.exception.PluginException;

public abstract class HostLink extends Plugin {
	
	protected Media media;
	protected String name;
	protected long size;
	protected String mimeType;
	
	public HostLink() {
		super();
	}
	/*
	public String getHoster() {
		return hoster;
	}
	
	public String getName() {
		return name;
	}
	
	public long getSize() {
		return size;
	}	
	*/
	public Media getMedia() throws InterruptedException {
		return media;
	}
		
	public abstract void scanInfo();
	public abstract void retriveDownloadLink() throws PluginException;
	public abstract boolean hasMetaInfo();	
	
	@Override
	public void init(WebBrowser browser, URL url) throws InterruptedException {
		super.init(browser, url);
		this.media = new Media();
		this.media.setSource(this.url);
		this.media.setHoster(this.hoster);		
	}
}
