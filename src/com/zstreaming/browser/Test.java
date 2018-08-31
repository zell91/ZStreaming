package com.zstreaming.browser;

import java.net.MalformedURLException;
import java.net.URL;

import com.zstreaming.browser.WebBrowser;
import com.zstreaming.browser.http.HttpMethod;
import com.zstreaming.browser.http.HttpRequest;
import com.zstreaming.browser.http.HttpResponse;
import com.zstreaming.browser.http.SimpleRequestHeader;

public class Test {

	public static void main(String[] args) throws MalformedURLException, InterruptedException{
			
		WebBrowser wb = new WebBrowser();

		URL url = new URL("http://www.prolocoleuca.it");
		URL url2 = new URL("https://www.google.it/");
		URL url3 = new URL("https://www.libero.it/");

		new Thread(()->send(url,wb.clone(), 10000)).start();
		new Thread(()->send(url2,wb.clone(), 30000)).start();
		new Thread(()->send(url3,wb.clone(), 16000)).start();

	}
	
	public static void send(URL url, WebBrowser wb, int wait) {
		HttpRequest request = new HttpRequest(url);
		request.setCookies();
		request.setRequestHeader(new SimpleRequestHeader(false));
		request.setMethod(HttpMethod.GET);
		wb.sendRequest(request);
		try {
			Thread.sleep(wait);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		wb.getSession();
		HttpResponse response = wb.getResponse();
		//StringBuilder content = response.getContent();
		//System.out.println(content);
		
		System.out.println(response.getSessionDuration());
	}
	
}
