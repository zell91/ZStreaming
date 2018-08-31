package com.zstreaming.statistics;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicLong;

import com.util.locale.ObservableResourceBundle;
import com.util.size.Size;
import com.util.size.SizePerSecond;
import com.zstreaming.download.Download;
import com.zstreaming.gui.download.DownloadValues;

import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;

public class SessionStatistics {

	private static Collection<DownloadValues> observableDownloadList;
	private static SimpleBooleanProperty connection =  new SimpleBooleanProperty(false);
	private static SimpleStringProperty state = new SimpleStringProperty();
	
	private static AtomicLong lastSpeedUpdate = new AtomicLong(0);
	private static AtomicLong lastWorkDoneUpdate = new AtomicLong(0);
	
	private static SizePerSecond bpsOptimizer = new SizePerSecond(0.0); 
	private static Size sizeOptimizer =  new Size(0.0);
	
	private static double workDone = 0;

	private SessionStatistics() { }
	
	public static void bind(Collection<DownloadValues> observableDownloadList) {
		SessionStatistics.observableDownloadList = observableDownloadList;		
		SessionStatistics.updateSpeed();
		SessionStatistics.updateWorkDone(0.0);
	}
	
	public static SizePerSecond getBpsOptimizer() {
		return SessionStatistics.bpsOptimizer;
	}	

	public static void updateSpeed(double speed) {
		Platform.runLater(()->SessionStatistics.bpsOptimizer.setSize(0.0));
	}	
	
	public static void updateSpeed() {		
		long interval = System.currentTimeMillis() - SessionStatistics.lastSpeedUpdate.get();
		
		if(SessionStatistics.observableDownloadList != null) {
			if(interval >= 1000) {			
				SessionStatistics.lastSpeedUpdate.set(System.currentTimeMillis());
				double speed = 0;
										
				for(DownloadValues downloadValues : SessionStatistics.observableDownloadList) {
					if(downloadValues.getDownload().getState().equals(Download.State.IN_PROGRESS))
						speed += downloadValues.getSpeed().getRealSize() >= 0 ?  downloadValues.getSpeed().getRealSize() : 0;
				}				
				
				final double finalSpeed = speed; 
				
				Platform.runLater(()->SessionStatistics.bpsOptimizer.setSize(finalSpeed));
			}else {
				if(SessionStatistics.observableDownloadList.stream().noneMatch(d->d.getDownload().getState().equals(Download.State.IN_PROGRESS))) {
					Platform.runLater(()->SessionStatistics.bpsOptimizer.setSize(0.0));
				}
			}
			
		}
	}
		
	public static SimpleStringProperty speedProperty() {
		return SessionStatistics.bpsOptimizer.sizeProperty();
	}
	
	public static void updateWorkDone(double workDone) {
		long interval = System.currentTimeMillis() - SessionStatistics.lastWorkDoneUpdate.get();		
		SessionStatistics.workDone += workDone >= 0 ? workDone : 0;
		
		if(SessionStatistics.observableDownloadList != null && interval >= 1000) {
			SessionStatistics.lastWorkDoneUpdate.set(System.currentTimeMillis());
			Platform.runLater(()->SessionStatistics.sizeOptimizer.setSize(SessionStatistics.workDone));
		}
	}
	
	public static SimpleStringProperty workDoneProperty() {
		return SessionStatistics.sizeOptimizer.sizeProperty();
	}
	
	public static SimpleBooleanProperty connectionProperty() {
		return SessionStatistics.connection;
	}
	
	public static void setConnection(boolean connected) {
		Platform.runLater(()->SessionStatistics.connection.set(connected));
	}
	
	public static boolean isConnected() {
		return SessionStatistics.connection.get();
	}
	
	public static SimpleStringProperty stateProperty() {
		return SessionStatistics.state;
	}
	
	public static void setState(String state, String suffix) {
		SessionStatistics.setState(state, suffix, true);
	}
	
	public static void setState(String state) {
		SessionStatistics.setState(state, null, true);
	}
	
	public static void setState(String state, boolean localized) {
		SessionStatistics.setState(state, null, localized);
	}
	
	public static void setState(String state, String suffix, boolean localized) {
		if(!localized) {
			Platform.runLater(()->SessionStatistics.state.set(suffix != null ? state + suffix : state));
		}else {
			SessionStatistics.setState(ObservableResourceBundle.getLocalizedString(state), suffix, false);
		}
	}


	public static String getState() {
		return SessionStatistics.state.get();
	}

	public static void clear() {
		SessionStatistics.bpsOptimizer.setSize(0.0);
		SessionStatistics.sizeOptimizer.setSize(0.0);
		SessionStatistics.workDone = 0.0;
		
		for(DownloadValues downloadValues : SessionStatistics.observableDownloadList) {
			downloadValues.getActivity().getDataChart().clear();
		}		
	}
}
