package com.zstreaming.gui.components;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.beans.value.WritableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.stage.Stage;
import javafx.util.Duration;

public abstract class StageListView<I> extends GridPane {
	
	protected Label headerLbl;
	protected Button actionBtn, closeBtn;
	protected ListView<I> listView;

	public StageListView() {
		this.closeBtn = new Button();
		this.listView = new ListView<>();
		this.headerLbl = new Label();
		this.actionBtn = new Button();

		this.setAlignment(Pos.TOP_CENTER);
		GridPane.setConstraints(this.closeBtn, 1, 0, 1, 1, HPos.RIGHT, VPos.TOP, Priority.ALWAYS, Priority.NEVER);

		ColumnConstraints col = new ColumnConstraints();
		RowConstraints row = new RowConstraints();

		col.setHgrow(Priority.ALWAYS);
		row.setPrefHeight(35);
		
		this.getColumnConstraints().add(col);
		this.getRowConstraints().add(row);	

		this.setOnMousePressed(e->this.moveStage(e));
		this.actionBtn.disableProperty().bind(Bindings.createBooleanBinding(()->this.listView.getSelectionModel().getSelectedItems().isEmpty(), this.listView.getSelectionModel().selectedItemProperty()));

		this.headerLbl.getStyleClass().add("list-view-header");
		this.actionBtn.getStyleClass().addAll("list-view-action", "list-view-btn");
		this.closeBtn.getStyleClass().addAll("list-view-close", "list-view-btn");
		this.listView.getStyleClass().add("_list-view");
		this.getStyleClass().add("list-view-container");
		
		this.getStylesheets().add(this.getClass().getResource("../fxml/styles/list_view.css").toExternalForm());		
	}	

	public ListView<I> getListView(){
		return this.listView;
	}
		
	protected abstract void fillListView();
	
	public void setOnAction(EventHandler<ActionEvent> value) {
		this.actionBtn.setOnAction(value);
	}	
	
	public void setOnCloseRequest(EventHandler<ActionEvent> value) {
		this.closeBtn.setOnAction(value);
	}	
	
	private void moveStage(MouseEvent event) {
		if(event.getPickResult().getIntersectedPoint().getY() < 25.0) {
			this.setOnMouseDragged(e->{
				if(event.getPickResult().getIntersectedNode() != null) {
					Stage stage = (Stage) event.getPickResult().getIntersectedNode().getScene().getWindow();
					stage.setX(e.getScreenX() - event.getSceneX());
					stage.setY(e.getScreenY() - event.getSceneY());
				}
			});
		}else {
			this.setOnMouseDragged(null);
		}
	}
	
	public void closeWindow() {
		Timeline animation = this.animation(0.0);
		
		animation.setOnFinished(e->{
			((Stage)this.getScene().getWindow()).close();	
			System.gc();
		});
		
		animation.play();
	}
		
	private Timeline animation(double fromValue, double toValue) {
		boolean showing = this.getOpacity() != fromValue;
		
		if(showing) {
			this.setScaleX(fromValue);
			this.setScaleY(fromValue);
			this.setOpacity(fromValue);
		}
		
		WritableValue<Double> writable0 = new WritableValue<Double>() {
			@Override
			public Double getValue() {
				return getOpacity();
			}	

			@Override
			public void setValue(Double value) {
				setScaleX(value);
				setScaleY(value);
			}
			
		};
		
		WritableValue<Double> writable1 = new WritableValue<Double>() {
			@Override
			public Double getValue() {
				return getOpacity();
			}

			@Override
			public void setValue(Double value) {
				setOpacity(value);
			}			
		};
		
		Timeline timeline = new Timeline();
		
		KeyValue key0 = new KeyValue(writable0, toValue);
		KeyValue key1 = new KeyValue(writable1, toValue);		

		KeyFrame frame0 = new KeyFrame(Duration.millis(showing ? 200 : 350), key0);
		KeyFrame frame1 = new KeyFrame(Duration.millis(showing ? 350 : 200), key1);

		timeline.getKeyFrames().addAll(frame0, frame1);
			
		return timeline;
	}
	
	private Timeline animation(double toValue) {
		return this.animation(this.getOpacity(), toValue);
	}

	public void showWindow(Stage stage) {
		Timeline animation = this.animation(0.0, 1.0);
		stage.setWidth(450);
		stage.setHeight(90 + this.listView.getFixedCellSize()*this.listView.getItems().size() + this.listView.getFixedCellSize()*.5);
		stage.setOnShowing(e->animation.play());
		if(!this.listView.getItems().isEmpty()) this.listView.getSelectionModel().select(0);
		stage.show();
	}
}
