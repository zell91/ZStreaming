package com.zstreaming.gui.components.contextmenu;

import com.zstreaming.gui.download.DownloadValues;

import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class TableHeaderContextMenu extends ContextMenu{
	
	private TableView<DownloadValues> downloadList;
		
	public TableHeaderContextMenu(TableView<DownloadValues> downloadList) {
		super();
		this.downloadList = downloadList;
		this.getStyleClass().add("table-header-context-menu");
		setup();
	}
	
	private void setup() {
		for(TableColumn<DownloadValues, ?> column : this.downloadList.getColumns()) {
			CheckMenuItem menuItem = new CheckMenuItem();
			menuItem.getStyleClass().add("column-menu-item");
			menuItem.setText(column.getUserData().toString());
			menuItem.setOnAction(e->menuItem.setSelected(menuItem.isSelected()));
			menuItem.setSelected(true);
			menuItem.selectedProperty().addListener((observable, oldValue, newValue)->column.setVisible(newValue));			
			this.getItems().add(menuItem);
		}
	}

}
