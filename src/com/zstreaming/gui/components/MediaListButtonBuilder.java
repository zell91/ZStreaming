package com.zstreaming.gui.components;

import java.util.Locale;

import com.zstreaming.media.MediaList;

public class MediaListButtonBuilder {
	
	private MediaListButtonBuilder() { }
	
	public static MediaListButton build(MediaList mediaList) {
		MediaListButton mediaListButton = new MediaListButton(mediaList);
		
		mediaListButton.nameProperty().bindBidirectional(mediaList.nameProperty());
		mediaListButton.sizeProperty().bind(mediaList.sizeProperty().asString("%d element" + getStringSuffix(mediaList.getSize())));
		mediaListButton.iconProperty().bindBidirectional(mediaList.imageProperty());
		
		return mediaListButton;
	}
	
	private static String getStringSuffix(int size) {
		String suffix = null;
		
		Locale locale = Locale.getDefault();
		
		if(locale.equals(Locale.ITALY)) {
			suffix = size == 1 ? "o" : "i";
		}else {
			suffix = size == 1 ? "" : "s";
		}		
		
		return suffix;
	}

}
