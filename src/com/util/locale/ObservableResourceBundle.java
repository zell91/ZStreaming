package com.util.locale;

import java.util.Locale;
import java.util.ResourceBundle;

import javafx.beans.binding.StringBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

public class ObservableResourceBundle {
	
	private ObjectProperty<ResourceBundle> resources = new SimpleObjectProperty<>();
	public static final String BUNDLE_PATH = "locale/";

	public ObservableResourceBundle() { }
	
	public void setBundle(ResourceBundle bundle) {
		this.resources.set(bundle);
	}
	
	public void setResources(Locale locale) {
		Locale.setDefault(locale);
		ResourceBundle bundle = ResourceBundle.getBundle(BUNDLE_PATH + locale + "/" + locale, locale);
		this.resources.set(bundle);
	}
	
	public ResourceBundle getResources() {
		return this.resources.get();
	}
	
	public ObjectProperty<ResourceBundle> getResourcesProperty(){
		return this.resources;
	}
	
	public StringBinding getStringBindings(String key) {
		return new StringBinding() {
			{
				this.bind(resources);
			}
			
			@Override
			public String computeValue() {
				return getString(key);
			}			
		};
	}

	public String getString(String key) {
		if(getResources().containsKey(key))
			return getResources().getString(key);
		else
			return null;
	}
	
	public static String getLocalizedString(String key) {
		ResourceBundle bundle = ResourceBundle.getBundle(BUNDLE_PATH + Locale.getDefault() + "/" + Locale.getDefault(), Locale.getDefault());
		if(!bundle.containsKey(key)) {
			System.out.print(key+" = ");
			System.out.println( bundle.getString(key));
		}
		return bundle.getString(key);
	}
}
