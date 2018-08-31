package com.zstreaming.gui.components;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class SettingButton extends Button{
	private StringProperty imageURL = new SimpleStringProperty();
	private ImageView graphic;
	
	public SettingButton(String text, String imageURL) {
		super(text);
		this.setContentDisplay(ContentDisplay.TOP);
		this.graphic = new ImageView();
		this.graphic.setPreserveRatio(true);
		
		this.imageURL.addListener((observable, oldValue, newValue)->{
			if(this.imageURL.get() != null) {
				if(this.getGraphic() == null) 
					this.setGraphic(this.graphic);
				
				this.graphic.setImage(new Image(this.imageURL.get()));
			}else {
				this.setGraphic(null);
			}
		});
		
		if(imageURL != null) {
			this.imageURL.set(imageURL);
		}
		
		this.getStyleClass().add("setting-button");
	}

	
	public SettingButton(String text) {
		this(text, null);
	}
	
	public SettingButton() {
		this(null);
	}
	
	public DoubleProperty imageWidthProperty() {
		return this.graphic.fitWidthProperty();
	}
	
	public DoubleProperty imageHeightProperty() {
		return this.graphic.fitHeightProperty();
	}
	
	public double getImageWidth() {
		return this.graphic.getFitWidth();
	}
	
	public double getImageHeight() {
		return this.graphic.getFitHeight();
	}
	
	public void setImageWidth(double imageWidth) {
		this.graphic.setFitWidth(imageWidth);
	}
	
	public void setImageHeight(double imageHeight) {
		this.graphic.setFitHeight(imageHeight);
	}
	
	public ObjectProperty<Image> imageProperty(){
		return this.graphic.imageProperty();
	}
	
	public Image getImage() {
		return this.graphic.getImage();
	}
	
	public void setImage(Image image) {
		this.graphic.setImage(image);
	}
	
	public StringProperty imageURLProperty() {
		return this.imageURL;
	}
	
	public void setImageURL(String url) {
		this.imageURL.set(url);
	}
	
	public String getImageURL() {
		return this.imageURL.get();
	}
	
}
