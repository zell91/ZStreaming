package com.zstreaming.gui.components;

import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;
import javafx.geometry.Pos;
import javafx.scene.effect.MotionBlur;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Ellipse;
import javafx.util.Duration;

public class WaitingBar extends StackPane {
	
	private Region track;
	private Ellipse bar;
	
	public WaitingBar() {
		super();
		
		this.track = new Region();
		this.bar = new Ellipse();
		
		this.setupTrack();
		this.setupBar();
		this.barAnimation();
		
		this.track.getStyleClass().add("track");
		this.bar.getStyleClass().add("bar");
		this.getStyleClass().add("waiting-bar");
		
		this.setAlignment(Pos.CENTER);
		this.getChildren().addAll(this.track, this.bar);
		
		this.setPrefHeight(6.0);
		this.setMinWidth(0.0);
	}

	private void setupBar() {
		MotionBlur motionBlur = new MotionBlur();
		motionBlur.setAngle(5);
		motionBlur.setRadius(50);
		
		this.bar.setEffect(motionBlur);
		this.bar.radiusYProperty().bind(this.heightProperty().subtract(this.getPadding().getTop() + this.getPadding().getBottom()).divide(2));
		this.bar.setStyle("-fx-fill:linear-gradient(to right, transparent 0%, rgba(255,255,255,.9) 50%,  transparent 80%);");
	}

	private void setupTrack() {
		MotionBlur motionBlur = new MotionBlur();
		motionBlur.setAngle(35);
		motionBlur.setRadius(10);
		
		this.track.setEffect(motionBlur);
		this.track.maxHeightProperty().bind(this.bar.radiusYProperty().multiply(.9));
		this.track.setStyle("-fx-background-color:linear-gradient(to right, transparent 0%,  transparent 10%, rgb(0, 17, 51) 40%, transparent 90%, transparent 100%); -fx-background-radius:100");
	}

	private void barAnimation() {
		TranslateTransition translateTransition = new TranslateTransition(Duration.millis(1800), this.bar);
		
		translateTransition.setInterpolator(Interpolator.EASE_BOTH);
		translateTransition.setAutoReverse(false);
		translateTransition.setCycleCount(-1);
		
		this.widthProperty().addListener((observable, oldValue, newValue)->{
			this.bar.setRadiusX(newValue.doubleValue()/6);
			
			translateTransition.stop();
			translateTransition.setFromX(this.bar.radiusXProperty().multiply(2.1).negate().get());
			translateTransition.setToX(newValue.doubleValue()*0.4);
			translateTransition.play();
		});
	}

}
