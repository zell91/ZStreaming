package com.zstreaming.gui.splash;

import java.io.IOException;
import java.util.List;

import com.util.network.OnlineChecker;
import com.zstreaming.gui.ZStreamingLoader;
import com.zstreaming.launcher.ZStreaming;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class SplashScreen extends Stage {
	
	private SplashController splashController;
	private Parent splashScreen;
	
	public SplashScreen() {
		super();
		this.setTitle("ZStreaming by zell91");
		this.setResizable(false);
		this.initStyle(StageStyle.TRANSPARENT);
		this.sizeToScene();
	}
	
	public SplashController getSplashController() {
		return this.splashController;
	}
	
	public Parent getSplashScreen() {
		return this.splashScreen;
	}
	
	public void init() throws IOException {
		FXMLLoader fxml = new FXMLLoader(this.getClass().getResource("./fxml/splash_screen.fxml"));
		this.splashScreen = fxml.load();
		this.splashController = fxml.getController();
		Scene scene = new Scene(this.splashScreen);
		scene.setFill(null);
		this.setScene(scene);
		this.splashController.startAnimation();
	}

	public void loadApplication(Stage stage, List<String> parameters) {
		stage.setOnShowing(e->{
			this.close();
			OnlineChecker.start();
			ZStreaming.gcClean(1000);
		});
		Thread loaderTask = new Thread(()->{
			ZStreamingLoader loader = new ZStreamingLoader(parameters);
			loader.load();
						
			try {
				loader.init(stage);
				
				if(loader.isAutorun()) {
					Platform.runLater(()->this.close());
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
		
		loaderTask.setName("ZLoaderTask");
		loaderTask.setDaemon(true);
		loaderTask.start();
	}

}
