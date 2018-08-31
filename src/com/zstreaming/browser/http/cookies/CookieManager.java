package com.zstreaming.browser.http.cookies;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.HttpCookie;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;

import com.zstreaming.browser.WebBrowser;

public class CookieManager extends java.net.CookieManager{
	
	private final static String COOKIE_PATH = "./browser/cookies/";
	private Cookies cookies;
	
	public CookieManager() {
		super();
		java.net.CookieHandler.setDefault(this);
		this.cookies = this.loadFromFileSystem();
	}
	
	public List<Cookie> load(URL url) {
		List<HttpCookie> cookies;
		List<Cookie> myCookies = new ArrayList<>();
		
		if(url != null){
			try {
				cookies = this.getCookieStore().get(url.toURI());
				
				for(HttpCookie cookie : cookies) { 
					myCookies.add(new Cookie(url, cookie.getName(), cookie.getValue(), cookie.getMaxAge()));
				}		
				
				this.cookies.addCookies(myCookies);

			} catch (Exception e) {
				e.printStackTrace();
			}	
		}
		
		return myCookies;
	}
		
	public Cookies getCookies(){		
		return this.cookies;
	}

	public synchronized void storeInFileSystem() {

		try {

			Iterator<Cookie> iterator = this.cookies.getCookies().iterator();
			
			while(iterator.hasNext()) {
				Cookie cookie = (Cookie)iterator.next();		
				String domain = cookie.getURL().getAuthority().replaceAll("www.", "");
				File fileCookie = new File(COOKIE_PATH, domain + ".cookies");
	
				try(ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(fileCookie))){
					out.writeObject(cookie);
					out.flush();
				} catch (IOException e) {
					WebBrowser.loggerManager.error("Impossibile memorizzare i cookie.", e);			
				}
			}
		}catch(ConcurrentModificationException ex) {
			ex.getMessage();
		}
	}
	
	public Cookies loadFromFileSystem() {
		
		File[] filesCookie = new File(COOKIE_PATH).listFiles();
		List<Cookie> cookies = new ArrayList<>();
		
		for(File fileCookie : filesCookie) {
			try(ObjectInputStream in = new ObjectInputStream(new FileInputStream(fileCookie))){
				Cookie cookie = null;
				if((cookie = (Cookie) in.readObject()) != null) {	
					cookies.add(cookie);
				}			
			} catch (IOException | ClassNotFoundException e) {
				WebBrowser.loggerManager.error("Impossibile caricare i cookie per dal file \"" + fileCookie.getAbsolutePath() +  "\".", e);	
			}catch(ArrayIndexOutOfBoundsException e) { }

		}
		
		Cookies cs = new Cookies(cookies);
		
		try {
			this.add(cs);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		
		return cs;		
	}
	
	public void add(Cookies cookies) throws URISyntaxException {
		
		for(Cookie c : cookies.getCookies()) {
						
			if(c.getExpired() < -1) continue;

			HttpCookie httpCookie = new HttpCookie(c.getName(), c.getValue());
			httpCookie.setMaxAge(c.getExpired());
			this.getCookieStore().add(c.getURL().toURI(), httpCookie);
		}
	}
	
	

}
