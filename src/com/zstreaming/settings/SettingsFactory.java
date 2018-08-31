package com.zstreaming.settings;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.SortedMap;
import java.util.TreeMap;

public class SettingsFactory {
	
	private static final String ROOT_PATH_KEY = "root.path";
	private static final String LOG_PATH_KEY = "log.path";
	private static final String DOWNLOAD_PATH_KEY = "download.path";
	private static final String ZDOWNLOAD_STORE_KEY = "zdownloads.store";
	private static final String MAX_DOWNLOAD_KEY = "max.download";
	private static final String COOKIE_PATH_KEY = "cookie.path";
	private static final String UPDATE_CHART_DELAY_KEY = "update.chart.delay";
	private static final String LIST_PATH_KEY = "list.path";
	private static final String LIST_IMAGE_URL_KEY = "list.image.url";
	private static final String HISTORY_PATH_KEY = "history.path";
	private static final String SETTINGS_PATH_KEY = "settings.store";
	private static final String LANG_KEY = "lang";
	private static final String STARTUP_KEY = "startup";
	private static final String STARTUP_SUB_KEY = "startup.sub";
	private static final String AUTO_MODE_KEY = "auto.mode";	
	private static final String AUTO_DELETE_KEY = "auto.delete";
	private static final String EXIT_OPTIONS_KEY = "exit.options";
	private static final String AUTO_DELETE_FILTER_KEY = "auto.delete.filter";
	private static final String CONFIRM_INTERRUPT_DOWN_KEY = "confirm.interrupt";
	private static final String MAIN_CHART_ENABLE_KEY = "main.chart.enable";
	private static final String SINGLE_CHART_ENABLE_KEY = "single.charts.enable";
	private static final String DEFAULT_LIST_NAME_KEY = "default.list.name";


	private static final String ROOT_PATH_VALUE = new File("").getAbsolutePath();
	private static final String LOG_PATH_VALUE = new File("log").getAbsolutePath();
	public static final String DOWNLOAD_PATH_VALUE = new File("downloads").getAbsolutePath();
	public static final String ZDOWNLOAD_STORE_VALUE = new File("downloads/zdownloads.zsav").getAbsolutePath();
	private static final String MAX_DOWNLOAD_VALUE = "4";
	private static final String COOKIE_PATH_VALUE = new File("browser/cookies").getAbsolutePath();
	private static final String UPDATE_CHART_DELAY_VALUE = "5";
	private static final String LIST_PATH_VALUE = new File("").getAbsolutePath();
	private static final String LIST_IMAGE_URL_VALUE = new File("images/image_mlist.png").getAbsolutePath();
	private static final String HISTORY_PATH_VALUE = new File("history/history.history").getAbsolutePath();
	private static final String SETTINGS_PATH_VALUE = new File("settings/settings.conf").getAbsolutePath();
	private static final String LANG_VALUE = Locale.getDefault().equals(Locale.ITALY) ? Locale.getDefault().toString() : Locale.UK.toString();
	private static final String STARTUP_VALUE = "0";
	private static final String STARTUP_SUB_VALUE = "-1";
	private static final String AUTO_MODE_VALUE = "0";	
	private static final String AUTO_DELETE_VALUE = "0";
	private static final String EXIT_OPTIONS_VALUE = "0";
	private static final String AUTO_DELETE_FILTER_VALUE = "1";
	private static final String CONFIRM_INTERRUPT_DOWN_VALUE = "1";
	private static final String MAIN_CHART_ENABLE_VALUE = "1";
	private static final String SINGLE_CHART_ENABLE_VALUE = "1";
	private static final String DEFAULT_LIST_NAME_VALUE = "MyList";


	private static final Map<String, String> configuration = SettingsFactory.getProperties();
	
	private SettingsFactory() { }

