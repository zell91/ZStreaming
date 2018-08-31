package com.util.network;

import java.net.MalformedURLException;
import java.net.URL;

public class URLValidator {
	
	private URLValidator() { }
	
	public static URL validateURL(String url) throws MalformedURLException {		
		String _url = null;
		
		if(url.startsWith("://")) {
			_url = "http" + url;
		}else if(url.startsWith("//")) {
			_url = "http:" + url;
		}else if(url.startsWith("/")) {
			_url = "http:/" + url;
		}else if(url.startsWith(":/")) {
			_url = "http://" + url.replaceFirst("\\:/", "");
		}else if(url.startsWith(":")) {
			_url = "http://" + url.replaceFirst("\\:", "");
		}else if(url.startsWith("http://") || url.startsWith("https://") ) {
			_url = url;
		}else {
			_url = "http://" + url;
		}
		
		return new URL(URLValidator.cleanURL(_url));		
	}
	
	private static String cleanURL(String url) {		
		try {
			return url.split("/http")[0];			
		}catch(Exception e) {
			return url;
		}		
	}

}
