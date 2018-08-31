package com.zstreaming.history;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import com.zstreaming.launcher.ZStreaming;
import com.zstreaming.media.Media;
import com.zstreaming.plugins.controller.URLScannerTask.State;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public class MediaHistory implements Serializable {

	private static final long serialVersionUID = 1L;
		
	private SortedMap<LocalDate, List<HistoryEntry>> content = new TreeMap<>();
	
	private File path;

	private transient BooleanProperty changeHistory;
	
	public MediaHistory() {
		this.path = new File(ZStreaming.getSettingManager().getSettings().get("history.path"));
		this.changeHistory = new SimpleBooleanProperty();
	}

	public File getPath() {
		return path;
	}

	public void setPath(File path) {
		this.path = path;
	}

	public SortedMap<LocalDate, List<HistoryEntry>> getHistory(){
		SortedMap<LocalDate, List<HistoryEntry>> sortedHistory = new TreeMap<>((x,y)->-x.compareTo(y));
		sortedHistory.putAll(this.content);
		return sortedHistory;
	}
	
	public static MediaHistory load(File path) {
		MediaHistory history = null;
		
		
		try(ObjectInputStream os = new ObjectInputStream(new FileInputStream(path))){	
			history = (MediaHistory) os.readObject();
			history.setPath(new File(ZStreaming.getSettingManager().getSettings().get("history.path")));
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		return history;
	}
	

	private void store() {
		if(this.path != null) {
			if(!this.path.getParentFile().exists()) {
				this.path.getParentFile().mkdir();
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

	public void addEntry(String source, Media media) {
		List<HistoryEntry> fullHistory = new ArrayList<>();		
		this.content.values().forEach(entry->fullHistory.addAll(entry));		
		
		HistoryEntry entry = fullHistory.stream().filter(e->e.getMedia().equals(media)).findFirst().orElse(null);
		
		if(entry != null) {
			this.addEntry(source, media);
		}
	}
	
	public void addEntry(String source, Media media, State state) {
		LocalDate day = LocalDate.now();
		HistoryEntry historyEntry = new HistoryEntry(source, media);
		List<HistoryEntry> entries = null;
		
		if(this.content.containsKey(day)) {
			entries = this.content.get(day);
			entries.add(historyEntry);
		}else {
			entries = new ArrayList<>();
			entries.add(historyEntry);
			this.content.put(day, entries);
		}
		
		entries.sort((x,y)->-(x.getDate().compareTo(y.getDate())));		
		
		this.store();		
		this.changeHistoryUpdate();

	}

	public void clean() {
		this.content.clear();
		this.store();
		this.changeHistoryUpdate();
	}

	public BooleanProperty changeHistoryProperty() {
		if(this.changeHistory == null) {
			this.changeHistory = new SimpleBooleanProperty();
		}
		return this.changeHistory;
	}	
	
	public void changeHistoryUpdate() {
		Platform.runLater(()->{
			this.changeHistory.set(true);
			this.changeHistory.set(false);
		});
	}

	public void removeAll(List<HistoryEntry> entries) {
		for(HistoryEntry entry : entries) {
			this.remove(entry);
		}
		this.changeHistoryUpdate();
		this.store();
	}

	private void remove(HistoryEntry entry) {
		this.content.get(entry.getDate().toLocalDate()).remove(entry);
		if(this.content.get(entry.getDate().toLocalDate()).isEmpty()) {
			this.content.remove(entry.getDate().toLocalDate());
		}
	}
}
