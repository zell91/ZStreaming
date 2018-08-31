package com.zstreaming.gui.components;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.control.CheckBox;

public class CheckSelectionGroup {
	
	private List<CheckBox> checkGroup;
	private BooleanProperty selectionMode;
	private SimpleIntegerProperty selectedGroupSize;
	
	public CheckSelectionGroup() {
		this.checkGroup = new ArrayList<>();
		this.selectionMode = new SimpleBooleanProperty();
		this.selectedGroupSize = new SimpleIntegerProperty();
		this.addListener();
	}
	
	private void addListener() {
		this.selectionMode.addListener((observable, oldValue, newValue)->{
			if(!newValue) {			
				this.checkGroup.forEach(check->check.setSelected(false));
			}
		});
	}

	public List<CheckBox> getGroup(){
		return this.checkGroup;
	}
	
	public BooleanProperty selectionModeProperty() {
		return this.selectionMode;
	}
	
	public void setSelectionMode(boolean selected) {
		this.selectionMode.set(selected);
	}
	
	public boolean getSelectionMode() {
		return this.selectionMode.get();
	}
	
	public List<CheckBox> getSelectedGroup(){
		return this.checkGroup.stream().filter(check->check.isSelected()).collect(Collectors.toList());		
	}
	
	public int getSelectedGroupSize() {
		return this.selectedGroupSize.get();
	}

	public IntegerProperty selectedGroupSizeProperty() {
		return this.selectedGroupSize;
	}

	public void setSelectedGroupSize(int size) {
		this.selectedGroupSize.set(size);
		if(this.getSelectedGroup().size() != size) throw new IllegalArgumentException("Parameter: " + size + " SelectionGroupSize : " + this.getSelectedGroup().size());
	}
}
