package com.zstreaming.search;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import com.zstreaming.search.plugins.Plugin;

public class SearchTask implements Callable<Void>{
	
	public Plugin site;
	public SearchRequest request;
	public CompletionService<Result> service;
	
	private LinkedBlockingQueue<Result> results;

	public SearchTask(Plugin site, SearchRequest request, LinkedBlockingQueue<Result> results) {
		this.site = site;
		this.request = request;
		this.service = new ExecutorCompletionService<>(SearchEngine.EXECUTOR);
		this.results = results;
	}
	
	@Override
	public Void call() throws Exception {
		this.site.startRequest(this.request);
		
		while(!this.site.getExecutor().isTerminated() || this.site.getResults().size() > 0) {
			Result result = this.site.getResults().poll(15, TimeUnit.SECONDS);
			if(result != null) {
				this.results.put(result);
			}
		}
				
		return null;
	}

}
