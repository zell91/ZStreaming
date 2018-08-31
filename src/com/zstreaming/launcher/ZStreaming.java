package com.zstreaming.launcher;

import com.zstreaming.gui.splash.SplashScreen;
import com.zstreaming.settings.SettingsManager;

import javafx.application.Application;
import javafx.stage.Stage;

public class ZStreaming extends Application {
	
	private static SettingsManager settingManager = new SettingsManager();
	
	public static void main(String[] args) {
		launch(args);
	}
	
	public static SettingsManager getSettingManager() {
		return settingManager;
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		SplashScreen splashScreen = new SplashScreen();
		splashScreen.init();
		splashScreen.loadApplication(primaryStage, this.getParameters().getRaw());
		splashScreen.show();
	}
	
	public synchronized static void gcClean(long millis) {
		Thread gc = new Thread(()-> {
			try {
				Thread.sleep(millis);
			} catch (InterruptedException e) { }
			System.gc();
			System.out.println("GC CLEAN");
		});
		
		gc.setDaemon(true);
		gc.setName("GC CleanerThread");
		
		if(Thread.getAllStackTraces().keySet().stream().noneMatch(th->th.getName().equals(gc.getName()))) {
			gc.start();
		}		
	}
}
