package com.zstreaming.media;

import java.net.URL;

import com.zstreaming.browser.WebBrowser;
import com.zstreaming.browser.http.HttpMethod;
import com.zstreaming.browser.http.HttpRequest;
import com.zstreaming.browser.http.Session;
import com.zstreaming.browser.http.SimpleRequestHeader;
import com.zstreaming.plugins.controller.URLController;

public class OnlineMediaChecker {
	
	private Media media;
	private WebBrowser webBrowser;
	
	public OnlineMediaChecker(Media media, WebBrowser webBrowser) {
		this.media = media;
		this.webBrowser = webBrowser;
	}
	
	public void setMedia(Media media) {
		this.media = media;
	}
	
	public Media getMedia() {
		return this.media;
	}
	
	public boolean lazyCheck() {
		URL mrl = this.media.getMRL();
		
		HttpRequest request = new HttpRequest(mrl, new SimpleRequestHeader(false));			
		request.setMethod(HttpMethod.HEAD);
		
		Session session = this.webBrowser.sendRequest(request);
		
		return session.getCode() < 400 && session.getContentType().equals(media.getMimeType());
	}

	public Media depthCheck() {
		URLController urlController = new URLController(this.media.getSource().toExternalForm(), this.webBrowser.clone());
		
		Media newMedia = null;
		
		try {
			urlController.run();
			newMedia = urlController.getMedia();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}		
		
		return newMedia;
	}

}
