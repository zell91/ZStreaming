package com.util.time;

import java.time.Year;
import java.time.YearMonth;
import java.util.concurrent.TimeUnit;

import javafx.beans.property.SimpleStringProperty;

public class TimeAdjuster {
		
	public static final long DAYS_IN_WEEK = 7;	
	public static final long HOURS_IN_DAY = 24;	
	public static final long MINUTES_IN_HOUR = 60;
	
	public static final long SECONDS_IN_MIN = 60;
	public static final long SECONDS_IN_HOUR = SECONDS_IN_MIN * MINUTES_IN_HOUR;
	public static final long SECONDS_IN_DAY = SECONDS_IN_HOUR * HOURS_IN_DAY;
	public static final long SECONDS_IN_WEEK = SECONDS_IN_DAY * DAYS_IN_WEEK;
	public static final long SECONDS_IN_MONTH = SECONDS_IN_DAY * YearMonth.now().lengthOfMonth();
	public static final long SECONDS_IN_YEAR = SECONDS_IN_DAY * Year.now().length();
		
	private long seconds;	
	private SimpleStringProperty timeProperty;
	
	public TimeAdjuster() {
		this(-1);
	}
	
	public TimeAdjuster(double seconds) {
		this((long) seconds);
	}
	
	public TimeAdjuster(long seconds) {
		this.seconds = seconds;
		this.timeProperty = new SimpleStringProperty(this.optimizeTime());
	}
	
	public long getSeconds() {
		return this.seconds;
	}
	
	public SimpleStringProperty timeProperty() {
		return this.timeProperty;
	}

	public void set(long seconds) {
		this.seconds = seconds;
		this.timeProperty.set(this.optimizeTime());
	}
	
	public void set(double seconds) {
		this.set((long)seconds);
	}
	
	public String optimizeTime() {		
		String time = "";
		
		if(this.seconds < 0) return time;
		
		long years = this.seconds/SECONDS_IN_YEAR;
		long months = (this.seconds%SECONDS_IN_YEAR)/SECONDS_IN_MONTH;
		long weeks = (this.seconds%SECONDS_IN_MONTH)/SECONDS_IN_WEEK;
		long days = TimeUnit.SECONDS.toDays(this.seconds%SECONDS_IN_WEEK);
		long hours = TimeUnit.SECONDS.toHours(this.seconds%SECONDS_IN_DAY);
		long minutes =TimeUnit.SECONDS.toMinutes(this.seconds%SECONDS_IN_HOUR) ;
		long seconds = this.seconds%SECONDS_IN_MIN;
		
		if(years > 0)
			time += years + (years>1 ? " anni " : " anno ");
		if(months > 0)
			time += months + (months>1 ? " mesi " : " mese ");
		if(weeks > 0)
			time += weeks + (weeks>1 ? " settimane " : " settimana ");
		if(days > 0)
			time += days + "d ";
		if(hours > 0)
			time += hours + "h ";
		if(minutes > 0)
			time += minutes + "m ";
		if(time.isEmpty() || seconds>0)
			time += seconds + "s ";
		
		return time;		
	}

	@Override
	public String toString() {
		return this.optimizeTime();
	}

}
