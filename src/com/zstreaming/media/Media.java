package com.zstreaming.media;

import java.io.Serializable;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Arrays;
import java.util.Locale;

import com.util.size.Size;
import com.zstreaming.launcher.ZStreaming;

public class Media implements Serializable {

	private static final long serialVersionUID = 1L;
	private String name, hoster, mime, ext, customName, description;
	private URL source, mrl;
	private URL[] mrls;
	private Size size;
	private LocalDateTime lastScan;
	
	private boolean chunked;
	private boolean available;

	private MediaList mediaList;
	
	public Media() {	
		this.lastScan = LocalDateTime.now();
		this.available = true;
	}	

	public void setMediaList(MediaList mediaList) {
		this.mediaList = mediaList;
	}
	
	public MediaList getMediaList() {
		return this.mediaList;
	}
	
	public LocalDateTime getLdtLastScan() {
		return this.lastScan;
	}
	
	public void setLastScan(LocalDateTime ldt) {
		this.lastScan = ldt;
	}
	
	public String getLastScan() {
		if(this.lastScan != null) {
			Locale locale = new Locale(ZStreaming.getSettingManager().getSettings().get("lang"));
			return this.lastScan.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL).withLocale(locale)) + ", " + this.lastScan.format(DateTimeFormatter.ofPattern("HH:mm").withLocale(locale));
		}		
		return null;
	}
	
	public String getCustomName() {
		return this.customName;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getDescription() {
		return this.description;
	}
	
	public void setCutomName(String customName) {
		this.customName = customName;
	}
		
	public String getHoster() {
		return hoster;
	}
	
	public void setHoster(String hoster) {
		this.hoster = hoster;
	}
	
	public URL getSource() {
		return source;
	}
	
	public void setSource(URL source) {
		this.source = source;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
		this.customName = this.name.substring(0, this.name.lastIndexOf("."));
		try {
			this.ext = name.split("\\.")[name.split("\\.").length - 1]; 
		}catch(Exception ex) { }
	}
	
	public String getMimeType() {
		return mime;
	}
	
	public void setMimeType(String mimeType) {
		this.mime = mimeType;
	}
	
	public String getExt() {
		return ext;
	}
	
	public URL[] getMRLs() {
		return mrls;
	}
	
	public void setMRLs(URL[] mrls) {
		this.mrls = mrls;
	}
	
	public URL getMRL() {
		return mrl;
	}
	
	public void setMRL(URL mrl) {
		this.mrl = mrl;
	}
	
	public boolean isChunked() {
		return chunked;
	}
	
	public void setChunked(boolean chunked) {
		this.chunked = chunked;
	}

	public void setSize(Size size) {
		this.size = size;
	}
	
	public Size getSize() {
		return size;
	}
	
	public boolean isAvalaible() {
		return this.available;
	}
	
	public void setAvalaible(boolean avalaible) {
		this.available = avalaible;
	}
	
	@Override
	public Media clone() {
		Media media = new Media();
		
		media.setChunked(this.chunked);
		media.setCutomName(this.customName);
		media.setDescription(this.description);
		media.setHoster(this.hoster);
		media.setLastScan(this.lastScan);
		media.setMimeType(this.mime);
		media.setMRL(this.mrl);
		media.setMRLs(this.mrls);
		media.setName(this.name);
		media.setSize(this.size);
		media.setSource(this.source);
		media.setMediaList(this.mediaList);
		
		return media;
	}
	
	public void switchTo(Media mediaTarget) {
		if(this.getMediaList() != null && mediaTarget.getMediaList() != null) {
			if(this.getMediaList().equals(mediaTarget.getMediaList())) {				
				MediaList mediaList = this.getMediaList();
				
				int indexSource = mediaList.getContent().indexOf(this);
				int indexTarget = mediaList.getContent().indexOf(mediaTarget);
				
				if(indexSource != indexTarget) {
					mediaList.removeMedia(this, mediaTarget);
					
					if(indexSource < indexTarget) {
						mediaList.addMedia(indexSource, mediaTarget);
						mediaList.addMedia(indexTarget, this);	
					}else {
						mediaList.addMedia(indexTarget, this);	
						mediaList.addMedia(indexSource, mediaTarget);
					}

				}			
			}else {
				throw new IllegalArgumentException(this + " e " + mediaTarget + " non appartengono alla stessa MediaList");
			}
		}else {
			String message = (this.getMediaList() == null ? this + " e " : "") + (mediaTarget.getMediaList() == null ? mediaTarget + " " : "") + "non " + (this.getMediaList() == null && mediaTarget.getMediaList() == null ? "possono " : "può ") + "essere null";  
			throw new NullPointerException(message);
		}	
	}
	
	public void toBack(Media mediaTarget) {
		if(this.getMediaList() != null && mediaTarget.getMediaList() != null) {
			if(this.getMediaList().equals(mediaTarget.getMediaList())) {
				MediaList mediaList = this.getMediaList();
				
				int indexTarget = mediaList.getContent().indexOf(mediaTarget);

				if(mediaList.getContent().indexOf(this) != indexTarget) {
					mediaList.removeMedia(this);
					mediaList.addMedia(indexTarget, this);
				}
				
			}else {
				throw new IllegalArgumentException(this + " e " + mediaTarget + " non appartengono alla stessa MediaList");
			}
		}else {
			String message = (this.getMediaList() == null ? this + " e " : "") + (mediaTarget.getMediaList() == null ? mediaTarget + " " : "") + "non " + (this.getMediaList() == null && mediaTarget.getMediaList() == null ? "possono " : "può ") + "essere null";  
			throw new NullPointerException(message);
		}
	}
	
	public boolean sameMedia(Media media) {
		if(this.isChunked() && !media.isChunked()) return false;
		if(!this.source.sameFile(media.getSource()) && !this.getMRL().sameFile(media.getMRL())) return false;		
		if(this.size.getRealSize() != media.getSize().getRealSize()) return false;		
		if(!this.mime.equals(media.getMimeType())) return false;		
		if(!this.name.equals(media.getName())) return false;
		
		return true;
	}
			
	@Override
	public String toString(){
		if(this.mediaList != null) {
			return this.customName;
		}else {
			return "Hoster: " + (this.hoster != null ? this.hoster : "unknown")  + "\n" +
					   "Name: " + (this.name != null ? this.name : "unknown") + "\n" +
					   "Mime-Type: " + (this.mime != null ? this.mime : "unknown") + "\n" +
					   "Source: " + this.source + "\n" +
					   "Size: " + (this.size.getRealSize() > 0 ? size.optimizeSize() : "unknown") + "\n" +
					   "Chunked: " + this.chunked + "\n" +
					   "MRL: " + (this.chunked ? (this.mrls != null ? Arrays.toString(this.mrls) : "unknown") : (this.mrl != null ? this.mrl : "unknown" ) );
		}
	}
}
