package com.zstreaming.plugins.shortlink;

import java.net.MalformedURLException;
import java.net.URL;

import com.util.regex.Regex;
import com.zstreaming.browser.http.HttpMethod;
import com.zstreaming.browser.http.HttpRequest;
import com.zstreaming.browser.http.HttpResponse;
import com.zstreaming.browser.http.SimpleRequestHeader;
import com.zstreaming.plugins.Link;

@Link(name = "vcrypt", urls = {"vcrypt.net"})
public class VCrypt extends ShortLink {
	
	final String[] urls = {"vcrypt.net"};
	
	private HttpResponse response;
	private CharSequence content;
	
	public VCrypt() {
		super();
		this.hoster = "vcrypt";
	}

	@Override
	protected void offlineCheck() throws InterruptedException {		
		this.response = this.browser.getResponse();
		this.content = this.response.getContent();
		this.online = !this.content.toString().contains("Sorry file not found");
		this.online = !this.response.getURL().toExternalForm().contains("/banned");
	}

	@Override
	public void decrypt() throws InterruptedException {
		String u = this.response.getURL().toExternalForm();
		if(u.contains("/fastshield/"))
			this.fastShield(u);
		else if(u.contains("/opencrypt"))
			this.finalLink = this.openCrypt(this.content);
		else if(content.toString().contains("class=\'g-recaptcha\'")) {
			this.finalLink = this.resolveRecaptcha(this.content);
		}else {
			for(String _u : urls) {	if(u.contains(_u)) return; }
			try {
				this.finalLink = new URL(u);
			} catch (MalformedURLException e) {	}
		}
		
	}

	private URL resolveRecaptcha(CharSequence content) {
		// TODO Auto-generated method stub
		return null;
	}

	private URL openCrypt(CharSequence content) {
		final String regex = "<a.*class=\"btddd[^>]*";
		String link = new Regex(regex, content).match().toString().split("href=\"")[1].split("\"")[0];

		try {
			return new URL(link);
		} catch (MalformedURLException e) {
			return null;
		}
	}

	private void fastShield(String u) throws InterruptedException {
		URL url = null;
		
		try {
			url = new URL(u);
		} catch (MalformedURLException e) {
			return;
		}
				
		HttpRequest request = new HttpRequest(url);
		request.setMethod(HttpMethod.POST);
		request.setBody("go=go");
		SimpleRequestHeader header = new SimpleRequestHeader();
		header.addRequestProperty("Host", "vcrypt.net");
		header.addRequestProperty("Referer", url.toExternalForm());
		header.addRequestProperty("Content-type", "application/x-www-form-urlencoded");
		
		this.browser.sendRequest(request);
		this.offlineCheck();
		if(this.online) this.decrypt();
	}

	@Override
	public boolean hasCaptcha() {
		return true;
	}

}
