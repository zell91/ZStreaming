package com.util.size;

import java.io.Serializable;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;

public class Size implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private double size;
	private transient SimpleStringProperty sizeProperty;
	private transient SimpleDoubleProperty realSizeProperty;
		
	public Size() {
		this(-1);
	}
	
	public Size(long size) {
		this((double)size);
	}	
	
	public Size(double size) {
		this.size = size;
		this.sizeProperty = new SimpleStringProperty(this.optimizeSize());
		this.realSizeProperty = new SimpleDoubleProperty(this.size);
	}

	public void setSize(double size) {
		this.size = size;
		this.sizeProperty.set(this.optimizeSize());
		this.realSizeProperty.set(size);
	}
	
	public double getRealSize() {
		return this.size;
	}
	
	public String optimizeSize(){		
		if(this.size < 0) return "";
		
		String opSize = null;
		
		double realSize = this.size;
				
		if(realSize > 1023999999){
			opSize = String.format("%.2f GB", ((realSize/1024)/1024)/1024);
		}else if(realSize > 1023999){
			opSize = String.format("%.2f MB", (realSize/1024)/1024);
		}else if(realSize > 1023){
			opSize = String.format("%.0f KB", realSize/1024);
		}else if(realSize > 0){
			opSize = String.format("%.0f byte", this.size);
		}else
			opSize = "0 byte";
		
		return opSize;
	}
	
	public SimpleStringProperty sizeProperty() {
		if(this.sizeProperty == null) this.sizeProperty = new SimpleStringProperty(this.optimizeSize());
		return this.sizeProperty;
	}
		
	public SimpleDoubleProperty realSizeProperty() {
		if(this.realSizeProperty == null) this.realSizeProperty = new SimpleDoubleProperty(this.size);
		return this.realSizeProperty;
	}
	
	@Override
	public String toString() {
		return this.optimizeSize();
	}
	
}
