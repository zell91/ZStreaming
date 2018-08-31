package com.zstreaming.plugins.controller;

import java.io.File;

import com.zstreaming.browser.WebBrowser;
import com.zstreaming.media.Media;
import com.zstreaming.statistics.SessionStatistics;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Task;
import javafx.scene.image.Image;

public class URLScannerTask extends Task<Void>{
			
	public enum State {
		QUEUED, PROGRESSED, SUCCESSED, STOPPED, FAILED;
	}
	
	private volatile boolean started;
	
	private URLController urlController;
	
	private SimpleStringProperty sourceText;
	private SimpleStringProperty stateText;
	private SimpleStringProperty nameText;
	private SimpleObjectProperty<Image> image;	
	private SimpleObjectProperty<State> stateProperty;
		
	private Media media;
	private WebBrowser webBrowser;

	private BooleanProperty onDoneProperty;

	public URLScannerTask(String source, WebBrowser webBrowser) {
		this.webBrowser = webBrowser;
		this.urlController = new URLController(source, this.webBrowser);
		this.image = new SimpleObjectProperty<>();
		this.sourceText = new SimpleStringProperty(source);
		this.stateText = new SimpleStringProperty("queue");
		this.nameText = new SimpleStringProperty();
		this.stateProperty = new SimpleObjectProperty<>(State.QUEUED);
		this.onDoneProperty = new SimpleBooleanProperty(false);
	}
	
	public boolean isStarted() {
		return this.started;
	}
	
	public synchronized boolean wasNotFound() {
		return this._getState().equals(State.STOPPED) || this._getState().equals(State.FAILED);
	}

	public synchronized boolean isStopped() {
		return this._getState().equals(State.STOPPED);
	}

	public synchronized boolean isActive() {
		return this._getState().equals(State.PROGRESSED);
	}

	public synchronized boolean isFound() {
		return this._getState().equals(State.SUCCESSED);
	}
	
	public Media getMedia() {
		return media;
	}
	
	public WebBrowser getBrowser() {
		return webBrowser;
	}		
						
	public void interrupt() {
		this.setStateText("interrupting");
		if(this.cancel(true)) this.setState(State.STOPPED);
	}
	
	public ObjectProperty<State> _stateProperty() {
		return this.stateProperty;
	}
	
	public State _getState() {
		return this.stateProperty.get();
	}
	
	public void setState(URLScannerTask.State state) {
		Platform.runLater(()->this.stateProperty.set(state));
	}

	public ObjectProperty<Image> imageProperty() {
		return this.image;
	}
	
	public void setImage(String source) {
		Platform.runLater(()->this.image.set(new Image(new File(source).toURI().toString())));
	}
	
	public Image getImage() {
		return this.image.get();
	}
	
	public void setSource(String sourceText) {
		Platform.runLater(()->this.sourceText.set(sourceText));
	}

	public StringProperty sourceProperty() {
		return this.sourceText;
	}
	
	public String getSource() {
		return this.sourceText.get();
	}

	public void setStateText(String stateText) {
		Platform.runLater(()->this.stateText.set(stateText));
	}
	
	public StringProperty stateTextProperty() {
		return this.stateText;
	}
	
	public String getStateText() {
		return this.stateText.get();
	}
	
	public void setNameText(String nameText) {
		Platform.runLater(()->this.nameText.set(nameText));
	}	
	
	public StringProperty nameProperty() {
		return this.nameText;
	}
	
	public String getName() {
		return this.nameText.get();
	}
	
	public BooleanProperty onDoneProperty() {
		return this.onDoneProperty;
	}
	
	public boolean isDone() {
		return this.onDoneProperty.get();
	}
	
	@Override
	protected Void call() throws Exception {
		Thread.currentThread().setName("URL-Scanner TASK");
		
		try {
			this.started = true;
			this.setStateText("searching");
			SessionStatistics.setState("connecting");
			this.setState(State.PROGRESSED);	
			try {
				this.urlController.run();
				this.media = urlController.getMedia();
				if(this.media.getMRL() == null) {
					this.media = null;
					throw new NullPointerException();
				}
				this.setNameText(this.media.getName());
				this.setState(State.SUCCESSED);
			}catch(InterruptedException e) {
				this.setState(State.STOPPED);
			}
		}catch(NullPointerException ex) {
			this.setState(State.FAILED);
		}finally {
			Platform.runLater(()->this.onDoneProperty.set(true));
		}		
		
		return null;
	}
}
