package com.zstreaming.gui.components;

import java.io.File;

import com.zstreaming.media.MediaList;

import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;

public class MediaListButton extends Button {

	private Label nameLbl;
	private Label sizeLbl;
	private ImageOptimizer icon;
	private CheckBox checkBtn;
	
	private StackPane graphic;
	private GridPane wrapper;
		
	private MediaList mediaList;

	private CheckSelectionGroup checkGroup;
	
	private ChangeListener<Boolean> selectedListener;
	
	public MediaListButton(MediaList mediaList) {
		super();
		this.graphic = new StackPane();
		this.wrapper = new GridPane();
		this.mediaList = mediaList;
		this.nameLbl = new Label(mediaList.getName());
		this.sizeLbl = new Label(String.format("%d elementi", mediaList.getContent().size()));
		this.icon = mediaList.getSourceIcon() != null ? new ImageOptimizer(mediaList.getSourceIcon(), new File(mediaList.getIconPath()), 100.0) : new ImageOptimizer(new File(mediaList.getIconPath()), 100.0);
		this.checkBtn = new CheckBox();
		this.checkBtn.setVisible(false);
		this.icon.setPreserveRatio(true);		
		this.icon.setCache(true);
				
		this.wrapper.getRowConstraints().add(new RowConstraints());
		this.wrapper.getColumnConstraints().add(new ColumnConstraints());
		this.icon.imageProperty().addListener((observable, oldValue, newValue)->{
			this.wrapper.getRowConstraints().get(0).setMinHeight(Math.min(newValue.getHeight() + 30.0, 100.0));
		});
		this.selectedListener = (observable, oldValue, newValue)->{
			this.checkGroup.setSelectedGroupSize(this.checkGroup.getSelectedGroup().size());
		};
		this.wrapper.getColumnConstraints().get(0).setMinWidth(100);

		this.wrapper.setAlignment(Pos.CENTER);
		GridPane.setHalignment(this.icon, HPos.CENTER);
		GridPane.setValignment(this.icon, VPos.CENTER);

		this.setGraphic(this.graphic);
		this.graphic.getChildren().addAll(this.wrapper, this.checkBtn);
		this.wrapper.getChildren().addAll(this.icon, this.sizeLbl, this.nameLbl);
		this.checkBtn.getStyleClass().add("check-selection-btn");
		this.nameLbl.getStyleClass().add("name-media-list");
		this.sizeLbl.getStyleClass().add("size-media-list");
		this.icon.getStyleClass().add("icon-media-list");
		this.graphic.getStyleClass().add("media-list-graphic");
		this.wrapper.getStyleClass().add("media-list-wrapper");
		this.getStyleClass().add("media-list-btn");
				
		StackPane.setAlignment(this.wrapper, Pos.CENTER);
		StackPane.setAlignment(this.checkBtn, Pos.BOTTOM_RIGHT);
		
		GridPane.setRowIndex(this.icon, 0);
		GridPane.setRowIndex(this.sizeLbl, 1);
		GridPane.setRowIndex(this.nameLbl, 2);
	}
		
	public MediaList getMediaList() {
		return this.mediaList;
	}	

	public void setCheckSelectionGroup(CheckSelectionGroup checkGroup) {
		this.checkGroup = checkGroup;
		this.checkGroup.getGroup().add(this.checkBtn);
		this.checkBtn.visibleProperty().bind(this.checkGroup.selectionModeProperty());
		this.checkBtn.selectedProperty().addListener(this.selectedListener);
	}
	

	public void removeCheckGroup() {
		this.checkBtn.visibleProperty().unbind();
		this.checkGroup.getGroup().remove(this.checkBtn);
		this.checkBtn.selectedProperty().removeListener(this.selectedListener);
	}
	
	public CheckSelectionGroup getCheckSelectionGroup() {
		return this.checkGroup;
	}
	
	public StringProperty nameProperty() {
		return this.nameLbl.textProperty();
	}
	
	public void setName(String name) {
		this.nameLbl.textProperty().set(name);
	}
	
	public String getName() {
		return this.nameLbl.getText();
	}
	
	public StringProperty sizeProperty() {
		return this.sizeLbl.textProperty();
	}

	public int getSize() {
		return Integer.parseInt(this.sizeLbl.getText().split(" ")[0]);
	}
	
	public StringProperty iconProperty() {
		return this.icon.imageURLProperty();
	}
	
	public File getIcon() {
		return this.icon.getImageFile();
	}
	
	public void setIcon(File path) {
		this.icon.load(path, 100.0, true);
	}
	
	public void setSelected(boolean selected) {
		this.checkBtn.setSelected(selected);	
	}

	public boolean isSelected() {
		return this.checkBtn.isSelected();
	}
	
	public CheckBox getCheckBox() {
		return this.checkBtn;
	}

	public ImageOptimizer getImageIcon() {
		return this.icon;
	}

	public Label getNameLabel() {
		return this.nameLbl;
	}
	
	public Label getSizeLabel() {
		return this.sizeLbl;
	}
}
