package com.zstreaming.gui.components;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import com.zstreaming.media.Media;
import com.zstreaming.media.MediaList;

import javafx.beans.property.StringProperty;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

public class MediaListFormBuilder extends FormListButton {
	
	public static final int NAME_MAX_LENGTH = 40;
	private HBox nameBox;
	private StackPane iconBox;
	private ToolBar buttonBox;
	
	private TextField nameText;
	private TextField iconText;
	private Button submitBtn, abortBtn, resetBtn, browseBtn;

	private MediaListButton preview;
	private MediaList mediaList;
	private VBox previewBox;
	private Media[] contentMediaList;
	private VBox previewWrapper;
	
	private Label nameLbl, iconLbl, previewLbl;
	
	public MediaListFormBuilder(String actionBtnText) {
		super();
		this.setup(actionBtnText);
		this.getChildren().addAll(this.nameBox, this.iconBox, this.previewBox, this.buttonBox);
		this.getStyleClass().add("media-list-builder-form");
	}
	
	private void setup(String actionBtnText) {
		this.setupPreview();
		this.setupButtonBox(actionBtnText);
		this.setupName();
		this.setupIcon();
		this.setupGrid();
	}
	
	public void setupGrid() {
		this.setGridLinesVisible(false);
		
		this.setHgap(50);
		this.setVgap(10);
		
		ColumnConstraints col0 = new ColumnConstraints();
		ColumnConstraints col1 = new ColumnConstraints();
		
		col0.setPercentWidth(60);
		col1.setPercentWidth(40);

		col0.setHgrow(Priority.ALWAYS);
		col1.setHgrow(Priority.ALWAYS);
	
		RowConstraints row0 = new RowConstraints();
		RowConstraints row1 = new RowConstraints();
		RowConstraints row2 = new RowConstraints();
		RowConstraints row3 = new RowConstraints();
		
		row0.setMinHeight(10);
		row1.setMinHeight(20);
		row2.setMinHeight(20);

		row0.setVgrow(Priority.NEVER);
		row1.setVgrow(Priority.NEVER);
		row2.setVgrow(Priority.NEVER);
		row3.setVgrow(Priority.ALWAYS);
		
		row0.setValignment(VPos.BOTTOM);
		row1.setValignment(VPos.BOTTOM);
		row2.setValignment(VPos.BOTTOM);
		row3.setValignment(VPos.BOTTOM);

		this.getColumnConstraints().addAll(col0, col1);
		this.getRowConstraints().addAll(row0, row1, row2, row3);

		
	}
	
	private void setupButtonBox(String actionBtnText) {
		this.buttonBox = new ToolBar();
		this.submitBtn = new Button(actionBtnText);
		this.abortBtn = new Button("Annulla");
		this.resetBtn = new Button("Reimposta");
		this.resetBtn.setOnAction(e->this.reset());
		
		this.buttonBox.getItems().addAll(this.resetBtn, this.submitBtn, this.abortBtn);
		
		GridPane.setRowIndex(this.buttonBox, 3);
		GridPane.setColumnIndex(this.buttonBox, 0);
		GridPane.setHalignment(this.buttonBox, HPos.RIGHT);
		
		this.buttonBox.getStyleClass().addAll("box-form", "toolbar-box-form");
		this.submitBtn.getStyleClass().addAll("media-form-btn", "submit-form-btn");
		this.abortBtn.getStyleClass().addAll("media-form-btn", "abort-form-btn");
		this.resetBtn.getStyleClass().addAll("media-form-btn", "reset-form-btn");

	}
	
	public void setMediaList(MediaList mediaList) {
		this.mediaList = mediaList;
		this.reset();
	}

	private void setupPreview() {
		this.previewBox = new VBox();
		this.previewWrapper = new VBox();
		MediaList mediaList = new MediaList();
		this.previewLbl = new Label("Anteprima:");
		this.mediaList = new MediaList();
		mediaList.setName(this.mediaList.getName());
		this.preview = MediaListButtonBuilder.build(mediaList);
		this.previewBox.setAlignment(Pos.CENTER_LEFT);
		this.previewBox.getChildren().addAll(this.previewLbl, previewWrapper);
		this.previewWrapper.getChildren().add(this.preview);
		this.previewWrapper.setAlignment(Pos.CENTER);

		GridPane.setHalignment(this.previewBox, HPos.CENTER);
		GridPane.setValignment(this.previewBox, VPos.CENTER);
		
		GridPane.setRowIndex(this.previewBox, 0);
		GridPane.setColumnIndex(this.previewBox, 1);
		GridPane.setRowSpan(this.previewBox, 4);
		
		this.previewWrapper.setPadding(new Insets(0));
		this.previewWrapper.setMinHeight(215.0);
		this.previewWrapper.setBackground(new Background(new BackgroundFill[] {
				new BackgroundFill(new Color(.588, .588, .588, .5), new CornerRadii(0), new Insets(0)),
				new BackgroundFill(new Color(1, 1, 1, .5), new CornerRadii(0), new Insets(.5))
		}));
		
		this.previewBox.getStyleClass().addAll("box-form", "preview-box-form");
		this.previewWrapper.getStyleClass().addAll("preview-wrapper");
		this.previewLbl.getStyleClass().add("label-form");
	}

