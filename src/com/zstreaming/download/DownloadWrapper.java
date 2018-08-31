package com.zstreaming.download;

public class DownloadWrapper {
	
	private Download download;
	private DownloadTask task;
	
	public DownloadWrapper(Download download) {
		this.download = download;
	}
	
	public synchronized void setTask(DownloadTask task) {		
		this.task = task;		
		DownloadManager.updateActive();
	}
	
	public DownloadTask getTask() {
		return task;
	}
	
	public Download getDownload() {
		return download;
	}

}
