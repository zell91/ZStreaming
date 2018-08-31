package com.zstreaming.gui.components;

import com.util.locale.ObservableResourceBundle;
import com.zstreaming.history.HistoryEntry;

import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class RescanListView extends StageListView<String>{

	private HistoryEntry historyEntry;
	
	public RescanListView(HistoryEntry historyEntry) {
		super();
		this.setVgap(10);
		this.historyEntry = historyEntry;
		this.headerLbl.setText(ObservableResourceBundle.getLocalizedString("pick.scan.url"));
		this.actionBtn.setText(ObservableResourceBundle.getLocalizedString("url.scan.button"));
		
		this.getChildren().addAll(this.closeBtn, this.headerLbl, this.listView, this.actionBtn);
		
		GridPane.setConstraints(this.headerLbl, 0, 0, 1, 1, HPos.LEFT, VPos.BOTTOM, Priority.ALWAYS, Priority.NEVER);
		GridPane.setConstraints(this.listView, 0, 1, 2, 1, HPos.CENTER, VPos.CENTER, Priority.ALWAYS, Priority.ALWAYS);
		GridPane.setConstraints(this.actionBtn, 0, 2, 2, 1, HPos.RIGHT, VPos.CENTER, Priority.ALWAYS, Priority.NEVER);

		GridPane.setHalignment(this.headerLbl, HPos.LEFT);
		GridPane.setHalignment(this.actionBtn, HPos.RIGHT);

		VBox.setVgrow(this.listView, Priority.ALWAYS);

		this.listView.setFixedCellSize(25);

		this.headerLbl.getStyleClass().add("rescan-list-view-header");
		this.actionBtn.getStyleClass().addAll("rescan-list-view-action", "rescan-list-view-btn");
		this.closeBtn.getStyleClass().addAll("rescan-list-view-close", "rescan-list-view-btn");
		this.listView.getStyleClass().add("rescan-list-view");
		this.getStyleClass().add("rescan-list-view-container");
		this.getStylesheets().add(this.getClass().getResource("../fxml/styles/rescan_list_view.css").toExternalForm());
		
		this.fillListView();
	}
	
	@Override
	protected void fillListView() {
		String searchURL = this.historyEntry.getSource();
		
		this.listView.getItems().add(searchURL);
		
		if(this.historyEntry.getMedia().isAvalaible() && !this.historyEntry.getMedia().getSource().toExternalForm().equals(searchURL)) {
			this.listView.getItems().add(this.historyEntry.getMedia().getSource().toExternalForm());
		}
	}
	
}
