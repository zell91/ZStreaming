package com.util.size;

public class SizePerSecond extends Size{
	
	private static final long serialVersionUID = 1L;

	public SizePerSecond() {
		super();
	}
	
	public SizePerSecond(long size) {
		super(size);
	}
	
	public SizePerSecond(double size) {
		super(size);
	}

	@Override
	public String optimizeSize() {
		return !super.optimizeSize().isEmpty() ? super.optimizeSize() + "/s" : "";
	}
}
