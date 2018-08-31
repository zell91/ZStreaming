package com.zstreaming.browser.http;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import com.util.network.URLValidator;
import com.zstreaming.browser.WebBrowser;

public class HttpRequest {
	
	protected RequestHeader headerProperties;
	protected String cookies;
	protected String userAgent;
	protected URL url;
	protected HttpMethod method;
	protected String body;
	private boolean toShow;

	public HttpRequest(URL url) {
		this(url, new SimpleRequestHeader());
	}
	
	public HttpRequest(URL url, RequestHeader headerProperties) {
		this.setUrl(url);
		this.method = HttpMethod.GET;
		this.headerProperties = headerProperties;
		this.userAgent = WebBrowser.DEFAULT_USER_AGENT;		
		this.setCookies();
	}
		
	public RequestHeader getRequestHeader(){
		return headerProperties;
	}
	
	public void setRequestHeader(RequestHeader headerProperties){
		this.headerProperties = headerProperties;
	}
	
	public String getCookies() {
		return cookies;
	}

	public void setCookies() {
		this.cookies = "";
		WebBrowser.cookieManager.load(this.url).forEach(c->this.cookies += c.getName() + "=" + c.getValue() + ";");
	}
		
	public String getUserAgent() {
		return userAgent;
	}

	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}

	public URL getUrl() {
		return url;
	}

	public void setUrl(URL url) {
		try {
			this.url = URLValidator.validateURL(url.toExternalForm());
		} catch (MalformedURLException e) {
			this.url = url;
		}
	}

	public HttpMethod getMethod() {
		return method;
	}

	public void setMethod(HttpMethod method) {
		this.method = method;
	}	
	
	public void setBody(String body) {
		this.body = body;
	}
	
	public String getBody() {
		return body;
	}
	
	public boolean toShow() {
		return toShow;
	}
	
	protected static String parseQuery(LinkedHashMap<String, String> param){
		String query = "";
		
		for(Entry<String, String> p : param.entrySet()) {
			query += p.getKey() + "=" + p.getValue();
		}
		
		query = query.substring(0, query.length() -2);
		
		System.out.println(query + "::::::CLASSE HttpRequest");
		return query;
	}
	
}
