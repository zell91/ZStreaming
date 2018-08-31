package com.zstreaming.history;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.zstreaming.media.Media;

public class HistoryEntry implements Serializable{

	private static final long serialVersionUID = 1L;
	
	private Media media;
	private LocalDateTime date;
	private String source;
	
	public HistoryEntry(String source, Media media) {
		this.source = source;
		this.media = media;
		this.date = LocalDateTime.now();
	}
	
	public String getSource() {
		return this.source;
	}
	
	public void setSource(String source) {
		this.source = source;
	}
	
	public void setMedia(Media media) {
		this.media = media;
	}
	
	public Media getMedia() {
		return this.media;
	}
	
	public LocalDateTime getDate() {
		return date;
	}
	public void setDate(LocalDateTime date) {
		this.date = date;
	}
	
}
