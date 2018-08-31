package com.zstreaming.search;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import com.zstreaming.search.plugins.Plugin;

public class SearchThread extends Thread{
	
	private List<Plugin> sites;
	private SearchRequest searchRequest;
	
	private LinkedBlockingQueue<Result> results; 
	
	private Collection<SearchTask> tasks = new ArrayList<>();
	
	public SearchThread(List<Plugin> sites, SearchRequest request, LinkedBlockingQueue<Result> results) {
		this.sites = sites;
		this.searchRequest = request;
		this.results = results;
	}

	@Override
	public void run() {
		for(Plugin site : sites) {
			SearchTask task = new SearchTask(site, searchRequest, results);
			tasks.add(task);
		}		
		try {
			SearchEngine.EXECUTOR.invokeAll(tasks);
			SearchEngine.EXECUTOR.shutdown();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

}
