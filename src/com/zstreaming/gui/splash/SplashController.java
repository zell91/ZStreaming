package com.zstreaming.gui.splash;

import java.net.URL;
import java.util.ResourceBundle;

import com.zstreaming.statistics.SessionStatistics;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class SplashController implements Initializable {

	@FXML
	private VBox main, center, bottom;;
	
	@FXML
	private StackPane stackPane, progressBar;
	
	@FXML
	private Pane backgroundPane, contentPane;
	
	@FXML
	private ImageView imageView;
		
	@FXML
	private HBox top, progressFooter;
		
	@FXML
	private Label title, progressText;
		
	@Override
	public void initialize(URL url, ResourceBundle bundle) {
		this.progressText.textProperty().bind(SessionStatistics.stateProperty());
	}

	public void startAnimation() {
		SplashScreenAnimation splashScreenAnimation = new SplashScreenAnimation(progressBar.getChildren(), title);
		splashScreenAnimation.start();
	}	

}