	private void setupName() {
		this.nameBox = new HBox(10);
		this.nameLbl = new Label("Nome:");
		this.nameLbl.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
		this.nameText = new TextField(this.preview.getName());
		this.preview.nameProperty().bindBidirectional(this.nameText.textProperty());
		this.nameText.setOnKeyTyped(e->{
			if(this.nameText.getText().length() - this.nameText.getSelectedText().length() >= NAME_MAX_LENGTH) {
				e.consume();
			}
		});		
		this.nameBox.setAlignment(Pos.CENTER_RIGHT);
		this.nameBox.getChildren().addAll(this.nameLbl, this.nameText);

		HBox.setHgrow(this.nameText,Priority.ALWAYS);
		GridPane.setHgrow(this.nameBox, Priority.ALWAYS);
		GridPane.setHalignment(this.nameBox, HPos.RIGHT);
		GridPane.setRowIndex(this.nameBox, 1);
		GridPane.setColumnIndex(this.nameBox, 0);
		
		this.nameBox.getStyleClass().addAll("box-form", "name-box-form");
		this.nameLbl.getStyleClass().add("label-form");
		this.nameText.getStyleClass().addAll("box-form-text", "name-box-text");
	}
	
	private void setupIcon() {
		this.iconBox = new StackPane();
		HBox textFieldWrapper = new HBox(10);
		this.iconLbl = new Label("Icona:");
		this.iconLbl.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
		this.browseBtn = new Button();
		this.iconText = new TextField(this.preview.getImageIcon().getSource().getAbsolutePath());
		this.iconText.setEditable(false);
		this.iconText.textProperty().addListener((observable, oldValue, newValue)->{
			this.preview.setIcon(new File(newValue));
		});
		this.iconBox.setAlignment(Pos.CENTER_RIGHT);
		textFieldWrapper.setAlignment(Pos.CENTER_LEFT);
		this.iconBox.getChildren().addAll(textFieldWrapper, this.browseBtn);
		textFieldWrapper.getChildren().addAll(this.iconLbl, this.iconText);
		
		HBox.setHgrow(this.iconText,Priority.ALWAYS);
		GridPane.setHalignment(this.iconBox, HPos.RIGHT);
		GridPane.setRowIndex(this.iconBox,2);
		GridPane.setColumnIndex(this.iconBox, 0);
		
		this.iconBox.getStyleClass().addAll("box-form", "icon-box-form");
		this.iconLbl.getStyleClass().add("label-form");
		this.iconText.getStyleClass().addAll("box-form-text", "icon-box-text");
		this.browseBtn.getStyleClass().addAll("media-form-btn", "icon-browse-btn");
		this.browseBtn.focusedProperty().addListener((observable, oldValue, newValue)->this.iconText.pseudoClassStateChanged(PseudoClass.getPseudoClass("focused"), newValue));
		this.browseBtn.hoverProperty().addListener((observable, oldValue, newValue)->this.iconText.pseudoClassStateChanged(PseudoClass.getPseudoClass("hover"), newValue));
		this.browseBtn.pressedProperty().addListener((observable, oldValue, newValue)->this.iconText.pseudoClassStateChanged(PseudoClass.getPseudoClass("pressed"), newValue));
	}
	
	public void setOnIconChooser(EventHandler<ActionEvent> value) {
		this.browseBtn.setOnAction(value);
	}

	public void setOnAction(EventHandler<ActionEvent> value) {
		this.submitBtn.setOnAction(value);
	}
	
	public void setOnCancel(EventHandler<ActionEvent> value) {
		this.abortBtn.setOnAction(value);
	}
	
	public void reset() {
		this.preview.setName(this.mediaList.getName().trim());
		this.preview.getImageIcon().setSource(this.mediaList.getSourceIcon().getAbsoluteFile());
		this.preview.getImageIcon().refresh();
		this.iconText.setText(this.mediaList.getSourceIcon().getAbsolutePath());
	}

	public MediaList getResult() throws IOException {
		MediaList mediaList = new MediaList(this.getName(), new File(this.mediaList.getPath().getParentFile(), "icon/" + this.getImagePreview().getImageFile().getName()).getAbsolutePath());
		mediaList.setPath(this.mediaList.getPath());
		mediaList.setSourceIcon(new File(this.getIconText()));
		return mediaList;
	}

	public MediaList getEditedMediaList() {
		return this.preview.getMediaList();
	}

	public MediaList getMediaList() {
		return this.mediaList;
	}
	
	public StringProperty submitTextProperty() {
		return this.submitBtn.textProperty();
	}
	
	public StringProperty abortTextProperty() {
		return this.abortBtn.textProperty();
	}
	
	public StringProperty resetTextProperty() {
		return this.resetBtn.textProperty();
	}
		
	public StringProperty nameHeadTextProperty() {
		return this.nameLbl.textProperty();
	}
	
	public StringProperty iconHeadTextProperty() {
		return this.iconLbl.textProperty();
	}
	
	public StringProperty previewHeadTextProperty() {
		return this.previewLbl.textProperty();
	}

	public String getName() {
		return this.nameText.getText().trim();
	}
	
	public String getIconText() {
		return this.iconText.getText().trim();

	}
	
	public void setImageURL(String imageURL) {
		this.iconText.setText(imageURL);
	}

	public static boolean isValidName(String name, Collection<Node> listContainerItems, String... validNames) {
		final String regex = "(?:(?i)\\sNUL\\s|.*[\\\\/\\:\\*\"<>|].*)";

		if(name.matches(regex) || name.equalsIgnoreCase("NUL")) return false;
		
		for(String n : validNames) {
			if(n.equals(name)) return true;
		}
		
		return listContainerItems.stream().filter(item->item instanceof MediaListButton).noneMatch(item->((MediaListButton)item).getMediaList().getName().equals(name));
	}

	public ImageOptimizer getImagePreview() {
		return this.preview.getImageIcon();
	}	

	public void resetContentMedialist() {
		this.contentMediaList = null;		
	}

	public void setContentMedialist(Media... media) {
		this.contentMediaList = media;		
	}
	
	public Media[] getContentMediaList() {
		return this.contentMediaList;
	}

	public void setDefaultName(String defaultName) {
		this.preview.setName(defaultName);
	}

}
