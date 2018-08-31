package com.zstreaming.gui.components;

import java.io.File;

import javafx.beans.property.StringProperty;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class SelectionForm extends FormListButton {
	
	private Button selectBtn, deselectBtn;
	private ImageView selectGraphic, deselectGraphic; 
	private CheckSelectionGroup checkGroup;
	
	public SelectionForm(CheckSelectionGroup checkGroup) {
		super();
		this.checkGroup = checkGroup;
		this.selectBtn = new Button("Seleziona tutto");
		this.selectGraphic = new ImageView(new Image(new File("images/select_all.png").toURI().toString()));
		this.selectGraphic.setFitWidth(15);
		this.selectGraphic.setFitHeight(15);
		this.selectBtn.setGraphic(this.selectGraphic);		
		this.deselectBtn = new Button("Deseleziona tutto");
		this.deselectGraphic = new ImageView(new Image(new File("images/deselect_all.png").toURI().toString()));
		this.deselectGraphic.setFitWidth(15);
		this.deselectGraphic.setFitHeight(15);		
		this.deselectBtn.setGraphic(this.deselectGraphic);
		this.deselectBtn.setDisable(true);
		this.setOnSelectAction(this.selectBtn, true);
		this.setOnSelectAction(this.deselectBtn, false);

		this.checkGroup.selectedGroupSizeProperty().addListener((observable, oldValue, newValue)->{
			this.selectBtn.setDisable(newValue.equals(this.checkGroup.getGroup().size()));
			this.deselectBtn.setDisable(newValue.doubleValue() == 0);
		});
		
		this.setHgap(10);
		this.add(this.selectBtn, 0, 0);
		this.add(this.deselectBtn, 1, 0);
		
		this.getStyleClass().add("selection-form");
		this.selectBtn.getStyleClass().addAll("select-form-btn", "select-all-btn");
		this.deselectBtn.getStyleClass().addAll("select-form-btn", "deselect-all-btn");
	}
	
	public StringProperty selectionBtnTextProperty() {
		return this.selectBtn.textProperty();
	}
	
	public StringProperty deselectionBtnTextProperty() {
		return this.deselectBtn.textProperty();
	}
	
	public void setOnSelectAction(Button btn, boolean select) {
		btn.setOnAction(event->{
			if(this.checkGroup.getSelectionMode()) {
				this.checkGroup.getGroup().forEach(checkBox->checkBox.setSelected(select));
			}
		});
	}
	
}
