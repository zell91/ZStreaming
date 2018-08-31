package com.zstreaming.gui.components;

import java.io.File;
import java.util.Locale;
import java.util.Optional;

import com.util.locale.ObservableResourceBundle;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.stage.StageStyle;

public class ExitAlert extends Alert {

	public enum Result {
		TRAY_ICON, EXIT, CANCEL;
	}
	
	private CheckBox checkBox;
	private ButtonType cancelBtn;
	private ButtonType exitBtn;
	private ButtonType trayBtn;
	
	public ExitAlert() {
		super(AlertType.CONFIRMATION, ObservableResourceBundle.getLocalizedString("confirm.exit.text"));
		this.setTitle(ObservableResourceBundle.getLocalizedString("confirm.exit.title"));
		this.setHeaderText(null);
		this.initStyle(StageStyle.TRANSPARENT);
		this.setGraphic(new ImageView(new Image("file:" + new File("images/_shutdown.png").getAbsolutePath())));
		
		this.cancelBtn = ButtonType.CANCEL;
		this.exitBtn = new ButtonType(ObservableResourceBundle.getLocalizedString("pause.exit"));
		this.trayBtn = new ButtonType(ObservableResourceBundle.getLocalizedString("hide.tray"));
		
		this.getButtonTypes().setAll(this.exitBtn, this.trayBtn, this.cancelBtn);
		this.setup();
		
		this.getDialogPane().setPrefHeight(150);
		this.getDialogPane().setPrefWidth(Locale.getDefault().equals(Locale.ITALY) ? 577.0 : 540.0);
		this.getDialogPane().getScene().setFill(null);
		this.getDialogPane().getStylesheets().add(this.getClass().getResource("../fxml/styles/exit_dialog.css").toExternalForm());
		
		this.getDialogPane().setOnMousePressed(e->this.moveWindow(e));
	}
	
	private void setup() {
		this.checkBox = new CheckBox(ObservableResourceBundle.getLocalizedString("remember.choice"));
		ButtonBar buttonBar = ((ButtonBar)this.getDialogPane().getChildren().get(this.getDialogPane().getChildren().size() - 1));
		buttonBar.getButtons().add(0, this.checkBox);
		
		this.checkBox.setMinWidth(Region.USE_PREF_SIZE);
		
		ButtonBar.setButtonData(this.checkBox, ButtonData.LEFT);
		ButtonBar.setButtonData(buttonBar.getButtons().get(buttonBar.getButtons().size() -1), ButtonData.SMALL_GAP);		
		ButtonBar.setButtonUniformSize(buttonBar.getButtons().get(buttonBar.getButtons().size() - 1), false);
		
		buttonBar.getStyleClass().add("exit-button-bar");
		this.checkBox.getStyleClass().add("choice-remember-check");
	}
	
	public boolean rememberChoice() {
		return this.checkBox.isSelected();
	}
	
	public Result showAndGetResult() {
		Optional<ButtonType> optional = this.showAndWait();
		
		if(optional.get().equals(this.cancelBtn))
			return Result.CANCEL;
		else if(optional.get().equals(this.exitBtn))
			return Result.EXIT;
		else if(optional.get().equals(this.trayBtn))
			return Result.TRAY_ICON;
		else
			return Result.CANCEL;
	}
	
	private void moveWindow(MouseEvent event) {
		if(event.getY() < 20.0) {
			this.getDialogPane().setOnMouseDragged(e->{
				if(event.getPickResult().getIntersectedNode() != null) {
					this.setX(e.getScreenX() - event.getSceneX());
					this.setY(e.getScreenY() - event.getSceneY());
				}
			});
		}else {
			this.getDialogPane().setOnMouseDragged(null);
		}
	}

}
