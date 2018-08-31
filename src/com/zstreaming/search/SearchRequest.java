package com.zstreaming.search;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.zstreaming.search.plugins.Plugin;

public class SearchRequest {
	
	private String request;
	private int season;
	private int episode;
	private List<Plugin> sites;
	private Locale lang;
	
	public SearchRequest(String request) {
		this(request, 0, 0);
	}
	
	public SearchRequest(String request, int season){
		this(request, season, 0);
	}
	
	public SearchRequest(String request, int season, int episode) {
		this.request = request;
		this.season = season;
		this.episode = episode;
		this.sites = new ArrayList<>();
		this.lang = Locale.getDefault();
	}
	
	public String getRequest() {
		return request;
	}
	
	public int getSeason() {
		return season;
	}
	
	public int getEpisode() {
		return episode;
	}
	
	public void setSiteFilter(List<Plugin> sites) {
		this.sites = sites;
	}
	
	public List<Plugin> getSiteFilter() {
		return sites;
	}
	
	public void setLang(Locale lang) {
		this.lang = lang;
	}
	
	public Locale getLang() {
		return lang;
	}
	
	@Override
	public String toString() {
		return "Request: " + this.request + (season > 0 ? "    Season: " + this.season + (this.episode > 0 ? "    Episode: " + this.episode : "") : "");
	}
}
