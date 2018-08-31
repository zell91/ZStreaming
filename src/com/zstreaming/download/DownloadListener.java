package com.zstreaming.download;

import java.io.File;
import java.util.List;
import java.util.Locale;

import com.zstreaming.download.Download.State;
import com.zstreaming.gui.components.StatisticsChart;
import com.zstreaming.gui.download.DownloadValues;
import com.zstreaming.launcher.ZStreaming;
import com.zstreaming.statistics.DisabledChartCounter;
import com.zstreaming.statistics.SessionStatistics;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;

public class DownloadListener implements ChangeListener<Download.State> {
	
	private volatile boolean progressed;
	
	private Thread progressThread;
	
	private DownloadValues downloadValues;
	private ObservableList<DownloadValues> observableDownloadList;

	private List<StatisticsChart> enabledCharts;	
			
	public DownloadListener(DownloadValues downloadValues, ObservableList<DownloadValues> observableDownloadList, DisabledChartCounter chartCounter) {
		super();
		this.downloadValues = downloadValues;
		this.observableDownloadList = observableDownloadList;
		this.enabledCharts = chartCounter.getEnabledCharts();
	}
	
	@Override
	public synchronized void changed(ObservableValue<? extends State> state, State oldValue, State newValue) {
		this.progressed = newValue.equals(Download.State.IN_PROGRESS);
		
		switch(newValue) {
			case UNDEFINED:
				break;
			case WAITING:
				if(this.progressThread != null)	this.progressThread.interrupt();
				SessionStatistics.setState("download.wait");

				this.setGraphic("waiting", 
								 null,
								 new File("images/waiting.gif").toURI().toString(),
								 null,
								 Locale.getDefault());
				break;
			case IN_PROGRESS:
				this.downloadValues.bindChart(this.enabledCharts);				
				this.downloadValues.getActivity().setLastActivity(System.currentTimeMillis());

				this.setGraphic("start.download",
								new File("images/down.png").toURI().toString(),
								new File("images/waiting.gif").toURI().toString(),
								"progressed",
								Locale.getDefault());
				this.downloadValues.updateStyle(newValue);
				
				this.progressThread = new Thread(this.bindProgressTask(downloadValues.getDownload().getProgress()));
				this.progressThread.setName("ProgressTask => " + downloadValues.getDownload().getMedia().getMRL());
				this.progressThread.setDaemon(true);
				this.progressThread.start();
				break;
			case STOPPED:
				if(this.progressThread != null)	this.progressThread.interrupt();
				SessionStatistics.setState("download.suspended");

				this.setGraphic("",
								new File("images/paused.png").toURI().toString(),
								"",
								"");
				
				this.setValues(-1.0,
							   this.downloadValues.getWorkDone().getRealSize(),
							   this.downloadValues.getSizeRemain().getRealSize(),
							   -1.0,
							   this.downloadValues.getProgress().get());
				
				this.downloadValues.updateStyle(newValue);
				break;
			case PAUSED:
				if(this.progressThread != null)	this.progressThread.interrupt();
				SessionStatistics.setState("download.suspended");

				this.setGraphic("",
								new File("images/down.png").toURI().toString(),
								"",
								"");
				
				this.setValues(-1,
							   this.downloadValues.getWorkDone().getRealSize(),
							   this.downloadValues.getSizeRemain().getRealSize(),
							   -1,
							   this.downloadValues.getProgress().get());
				
				this.downloadValues.updateStyle(newValue);
				break;
			case INTERRUPTED:
				SessionStatistics.setState("download.failed");

				this.setGraphic("waiting",	null, null,null, Locale.getDefault());
				this.downloadValues.updateStyle(newValue);
				if(this.progressThread != null)	this.progressThread.interrupt();
			
				if(this.autoDelete(1)) break;
				
				this.setGraphic("interrupted",
								new File("images/down_disabled.png").toURI().toString(),
								new File("images/not_found2.png").toURI().toString(),
								"interrupted",
								Locale.getDefault());
				
				this.setValues(-1.0, -1.0, -1.0, -1.0, -1.0);				
				this.downloadValues.setPriority(-1);
				break;
			case COMPLETED:
				if(this.progressThread != null)	this.progressThread.interrupt();
				SessionStatistics.setState("finalizing", "...");

				this.setGraphic("finalizing",
								new File("images/dw_cell.png").toURI().toString(), 
								new File("images/downloading.gif").toURI().toString(),
								"completed",
								Locale.getDefault());				
			
				this.setValues(-1.0, this.downloadValues.getSize().getRealSize(), -1.0, -1.0, 0.0);
			
				this.downloadValues.updateStyle(newValue);				
				this.downloadValues.setPriority(-1);
								
				Thread finalizeTask = new Thread(()->{
					this.downloadValues.getDownload().setDestination(DigitalSignature.removeSignature(this.downloadValues));
					
					DownloadManager.storeDownlads();
					
					SessionStatistics.setState("download.completed");
					
					if(this.autoDelete(0)) return;
					
					this.setGraphic("completed.capitalize",
									new File("images/dw_cell.png").toURI().toString(), 
									new File("images/found2.png").toURI().toString(),
									"completed",
									Locale.getDefault());
					
					this.setValues(-1.0, this.downloadValues.getSize().getRealSize(), -1.0, -1.0, 1.0);
				});
				
				finalizeTask.setName("FinalizeTask");
				finalizeTask.start();
				break;
			default:
				break;
		}
				
		SessionStatistics.updateSpeed();
		DownloadManager.storeDownlads();
	}
	