	private static Map<String, String> getProperties() {
		SortedMap<String, String> configuration = new TreeMap<>();
		
		configuration.put(ROOT_PATH_KEY, ROOT_PATH_VALUE);
		configuration.put(LOG_PATH_KEY, LOG_PATH_VALUE);
		configuration.put(DOWNLOAD_PATH_KEY, DOWNLOAD_PATH_VALUE);
		configuration.put(ZDOWNLOAD_STORE_KEY, ZDOWNLOAD_STORE_VALUE);
		configuration.put(MAX_DOWNLOAD_KEY, MAX_DOWNLOAD_VALUE);
		configuration.put(COOKIE_PATH_KEY, COOKIE_PATH_VALUE);
		configuration.put(UPDATE_CHART_DELAY_KEY, UPDATE_CHART_DELAY_VALUE);
		configuration.put(LIST_PATH_KEY, LIST_PATH_VALUE);
		configuration.put(LIST_IMAGE_URL_KEY, LIST_IMAGE_URL_VALUE);
		configuration.put(HISTORY_PATH_KEY, HISTORY_PATH_VALUE);
		configuration.put(LANG_KEY, LANG_VALUE);
		configuration.put(SETTINGS_PATH_KEY, SETTINGS_PATH_VALUE);
		configuration.put(STARTUP_KEY, STARTUP_VALUE);
		configuration.put(STARTUP_SUB_KEY, STARTUP_SUB_VALUE);
		configuration.put(AUTO_MODE_KEY, AUTO_MODE_VALUE);
		configuration.put(AUTO_DELETE_KEY, AUTO_DELETE_VALUE);
		configuration.put(AUTO_DELETE_FILTER_KEY, AUTO_DELETE_FILTER_VALUE);		
		configuration.put(EXIT_OPTIONS_KEY, EXIT_OPTIONS_VALUE);
		configuration.put(CONFIRM_INTERRUPT_DOWN_KEY, CONFIRM_INTERRUPT_DOWN_VALUE);
		configuration.put(MAIN_CHART_ENABLE_KEY, MAIN_CHART_ENABLE_VALUE);
		configuration.put(SINGLE_CHART_ENABLE_KEY, SINGLE_CHART_ENABLE_VALUE);
		configuration.put(DEFAULT_LIST_NAME_KEY, DEFAULT_LIST_NAME_VALUE);


		return configuration;
	}
	
	public static void restorePropertiesFile() {
		SettingsFactory.restoreProperties(SettingsFactory.configuration, false);
	}
		
	public static void restoreGeneral() {
		Map<String, String> general = new HashMap<>();
		
		general.put(LOG_PATH_KEY, LOG_PATH_VALUE);
		general.put(LANG_KEY, LANG_VALUE);
		general.put(STARTUP_KEY, STARTUP_VALUE);
		general.put(STARTUP_SUB_KEY, STARTUP_SUB_VALUE);
		general.put(EXIT_OPTIONS_KEY, EXIT_OPTIONS_VALUE);
		
		SettingsFactory.restoreProperties(general, true);
	}
	
	public static void restoreDownload() {
		Map<String, String> download = new HashMap<>();
		
		download.put(DOWNLOAD_PATH_KEY, DOWNLOAD_PATH_VALUE);
		download.put(ZDOWNLOAD_STORE_KEY, ZDOWNLOAD_STORE_VALUE);
		download.put(MAX_DOWNLOAD_KEY, MAX_DOWNLOAD_VALUE);
		download.put(UPDATE_CHART_DELAY_KEY, UPDATE_CHART_DELAY_VALUE);
		download.put(AUTO_MODE_KEY, AUTO_MODE_VALUE);
		download.put(AUTO_DELETE_KEY, AUTO_DELETE_VALUE);
		download.put(AUTO_DELETE_FILTER_KEY, AUTO_DELETE_FILTER_VALUE);		
		download.put(CONFIRM_INTERRUPT_DOWN_KEY, CONFIRM_INTERRUPT_DOWN_VALUE);
		download.put(MAIN_CHART_ENABLE_KEY, MAIN_CHART_ENABLE_VALUE);
		download.put(SINGLE_CHART_ENABLE_KEY, SINGLE_CHART_ENABLE_VALUE);
		
		SettingsFactory.restoreProperties(download, true);
	}
	
