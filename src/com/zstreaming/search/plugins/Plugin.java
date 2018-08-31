package com.zstreaming.search.plugins;

import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import com.zstreaming.search.Result;
import com.zstreaming.search.SearchRequest;

public abstract class Plugin {
	
	protected SearchRequest request;
	
	protected String search;
	protected int season;
	protected int episode;
	protected LinkedBlockingQueue<Result> results;
	
	protected ExecutorService executor;

	public abstract void startRequest(SearchRequest request);
	
	protected void setRequest(SearchRequest request) {
		this.request = request;
		this.search = this.request.getRequest();
		this.season = this.request.getSeason();
		this.episode = this.request.getEpisode();
		this.results = new LinkedBlockingQueue<>();
		this.executor = Executors.newFixedThreadPool(20);
	}
	
	public ExecutorService getExecutor() {
		return executor;
	}
	
	public SearchRequest getRequest() {
		return request;
	}

	public LinkedBlockingQueue<Result> getResults() {
		return results;
	}

	public abstract Locale getLanguage();


}
