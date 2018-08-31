package com.zstreaming.browser.http.cookies;

import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Cookies implements Serializable{

	private static final long serialVersionUID = 1L;
	
	private List<Cookie> cookieList;
	
	public Cookies(List<Cookie> cookieList) {
		this.cookieList = cookieList;
	}

	public synchronized List<Cookie> getCookies(){
		return cookieList;
	}
	
	public List<Cookie> getCookies(URL url) {
		//List<Cookie> cookies = Collections.synchronizedList(new ArrayList<>());
		List<Cookie> cookies = cookieList.stream().filter(c->c.getURL().equals(url)).collect(Collectors.toList());
		/*
		Iterator<Cookie> iter = cookieList.iterator();
		
		while(iter.hasNext()) {
			Cookie cookie = (Cookie)iter.next();
			if(url.equals(cookie.getURL())) cookies.add(cookie);
		}*/
		
		return cookies;
	}
	
	public void setCookies(List<Cookie> cookieList){
		this.cookieList = cookieList;
	}
	
	public void addCookies(List<Cookie> cookieList) {
		List<Cookie> removeList = new ArrayList<>();	
		for(Cookie c : cookieList) {
			for(Cookie cookie : Collections.synchronizedCollection(this.cookieList)) {
				if(c.equals(cookie)) continue;
				if(c.getName().equals(cookie.getName())) {
					if(c.getExpired() >= cookie.getExpired()) {
						removeList.add(cookie);
					}
				}
			}
			this.cookieList.add(c);
		}
		
		this.cookieList.removeAll(removeList);
	}
}
