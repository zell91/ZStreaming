package com.zstreaming.browser.view;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import com.zstreaming.browser.WebBrowser;
import com.zstreaming.browser.http.HttpRequest;
import com.zstreaming.browser.http.HttpResponse;

import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker.State;
import javafx.scene.Scene;
import javafx.scene.web.PopupFeatures;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import netscape.javascript.JSException;

public class ViewBrowser {
	
	private volatile SimpleBooleanProperty onSession;
	
	private WebView webView;
	private WebEngine webEngine;
	private HttpResponse response;
	private HttpRequest request;
	
	private String userAgent;
	
	private Stage stage;
	private boolean isFailed;
	private URL url;

	protected ViewBrowser() {
		this.onSession = new SimpleBooleanProperty();
		this.webView = new WebView();
		this.webEngine = webView.getEngine();
		this.setUserAgent(WebBrowser.DEFAULT_USER_AGENT);
		this.webEngine.setJavaScriptEnabled(true);
		this.setPopup();
		this.addListener();
	}
	
	public void setRequest(HttpRequest request) {
		this.request = request;
		this.userAgent = request.getUserAgent();
	}
	
	public WebEngine getWebEngine() {
		return webEngine;
	}
	
	public URL getURL() {
		return url;
	}
	
	public synchronized boolean isFailed() {
		return isFailed;
	}
	
	private synchronized void setFailed(boolean failed) {
		this.isFailed = failed;
	}	
	
	private void setPopup() {		
		this.webEngine.setCreatePopupHandler(new Callback<PopupFeatures, WebEngine>() {			
		    @Override
		    public WebEngine call(PopupFeatures p) {		    			    	
		    	return null;
		    }
		});
		
		this.webEngine.createPopupHandlerProperty().addListener((obsaervable, oldValue, newValue)->{
			System.out.println("Popup bloccato");
		});
	}
	
	private void addListener() {		
		this.onSession.addListener(new ChangeListener<Boolean>() {
			@Override
			public synchronized void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				if(!newValue) {
					synchronized(onSession) {
						onSession.notify();
					}
				}
			}				
		});
	
		this.webEngine.getLoadWorker().stateProperty().addListener(new ChangeListener<State>() {

			@Override
			public void changed(ObservableValue<? extends State> observable, State oldValue, State newValue) {
				switch(newValue) {
					case SUCCEEDED:
						WebBrowser.loggerManager.info("Connessione effettuata.");
						WebBrowser.history.add(response);
						break;
					case FAILED:
						WebBrowser.loggerManager.error("Connessione fallita.", null);
						reset();
						break;
					default:
						break;
				}
			}
			
		});
		
