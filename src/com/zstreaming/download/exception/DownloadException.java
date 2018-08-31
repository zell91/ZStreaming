package com.zstreaming.download.exception;

import com.zstreaming.download.Download;

public class DownloadException extends Exception{

	private static final long serialVersionUID = 1L;
	
	private Download.State state;
		
	public DownloadException(Download.State state) {
		super();
		this.state = state;
	}
		
	@Override
	public String getMessage() {
		return "State \"" + this.state + "\" not valid for request action.";
	}

}
