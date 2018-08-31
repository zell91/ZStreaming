package com.zstreaming.plugins.shortlink;

import com.zstreaming.plugins.Plugin;
import com.zstreaming.plugins.exception.PluginException;

public abstract class ShortLink extends Plugin { 
	
	public ShortLink() {
		super();
	}
	
	public abstract void decrypt() throws PluginException, InterruptedException;	
}
