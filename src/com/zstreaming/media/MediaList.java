package com.zstreaming.media;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import com.zstreaming.launcher.ZStreaming;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class MediaList implements Serializable{

	private static final long serialVersionUID = 1L;
	public static final String NAME_ROOT_FOLDER = "myLists";
	public static final String LIST_EXTENTION = ".zmlist";
	
	private int index;
	private File iconPath;
	private transient SimpleStringProperty imageProperty;
	private transient SimpleStringProperty nameProperty;
	private transient SimpleIntegerProperty sizeProperty;
	private File sourceIconPath;
	private double originalWidth;
	private double originalHeight;
	private List<Media> content;

	protected String name;	
	protected File path;
		
	public MediaList() {
		this(null);
	}
	
	public MediaList(String name) {
		this(name, ZStreaming.getSettingManager().getSettings().get("list.image.url"));
	}
	
	public MediaList(String name, String iconPath) {
		this.content = new ArrayList<>();	
		this.name = name != null ? name : ZStreaming.getSettingManager().getSettings().get("default.list.name");
		if(iconPath != null) {
			this.iconPath = new File(iconPath);
			this.initProperty();
		}
	}	
	
	public double getOriginalWidth() {
		return this.originalWidth;
	}
	
	public void setOriginalWidth(double originalWidth) {
		this.originalWidth = originalWidth;
	}
	
	public double getOriginalHeight() {
		return this.originalHeight;
	}
	
	public void setOriginalHeight(double originalHeight) {
		this.originalHeight = originalHeight;
	}

	private void initProperty() {
		this.imageProperty = new SimpleStringProperty(this.iconPath.getAbsolutePath());
		this.nameProperty = new SimpleStringProperty(this.name);
		this.sizeProperty = new SimpleIntegerProperty(this.content.size());
	}
	
	public File getSourceIcon() {
		return this.sourceIconPath != null ? this.sourceIconPath :  new File(ZStreaming.getSettingManager().getSettings().get("list.image.url"));
	}
	
	public void setSourceIcon(File source) {
		this.sourceIconPath = source;
	}
	
	public int getIndex() {
		return this.index;
	}
	
	public void setIndex(int index) {
		this.index = index;
		this.store();
	}
		
	public String getIconPath() {
		return this.iconPath.getAbsolutePath();
	}
	
	public void setIconPath(String iconPath) {
		this.iconPath = new File(iconPath);
		this.imageProperty.set(iconPath);
	}
	
	public SimpleStringProperty imageProperty() {
		return this.imageProperty;
	}	

	public File getPath() {
		return this.path;
	}
	
	public SimpleIntegerProperty sizeProperty() {
		return this.sizeProperty;
	}
	
	public int getSize() {
		return this.content.size();
	}
	
	private void updateSize() {
		this.sizeProperty.set(this.content.size());
	}
	
	public void setName(String name) {
		this.name = name;
		this.nameProperty.set(name);
	}

	public String getName() {
		return this.name;
	}
	
	public SimpleStringProperty nameProperty() {
		return this.nameProperty;
	}
		
	public void setContent(List<Media> content) {
		this.content = content;
		this.store();
	}
	
	public List<Media> getContent(){
		return this.content;
	}
	
	public void addMedia(Media... media) {
		for(Media m : media) {
			m.setMediaList(this);
			this.content.add(m);
		}
		this.store();
	}	

	public void addMedia(int index, Media media) {
		media.setMediaList(this);
		this.content.add(index, media);
		this.store();
	}
	
	public void removeMedia(Media... media) {
		for(Media m : media) {
			this.content.remove(m);
		}
		this.store();
	}
	
	public void removeMedia(List<Media> mediaFilter) {
		this.content.removeAll(mediaFilter);
		this.store();
	}
	
	public void clearContent() {
		this.content.clear();
		this.store();
	}
		
	public void pathGenerate() throws IOException {
		String listPath = ZStreaming.getSettingManager().getSettings().get("list.path") + File.separator + MediaList.NAME_ROOT_FOLDER;
		String append = " (1)";
		File path;
		int x = 0;
		
		while((path = new File(new File(listPath, this.name), this.name + MediaList.LIST_EXTENTION)).exists()){
			String name = this.name;			
			String[] split = name.split(" ");
			
			if(split.length > 1) {
				String _append = split[split.length - 1];				
				name = name.substring(0, name.length() - (_append.length() + 1));
				
				if(_append.matches("\\(\\d+\\)")){					
					append = " (" + ++x + ")";
				}
			}
			
			this.setName(name + append);
		}

		this.delete(this.path, this.iconPath);
		this.setPath(path);
	}
	
	public void store() {
		this.updateSize();
		if(this.path != null) {
			if(!path.getParentFile().exists()) {
				path.getParentFile().mkdir();
			}
			if(this.iconPath != null){
				if(!this.iconPath.getParentFile().exists()) {
					this.iconPath.getParentFile().mkdir();
				}
			}
			try(ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(this.path))){		
				out.writeObject(this);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void delete(File path, File iconPath) {
		try {
			if(path != null && path.exists()) {
				Files.delete(path.toPath());			
			}
			if(iconPath != null && path != null && iconPath.getParentFile().getParentFile().equals(path.getParentFile())) {
				try {
					Files.delete(iconPath.toPath());
					Files.delete(iconPath.getParentFile().toPath());
				}catch(IOException ex) { }
			}
			
			try {
				Files.delete(path.getParentFile().toPath());
			}catch(IOException | NullPointerException ex) { }
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static MediaList load(File mediaListPath) {
		MediaList mediaList = null;
		
		try(ObjectInputStream in = new ObjectInputStream(new FileInputStream(mediaListPath))){
			mediaList = (MediaList)in.readObject();
			mediaList.initProperty();
		}catch (IOException | ClassNotFoundException e) { }

		return mediaList;
	}

	public void setPath(File path) {
		this.path = path;
	}

	public void delete() {
		this.delete(this.path, this.iconPath);
	}	
}
