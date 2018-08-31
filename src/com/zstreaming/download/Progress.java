package com.zstreaming.download;

import java.io.Serializable;

public class Progress implements Serializable {

	private static final long serialVersionUID = 1237837686694508014L;
	private long maxSize;
	private long currentSize;
	private long remainingSize;
	private long remainingTime;
	private long bps;
			
	public Progress(long maxSize) {
		this.maxSize = maxSize;	
		this.remainingSize = maxSize;
		this.currentSize = 0;
		this.remainingTime = 0;
	}
	
	public synchronized double getPercentage() {
		return ((double)this.currentSize / (double)this.maxSize);
	}
	
	public long getMaxSize() {
		return maxSize;
	}
	
	public synchronized void setCurrentSize(long newValue) {
		this.currentSize = newValue;
	}
	
	public synchronized long getCurrentSize() {
		return currentSize;
	}
	
	public synchronized long getRemainingTime() {
		return remainingTime;
	}
	
	public synchronized void setRemainingTime(long time) {
		this.remainingTime = time;
	}
	
	public synchronized long getRemainingSize() {
		return remainingSize;
	}
	
	public synchronized void setRemainingSize(long size) {
		this.remainingSize = size;
	}
	
	public synchronized void setBps(long bps) {
		this.bps = bps;
	}
	
	public synchronized long getBps() {
		return this.bps;
	}
	
}
