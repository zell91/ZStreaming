package com.zstreaming.gui.components;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;

public class PriorityIndicator extends HBox{

	private DoubleProperty valueProperty;
	private DoubleProperty widthProperty;
	private DoubleProperty heightProperty;

	private StackPane stackPane;
	
	private HBox track;
	private HBox bar;
	
	private static final int MAX_RECT_NUM = 5;
	
	public PriorityIndicator(double value) {
		super();
		Region region = new Region();
		HBox.setHgrow(region, Priority.ALWAYS);
		region.getStyleClass().add("_filler");
		this.getChildren().add(region);
		this.getStyleClass().add("priority-indicator");
		this.valueProperty = new SimpleDoubleProperty(value);
		this.widthProperty = new SimpleDoubleProperty(50);
		this.heightProperty = new SimpleDoubleProperty(30);
		this.stackPane = new StackPane();
		this.stackPane.setAlignment(Pos.CENTER_LEFT);
		this.getChildren().add(stackPane);
		this.track = new HBox(1);
		this.bar = new HBox(1);
		this.bar.setMinWidth(0.0);
		this.track.setMinWidth(0.0);
		this.stackPane.getChildren().add(this.track);
		this.stackPane.getChildren().add(this.bar);
		this.setTrack();
		this.setBar();
		this.setListener();
	
	}

	public PriorityIndicator() {
		this(0.0);
	}
	

	public DoubleProperty valueProperty() {
		return this.valueProperty;
	}	
	public DoubleProperty _widthProperty() {
		return this.widthProperty;
	}	
	public DoubleProperty _heightProperty() {
		return this.heightProperty;
	}	
	
	public double getValue() {
		return this.valueProperty.get();
	}
	
	public double getPIWidth() {
		return this.widthProperty.get();
	}
	
	private void setValue(double value) {
		this.valueProperty.set(value);
	}
	
	public void setPIWidth(double width) {
		this.widthProperty.set(width);
		
		if(width%MAX_RECT_NUM != 0) throw new IllegalArgumentException("It is not divisible by 5.");
		
		this.track.setPrefWidth(width);	
		this.bar.setPrefWidth((width/5)*this.getValue());		

		for(int i=0;i<this.track.getChildren().size();i++) {
			Region rect = (Region) this.track.getChildren().get(i);
			rect.setPrefWidth(width/5);
		}		
		for(int i=0;i<this.bar.getChildren().size();i++) {
			Region rect = (Region) this.bar.getChildren().get(i);
			rect.setPrefWidth(width/5);
		}
	}
	
	public double getPIHeight() {
		return this.heightProperty.get();
	}
	
	public void setPIHeight(double height) {		
		this.heightProperty.set(height);
		
		this.track.setPrefHeight(height);
		this.bar.setPrefHeight(height);

		for(int i=0;i<this.track.getChildren().size();i++) {
			Region rect = (Region) this.track.getChildren().get(i);
			rect.setPrefHeight((height/MAX_RECT_NUM)*(i+1));
			rect.setMaxHeight((height/MAX_RECT_NUM)*(i+1));
		}		
		for(int i=0;i<this.bar.getChildren().size();i++) {
			Region rect = (Region) this.bar.getChildren().get(i);
			rect.setPrefHeight((height/MAX_RECT_NUM)*(i+1));
			rect.setMaxHeight((height/MAX_RECT_NUM)*(i+1));
		}		
	}	

	public void setTrack() {				
		Background background = new Background(new BackgroundFill[] {
				new BackgroundFill(Color.WHITE, new CornerRadii(2,2,0,0, false), new Insets(0.2)),
				new BackgroundFill(Color.LIGHTGREY, new CornerRadii(2,2,0,0, false), new Insets(1)),
				});
		
		Background hoverBackground = new Background(new BackgroundFill[] {
				new BackgroundFill(Color.WHITE, new CornerRadii(2,2,0,0, false), new Insets(0.2)),
				new BackgroundFill(Color.LIGHTGREY, new CornerRadii(2,2,0,0, false), new Insets(1)),
				new BackgroundFill(new Color(1,1,1,.4), new CornerRadii(2,2,0,0, false), new Insets(0.2)),
				});
		
		this.build(MAX_RECT_NUM, background, hoverBackground, this.track);

	}
	
	
	private void setBar() {	
		Background background = (new Background(new BackgroundFill[] {
					new BackgroundFill(Color.WHITE, new CornerRadii(2,2,0,0, false), new Insets(0)),
					new BackgroundFill(new LinearGradient(100, 0, 225, 120, true, CycleMethod.REFLECT, new Stop(0, Color.CORNFLOWERBLUE), new Stop(1, Color.ALICEBLUE)), new CornerRadii(2,2,0,0, false), new Insets(.5, .5, 0, 0)),
					}));
		
		Background hoverBackground = new Background(new BackgroundFill[] {
				new BackgroundFill(Color.WHITE, new CornerRadii(2,2,0,0, false), new Insets(0)),
				new BackgroundFill(new LinearGradient(100, 0, 225, 120, true, CycleMethod.REFLECT, new Stop(0, Color.CORNFLOWERBLUE), new Stop(1, Color.ALICEBLUE)), new CornerRadii(2,2,0,0, false), new Insets(.5, .5, 0, 0)),
				new BackgroundFill(new Color(0,0,0,.15), new CornerRadii(2,2,0,0, false), new Insets(0))
				});
		
		this.build(this.getValue(), background, hoverBackground, this.bar);

	}
	
	private void build(double value, Background background, Background hoverBackground, HBox parent) {
		parent.maxWidthProperty().bind(parent.prefWidthProperty());
		parent.setPrefWidth(this.getPIWidth()/MAX_RECT_NUM*value);
		parent.setPrefHeight(this.getPIHeight());
		
		parent.setAlignment(Pos.BOTTOM_LEFT);
		
		double width = (parent.getPrefWidth() - value)/value;
		double height = parent.getPrefHeight()/MAX_RECT_NUM;
		
		for(int i=0;i<value;i++) {
			Region rect = new Region();
			this.getStyleClass().add("rect");
			rect.setCursor(Cursor.HAND);				
			rect.setBackground(background);
			rect.setPrefWidth(width);
			rect.setPrefHeight(height*(i+1));
			rect.setMaxHeight(height*(i+1));
			rect.setOnMouseClicked(e->this.setValue(parent.getChildren().indexOf(e.getSource()) + 1));
			rect.hoverProperty().addListener(new ChangeListener<Boolean>() {
				@Override
				public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
					if(newValue) {
						rect.setBackground(hoverBackground);
					}else {
						rect.setBackground(background);
					}
				}
									
			});
			
			parent.getChildren().add(rect);
		}

	}
	

	private void setListener() {
		this.valueProperty.addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> arg0, Number arg1, Number arg2) {
				bar.getChildren().clear();
				setBar();				
				System.gc();
			}			
		});		
	}
}
