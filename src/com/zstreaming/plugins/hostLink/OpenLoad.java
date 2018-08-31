package com.zstreaming.plugins.hostLink;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;

import com.util.regex.Regex;
import com.util.size.Size;
import com.zstreaming.browser.http.HttpRequest;
import com.zstreaming.browser.http.HttpResponse;
import com.zstreaming.browser.http.Session;
import com.zstreaming.browser.view.ViewBrowser;
import com.zstreaming.plugins.Link;
import com.zstreaming.plugins.exception.PluginException;

@Link(name = "openload", urls = {"openload.co", "oload.tv", "openloads.co"})
public class OpenLoad extends HostLink {
	
	final String[] urls = {"openload.co"};
	
	private HttpResponse response;
	private CharSequence content;

	public OpenLoad() {
		super();
		this.hoster = "openload";		
	}
	
	@Override
	protected void offlineCheck() throws InterruptedException {
		super.offlineCheck();
		this.response = this.browser.getResponse();
		this.url = this.response.getURL();
		this.content = this.response.getContent();
		this.online = !this.content.toString().contains("We’re Sorry") && !this.content.toString().contains("We are Sorry");		
	}

	@Override
	public void scanInfo() { 
		HttpRequest req = new HttpRequest(this.finalLink);
		Session session = this.browser.sendRequest(req);
		try {
			this.name = session.getURL().getFile().split("/")[session.getURL().getFile().split("/").length - 1].split("\\?")[0];
			this.name = URLDecoder.decode(name, "UTF-8");
			this.size = Long.parseLong(session.getResponseHeader().get("Content-Length").get(0));
			this.mimeType = session.getContentType();
		} catch (UnsupportedEncodingException e) { }
	}

	@Override
	public void retriveDownloadLink() throws PluginException {	
		/*
		 * Old method
		 * 
		 
		String streamurlId = new Regex("streamur\\w", this.content).match().toString();
*/
		String streamurlId = null;
		
		/*
		 * New method
		 * 
		 
		if(streamurlId.isEmpty()) {
			streamurlId = new Regex("DtsBlkVFQx", this.content).match().toString();
		}
		
		if(streamurlId.isEmpty()) {
			String line = new Regex("<p\\s*style=\"\"\\s*class=\"\".*</p>", this.content).match().toString();
			streamurlId = new Regex("id\\s*=\\s*\"[^\"]*", line).match().toString().split("\"")[1];		
		}
		*/		
		HttpRequest req = new HttpRequest(this.toEmbed(this.url));
		
		ViewBrowser webView = this.browser.getSessionView();

		if(webView.getOnSession()) {
			synchronized(webView.onSession()) {
				try {
					webView.onSession().wait();
				} catch (InterruptedException e) {
					return;
				}
			}
		}
		webView.setRequest(req);
		webView.load();
		
		/*String streamurl = webView.getTextElementById(streamurlId);

		if(streamurl == null || streamurl.equalsIgnoreCase("null")) {*/
			this.content = webView.readContent();
			String streamurl;
			/*
			 * New method
			 * 
			 */			
			streamurlId = new Regex("DtsBlkVFQx", this.content).match().toString();

			if(streamurlId.isEmpty()) {
				String line = new Regex("<p\\s*style=\"\"\\s*class=\"\".*</p>", this.content).match().toString();
				streamurl = new Regex(">[^<]*", line).match().toString().substring(1);
			}else {
				streamurl = webView.getTextElementById(streamurlId);
			}
		//}
		
		webView.close();
		
		if(streamurl != null && !streamurl.equalsIgnoreCase("null")) {
			String link = "https://openload.co/stream/" + streamurl + "?mime=true";
			try {
				this.finalLink = new URL(link);
				this.scanInfo();
				this.media.setMRL(this.finalLink);
				this.media.setName(this.name);
				this.media.setSize(new Size(this.size));		
				this.media.setMimeType(this.mimeType);
			} catch (MalformedURLException e) {	}			
		}else {
			throw new PluginException(this, PluginException.State.OFFLINE);
		}
	}
	
	private URL toEmbed(URL url) {
		
		try {
			return new URL(url.toExternalForm().replaceFirst("[s]*\\.co/f/", ".co/embed/"));
		}catch(Exception e) {
			return url;
		}	
	}

	@Override
	public boolean hasMetaInfo() {
		return false;
	}

	@Override
	public boolean hasCaptcha() {
		return false;
	}

}
