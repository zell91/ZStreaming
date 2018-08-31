package com.zstreaming.gui.components;

import java.util.Optional;

import com.util.locale.ObservableResourceBundle;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.input.MouseEvent;
import javafx.stage.StageStyle;

public class DeleteAlert extends Alert {
	
	public enum Result{
		ACTION, CANCEL;
	}

	private ButtonType cancelBtn;
	private ButtonType actionBtn;
	
	public DeleteAlert(String param) {
		super(AlertType.CONFIRMATION, ObservableResourceBundle.getLocalizedString("confirm." + param + ".text"));
		this.setHeaderText(ObservableResourceBundle.getLocalizedString("confirm." + param + ".title"));
		this.initStyle(StageStyle.TRANSPARENT);
		this.setGraphic(null);
		
		this.cancelBtn = ButtonType.CANCEL;
		this.actionBtn = new ButtonType(ObservableResourceBundle.getLocalizedString(param));
		
		this.getButtonTypes().setAll(this.cancelBtn, this.actionBtn);		
		this.setup();
		
		this.getDialogPane().setOnMousePressed(e->this.actionWindow(e));
		this.getDialogPane().setOnMouseMoved(e->this.closeHoverStyle(e));
		this.getDialogPane().getStylesheets().add(this.getClass().getResource("../fxml/styles/delete_dialog.css").toExternalForm());
	}

	private void setup() {
		ButtonBar buttonBar = ((ButtonBar)this.getDialogPane().getChildren().get(this.getDialogPane().getChildren().size() - 1));
		ButtonBar.setButtonData(buttonBar.getButtons().get(0), ButtonData.CANCEL_CLOSE);
		ButtonBar.setButtonData(buttonBar.getButtons().get(1), ButtonData.APPLY);
		
		buttonBar.getButtons().get(0).getStyleClass().add("cancel-dialog-btn");
		buttonBar.getButtons().get(1).getStyleClass().add("action-dialog-btn");
	}

	public Result showAndGetResult() {
		Optional<ButtonType> optional = this.showAndWait();
		
		if(optional.get().equals(this.cancelBtn))
			return Result.CANCEL;
		else if(optional.get().equals(this.actionBtn))
			return Result.ACTION;
		else
			return Result.CANCEL;
	}
	
	public void actionWindow(MouseEvent event) {		
		if(event.getX() > this.getWidth() - 35.0 && event.getY() < 23.0) {
			this.getDialogPane().getStyleClass().add("pressed-close");
			this.getDialogPane().setOnMouseReleased(e->{
				this.getDialogPane().getStyleClass().removeAll("pressed-close");
				if(e.getX() > this.getWidth() - 35.0 && e.getY() < 23.0)
					this.close();
			});
		}
		
		if(event.getY() < 25.0 && event.getX() < this.getWidth() - 35.0) {
			this.moveWindow(event);
		}else {
			this.getDialogPane().setOnMouseDragged(null);
		}
	}
	
	private void moveWindow(MouseEvent event) {
		this.getDialogPane().setOnMouseDragged(e->{
			if(event.getPickResult().getIntersectedNode() != null) {
				this.setX(e.getScreenX() - event.getSceneX());
				this.setY(e.getScreenY() - event.getSceneY());
			}
		});
	}		

	private void closeHoverStyle(MouseEvent event) {
		if(event.getX() > this.getWidth() - 35.0 && event.getY() < 23.0) {
			this.getDialogPane().getStyleClass().add("hover-close");			
		}else {
			this.getDialogPane().getStyleClass().removeAll("hover-close");		}
	}

}
