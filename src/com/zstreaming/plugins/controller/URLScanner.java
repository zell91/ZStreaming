package com.zstreaming.plugins.controller;

import java.io.File;
import java.net.URL;
import java.util.Objects;

import com.zstreaming.browser.WebBrowser;
import com.zstreaming.browser.http.HttpMethod;
import com.zstreaming.browser.http.HttpRequest;
import com.zstreaming.browser.http.Session;
import com.zstreaming.plugins.DDosProtectionDecrypter;
import com.zstreaming.plugins.Link;
import com.zstreaming.plugins.Plugin;
import com.zstreaming.plugins.exception.PluginException;
import com.zstreaming.plugins.hostLink.HostLink;
import com.zstreaming.plugins.shortlink.ShortLink;

public class URLScanner {
	
	private URL url;
	
	protected final static String PACKAGE_SHORTLINK = ShortLink.class.getPackage().getName();
	protected final static String PACKAGE_PATH_SHORTLINK = PACKAGE_SHORTLINK.replaceAll("\\.", "/");
	
	protected final static String PACKAGE_HOSTLINK = HostLink.class.getPackage().getName();
	protected final static String PACKAGE_PATH_HOSTLINK = PACKAGE_HOSTLINK.replaceAll("\\.", "/");	
	
	private Plugin result;
	
	private boolean isDirectLink;

	private WebBrowser browser;

	private int code;
	
	private int loop;
	
	public URLScanner(WebBrowser browser) {
		this.browser = browser;
	}
	
	public boolean isDirectLink() {
		return isDirectLink;
	}
	
	public URL getURL() {
		return url;
	}
	
	public Plugin getResult() {
		return result;
	}
	
	public void scan(URL url) throws PluginException, InterruptedException {
		this.url = url;

		this.checkDirectLink();
		
		if(this.code == -1) throw new PluginException(url.toExternalForm());
		
		if(this.code == 503) {
			DDosProtectionDecrypter dDosDecrypter = new DDosProtectionDecrypter(this.browser.clone());
			URL finalLink = null;
			try {
				dDosDecrypter.resolveChallenge();
				finalLink = dDosDecrypter.getFinalLink();
			} catch (InterruptedException e) {	}
			
			if(finalLink == null) {
				throw new PluginException(url.toExternalForm());
			}else if(!finalLink.equals(url)) {
				this.scan(this.loop == 0 ? url : finalLink);
				this.loop++;
				return;			
			}else
				this.checkDirectLink();

		}
		
		if(isDirectLink) return;
		
		this.scan(PACKAGE_SHORTLINK, PACKAGE_PATH_SHORTLINK);
		if(Objects.isNull(this.result)) this.scan(PACKAGE_HOSTLINK, PACKAGE_PATH_HOSTLINK);
		
		if(this.result != null) {
			this.result.init(this.browser, url);
			this.loop = 0;
			return;
		}
		
		throw new PluginException(url.toExternalForm());
	}
	
	protected void scan(String pkg, String pkg_path) throws PluginException {
		
		try {
			File path = new File("bin/" + pkg_path);
			
			String[] classesPath = path.list();
			
			for(String p : classesPath) {			
				Class<?> plugin = Class.forName(pkg + "."+ p.replace(".class", ""));
				
				Link annotation = plugin.getAnnotation(Link.class);
				
				if(annotation != null) {
					if((this.result = compareURL(plugin, annotation.urls())) != null) break;
				}		
			}			
		}catch(ClassNotFoundException | InstantiationException | IllegalAccessException e) {
			throw new PluginException(this.url.toExternalForm());
		}
	}

	private void checkDirectLink() {
		if(this.isDirectLink) return;
		final String text = "text/html";
		HttpRequest req = new HttpRequest(this.url);
		req.setMethod(HttpMethod.GET);
		Session session = browser.sendRequest(req);
		if(session == null) return;
		this.url = this.url.getAuthority().equals(session.getURL().getAuthority()) ? this.url : session.getURL();
		this.code = session.getCode();
		this.isDirectLink = !session.getContentType().contains(text);
	}

	private Plugin compareURL(Class<?> plugin, String[] urls) throws InstantiationException, IllegalAccessException {

		String url = this.url.toExternalForm().replaceFirst("://www.", "://").replaceFirst("http\\w*://", "").replaceAll(this.url.getPath(), "").replaceAll("\\?" + this.url.getQuery(), "");
		
		for(String u: urls) {
			if(url.equals(u)) 
				return (Plugin) plugin.newInstance();			
		}
		
		return null;
	}

	public WebBrowser getBrowser() {
		return browser;
	}

	public void setDirectLink(boolean directLink) {
		this.isDirectLink = directLink;		
	}

}
