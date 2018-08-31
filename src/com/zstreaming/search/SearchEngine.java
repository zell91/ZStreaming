package com.zstreaming.search;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

import com.zstreaming.search.plugins.Plugin;

public class SearchEngine {
	
	public final static ExecutorService EXECUTOR = Executors.newCachedThreadPool();
		
	private List<Plugin> sites;
		
	private Map<SearchRequest, LinkedBlockingQueue<Result>> searches;
		
	public SearchEngine() {
		this.sites = new ArrayList<>();
		this.searches = new HashMap<>();
	}
	
	public Map<SearchRequest, LinkedBlockingQueue<Result>> getSearches(){
		return searches;
	}
	
	public LinkedBlockingQueue<Result> getResultOfRequest(SearchRequest searchRequest) {
		return this.searches.get(searchRequest);
	}
	
	public SearchThread initSearch(SearchRequest searchRequest) {
		if(searchRequest.getSiteFilter().isEmpty()) {
			File[] clzzz = new File(this.getClass().getResource("plugins").getFile()).listFiles();
			
			for(File clzz : clzzz) {
				if(clzz.getName().equals("Plugin.class") || clzz.getName().contains("$")) continue;
				Class<?> plugin;
				try {
					plugin = Class.forName(this.getClass().getPackage().getName() + ".plugins."+ clzz.getName().replace(".class", ""));
					this.sites.add((Plugin)plugin.newInstance());
				} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
					e.printStackTrace();
				}
			}
			
			this.filterLang(searchRequest.getLang());
		}else{
			this.sites = searchRequest.getSiteFilter();
		}
		
		this.searches.put(searchRequest, new LinkedBlockingQueue<>());
				
		return new SearchThread(this.sites, searchRequest, this.searches.get(searchRequest));
	}
	
	public void search(SearchRequest searchRequest) {
		SearchThread searchThread = this.initSearch(searchRequest);
		EXECUTOR.submit(()->searchThread.start());
	}
	
	private void filterLang(Locale lang) {
		this.sites = this.sites.stream().filter(p->p.getLanguage().equals(lang)).collect(Collectors.toList());
	}

}
