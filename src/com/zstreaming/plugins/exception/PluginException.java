package com.zstreaming.plugins.exception;

import com.zstreaming.plugins.Plugin;
import com.zstreaming.statistics.SessionStatistics;

public class PluginException extends Throwable {

	public enum State{
		OFFLINE, NOT_FOUND;
	}
	
	private static final long serialVersionUID = 715792187414238317L;
	
	private Plugin plugin;
	private PluginException.State state;
	private String url;
		
	public PluginException(Plugin plugin, PluginException.State state) {
		super();
		this.plugin = plugin;
		if(plugin != null) this.url = plugin.getURL().toExternalForm();
		this.state = state;
	}
	
	public PluginException(String url) {
		this(null, State.NOT_FOUND);
		this.url = url;
	}
	
	public Plugin getPlugin() {
		return plugin;
	}
	
	@Override
	public String getMessage() {
		switch(this.state) {
			case OFFLINE:
				SessionStatistics.setState("address.not.exists");
				return "Address \"" +  this.url + "\" dell'hoster \"" + this.plugin.getHoster() + "\" doesn't exists or is no longer active.";
			case NOT_FOUND:
				SessionStatistics.setState("plugin.not.found");
				return "Plugin not found for \"" + this.url + "\".";
			default:
				return null;
		}
	}
	
	@Override
	public String toString() {
		return this.getClass().getName();
	}


}