	private boolean autoDelete(int value) {
		try {
			if(Integer.parseInt(ZStreaming.getSettingManager().getSettings().get("auto.delete")) == 1) {
				int _value = Integer.parseInt(ZStreaming.getSettingManager().getSettings().get("auto.delete.filter"));
				
				if(value == _value || _value < 0) {
					Platform.runLater(()->this.observableDownloadList.remove(this.downloadValues));
					DownloadManager.getDownloads().remove(this.downloadValues.getDownload());
					return true;
				}	
			}			
		}catch(NumberFormatException e) { }		
	
		return false;
	}

	private Runnable bindProgressTask(Progress progress){		
		return ()->{
			boolean updateLineChart = false;
			boolean updateWorkDone = false;
			long oldValue = 0L;
			long time = -1;
			long workDone = 0L;
			long delay = 0L;	
			long sec = 0L;
			
			try {								
				this.downloadValues.setState("downloading", Locale.getDefault());
				
				if(DownloadActivity.isSingleChartActive()) {
					try {
						delay = Long.parseLong(ZStreaming.getSettingManager().getSettings().get("update.chart.delay")) * 1000;
					}catch(NumberFormatException ex) {
						delay = 5;
					}
				}				
				
				if(this.downloadValues.getActivity().getDataChart().size() == 0) this.downloadValues.getActivity().addDataChart(0, 0.0);
				
				SessionStatistics.setState("In download", false);

				while(this.progressed) {
					try {
						delay = Long.parseLong(ZStreaming.getSettingManager().getSettings().get("update.chart.delay"));
					}catch(NumberFormatException ex) {
						delay = 5;
					}
					oldValue = progress.getCurrentSize();
					workDone = 0;

					synchronized(this.progressThread) { this.progressThread.wait(500); }					
					
					sec += 500;						
					workDone += progress.getCurrentSize() - oldValue;
					
					SessionStatistics.updateWorkDone(workDone);
					SessionStatistics.updateSpeed();						
					
					updateWorkDone = !updateWorkDone;
					
					this.downloadValues.getActivity().addActivityTime(500);
					
					long bps = (progress.getCurrentSize() - oldValue)*2;
					
					progress.setRemainingSize(progress.getMaxSize() - progress.getCurrentSize());
					
					double percentage = progress.getPercentage();
					
					try{
						time = progress.getRemainingSize() / bps;
					}catch(ArithmeticException e) { };
										
					this.setValues(bps,
								   progress.getCurrentSize(),
								   progress.getRemainingSize(),
								   time,
								   percentage);
										
					if(sec >= (delay*1000)) {
						updateLineChart = true;
						sec = 0;
					}					
					if(updateLineChart && DownloadActivity.isSingleChartActive()) {
						this.downloadValues.getActivity().addDataChart(delay, bps);
						this.downloadValues.getActivity().updateDataChart(delay);
						updateLineChart = false;
					}					
					if(!SessionStatistics.getState().equals("In download") && !SessionStatistics.getState().endsWith("...") && updateLineChart) SessionStatistics.setState("down.sec.btn");
				}
			}catch(InterruptedException ex) {				
			}finally {
				this.downloadValues.unbindChart(this.enabledCharts);
				synchronized(this.progressThread) { 
					try {
						this.progressThread.wait(1000);
					} catch (InterruptedException e) {	}
				}
				workDone += progress.getCurrentSize() - oldValue;
				SessionStatistics.updateWorkDone(workDone);				
			}
		};
	}
	
	public void setValues(long speed, long workDone, long sizeRemain, long timeRemain, double percentage) {		
		this.setValues((double)speed, (double)workDone, (double)sizeRemain, (double)timeRemain, (double)percentage);
	}
	
	public void setValues(double speed, double workDone, double sizeRemain, double timeRemain, double percentage) {
		if(Thread.currentThread().isInterrupted()) return;
		
		this.downloadValues.setSpeed(speed);
		this.downloadValues.setWorkDone(workDone);
		this.downloadValues.setSizeRemain(sizeRemain);
		this.downloadValues.setTimeRemain(timeRemain);
		this.downloadValues.setProgress(percentage);
	}
	
	public void setGraphic(String state, String imgSource0, String imgSource1, String styleClass) {		
		this.setGraphic(state, imgSource0, imgSource1, styleClass, null);
	}
	
	public void setGraphic(String state, String imgSource0, String imgSource1, String styleClass, Locale locale) {
		if(locale != null) {
			if(state != null) this.downloadValues.setState(state, locale);
		}else {
			if(state != null) this.downloadValues.setState(state);
		}
		if(imgSource0 != null) this.downloadValues.setImageName(imgSource0.isEmpty() ? null : imgSource0);
		if(imgSource1 != null) this.downloadValues.setImageState(imgSource1.isEmpty() ? null : imgSource1);
	}
	
/*	private void sort(ObservableList<DownloadValues> list) {
		Platform.runLater(()->{
			try {
				if(!this.downloadValues.getRow().getTableView().getSortOrder().isEmpty() && list != null) {						
					if(!this.observableDownloadList.sorted(this.downloadValues.getComparator(this.downloadValues.getRow().getTableView().getSortOrder().get(0))).equals(list))	{
						try {
							this.downloadValues.getRow().getTableView().sort();
						}catch(NullPointerException ex) {
							System.out.println(ex.getLocalizedMessage());
						}
					}
				}
			}catch(Exception ex) { ex.printStackTrace(); }
		});
	}*/
}
