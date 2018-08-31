package com.zstreaming.download;

import java.io.File;
import java.io.Serializable;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.Locale;

import com.zstreaming.browser.WebBrowser;
import com.zstreaming.launcher.ZStreaming;
import com.zstreaming.media.Media;

import javafx.beans.property.SimpleObjectProperty;

public class Download implements Serializable {

	private static final long serialVersionUID = -6841886989751133691L;
	private Media media;
	private transient WebBrowser wb;
	private Progress progress;
	private File dest;	
	private Download.Priority priority;
	private long totalTime;
	private LocalDateTime startDate;
	
	public transient SimpleObjectProperty<State> stateProperty;
	private State state;
	private File newDest;
	
	public enum State{
		UNDEFINED, WAITING, IN_PROGRESS, PAUSED, STOPPED, COMPLETED, INTERRUPTED;
	}
	
	public enum Priority{		
		UNDEFINED("Nessuna", 0), MIN("Minima", 1), LOW("Bassa", 2), MEDIUM("Media", 3), HIGH("Alta", 4), MAX("Massima", 5);
		
		private int value;
		private String name;
		
		Priority(String name, int value){ 
			this.name = name;
			this.value = value;
		}
		
		public int getValue() {
			return value;
		}
		
		public String getName() {
			return name;
		}

		public static Priority valueOf(double value) {
			for(Priority p : Priority.values()) {
				if(p.getValue() == value) return p;
			}
			
			return null;
		}
	}
	
	public Download(Media media, WebBrowser wb, String path) {		
		this.media = media;
		this.wb = wb;
		this.progress = new Progress((long)media.getSize().getRealSize());
		this.dest = Download.finalOutFile(new File(path, media.getName()));
		this.priority = Priority.UNDEFINED;
		this.stateProperty = new SimpleObjectProperty<>(this.state = State.UNDEFINED);
	}
	
	public void setStartDate(LocalDateTime ldt) {
		this.startDate = ldt;
	}
	
	public String getStartDate() {
		if(this.startDate != null) {
			Locale locale = new Locale(ZStreaming.getSettingManager().getSettings().get("lang"));
			return this.startDate.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL).withLocale(locale)) + ", " + this.startDate.format(DateTimeFormatter.ofPattern("HH:mm").withLocale(locale));
		}else
			return " -";
	}
	
	public void setTotalTime(long totalTime) {
		this.totalTime = totalTime;
	}
	
	public long getTotalTime() {
		return this.totalTime;
	}
	
	public void setPriority(Priority priority) {
		this.priority = priority;
		DownloadManager.storeDownlads();
	}
	
	public Priority getPriority() {
		return priority;
	}
	

	public SimpleObjectProperty<State> stateProperty() {
		return this.stateProperty;
	}
	
	public synchronized void setState(State state) {
		if(this.stateProperty == null) 
			this.stateProperty = new SimpleObjectProperty<>(this.state = state);
		else
			this.stateProperty.set(this.state = state);
	}
	
	public synchronized Download.State getState() {
		return this.state;
	}
	
	public Progress getProgress() {
		return progress;
	}
	
	public File getDestination() {
		return dest;
	}
	
	public void setWebBrowser(WebBrowser wb) {
		this.wb = wb;
	}
	
	public WebBrowser getWebBrowser() {
		return wb;
	}

	public List<URL> getSegments() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setDestination(File dest) {
		this.dest = dest;
	}
	
	public void setMedia(Media media) {
		this.media = media;
	}

	public Media getMedia() {
		return this.media;
	}
	
	public synchronized boolean isActive() {
		return this.getState().equals(State.IN_PROGRESS) || this.getState().equals(State.WAITING);
	}
	
	public synchronized boolean isCompleted() {
		return this.getState().equals(State.COMPLETED);
	}
	
	public synchronized boolean isInterrupted() {
		return this.getState().equals(State.INTERRUPTED);
	}
	
	public synchronized boolean isDone() {
		return this.isCompleted() || this.isInterrupted();
	}
	
	public synchronized boolean isPaused() {
		return this.getState().equals(State.PAUSED);
	}
	
	public synchronized boolean isStopped() {
		return this.getState().equals(State.STOPPED);
	}
	
	public synchronized boolean isQueued() {
		return this.getState().equals(State.UNDEFINED);
	}	
	
	public synchronized boolean isWaitinig() {
		return this.getState().equals(State.WAITING);
	}
	
	public static File finalOutFile(File destination) {		
		int num = 1;		
		String path = destination.getAbsoluteFile().getParent();
		String name = destination.getAbsoluteFile().getName();
		String _name = name;
		
		while(true) {
			File file = new File(path, _name);
			File fileWithExt = new File(path, _name + DownloadTask.DOWNLOAD_EXTENTION);
			boolean exists = DownloadManager.getDownloads().stream().anyMatch(d->d.getDownload().getDestination().getParentFile().equals(destination.getParentFile()) && (d.getDownload().getDestination().equals(file) || d.getDownload().getDestination().equals(fileWithExt)));

			if( !file.exists() &&
				!fileWithExt.exists() &&
				!exists) break; 
			_name = name.substring(0, name.lastIndexOf(".")) + " (" + num + ")" + name.substring(name.lastIndexOf("."));
			num++;
		}
		
		return new File(path, _name + DownloadTask.DOWNLOAD_EXTENTION);
	}
	
	@Override
	public String toString() {
		return "Download >>>>>\nMEDIA =>\n" + this.getMedia() + "\n\n"
			 			+ "STATE =>\n" + this.getState() + "\n\n"
			 			+ "PROGRESS =>\n" + this.getProgress() + "\n\n"
						+ "PATH =>\n" + this.getDestination();
	}

	public File getNewDestination() {
		return this.newDest;
	}

	public void setNewDestination(File newDest) {
		this.newDest = newDest;
	}

	public boolean inPendingFinalization() {
		return this.isCompleted() && this.getDestination().getName().endsWith(DownloadTask.DOWNLOAD_EXTENTION) &&
			   new File(this.getDestination().getParent(), this.getDestination().getName().replace(DownloadTask.DOWNLOAD_EXTENTION, "")).length() <
			   this.getDestination().length() - DigitalSignature.DATA_LENGTH;
	}
}
