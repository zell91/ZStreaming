package com.zstreaming.plugins.shortlink;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import com.util.regex.Regex;
import com.zstreaming.browser.http.HttpMethod;
import com.zstreaming.browser.http.HttpRequest;
import com.zstreaming.browser.http.HttpResponse;
import com.zstreaming.browser.http.RequestHeader;
import com.zstreaming.browser.http.Session;
import com.zstreaming.browser.http.SimpleRequestHeader;
import com.zstreaming.plugins.Link;
import com.zstreaming.plugins.exception.PluginException;

@Link(name = "bcvc", urls = {"bc.vc"})
public class BcVc extends ShortLink {
	
	final String[] urls = {"bc.vc"};
	
	private CharSequence content;
	
	private final String TIME_QUERY = "1845,30:71.01:20:1457";
	
	public BcVc() {
		super();
		this.hoster = "bcvc";
	}
	
	@Override
	protected void offlineCheck() throws InterruptedException {
		super.offlineCheck();
		HttpResponse response = null;
		try {
			response = this.browser.getResponse(5500);
			this.content = response.getContent();
			this.online = !this.content.toString().contains("=http://go.onclasrv.com/afu.php");
		} catch (InterruptedException e) {
			this.online = false;
		}
		
		this.content = response.getContent();
	}

	@Override
	public void decrypt() throws PluginException {
		
		String query = null;
		
		try {			
			try {
				query = this.encodeQuery(this.content.toString());
			} catch (UnsupportedEncodingException e) {	}
			
			String raw_url = new Regex("http://bc.vc/fly/ajax.php[^']*", content).match().toString().split("'")[0];
			
			URL _url = new URL(raw_url + TIME_QUERY );
			
			HttpResponse response = this.ajaxRequest(_url, query);
			
			if(response == null) 
				throw new PluginException(this, PluginException.State.OFFLINE);
			else {
				String raw = response.getContent().toString();
				this.finalLink = new URL(raw.split("\"")[raw.split("\"").length - 2].replaceAll("\\\\", ""));
			}
		}catch(MalformedURLException e) {
			throw new PluginException(this, PluginException.State.OFFLINE);
		}
	}

	private String encodeQuery(String content) throws UnsupportedEncodingException {
		final String jki = new Regex("jki: .*", content.toString()).match().toString().split("'")[1];
		String query = "";
		
		String[] param = new String[] { "xdf[afg]=60",
										"xdf[bfg]=1920",
									    "xdf[cfg]=450",
									    "xdf[jki]=" + jki,
									    "xdf[dfg]=1920",
									    "xdf[efg]=1080",
									    "ojk=jfhg"
									  };
		
		for(String p : param) {
			String[] keyValue = p.split("=");
			String q = URLEncoder.encode(keyValue[0], "UTF-8") + "=" + URLEncoder.encode(keyValue[1], "UTF-8");
			query += q + "&";
		}		
		query = query.substring(0, query.length() - 1);
		
		return query;
	}

	private HttpResponse ajaxRequest(URL url, String query) {
		HttpRequest request = new HttpRequest(url);
		request.setMethod(HttpMethod.POST);
		request.setBody(query);		
		request.setRequestHeader(this.ajaxHeader());
		
		Session session = this.browser.sendRequest(request);
		
		if(session == null) return null;
			
		return this.browser.getResponse();
	}

	private RequestHeader ajaxHeader() {
		SimpleRequestHeader rHeader = new SimpleRequestHeader();
		rHeader.addRequestProperty("Host", url.getAuthority());
		rHeader.addRequestProperty("Origin", url.getProtocol() + "://" + url.getAuthority());
		rHeader.addRequestProperty("Content-type", "application/x-www-form-urlencoded");
		rHeader.addRequestProperty("X-Requested-With", "XMLHttpRequest");
		
		return rHeader;
	}

	@Override
	public boolean hasCaptcha() {
		return false;
	}
	
	

}
