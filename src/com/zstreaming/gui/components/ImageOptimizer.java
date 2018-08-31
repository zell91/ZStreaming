package com.zstreaming.gui.components;

import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.zstreaming.launcher.ZStreaming;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class ImageOptimizer extends ImageView {
	
	private Image image;
	
	private File imageFile;
	private File source;
	
	private StringProperty imageURLProperty = new SimpleStringProperty();

	private BooleanProperty loadedProperty = new SimpleBooleanProperty();
		
	public final static Image LOADING_IMAGE = new Image("file:" + new File("images/loading.gif").getAbsolutePath(), 40, 40, true, true, true);
	
	public ImageOptimizer() {
		super();
	}
	
	public ImageOptimizer(File imageFile, double size) {
		this(imageFile, imageFile, size);
	}
	
	public ImageOptimizer(File source, File imageFile, double size){
		super();
		this.imageFile = imageFile;
		this.source = source;

		this.setPreserveRatio(true);
		this.load(imageFile, size, false);
	}
	
	public File getSource() {
		return this.source;
	}
	
	public void setSource(File source) {
		this.source = source;
	}

	public void load(File imageFile, double size, boolean backgroundLoading) {	
		this.loadedProperty().set(false);
		this.setFitWidth(40.0);
		this.setFitHeight(40.0);
		this.setImage(LOADING_IMAGE);
		
		this.image = new Image("file:" + imageFile, size, size,  true, true, backgroundLoading);

		if(backgroundLoading) {
			
			this.image.progressProperty().addListener((observable, oldValue, newValue)->{
				if(newValue.doubleValue() == 1.0) {
					this.setImage(this.image);
					this.loadedProperty().set(true);
					int res = Double.compare(this.image.getWidth(), this.image.getWidth());
					
					if(res > 0) {
						this.setFitWidth(size);
					}else if(res < 0) {
						this.setFitHeight(size);
					}else {
						this.setFitWidth(size);
						this.setFitHeight(size);
					}
					
					ZStreaming.gcClean(1000);
				}
			});
		}else {
			this.setImage(this.image);
			this.loadedProperty().set(true);
			
			int res = Double.compare(this.image.getWidth(), this.image.getWidth());
	
			if(res > 0) {
				this.setFitWidth(size);
			}else if(res < 0) {
				this.setFitHeight(size);
			}else {
				this.setFitWidth(size);
				this.setFitHeight(size);
			}
		}
	}
	
	public void store(File path) throws IOException {
		RenderedImage renderedImage = SwingFXUtils.fromFXImage(this.image, null);
		
		if(renderedImage == null) throw new IOException();

		ImageIO.write(renderedImage, path.getName().split("\\.")[path.getName().split("\\.").length - 1], path);
		this.refresh();			
	}
	
	public static void store(File source, File path) throws IOException{
		Image image = new Image("file:" + source, 100.0, 100.0, true, true, false);		

		RenderedImage renderedImage = SwingFXUtils.fromFXImage(image, null);
		if(renderedImage == null) throw new IOException();
		ImageIO.write(renderedImage, path.getName().split("\\.")[path.getName().split("\\.").length - 1], path);
	}
	
	public StringProperty imageURLProperty() {
		return this.imageURLProperty;
	}

	public File getImageFile() {
		return this.imageFile;
	}

	public void refresh() {
		this.load(this.source, 100.0, true);
	}

	public BooleanProperty loadedProperty() {
		return this.loadedProperty;
	}
	
}
