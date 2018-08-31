package com.zstreaming.download;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Date;
import java.util.Locale;

import com.util.time.TimeAdjuster;
import com.zstreaming.gui.download.DownloadValues;
import com.zstreaming.launcher.ZStreaming;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionModel;

public class DownloadActivity {
	
	protected TimeAdjuster totalActivity;
	protected SimpleStringProperty lastActivity;
	protected SimpleStringProperty startActivity;
	
	private long total;
	
	private Download download;
	
	private ObservableList<Data<Number, Number>> data;
			 
	public DownloadActivity(Download download) {
		this.download = download;
		this.total = download.getTotalTime();
		this.totalActivity = new TimeAdjuster(this.total/1000);
		this.lastActivity = new SimpleStringProperty(DownloadActivity.formatDate(this.download.getDestination().lastModified()));
		this.startActivity = new SimpleStringProperty(this.download.getStartDate());
		this.data = FXCollections.observableArrayList();
	}
	
	public void addActivityTime(long time) {
		this.download.setTotalTime(this.total += time);
		this.setTotalActivity(this.total/1000);
	}
	
	public void updateDataChart(double delay) {
		Platform.runLater(()->{
			if(this.download.isActive())
				this.data.forEach(d->d.setXValue(d.getXValue().doubleValue() - delay));			
		});
	}
	
	public void addDataChart(double delay, double bps) {
		Platform.runLater(()->{
			if(this.download.isActive())
				this.data.add(new Data<Number, Number>(delay, bps));			
		});
	}
	
	public  ObservableList<Data<Number, Number>> getDataChart(){
		return this.data;
	}

	public String getTotalActivity() {
		return this.totalActivity.optimizeTime();
	}
	
	public void setTotalActivity(long time) {
		Platform.runLater(()->this.totalActivity.set(time));
	}	
	
	public void setLastActivity(long time) {
		Platform.runLater(()->this.lastActivity.set(DownloadActivity.formatDate(time)));
	}
	
	public String getLastActivity() {
		return this.lastActivity.get();
	}

	public void setStartActivity(String startDate) {
		Platform.runLater(()->this.startActivity.set(startDate));
	}
	
	public String getStartActivity() {
		return this.startActivity.get();
	}
	
	public void bindActivity(SelectionModel<DownloadValues> selectionModel, int index, Label start, Label last, Label total) {
		this.bind(this.startActivity, start, selectionModel, index);
		this.bind(this.lastActivity, last, selectionModel, index);
		this.bind(this.totalActivity.timeProperty(), total, selectionModel, index);
	}
	
	public void bind(StringProperty stringProperty, Label label, SelectionModel<DownloadValues> selectionModel, int index) {
		label.setText(stringProperty.get());
		
		stringProperty.addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				if(selectionModel.isSelected(index))
					label.setText(newValue);	
				else
					stringProperty.removeListener(this);
			}			
		});	
	}
			
	public static String formatDate(long time) {
		if(time < 1) return " -";
		
		Date date = new Date(time);
				
		DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL).withLocale(Locale.getDefault());
		DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("HH:mm").withLocale(Locale.getDefault());
		
		return formatter.format(LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault())) + ", " + formatter2.format(LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault()));
	}

	public Download getDownload() {
		return this.download;
	}

	public static boolean isSingleChartActive() {		
		try {
			int value = Integer.parseInt(ZStreaming.getSettingManager().getSettings().get("single.charts.enable"));
			if(value == 0)
				return false;
			else if(value == 1)
				return true;
		}catch(NumberFormatException ex){ }
		
		return true;
	}
}