		this.webEngine.locationProperty().addListener(new ChangeListener<String>() {

			@Override
			public void changed(ObservableValue<? extends String> abservable, String oldValue, String newValue) {
				if(response == null) return;
				if(oldValue != null && newValue != null && !oldValue.equals(newValue)) {
					try {
						url = new URL(newValue);
						response.setCookies(WebBrowser.cookieManager.load(url));
						response.setURL(url);
						response.setContent(readContent());
					} catch (MalformedURLException e1) { }
				}				
			}			
		});
		
	}

	public static ViewBrowser createBrowser() {
		WebBrowser.loggerManager.info("Inizializzazione WebView.");
		
		if(Platform.isFxApplicationThread()) {
			return new ViewBrowser();
		}else {
			final AtomicReference<ViewBrowser> atomicObj = new AtomicReference<>();
			Platform.runLater(()->{
				atomicObj.set(new ViewBrowser());
				synchronized(atomicObj) { atomicObj.notify(); }
			});
			
			synchronized(atomicObj) {
				try {
					atomicObj.wait(10000);
				}catch(InterruptedException e) {
					WebBrowser.loggerManager.error("Errore imprevisto. Operazione di inizializzazione interrotta", e);
					return null;
				}
			}
			
			return atomicObj.get();
		}
	}	
	
	private void setUserAgent(String userAgent) {
		if(Platform.isFxApplicationThread())
			this.webEngine.setUserAgent(userAgent);
		else {
			Platform.runLater(()->webEngine.setUserAgent(userAgent));
			return;
		}
		this.userAgent = userAgent;
	}
	
	public String getUserAgent() {
		return this.userAgent;
	}

	public HttpResponse readResponse() throws MalformedURLException{		
		if(this.isFailed) return null;		
		if(this.response == null) {
			this.response = new HttpResponse(System.currentTimeMillis());
			this.response.setViewBrowser(true);
			this.response.setURL(new URL(webEngine.getLocation()));
			this.response.setCookies(WebBrowser.cookieManager.load(this.response.getURL()));
			this.response.setContent(this.readContent());
		}
		return this.response;
	}
	
	private void nioload(String location) {
		
		if(location !=null) this.setOnSession(true);
		
		if(Objects.nonNull(location)) {
			WebBrowser.loggerManager.info("Avvio richiesta all'indirizzo: \"" + location +"\".");
		}
		
		if(Platform.isFxApplicationThread())
			this.webEngine.load(location);
		else
			Platform.runLater(()->this.nioload(location));
	}

	private void load(String location) {
		this.nioload(location);
		if(!Platform.isFxApplicationThread()) this.waitReady(Thread.currentThread());
	}
	
	
	public void load() {
		this.load(this.request.getUrl().toExternalForm());
	}
	
	public void nioload() {
		this.nioload(this.request.getUrl().toExternalForm());
	}	
	
	public void nioReload() {
		this.nioload(this.webEngine.getLocation());
	}

	public void reload() {
		this.load(this.webEngine.getLocation());			
	}
	
	public void waitReady(Thread thread) {
		waitReady(thread, State.SUCCEEDED);		
	}
	
	private void waitReady(Thread thread, javafx.concurrent.Worker.State state){
		Platform.runLater(()->{
			if(state.equals(this.webEngine.getLoadWorker().getState())) return;
			
			ChangeListener<State> listener = new ChangeListener<State>(){
				
				@Override
				public void changed(ObservableValue<? extends State> observable, State oldValue, State newValue) {
					setFailed(State.FAILED.equals(newValue));
					if(newValue.equals(state) || newValue.equals(State.FAILED)){
						synchronized(thread){
							thread.notify();
							webEngine.getLoadWorker().stateProperty().removeListener(this);
						}
					}
				}
			};		
			
			this.webEngine.getLoadWorker().stateProperty().addListener(listener);				
		});
		
		synchronized(thread){
			try {
				thread.wait();
			} catch (InterruptedException e) {
				this.reset();
			}
		}	
	}
	
	public void close(){
		if(Platform.isFxApplicationThread()) {
			this.reset();
			if(this.stage != null) this.stage.close();
		}else {
			Platform.runLater(()->close());
		}
	}
	
	public void reset(){
		this.nioload(null);
		this.setOnSession(false);
	}
	
	public void show(){
		if(Platform.isFxApplicationThread()) {
			Scene scene = new Scene(this.webView);
			this.stage = new Stage();
			this.stage.initModality(Modality.WINDOW_MODAL);
			this.stage.setScene(scene);			
			this.stage.sizeToScene();			
			this.stage.show();
		}else {
			Platform.runLater(()->show());
		}
	}
	
	public StringBuilder readContent() {		
		final String script = "document.getElementsByTagName(\"html\")[0].innerHTML";
		StringBuilder content = new StringBuilder();
		
		content.append(this.returnScript(script).toString());

		return content;		
	}

	public Object returnScript(String script){
		if(!this.getOnSession()) return null;
		
		if(Platform.isFxApplicationThread()) {
			try {
				return this.webEngine.executeScript(script);
			}catch(JSException e) {
				return null;
			}
		}else {
			final AtomicReference<Object> atomicObj = new AtomicReference<>();
			
			Platform.runLater(()->{
				atomicObj.set(this.returnScript(script));
				synchronized(atomicObj) { atomicObj.notify(); }
			});
			
			synchronized(atomicObj) {
				try {
					atomicObj.wait(30000);
				}catch(InterruptedException e) { }
			}
			
			return atomicObj.get();
		}		
	}

	public void executeScript(String script) {
		if(Platform.isFxApplicationThread())
			this.webEngine.executeScript(script);
		else
			Platform.runLater(()->executeScript(script));
	}
	
	public String getTextElementById(String id) {
		if(!this.getOnSession()) return null;
		
		if(Platform.isFxApplicationThread()) {
			try {
				return this.webEngine.executeScript("document.getElelemntById(\"id\").innerHTML").toString();
			}catch(JSException e) {
				try {
					return this.webEngine.getDocument().getElementById(id).getTextContent();
				}catch(NullPointerException ex) {
					this.reload();
					try {
						return this.webEngine.executeScript("document.getElelemntById(\"id\").innerHTML").toString();
					}catch(JSException ex1) {
						try {
							return this.webEngine.getDocument().getElementById(id).getTextContent();
						}catch(NullPointerException ex2) {
							return null;							
						}
					}
				}
			}
		}else {
			final StringBuilder result = new StringBuilder();
			
			Platform.runLater(()->{
				result.append(this.getTextElementById(id));
				synchronized(result) { result.notify(); }
			});
			
			synchronized(result) {
				try {
					result.wait(60000);
				}catch(InterruptedException e) { }
			}
			
			return result.toString().isEmpty() ? null : result.toString();
		}
	}
	
	public void loadContent(String content) {
		if(Platform.isFxApplicationThread())
			this.webEngine.loadContent(content);
		else
			Platform.runLater(()->this.webEngine.loadContent(content));
	}

	public SimpleBooleanProperty onSession() {
		return this.onSession;
	}
	
	public synchronized boolean getOnSession() {
		return this.onSession.get();
	}
	
	public synchronized void setOnSession(boolean value) {
		this.onSession.set(value);
	}

}
