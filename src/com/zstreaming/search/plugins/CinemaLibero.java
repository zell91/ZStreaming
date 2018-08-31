package com.zstreaming.search.plugins;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.util.regex.Regex;
import com.zstreaming.browser.WebBrowser;
import com.zstreaming.browser.http.HttpRequest;
import com.zstreaming.browser.http.HttpResponse;
import com.zstreaming.browser.http.SimpleHttpRequest;
import com.zstreaming.search.Result;
import com.zstreaming.search.SearchRequest;

public class CinemaLibero extends Plugin{
	
	public static final Locale LANGUAGE = Locale.ITALY;
	
	private static final String QUERY = "?s=";
	
	protected String name;
	protected String url;
	
	private List<URL> series;	
	private List<URL> links;
	
	public CinemaLibero() {
		super();
		this.name = "cinemalibero.club";
		this.url = "https://www.cinemalibero.club/" + QUERY;
		this.series = new ArrayList<>();
		this.links = new ArrayList<>();
	}

	@Override
	public void startRequest(SearchRequest request) {
		this.setRequest(request);
		try {
			HttpResponse page = this.getPage(this.url + URLEncoder.encode(this.search, "UTF-8"));
		
			if(page != null) {
				final String regex = "<div\\s*class\\s*=\"locandine.*<footer\\s*class\\s*=\"container\">";
				final String regex1 = "<a\\s*href\\s*=\"[^\"]*";

				CharSequence content = page.getContent().toString().replaceAll("\r\n", "");
				CharSequence section = new Regex(regex, content).match();			

				CharSequence[] links = new Regex(regex1, section).matches();
				
				for(CharSequence link : links) {
					try {
						link = link.toString().split("\"")[link.toString().split("\"").length - 1];
						this.series.add(new URL(link.toString()));
					}catch(MalformedURLException e) { }
				}				
			}
			
			for(URL url : this.series) {
				this.executor.submit(new Thread(()->{
					HttpResponse response = this.getPage(url);
					StringBuilder content = response.getContent();
					
					this.extractEpisode(content);
				}));
			}
		}catch(UnsupportedEncodingException ex) {	}
	}

	private void extractEpisode(CharSequence content) {
		final String regex = "\\d+&#215;\\d+.*<br />";

		//final String regex = "\\d+(?:&#215;|x|ï¿½)\\d+[^\\n]*";
		final String regex1 = "\\d+(?:&#215;|x|ï¿½)\\d+-*\\d*";
		
		String[] ch = new Regex(regex, content).match().toString().split("<br />");
		for(CharSequence c : ch) {
			c = new Regex("\\d+&#215;\\d+.*", c).match();
			
			if(c.toString().isEmpty())
				continue;
			
			if(this.checkEpisode(c)) {
				CharSequence[] rawLinks = new Regex("a href=\"[^\"]*", c).matches();
				
				for(CharSequence raw : rawLinks) {
					try {
						if(raw.toString().contains("\"")) {
							String link = raw.toString().split("\"")[1];
							Result result = new Result(new URL(link.toString()).toExternalForm());
							this.results.put(result);
						}						
					}catch(MalformedURLException | InterruptedException e) { }
				}
				
			}
		}
	}
	

	private boolean checkEpisode(CharSequence match) {		
		if(this.season <= 0) return true;
		
		final String regex = "\\d+(?:&#215;|x|×)\\d+(?:-\\d+)*";
		String line = new Regex(regex, match).match().toString();
		try {
			if(Integer.parseInt(line.split("(?:&#215;|x|×)")[0]) == this.season) {
				if(this.episode <= 0) 
					return true;
				else if(this.episode == Integer.parseInt(line.split("(?:&#215;|x|×)")[1])) 
					return true;				
			}
		}catch(Exception e) {
			if(Integer.parseInt(line.split("(?:&#215;|x|×)")[1].split("-")[0]) ==  this.episode || Integer.parseInt(line.split("(?:&#215;|x|×)")[1].split("-")[1]) == this.episode)
				return true;	
		}

		
		return false;
	}

	private HttpResponse getPage(URL url) {
		HttpRequest request = new SimpleHttpRequest(url);

		WebBrowser wb = new WebBrowser();
		
		wb.sendRequest(request);
		
		return wb.getResponse();
	}
	
	private HttpResponse getPage(String url) {
		try {
			URL _url = new URL(url);
			return this.getPage(_url);
		}catch(MalformedURLException ex) { }
		
		return null;
	}

	@Override
	public Locale getLanguage() {
		return CinemaLibero.LANGUAGE;
	}

}