	public static void restoreLists() {
		Map<String, String> lists = new HashMap<>();
		
		lists.put(LIST_PATH_KEY, LIST_PATH_VALUE);
		lists.put(LIST_IMAGE_URL_KEY, LIST_IMAGE_URL_VALUE);
		lists.put(DEFAULT_LIST_NAME_KEY, DEFAULT_LIST_NAME_VALUE);
		
		SettingsFactory.restoreProperties(lists, true);
	}
	
	public static void restoreConnection() {
		Map<String, String> connection = new HashMap<>();
		
		connection.put(LOG_PATH_KEY, LOG_PATH_VALUE);
		connection.put(COOKIE_PATH_KEY, COOKIE_PATH_VALUE);
		connection.put(HISTORY_PATH_KEY, HISTORY_PATH_VALUE);
		
		SettingsFactory.restoreProperties(connection, true);
	}
	
	private static void restoreProperties(Map<String, String> configuration, boolean forced) {
		File settingsPath = new File(SETTINGS_PATH_VALUE);
		
		try {	
			if(!settingsPath.getParentFile().exists()) {
				settingsPath.mkdirs();
			}
			Properties prop = new Properties();

			if(!settingsPath.exists()) {
				prop.putAll(configuration);
			}else {
				prop.load(new FileInputStream(settingsPath));

				for(String key : configuration.keySet()) {
					if(!prop.containsKey(key) || prop.getProperty(key).isEmpty() || forced) {
						prop.setProperty(key, configuration.get(key));					
					}
				}
			}			
			FileOutputStream out = new FileOutputStream(settingsPath);
			
			prop.store(out, "");
		}catch(IOException ex) {
			ex.printStackTrace();
		}		
	}
	
	public static boolean isValidPropertiesFile() {
		File settingsPath = new File(SETTINGS_PATH_VALUE);
		
		try {
			if(settingsPath.exists()) {
				Properties prop = new Properties();
				prop.load(new FileInputStream(settingsPath));

				for(String key : configuration.keySet()) {
					
					if(!prop.containsKey(key) || prop.getProperty(key).isEmpty())
						return false;
				}				
				return true;
			}
		}catch(IOException e) { }
		
		return false;
	}

	public static void checkAndRestoreIf() {		
		if(!SettingsFactory.isValidPropertiesFile()) {
			SettingsFactory.restorePropertiesFile();
			return;
		}
		
		File settingsPath = new File(SETTINGS_PATH_VALUE);
		
		try {
			if(settingsPath.exists()) {
				Properties prop = new Properties();
				prop.load(new FileInputStream(settingsPath));
				
				for(String key : configuration.keySet()) {
					if(key.endsWith("store") || key.endsWith("path") || key.endsWith("url")){
						if(!(new File(prop.getProperty(key)).exists())) {
							prop.remove(key);
						}
					}
				}
				
				FileOutputStream out = new FileOutputStream(settingsPath);
				
				prop.store(out, "");
				out.flush();
				out.close();
				SettingsFactory.restoreProperties(SettingsFactory.configuration, false);
			}
		}catch(IOException e) {
			SettingsFactory.restorePropertiesFile();
		}		
	}

	public static void clearPlayer() {
		File settingsPath = new File(SETTINGS_PATH_VALUE);
		
		try {	
			Properties prop = new Properties();

			prop.load(new FileInputStream(settingsPath));
			
			prop.remove("player.default");
			
			FileOutputStream out = new FileOutputStream(settingsPath);
			
			prop.store(out, "");
			out.flush();
			out.close();
		}catch(IOException ex) {
			ex.printStackTrace();
		}	
	}	

}
