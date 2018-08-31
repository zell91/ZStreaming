package com.zstreaming.gui.components;

import java.io.File;

import com.zstreaming.media.Media;

import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

public class FoundItem extends HBox {
		
	private ImageView imgView;
	private HBox nameBox;
	private Label sourceLbl, stateLbl, name;
	private Button downBtn, streamBtn, removeBtn, stopBtn, addToListBtn;
	private Region filler;
	private ProgressIndicator progressIndicator;
	
	private String source;

	private ListView<FoundItem> resultList;
	private Media media;
	
	public FoundItem(String url) {
		super(8);
		this.source = url;
		this.setPadding(new Insets(2,0,2,0));
		this.setAlignment(Pos.CENTER);
		this.getStyleClass().add("found_item");
		this.getStylesheets().add(this.getClass().getResource("../fxml/styles/found_items.css").toExternalForm());
		this.setup();
	}	
	
	public StringProperty stateProperty() {
		return this.stateLbl.textProperty();
	}
	
	public StringProperty nameProperty() {
		return this.name.textProperty();
	}
	
	public StringProperty sourceProperty() {
		return this.sourceLbl.textProperty();
	}
	
	public String getSource() {
		return source;
	}

	private void setup() {
		this.initView();
		this.foundView();
		this.setStyleClasses();
		this.setChildrenSize();
		this.setTooltip();
		this.getChildren().addAll(this.nameBox, this.stopBtn, this.stateLbl, this.filler, this.removeBtn);
		this.nameBox.getChildren().addAll(this.progressIndicator, this.sourceLbl);		
		
		HBox.setHgrow(this.nameBox, Priority.ALWAYS);
		HBox.setHgrow(this.stateLbl, Priority.ALWAYS);
		HBox.setHgrow(this.filler, Priority.SOMETIMES);
		
		this.stateLbl.setMinWidth(Region.USE_PREF_SIZE);
	}

	private void setTooltip() {
		this.name.setTooltip(new Tooltip());
		this.sourceLbl.setTooltip(new Tooltip(this.source));
		this.downBtn.setTooltip(new Tooltip("Download"));
		this.streamBtn.setTooltip(new Tooltip("Streaming"));
		this.addToListBtn.setTooltip(new Tooltip("Aggiungi alla lista"));
	}

	private void initView() {
		this.nameBox = new HBox(5);
		this.imgView = new ImageView(/*new Image(new File("images/loading.gif").toURI().toString())*/);
		this.sourceLbl = new Label(this.source);
		this.stateLbl = new Label("In coda");
		this.filler = new Region();
		this.removeBtn = new Button();
		this.stopBtn = new Button();
		this.progressIndicator = new ProgressIndicator();
		
		this.sourceLbl.setDisable(true);
		this.stateLbl.setDisable(true);
	}

	private void foundView() {
		this.name = new Label();
		this.downBtn = new Button();
		this.streamBtn = new Button();
		this.addToListBtn = new Button();
	}
	
	private void setStyleClasses() {
		this.nameBox.getStyleClass().add("name_box");
		this.progressIndicator.getStyleClass().add("progress_scan");
		this.imgView.getStyleClass().add("iconFoundWrapper");
		this.sourceLbl.getStyleClass().add("source_lbl");
		this.removeBtn.getStyleClass().add("remove_btn");
		this.stopBtn.getStyleClass().add("stop_btn");
		this.stateLbl.getStyleClass().add("state_lbl");
		this.filler.getStyleClass().add("_filler");
		this.name.getStyleClass().add("name_lbl");	
		this.downBtn.getStyleClass().add("down_btn");
		this.streamBtn.getStyleClass().add("stream_btn");
		this.addToListBtn.getStyleClass().add("add-to-list-main-btn");
	}

