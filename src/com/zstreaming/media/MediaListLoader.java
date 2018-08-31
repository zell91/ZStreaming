package com.zstreaming.media;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.zstreaming.gui.components.ImageOptimizer;
import com.zstreaming.launcher.ZStreaming;

public class MediaListLoader {
	
	private File mainPath;
	private List<MediaList> mediaLists;
	private boolean loaded;
	
	public MediaListLoader() {
		this.mediaLists = new ArrayList<>();
		this.mainPath = new File(ZStreaming.getSettingManager().getSettings().get("list.path"), MediaList.NAME_ROOT_FOLDER);
	}
	
	public boolean isLoaded() {
		return this.loaded;
	}
	
	public List<MediaList> getLoadList(){
		if(this.loaded)
			return this.mediaLists;
		else
			throw new NullPointerException("Lists not loaded");
	}
	
	public void load() {
		if(this.mainPath != null && this.mainPath.exists()) {
			File[] paths = this.mainPath.listFiles();
			
			for(File path : paths) {
				if(path.isDirectory()) {
					File[] finalPaths = path.listFiles();
					
					for(File finalFile : finalPaths) {
						if(this.isMediaList(finalFile)) {
							MediaList mediaList = MediaList.load(finalFile);
							if(mediaList != null) {
								try {
									if(mediaList.getPath() != null && mediaList.getPath().exists()) {
										File iconPath = new File(mediaList.getIconPath());
										if(!iconPath.exists()) {
											if(!iconPath.getParentFile().exists()) {
												if(!new File(mediaList.getPath().getParentFile(), "icon").mkdir()) throw new IOException();
											}
											ImageOptimizer.store(mediaList.getSourceIcon(), new File(mediaList.getPath().getParentFile(), "icon/" + mediaList.getSourceIcon().getName()));
										}
									}
									
									this.mediaLists.add(mediaList);
								} catch (IOException e) {
									e.printStackTrace();
								}
								
							}
						}
					}
				}
			}
			
			this.loaded = true;
		}
	}
	
	public boolean isMediaList(File file) {
		return file.getName().equals(file.getParentFile().getName() + MediaList.LIST_EXTENTION);
	}

}
