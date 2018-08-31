package com.zstreaming.gui.components.contextmenu;

import javafx.collections.ObservableList;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.ToggleGroup;

public class ScaleChoicerContextMenu extends ContextMenu{
	
	private ComboBox<String> comboBox;
	private ToggleGroup group;
	
	private LineChart<Number, Number> chart;
	
	public ScaleChoicerContextMenu(LineChart<Number, Number> chart, ComboBox<String> comboBox) {
		super();
		this.chart = chart;
		this.comboBox = comboBox;
		this.group = new ToggleGroup();
		this.chart.setOnMouseClicked(e->this.hide());
		this.setup();
	}

	private void setup() {		
		ObservableList<String> choices = this.comboBox.getItems();
		
		for(String choice : choices) {
			RadioMenuItem menuItem = new RadioMenuItem();
			this.group.getToggles().add(menuItem);
			menuItem.getStyleClass().add("chart-menu-item");
			menuItem.setText(choice);
			if(choice.equals(this.comboBox.getValue())) menuItem.setSelected(true);
			menuItem.selectedProperty().addListener((observable, oldValue, newValue)->{
				if(newValue) {
					if(this.comboBox.getScene() != null)
						this.comboBox.getSelectionModel().select(choice);
					else 
						this.changeLowerBounds(choice);					
				}
			});			
			this.getItems().add(menuItem);
		}		
	}

	private void changeLowerBounds(String choice) {
		double lowerBound;
		
		int value = this.group.getToggles().indexOf(this.group.getSelectedToggle());

		switch(value) {
			case 0:
				lowerBound = -120;
				break;
			case 1:
				lowerBound = -360;
				break;
			case 2:
				lowerBound = -720;
				break;
			case 3:
				lowerBound = -2160;
				break;
			case 4:
				lowerBound = -7200;
				break;
			case 5:
				lowerBound = -21600;
				break;
			case 6:
				lowerBound = -43200;
				break;
			case 7:
				lowerBound = -86400;
				break;
			default:
				return;
		}
		
		((NumberAxis)this.chart.getXAxis()).setLowerBound(lowerBound);
	}

}
