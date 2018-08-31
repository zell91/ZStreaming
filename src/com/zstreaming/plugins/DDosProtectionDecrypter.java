package com.zstreaming.plugins;

import java.net.MalformedURLException;
import java.net.URL;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import com.util.regex.Regex;
import com.zstreaming.browser.WebBrowser;
import com.zstreaming.browser.http.HttpRequest;
import com.zstreaming.browser.http.HttpResponse;
import com.zstreaming.browser.http.Session;
import com.zstreaming.browser.http.SimpleRequestHeader;
import com.zstreaming.plugins.exception.PluginException;

public class DDosProtectionDecrypter extends Plugin {
	
	private HttpResponse response;
	private WebBrowser browser;
	private ScriptEngineManager scriptManager;

	
	public DDosProtectionDecrypter(WebBrowser browser) {
		super();
		this.browser = browser;
		this.scriptManager = new  ScriptEngineManager();
	}
	
	public void resolveChallenge() throws InterruptedException, PluginException {				
		this.response = this.browser.getResponse(5000);
		
		this.url = this.response.getURL();
		
		if(!this.response.getResponseHeader().get("Server").get(0).equalsIgnoreCase("cloudflare")) {
			this.finalLink = this.url;
			return;
		}
		
		String content = this.response.getContent().toString();
		String answer = null;
		
		try {
			answer = this.getAnswer(content);
		} catch (ScriptException e) {
			return;
		}catch(NumberFormatException e) {
			this.browser.sendRequest();
			this.resolveChallenge();
			return;
		}

		CharSequence[] formLines = new Regex("<form\\s*id=\"challenge-form.*\r\n.*=\"jschl_vc.*\r\n.*=\"pass.*\r\n.*=\"jschl_answer", content).getLines();
		String action = new Regex("action=\"[^\"]*", formLines[0]).match().toString().split("\"")[1];
		String query = "";
		
		for(CharSequence line : formLines) {
			String name = new Regex("name=\"[^\"]*", line).match().toString();
			String value = new Regex("value=\"[^\"]*", line).match().toString();
			query += ( name.isEmpty() ? "" : name.split("\"")[1] ) +  ( name.isEmpty() ? "" : "=" ) + ( value.isEmpty() ? "" :  value.split("\"")[1] ) +  ( name.isEmpty() ? "" : "&" );
		}
		
		query = query.substring(0, query.length() - 1) + answer;
		
		URL _url = null;
		
		try {
			_url = new URL("http://" + this.url.getAuthority() + action + "?" + query);
		} catch (MalformedURLException e) {
			throw new PluginException(url.toExternalForm());
		}
		this.sendAnswer(_url);

	}
	

	private void sendAnswer(URL url) throws InterruptedException, PluginException {
		HttpRequest request = new HttpRequest(url);
		request.setCookies();
		SimpleRequestHeader header = new SimpleRequestHeader();
		header.addRequestProperty("Connection", "close");
		header.addRequestProperty("Referer", this.url.toExternalForm());
		request.setRequestHeader(header);
		Session	resp = 	this.browser.sendRequest(request);
		if(resp.getCode() == 503) {
			this.resolveChallenge();
		}else
			this.finalLink = resp.getURL();
	}

	private String getAnswer(String content) throws ScriptException, NumberFormatException {
		final String line =  new Regex("var\\ss,t,o,p,b,r,e,a,k,i,n,g,f,.*", content).match().toString();
		final String var = line.substring(line.lastIndexOf(",") + 1).split("=")[0].replaceAll("\\s", "") + "." + line.split("\"")[1];
		String t = response.getURL().getAuthority();
		String base = line.toString().split(":")[1].split("\\}")[0];
		String script = "var base = " + base + ";";

		CharSequence[] others = new Regex(var + "[^;]*", content).matches();	

		for(CharSequence other : others) {
			if(others[others.length -1].equals(other)) break;
			script += other.toString().replaceAll(var,  "base") + ";";
		}

		script += "base = +base.toFixed(10) + " + t.length() + ";";
		script += "base = base.toFixed(10)";
		ScriptEngine scriptEngine = scriptManager.getEngineByName("JavaScript");
		
		Object result = scriptEngine.eval(script);
System.out.println(result);
		result = result.toString();
		
		return result.toString();
	}

	@Override
	public boolean hasCaptcha() {
		return false;
	}

}
