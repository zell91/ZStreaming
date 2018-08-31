package com.zstreaming.plugins.shortlink;

import java.net.MalformedURLException;
import java.net.URL;

import com.util.regex.Regex;
import com.zstreaming.plugins.Link;
import com.zstreaming.plugins.exception.PluginException;

@Link(name = "rapidcrypt",  urls = {"rapidcrypt.net"})
public class RapidCrypt extends ShortLink{
	
	@Override
	public void decrypt() throws PluginException, InterruptedException {
		final String regex = "<a\\s*class\\s*=\\s*\"button\"[^>]*";

		StringBuilder content = this.browser.getResponse().getContent();		
		String match = new Regex(regex, content).match().toString();
		if(match.contains("\"")) {
			String url = match.split("\"")[match.split("\"").length - 1];		
			
			try {
				this.finalLink = new URL(url);
			}catch(MalformedURLException ex) {	}
		}

	}

	@Override
	public boolean hasCaptcha() {
		return false;
	}

}
