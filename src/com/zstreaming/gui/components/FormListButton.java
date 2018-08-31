package com.zstreaming.gui.components;

import javafx.geometry.Insets;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

public abstract class FormListButton extends GridPane{

	public FormListButton() {
		super();
		this.setLayout();
		this.getStyleClass().add("form-list-btn");
	}
	
	private void setLayout() {
		this.setPadding(new Insets(5, 10, 8, 15));
		this.setBackground(new Background(new BackgroundFill[] {
				new BackgroundFill(new Color(.313, .313, .313, 1), new CornerRadii(0), new Insets(0)),
				new BackgroundFill(Color.WHITESMOKE, new CornerRadii(0), new Insets(1)),
		}));
	}

	public void hide() {
		((Pane)this.getParent()).getChildren().remove(this);
	}
}
