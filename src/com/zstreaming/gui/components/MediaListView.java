package com.zstreaming.gui.components;

import java.util.List;

import com.util.locale.ObservableResourceBundle;
import com.zstreaming.media.MediaList;

import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class MediaListView extends StageListView<GridPane>{
	
	private Button addListBtn;
	private List<MediaListButton> mediaListButtons;
		
	public MediaListView(List<MediaListButton> mediaListButtons) {
		super();
		this.setVgap(15);
		this.mediaListButtons = mediaListButtons;
		this.headerLbl.setText(ObservableResourceBundle.getLocalizedString("pick.list"));
		this.addListBtn = new Button(ObservableResourceBundle.getLocalizedString("add.to.new.list"));
		this.actionBtn.setText(ObservableResourceBundle.getLocalizedString("add"));		

		this.addListBtn.disableProperty().bind(Bindings.createBooleanBinding(()->this.listView.getSelectionModel().getSelectedItems().isEmpty(), this.listView.getSelectionModel().selectedItemProperty()));

		this.getChildren().addAll(this.closeBtn, this.headerLbl, this.listView, this.addListBtn, this.actionBtn);

		GridPane.setConstraints(this.headerLbl, 0, 0, 1, 1, HPos.LEFT, VPos.BOTTOM, Priority.ALWAYS, Priority.NEVER);
		GridPane.setConstraints(this.addListBtn, 0, 1, 2, 1, HPos.CENTER, VPos.CENTER, Priority.ALWAYS, Priority.ALWAYS);
		GridPane.setConstraints(this.listView, 0, 2, 2, 1, HPos.CENTER, VPos.CENTER, Priority.ALWAYS, Priority.ALWAYS);
		GridPane.setConstraints(this.actionBtn, 0, 3, 2, 1, HPos.RIGHT, VPos.CENTER, Priority.ALWAYS, Priority.NEVER);

		GridPane.setHalignment(this.headerLbl, HPos.LEFT);
		GridPane.setHalignment(this.addListBtn, HPos.CENTER);
		GridPane.setHalignment(this.actionBtn, HPos.RIGHT);

		VBox.setVgrow(this.listView, Priority.ALWAYS);
		
		this.listView.setFixedCellSize(74);

		this.headerLbl.getStyleClass().add("media-list-view-header");
		this.addListBtn.getStyleClass().addAll("media-list-view-new", "media-list-view-btn");
		this.actionBtn.getStyleClass().addAll("media-list-view-action", "media-list-view-btn");
		this.closeBtn.getStyleClass().addAll("media-list-view-close", "media-list-view-btn");
		this.listView.getStyleClass().add("media-list-view");
		this.getStyleClass().add("media-list-view-container");
		this.getStylesheets().add(this.getClass().getResource("../fxml/styles/media_list_view.css").toExternalForm());

		this.fillListView();	
	}
	
	public MediaList getSelectedMediaList() {
		MediaList mediaList = null;
				
		if(this.listView.getSelectionModel().getSelectedItem() != null) {
			mediaList = (MediaList) this.listView.getSelectionModel().getSelectedItem().getUserData();
		}
		
		return mediaList;
	}
	
	@Override
	protected void fillListView() {
		for(MediaListButton mediaListBtn : this.mediaListButtons) {
			this.listView.getItems().add(createItem(mediaListBtn));
		}	
	}

	private GridPane createItem(Object object) {
		MediaListButton mediaListBtn = (MediaListButton)object;
		GridPane grid = new GridPane();
		grid.setUserData(mediaListBtn.getMediaList());
		grid.setHgap(10);
		
		ImageView imageWrapper = new ImageView(mediaListBtn.getImageIcon().getImage());
		imageWrapper.setFitHeight(65);
		imageWrapper.setFitWidth(65);
		imageWrapper.setPreserveRatio(true);
		
		Label nameLbl = new Label(mediaListBtn.getName());
		Label sizeLbl = new Label(mediaListBtn.getSizeLabel().getText());
		
		grid.setAlignment(Pos.CENTER_LEFT);
		grid.add(imageWrapper, 0, 0, 1, 3);
		grid.add(nameLbl, 1, 0);
		grid.add(sizeLbl, 1, 1);
		
		GridPane.setValignment(imageWrapper, VPos.CENTER);
		
		nameLbl.getStyleClass().add("name-cell-lbl");
		sizeLbl.getStyleClass().add("size-cell-lbl");
		
		return grid;
	}

	public void setNewListAction(EventHandler<ActionEvent> value) {
		this.addListBtn.setOnAction(value);
	}
}
