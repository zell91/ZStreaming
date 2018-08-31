package com.zstreaming.plugins.controller;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import com.util.network.URLValidator;
import com.util.size.Size;
import com.zstreaming.browser.WebBrowser;
import com.zstreaming.media.Media;
import com.zstreaming.plugins.Plugin;
import com.zstreaming.plugins.exception.PluginException;
import com.zstreaming.plugins.hostLink.HostLink;
import com.zstreaming.plugins.shortlink.ShortLink;
import com.zstreaming.statistics.SessionStatistics;

public class URLController {
	
	private String link;
	private URLScanner scanner;
	private URL finalURL;
	private WebBrowser browser;
	private BlockingQueue<Media> mediaBlockingQueue;
	
	public URLController(String link, WebBrowser wb) {
		this.link = link;
		this.scanner = new URLScanner(wb);
		this.mediaBlockingQueue = new LinkedBlockingQueue<>(1);
	}
	
	public Media getMedia() throws InterruptedException {
		return this.mediaBlockingQueue.poll(30, TimeUnit.SECONDS);
	}
	
	public URL getFinalURL() {
		return finalURL;
	}
	
	public WebBrowser getBrowser() {
		return browser;
	}
	
	public void run() throws InterruptedException {		
		URL url = null;		
		try {
			url = URLValidator.validateURL(this.link);
		}catch(MalformedURLException ex) {
			return;
		}

		try {						
			while(url != null && !this.scanner.isDirectLink()) {
				if(Thread.currentThread().isInterrupted()) throw new InterruptedException();
				
				url = this.scanner(url); 
			}
			
			if(this.scanner.isDirectLink()) {
				this.browser = this.scanner.getBrowser();;		
			}

			this.finalURL = url;
			
		}catch(Exception e) {
			e.printStackTrace();
		} catch (PluginException e) {
			WebBrowser.loggerManager.warning(e.getMessage(), e);
		}finally {
			if(this.mediaBlockingQueue.size() == 0) {
				Media media = new Media();
				if(this.scanner.isDirectLink()) {
					try {
						this.scanner.scan(URLScanner.PACKAGE_HOSTLINK, URLScanner.PACKAGE_PATH_HOSTLINK);
						if(this.scanner.getResult() != null) media.setHoster(this.scanner.getResult().getHoster());
					} catch (PluginException e) { }
					try {
						media.setMRL(this.browser.getSession().getURL());
						if(media.getMRL().getQuery() != null)
							media.setName(media.getMRL().toExternalForm().substring(media.getMRL().toExternalForm().lastIndexOf("/") + 1).replace("?" + media.getMRL().getQuery(), ""));
						else
							media.setName(media.getMRL().toExternalForm().substring(media.getMRL().toExternalForm().lastIndexOf("/") + 1));
						media.setSize(new Size(Long.parseLong(this.browser.getSession().getResponseHeader().get("Content-Length").get(0))));
						media.setSource(this.finalURL);
						media.setMimeType(this.browser.getSession().getContentType());
					}catch(NullPointerException e) {}
				}
				this.mediaBlockingQueue.put(media);
			}
		}
	}

	private URL scanner(URL url) throws PluginException, InterruptedException {
		this.scanner.scan(url);
		SessionStatistics.setState("searching");
		if(this.scanner.isDirectLink()) return url;
		Plugin plugin = this.scanner.getResult();		
		this.elabore(plugin);
		return plugin.getFinalLink();
	}

	private void elabore(Plugin plugin) throws PluginException, InterruptedException {
		if(plugin instanceof ShortLink) {
			ShortLink shortLink = (ShortLink) plugin;
			if(shortLink.isOnline()) 
				shortLink.decrypt();
			else
				throw new PluginException(plugin, PluginException.State.OFFLINE);
		}else if(plugin instanceof HostLink) {
			HostLink hostLink = (HostLink) plugin;
			if(hostLink.isOnline()) {
				if(hostLink.hasMetaInfo())	hostLink.scanInfo();
				
				hostLink.retriveDownloadLink();
				this.scanner.setDirectLink(true);
				try {
					this.mediaBlockingQueue.put(hostLink.getMedia());
				} catch (InterruptedException e) {	}			
			}else {
				try {
					throw new InterruptedException();
				} catch (InterruptedException e) {
					throw new PluginException(plugin, PluginException.State.OFFLINE);
				}
			}
		}
	}

}
