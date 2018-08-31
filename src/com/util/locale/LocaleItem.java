package com.util.locale;

import java.util.Locale;

public class LocaleItem {

	private Locale locale;
	
	public LocaleItem(Locale locale) {
		this.locale = locale;
	}
	
	public Locale getLocale() {
		return this.locale;
	}
	
	@Override
	public String toString() {
		return this.locale.getDisplayLanguage(this.locale).substring(0, 1).toUpperCase() + this.locale.getDisplayLanguage(this.locale).substring(1);
	}

	public static LocaleItem getDefault() {
		return new LocaleItem(Locale.ITALY);
	}
	
}
