package com.zstreaming.settings;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.zstreaming.statistics.SessionStatistics;

import java.util.Properties;
import java.util.TreeMap;

public class SettingsManager {

	private final String SETTINGS_PATH = new File("./settings/settings.conf").getAbsolutePath();
	private Properties settingsStore;
	private Map<String, String> settings;
	
	public final static String DOWNLOAD_PATH = "download.path";
	
	public SettingsManager() {
		this.settingsStore = new Properties();
		this.settings = new TreeMap<String, String>();
	}
	
	public Map<String, String> getSettings(){
		return settings;
	}
		
	public void load() {
		this.settingsStore.clear();
		this.settings.clear();
		SessionStatistics.setState("settings.updating");
		SettingsFactory.checkAndRestoreIf();
		
		try(FileInputStream in = new FileInputStream(SETTINGS_PATH)){
			this.settingsStore.load(in);
			Iterator<Object> iterator = this.settingsStore.keySet().iterator();
			while(iterator.hasNext()) {
				String key = iterator.next().toString();
				this.settings.put(key, this.settingsStore.getProperty(key));
			}
		}catch(IOException e){
			e.printStackTrace();
			SessionStatistics.setState("settings.not.found");
		}		
		SessionStatistics.setState("settings.updated");
	}

	public void storeSettings(Map<String, String> settings) {
		Iterator<Entry<String, String>> iterator = settings.entrySet().iterator();
		while(iterator.hasNext()) {
			Entry<String, String> keyValue = iterator.next();
			this.settingsStore.setProperty(keyValue.getKey(), keyValue.getValue());
		}
		
		try(FileOutputStream out = new FileOutputStream(SETTINGS_PATH)){
			this.settingsStore.store(out, "");
		}catch(IOException e) {
			e.printStackTrace();
		}
		this.load();
	}
	
	public void storeSettings(String key, String value) {
		Map<String, String> settings = new TreeMap<String, String>();
		settings.put(key, value);
		this.storeSettings(settings);
	}
	
}
