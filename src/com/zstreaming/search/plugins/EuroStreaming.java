package com.zstreaming.search.plugins;

import java.io.IOException;
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
import com.zstreaming.search.Result;
import com.zstreaming.search.SearchRequest;

public class EuroStreaming  extends Plugin{
	
	public static final Locale LANGUAGE = Locale.ITALY;
	
	protected String name;
	protected String url;
	private String url2;

	private final String QUERY_SEARCH = "?s=";

		
	public EuroStreaming() {
		super();
		this.name = "eurostreaming.club";
		this.url = "https://eurostreaming.club";
		this.url2 = "https://eurostreaming.club/elenco-serie-tv/";
	}
	
	@Override
	public void startRequest(SearchRequest request) {
		this.setRequest(request);		
		HttpResponse page = this.getPage(this.url2);				
		
		String regex = "<ul\\s*class\\s*=\\s*\"lcp_catlist.*";
		
		CharSequence content = new Regex(regex, page.getContent()).match();
		
		List<String> listURL = new ArrayList<>();
		
		CharSequence[] matches = new Regex("<a[^>]*>[^<]*</a>", content).matches();
		
		for(CharSequence match : matches) {
			if(match.toString().replaceAll("[\\.-:,;] ", " ").toUpperCase().contains(this.request.getRequest().toUpperCase())) {
				listURL.add(match.toString().split("href=\"")[1].split("\"")[0]);
			}
		}
		
		if(listURL.size() > 0) {
			listURL.forEach(u->this.executor.submit(new MyTask(u)));
		}else {
			String urlQuery = null;
			
			try {
				urlQuery = this.url + QUERY_SEARCH + URLEncoder.encode(this.search, "UTF-8");
			} catch (UnsupportedEncodingException e) {	}
			
			
			if(urlQuery != null) {
				page = this.getPage(urlQuery);
				
				content = page.getContent();
				
				regex = "<ul[^(class)]*class\\s*=\\s*\"recent-posts.*</ul>";
				
				String _page = new Regex(regex, content.toString().replaceAll("\\r\\n", "[\\\\r\\\\n]")).match().toString().replaceAll("\\[\\\\r\\\\n\\]", "\r\n");
				
				CharSequence[] c = new Regex("<a.*", _page).matches();
				
				int i = 0;
				for(CharSequence u : c) {
					if(i>0) {
						i = 0;
						continue;
					}
					String url = u.toString().split("href=\"")[1].split("\"")[0];
					this.executor.submit(new MyTask(url));
					i++;
				}
			}				
		}	
		
		this.executor.shutdown();
	}
	
	private class MyTask implements Runnable{
		
		private String url;
		
		private MyTask(String url) {
			this.url = url;			
		}
		
		@Override
		public void run() {
			HttpResponse response = getPage(this.url);
			
			String content = response.getContent().toString();
			
			try {
				this.url = beforeStore(content);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			if(this.url!=null) {
				response = getPage(this.url);
				content = response.getContent().toString();
			}

			final String regex = ">PRIMA STAGIONE.*";
			content = new Regex(regex, content.replaceAll("\\r\\n", "[\\\\r\\\\n]")).match().toString().replaceAll("\\[\\\\r\\\\n\\]", "\r\n");
			content = content.substring(0, content.indexOf("<div class=\"clear"));
			extractEpisode(content);
		}				
	}
	
	private void extractEpisode(String page) {
		final String regex = "\\d+(?:&#215;|x|×)\\d+.*";
		
		CharSequence[] matches = new Regex(regex, page).matches();
				
		for(CharSequence match : matches) {
			if(this.checkEpisode(match)) {
				
				final String regex2 = "href=\".[^\"]*";
				
				CharSequence[] links = new Regex(regex2, match).matches();
				
				for(CharSequence link : links) {
					String finalLink = link.toString().replace("href=\"", "");
					Result result = new Result(finalLink);
					try {
						this.results.put(result);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}					
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

	private String beforeStore(String content) throws IOException {
								
		String regex = "var nano_ajax_object[^;]*"; 
		String episodeWrapper = new Regex(regex, content).match().toString();
		
		if(!episodeWrapper.isEmpty()) episodeWrapper = episodeWrapper.split("\"")[episodeWrapper.split("\"").length - 2].replaceAll("\\\\", "");

		if(episodeWrapper.isEmpty()) {
			regex = "<h2.*CLICCA QUI";							
			episodeWrapper = new Regex(regex, content).match().toString();
			if(!episodeWrapper.isEmpty()) episodeWrapper = episodeWrapper.split("href=\"")[1].split("\"")[0];										
		}
		
		if(episodeWrapper.isEmpty()) return null;
		
		return  episodeWrapper;
	}
		
	private HttpResponse getPage(String addr) {

		try {
			URL url = new URL(addr);
			HttpRequest req = new HttpRequest(url);
			
			WebBrowser wb = new WebBrowser();			
			wb.sendRequest(req);
			
			return wb.getResponse();
		
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		
		return null;
	}

	@Override
	public Locale getLanguage() {
		return EuroStreaming.LANGUAGE;
	}
	
	

}
