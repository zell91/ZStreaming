package com.zstreaming.gui.splash;

import com.zstreaming.statistics.SessionStatistics;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.util.Duration;

public class SplashScreenAnimation {
	
	private ObservableList<Node> progressBar;
	private Label title;
	
	public SplashScreenAnimation(ObservableList<Node> progressBar, Label title) {
		this.progressBar = progressBar;
		this.title = title;
	}

	public void start() {
		this.startProgressAnimation();
		this.startTitleAnimation();
	}
	
	private void startTitleAnimation() {
		TranslateTransition tt = new TranslateTransition(Duration.millis(200), this.title);		
		new Thread(()->{
			tt.setCycleCount(1);
			tt.setFromX(-500);
			tt.setFromY(0);
			tt.play();
			SessionStatistics.setState("loading.app");
			
			tt.setOnFinished(e->{
				FadeTransition ft = new FadeTransition(Duration.millis(200), this.title);
				ft.setFromValue(0);
				ft.setToValue(1);
				TranslateTransition tt2 = new TranslateTransition(Duration.millis(200), this.title);		
				tt2.setToX(0);
				tt2.setToY(0);
				tt2.play();
				ft.play();
			});
		}).start();
	}

	private void startProgressAnimation() {
		int timer = 600;
		for(Node node : this.progressBar) {
			TranslateTransition tt = new TranslateTransition(Duration.millis(timer), node);
			FadeTransition ft = new FadeTransition(Duration.millis(timer), node);
			ft.setFromValue(0.3);
			ft.setToValue(1);
			tt.setToX(420);
			ft.setAutoReverse(true);
			tt.setCycleCount(Animation.INDEFINITE);
			ft.setCycleCount(Animation.INDEFINITE);
			tt.play();
			ft.play();
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}