	private void setChildrenSize() {		
		this.imgView.minWidth(Region.USE_PREF_SIZE);
		this.imgView.setFitHeight(16);
		this.imgView.setFitWidth(15);
		this.removeBtn.setMinWidth(Region.USE_PREF_SIZE);
		this.stopBtn.setMinWidth(Region.USE_PREF_SIZE);
		this.progressIndicator.setMinWidth(Region.USE_PREF_SIZE);
		this.stateLbl.setMinWidth(Region.USE_PREF_SIZE);
		this.addToListBtn.setMinWidth(Region.USE_PREF_SIZE);

		this.stateLbl.setAlignment(Pos.CENTER_LEFT);
		this.nameBox.setAlignment(Pos.CENTER_LEFT);
		HBox.setHgrow(this.filler, Priority.ALWAYS);
		HBox.setHgrow(this.removeBtn, Priority.NEVER);
		HBox.setMargin(this.removeBtn, new Insets(0,0,0,0));
		HBox.setMargin(this.imgView, new Insets(1,0,0,0));
		HBox.setMargin(this.progressIndicator, new Insets(1,0,0,0));	
		HBox.setMargin(this.downBtn, new Insets(0,5,0,5));	
		HBox.setMargin(this.streamBtn, new Insets(0,5,0,5));
		HBox.setMargin(this.addToListBtn
				, new Insets(0,5,0,5));	
	}
	
	public Tooltip getNameTooltip() {
		return this.name.getTooltip();
	}

	public void foundLayout() {
		this.removeBtn.setDisable(false);
		this.imgView.setImage(new Image("file:" + new File("images/found2.png").getAbsolutePath()));
		this.getChildren().setAll(this.nameBox, this.downBtn, this.streamBtn, this.addToListBtn, this.filler, this.removeBtn);
		this.nameBox.getChildren().setAll(this.imgView, this.name);
		this.getStyleClass().add("found");
	}
	
	public void notFoundLayout() {
		this.removeBtn.setDisable(false);
		this.imgView.setImage(new Image("file:" + new File("images/not_found2.png").getAbsolutePath()));
		this.getChildren().removeAll(this.stopBtn, this.progressIndicator);
		this.nameBox.getChildren().set(0, this.imgView);
		this.getStyleClass().add("not_found");
	}
	
	public void setOnStopAction(EventHandler<ActionEvent> value) {
		this.stopBtn.setOnAction(value);
	}

	public void setOnRemoveItemAction(EventHandler<ActionEvent> value) {
		this.removeBtn.setOnAction(value);
	}	

	public void setOnDownloadAction(EventHandler<ActionEvent> value) {
		this.downBtn.setOnAction(value);
	}	
	
	public void setOnStreamingAction(EventHandler<ActionEvent> value) {
		this.streamBtn.setOnAction(value);
	}	

	public void setOnAddToListAction(EventHandler<ActionEvent> value) {
		this.addToListBtn.setOnAction(value);
	}	

	public void setResultList(ListView<FoundItem> resultList) {
		this.resultList = resultList;
		this.resultList.getItems().add(this);

		double maxWidth = Math.min(650.0, this.resultList.getBoundsInLocal().getWidth()*.55);
		this.nameBox.setMaxWidth(maxWidth);
		
		this.resultList.widthProperty().addListener((observable, oldValue, newValue)->{
			double _maxWidth = Math.min(650.0, newValue.doubleValue()*.55);
			this.nameBox.setMaxWidth(_maxWidth);
		});
		
		FoundItem foundItem = this;
		
		ChangeListener<Boolean> buttonListener = new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				if(newValue) resultList.getSelectionModel().select(foundItem);
			}	
		};
				
		this.stopBtn.focusedProperty().addListener(buttonListener);
		this.downBtn.focusedProperty().addListener(buttonListener);
		this.streamBtn.focusedProperty().addListener(buttonListener);
		this.addToListBtn.focusedProperty().addListener(buttonListener);
	}
		
	public ListView<FoundItem> getResultList(){
		return this.resultList;
	}

	public boolean isStopped() {
		return this.getStyleClass().contains("stopped");
	}
	
	public boolean isFound() {
		return this.getStyleClass().contains("found");
	}
	
	public boolean isNotFound() {
		return this.getStyleClass().contains("not_found") && !this.getStyleClass().contains("stopped");
	}
	
	public boolean inProgress() {
		return !this.getStyleClass().contains("not_found") && !this.getStyleClass().contains("stopped") && this.getStyleClass().contains("found");
	}

	public void setMedia(Media media) {
		this.media = media;;
	}
	
	public Media getMedia() {
		return this.media;
	}
}
