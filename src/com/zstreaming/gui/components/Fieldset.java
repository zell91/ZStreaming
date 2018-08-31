package com.zstreaming.gui.components;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

public class Fieldset extends StackPane{
		
	private Pane pane;
	private GridPane content;
	
	private Label title;
	
	public Fieldset(String text) {
		this.getStyleClass().add("fieldset");
		this.title = new Label(text);
		this.title.setPadding(new Insets(0,5,0,5));
		this.title.getStyleClass().add("title-lbl");
		this.content = new GridPane();
		this.content.setHgap(5.0);
		this.pane = new Pane();
		this.pane.getStyleClass().add("fieldset-container");
		this.content.getStyleClass().add("fieldset-content");
		this.getChildren().addAll(this.pane, this.title, this.content);
		this.setAlignment(Pos.TOP_LEFT);
		this.pane.setBorder(new Border(new BorderStroke[] {new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, null, BorderWidths.DEFAULT, new Insets(1))}));
		this.title.setGraphicTextGap(0);
		this.title.boundsInLocalProperty().addListener((observable, oldValue, newValue)->{
			StackPane.setMargin(this.pane, new Insets(this.title.getHeight()*.4, 0, 0, 0));
			StackPane.setMargin(this.content, new Insets(this.title.getHeight(), 0, this.title.getHeight()*.5, 10));
			StackPane.setMargin(this.title, new Insets(0, 0, 0, 5));

			this.pane.setMinHeight(this.content.getHeight());
			this.pane.setMinWidth(this.title.getWidth() + 20);
			
			this.layout();
		});
		
		this.title.setBackground(new Background(new BackgroundFill[] {new BackgroundFill(new Color(.96, .96, .96, 1), null, null)}));
		this.setMinHeight(Region.USE_COMPUTED_SIZE);
	}
	
	public Fieldset() {
		this(null);
	}
	
	public DoubleProperty hgapProperty() {
		return this.content.hgapProperty();
	}
	
	public double getHgap() {
		return this.content.getHgap();
	}
	
	public void setHgap(double hgap) {
		this.content.setHgap(hgap);
	}
	
	public DoubleProperty vgapProperty() {
		return this.content.vgapProperty();
	}
	
	public void setVgap(double vgap) {
		this.content.setVgap(vgap);
	}
	
	public double getVgap() {
		return this.content.getVgap();
	}
	
	public ObservableList<Node> getContent(){
		return this.content.getChildren();
	}
	
	public ObservableList<ColumnConstraints> getColumnConstraints(){
		return this.content.getColumnConstraints();
	}
	
	public ObservableList<RowConstraints> getRowConstraints(){
		return this.content.getRowConstraints();
	}
	
	public StringProperty titleProperty() {
		return this.title.textProperty();
	}
	
	public void setTitle(String text) {
		this.title.setText(text);
	}
	
	public String getTitle() {
		return this.title.getText();
	}

}
