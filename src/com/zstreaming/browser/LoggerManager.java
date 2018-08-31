package com.zstreaming.browser;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import com.zstreaming.launcher.ZStreaming;

public class LoggerManager {
	
	private  Logger requestLogger = Logger.getLogger("request");
	private  Logger errorLogger = Logger.getLogger("error");	
	private  Logger warninglogger = Logger.getLogger("warning");	

	private FileHandler fileHandler;
	
	public LoggerManager() {
		this.requestLogger.setUseParentHandlers(System.console() == null);
		this.warninglogger.setUseParentHandlers(System.console() == null);
		/*this.requestLogger.setUseParentHandlers(false);
		this.warninglogger.setUseParentHandlers(false);*/
				
		try {
			this.loadFileHandler();
			this.requestLogger.setLevel(Level.INFO);
			this.requestLogger.addHandler(fileHandler);
			this.errorLogger.setLevel(Level.SEVERE);
			this.errorLogger.addHandler(fileHandler);
			this.warninglogger.setLevel(Level.WARNING);
			this.warninglogger.addHandler(fileHandler);
			
		} catch (SecurityException | IOException e) {
			e.printStackTrace();
			System.out.println("Creation log file failed");
		}
	}
	
	public void setFileHandler(FileHandler fileHandler) {
		this.fileHandler = fileHandler;
	}	
	
	public FileHandler getFileHandler() {
		return this.fileHandler;
	}

	public FileHandler loadFileHandler() throws SecurityException, IOException {
		String logPath = ZStreaming.getSettingManager().getSettings().get("log.path");
		if(logPath == null) logPath = new File("log").getAbsolutePath();
		this.fileHandler = new FileHandler(logPath + "/log_session_" + LoggerManager.random() + ".txt");
		this.fileHandler.setFormatter(this.getSimpleFormatter());
		return this.fileHandler;
	}
		
	private Formatter getSimpleFormatter() {
		SimpleFormatter formatter = new SimpleFormatter();
				
		return formatter;
	}
	
	public void error(String message, Throwable exception) {
		this.errorLogger.log(Level.SEVERE, message + (Objects.nonNull(exception)? " Exception: " + exception : ""));
	}
	
	public void warning(String message, Throwable exception) {
		this.warninglogger.log(Level.WARNING, message + (Objects.nonNull(exception)? " Exception: " + exception : ""));
	}
	
	public void info(String message) {
		this.requestLogger.log(Level.INFO,message);
	}

	public Logger getRequestLogger() {
		return this.requestLogger;
	}
	
	public Logger getErrorLogger() {
		return this.errorLogger;
	}
	
	private static int random() {
		String raw_random = ((int)(Math.random()*10000000)) + "";
		
		for(int i=0;i<10 - raw_random.length();i++) {
			raw_random += ((int)(Math.random() * 10));
		}
		
		return Integer.parseInt(raw_random);		
	}
}
