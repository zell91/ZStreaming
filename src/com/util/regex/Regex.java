package com.util.regex;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Regex {
	
	private Matcher matcher;
	private Pattern pattern;
	
	public Regex(String regex, CharSequence text) {
		this.pattern = Pattern.compile(regex);
		this.matcher = this.pattern.matcher(text);
	}
	
	public void reset(String regex, String text) {
		this.reset(text);
		this.useRegex(regex);
	}
	
	public void reset(String text) {
		this.matcher.reset(text);
	}
	
	public void useRegex(String regex) {
		this.pattern = Pattern.compile(regex);
		this.matcher.usePattern(this.pattern);
	}
	
	public CharSequence match() {
		return this.matcher.find() ? this.matcher.group() : "";
	}
	
	public CharSequence[] matches() {
		List<CharSequence> results = new ArrayList<>();
		
		while(this.matcher.find()) {
			results.add(this.matcher.group());
		}
		
		return results.toArray(new CharSequence[] {});
	}
	
	public CharSequence[] getLines() {
		return this.matcher.find() ? this.matcher.group().split("\r\n") : new String[0];
	}

}
