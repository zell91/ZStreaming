package com.zstreaming.gui.controller;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.util.locale.ObservableResourceBundle;
import com.zstreaming.download.Download;
import com.zstreaming.download.Download.Priority;
import com.zstreaming.download.DownloadManager;
import com.zstreaming.download.DownloadTask;
import com.zstreaming.gui.components.DeleteAlert;
import com.zstreaming.gui.components.DeleteAlert.Result;
import com.zstreaming.gui.components.contextmenu.DownloadViewContextMenu;
import com.zstreaming.gui.download.DownloadValues;
import com.zstreaming.launcher.ZStreaming;
import com.zstreaming.statistics.SessionStatistics;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.TableView;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.ContextMenuEvent;
import javafx.stage.FileChooser;

public class DownloadViewContextMenuController {
		
	protected DownloadViewContextMenu root;
	protected TableView<DownloadValues> downloadList;
	protected MenuItem playCMItem, pauseCMItem, stopCMItem, cancelCMItem, openFileCMItem, openFolderCMItem, infoCMItem, chnageDirCMItem, removeSelectedCMItem, removeCompletedCMItem, removeSingleCMItem, removeCancelledCMItem, removeDoneCMItem;
	protected Menu priorityCMitem, removeCMItem;
	protected RadioMenuItem undefindedPriority, minPriority, lowPriority, mediumPriority, highPriority, maxPriority;
	protected ToggleGroup toggleGroup;
	private ObservableResourceBundle bundleFactory;
	
	public DownloadViewContextMenuController(TableView<DownloadValues> downloadList,
			DownloadViewContextMenu downloadViewContextMenu, MenuItem playCMItem, MenuItem pauseCMItem,
			MenuItem stopCMItem, MenuItem cancelCMItem, Menu removeCMItem, MenuItem openFileCMItem,
			MenuItem openFolderCMItem, MenuItem infoCMItem, Menu priorityCMitem, RadioMenuItem undefindedPriority,
			RadioMenuItem minPriority, RadioMenuItem lowPriority, RadioMenuItem mediumPriority, RadioMenuItem highPriority,
			RadioMenuItem maxPriority, MenuItem chnageDirCMItem, MenuItem removeSelectedCMItem, MenuItem removeCompletedCMItem,
			MenuItem removeSingleCMItem,	MenuItem removeDoneCMItem, MenuItem removeCancelledCMItem, ToggleGroup toggleGroup) {
	
		this.downloadList = downloadList;
		this.root = downloadViewContextMenu;
		this.playCMItem = playCMItem;
		this.stopCMItem = stopCMItem;
		this.pauseCMItem = pauseCMItem;
		this.cancelCMItem = cancelCMItem;
		this.removeCMItem = removeCMItem;
		this.openFileCMItem = openFileCMItem;
		this.openFolderCMItem = openFolderCMItem;
		this.infoCMItem = infoCMItem;
		this.priorityCMitem = priorityCMitem;
		this.undefindedPriority = undefindedPriority;
		this.minPriority = minPriority;
		this.lowPriority = lowPriority;
		this.mediumPriority = mediumPriority;
		this.highPriority = highPriority;
		this.maxPriority = maxPriority;
		this.chnageDirCMItem = chnageDirCMItem;
		this.removeCompletedCMItem = removeCompletedCMItem;
		this.removeSelectedCMItem = removeSelectedCMItem;
		this.removeSingleCMItem = removeSingleCMItem;
		this.removeCancelledCMItem = removeCancelledCMItem;
		this.removeDoneCMItem = removeDoneCMItem;
		this.toggleGroup = toggleGroup;
	}	
	
	public void setObservableBundleFactory(ObservableResourceBundle bundleFactory) {
		this.bundleFactory = bundleFactory;		
		this.pauseCMItem.textProperty().bind(this.bundleFactory.getStringBindings("pause"));
		this.cancelCMItem.textProperty().bind(this.bundleFactory.getStringBindings("interrupt"));
		this.removeCMItem.textProperty().bind(this.bundleFactory.getStringBindings("remove"));
		this.openFileCMItem.textProperty().bind(this.bundleFactory.getStringBindings("open.file"));
		this.openFolderCMItem.textProperty().bind(this.bundleFactory.getStringBindings("open.dest.folder"));
		this.infoCMItem.textProperty().bind(this.bundleFactory.getStringBindings("media.info"));
		this.priorityCMitem.textProperty().bind(this.bundleFactory.getStringBindings("priority"));
		this.undefindedPriority.textProperty().bind(this.bundleFactory.getStringBindings("no.priority"));
		this.minPriority.textProperty().bind(this.bundleFactory.getStringBindings("minimum"));
		this.lowPriority.textProperty().bind(this.bundleFactory.getStringBindings("low"));
		this.mediumPriority.textProperty().bind(this.bundleFactory.getStringBindings("medium"));
		this.highPriority.textProperty().bind(this.bundleFactory.getStringBindings("high"));
		this.maxPriority.textProperty().bind(this.bundleFactory.getStringBindings("max"));
		this.chnageDirCMItem.textProperty().bind(this.bundleFactory.getStringBindings("change.path"));
		this.removeCompletedCMItem.textProperty().bind(this.bundleFactory.getStringBindings("remove.completed"));
		this.removeSelectedCMItem.textProperty().bind(this.bundleFactory.getStringBindings("remove.all.selected"));
		this.removeSingleCMItem.textProperty().bind(this.bundleFactory.getStringBindings("remove.selected"));
		this.removeCancelledCMItem.textProperty().bind(this.bundleFactory.getStringBindings("remove.interrupted"));
		this.removeDoneCMItem.textProperty().bind(this.bundleFactory.getStringBindings("remove.interrupted.completed"));		
	}
	
	public void play(ActionEvent event) {
		Thread playThread = new Thread(()->{			
			DownloadValues downloadValues = this.detectValues();
			
			if(downloadValues != null) {
				Download download = downloadValues.getDownload();
				
				if(download.isStopped()) {
					
					if(download.getDestination().exists())
						download.setState(Download.State.PAUSED);
					else
						download.setState(Download.State.UNDEFINED);
					
					downloadValues.setImageName(new File("images/down.png").toURI().toString());
					Platform.runLater(()->this.playCMItem.setText("Avvia"));
				}
				
				if(DownloadManager.inLimitActive()) {				
					if(download.isPaused()) {
						DownloadManager.resume(downloadValues, this.downloadList.getItems());
					}else if(download.isQueued()) {
						DownloadTask task = DownloadManager.createDownloadTaskOnPlatform(downloadValues, this.downloadList.getItems());
						DownloadManager.start(task);
					}			
				}
			}
		});
		
		playThread.setName("Play THREAD");
		playThread.setDaemon(true);
		playThread.start();
	}
	
	public void pause(ActionEvent event) {
		Thread stopThread = new Thread(()->this.stopAction(event));
		
		stopThread.setName("Paused THREAD");
		stopThread.setDaemon(true);
		stopThread.start();		
	}
		
	public void stop(ActionEvent event) {	
		Thread stopThread = new Thread(()->this.pause(event));
		
		stopThread.setName("Paused THREAD");
		stopThread.setDaemon(true);
		stopThread.start();
		
		Platform.runLater(()->this.playCMItem.setText("Riprendi"));
	}
	
	private boolean confirmDelete(String param) {
		int value = 1;
		
		try{
			value = Integer.parseInt(ZStreaming.getSettingManager().getSettings().get("confirm.interrupt"));
		}catch(NumberFormatException ex) {	}
		
		if(value == 1) {
			DeleteAlert delAlert = new DeleteAlert(param);
			
			Result result = delAlert.showAndGetResult();
			
			switch(result) {
				case ACTION:
					return true;
				case CANCEL:
				default:
					return false;
			}
		}
		
		return true;
	}
	
	public void cancel(ActionEvent event) {
		DownloadValues downloadValues = this.detectValues();
		
		if(!this.confirmDelete("interrupt")) {
			return;
		}
		
		Thread cancelThread = new Thread(()-> {
			if(downloadValues != null) this._cancel(downloadValues);
			DownloadManager.storeDownlads();
		});
		
		cancelThread.setDaemon(true);
		cancelThread.setName("Cancel THREAD");
		cancelThread.start();
	}
	
	private void _cancel(DownloadValues downloadValues) {
		Download download = downloadValues.getDownload();						
		DownloadTask task = DownloadManager.getDownloads().getTask(download);
		
		if(task != null) {
			DownloadManager.interrupt(task);
		}else {
			download.setState(Download.State.INTERRUPTED);
		}
	}

	public void removeSingle(ActionEvent event) {		
		DownloadValues downloadValues = this.detectValues();

		if(!this.confirmDelete("delete")) {
			return;
		}
		
		if(downloadValues != null) {
			if(!downloadValues.getDownload().isDone()) this._cancel(downloadValues);
			DownloadManager.getDownloads().remove(downloadValues.getDownload());
			this.downloadList.getItems().remove(downloadValues);
			SessionStatistics.setState("download.removed");
			DownloadManager.storeDownlads();
			ZStreaming.gcClean(1000);
		}
	}
	
	public void removeCompleted(ActionEvent event) {
		this.removeFilter(item->item.getDownload().isCompleted());
	}
	
	public void removeCancelled(ActionEvent event) {
		this.removeFilter(item->item.getDownload().isInterrupted());
	}
	
	public void removeDone(ActionEvent event) {
		this.removeFilter(item->item.getDownload().isDone());
	}
	
	protected void removeFilter(Predicate<? super DownloadValues> filter) {
		List<DownloadValues> filterList = downloadList.getItems().stream().filter(filter).collect(Collectors.toList());
		
		if(!this.confirmDelete("delete")) {
			return;
		}
		
		for(DownloadValues downloadValues : filterList) {
			DownloadManager.getDownloads().remove(downloadValues.getDownload());
		}		
		this.downloadList.getItems().removeAll(filterList);
		this.downloadList.getSelectionModel().clearSelection();
		SessionStatistics.setState(filterList.size() == 1 ? "download.removed" : String.format("%d %s", filterList.size(), ObservableResourceBundle.getLocalizedString("downloads.removed")), filterList.size() == 1);
		DownloadManager.storeDownlads();
		ZStreaming.gcClean(1000);
	}
	
	public void removeSelected(ActionEvent event) {
		List<DownloadValues> selectedList = new ArrayList<>();
		selectedList.addAll(this.downloadList.getSelectionModel().getSelectedItems());
		List<DownloadValues> remainList = this.downloadList.getItems().stream().filter(item->!selectedList.contains(item)).collect(Collectors.toList());
		
		if(!this.confirmDelete("delete")) {
			return;
		}
		
		this.downloadList.getItems().removeAll(selectedList);
		this.downloadList.getSelectionModel().clearSelection();
		
		SessionStatistics.setState(String.format("%d %s", selectedList.size(), ObservableResourceBundle.getLocalizedString("downloads.removed")), false);
		
		final boolean stopped = DownloadManager.isStopped();
		
		if(!stopped) DownloadManager.setStopped(true);

		Thread cancelThread = new Thread(()-> {
			for(DownloadValues downloadValues : selectedList) {
				if(downloadValues.getDownload().isActive()) this._cancel(downloadValues);
			}	
			
			DownloadManager.getDownloads().removeAll(DownloadManager.getDownloads().stream().filter(dw->selectedList.stream().anyMatch(item->item.getDownload().equals(dw.getDownload()))).collect(Collectors.toList()));
			DownloadManager.storeDownlads();
			
			ZStreaming.gcClean(500);
			
			if(!stopped) {
				DownloadValues values = null;
				while((values = DownloadManager.startNext(remainList)) != null) {					
					Download download = values.getDownload();
					
					synchronized(download) {
						while(!download.isActive()){
							try {
								download.wait(500);
							} catch (InterruptedException e) {	}
						}
					}	
				}
				
				DownloadManager.setStopped(false);				
			}
		});
		
		cancelThread.setDaemon(true);
		cancelThread.setName("MultiCancel THREAD");
		cancelThread.start();	
	}
	
	public void openFolder(ActionEvent e) {
		DownloadValues downloadValues = this.detectValues();
		
		if(downloadValues != null) {
			File file = downloadValues.getDownload().getDestination();
			if(!file.getParentFile().isDirectory()) {
				/*
				 *MESSAGGIO "LA DIRECOTORY NON ESISTE" 
				 */
				return;
			}
			
			Runtime runtime = Runtime.getRuntime();
			
			try {
				runtime.exec("explorer.exe /select, " + file);
			} catch (IOException ex) {
				/*
				 *MESSAGGIO "LA DIRECOTORY NON ESISTE" 
				 */
			}
		}
	}
	
	public void openFile(ActionEvent e) {
		DownloadValues downloadValues = this.detectValues();

		if(downloadValues != null) {
			File file = downloadValues.getDownload().getDestination();
			if(!file.exists() || file.isDirectory()) {
				/*
				 *MESSAGGIO "IL FILE NON ESISTE" 
				 */				
				return;
			}
			
			Desktop desktop = Desktop.getDesktop();
			
			try {
				desktop.open(file);
			} catch (IOException ex) {
				/*
				 *MESSAGGIO "ERRORE INASPETTATO" 
				 */
			}
		}
	}
	
	public void openInfo(ActionEvent e) {
	}
	
	private void stopAction(ActionEvent event) {		
		DownloadValues downloadValues = this.detectValues();
		
		if(downloadValues != null) {
			DownloadTask task = DownloadManager.getDownloads().getTask(downloadValues.getDownload());
			if(task != null) {
				if(event.getTarget().equals(this.pauseCMItem))
					DownloadManager.pause(task);
				else if(event.getTarget().equals(this.stopCMItem))
					DownloadManager.stop(task);
				else
					return;
				
				synchronized(this.downloadList.getItems()) {
					this.downloadList.getItems().notify();
				}
			} else {
				if(event.getTarget().equals(this.pauseCMItem))
					System.out.println("Non può essere fermato");
				else if(event.getTarget().equals(this.stopCMItem)) {
					downloadValues.setState("");
					downloadValues.setImageState(null);
					downloadValues.setImageName(new File("images/paused.png").toURI().toString());
					downloadValues.getDownload().setState(Download.State.STOPPED);
				}
			}
		}
	}

	public void updateContextMenu(ContextMenuEvent event) {
		DownloadValues downloadValues = this.detectValues();
		if(downloadValues != null) {
			Download download = this.downloadList.getItems().get(this.downloadList.getSelectionModel().getFocusedIndex()).getDownload();

			this.playCMItem.setText(download.isStopped() ? ObservableResourceBundle.getLocalizedString("resume") : ObservableResourceBundle.getLocalizedString("start"));
			this.playCMItem.setDisable((!DownloadManager.inLimitActive() && !download.isStopped()) || download.isActive() || download.isDone());
			this.pauseCMItem.setDisable(!download.isActive() || download.isDone());
			this.stopCMItem.setDisable(download.isDone() || download.isStopped());
			this.stopCMItem.setText(download.isActive() ? ObservableResourceBundle.getLocalizedString("stop") : ObservableResourceBundle.getLocalizedString("skip"));
			this.cancelCMItem.setDisable(download.isDone());
			this.openFileCMItem.setDisable(!download.isCompleted());
			this.openFolderCMItem.setDisable(download.isInterrupted());
			this.infoCMItem.setDisable(download.isInterrupted());
			this.toggleGroup.getToggles().forEach(radio->((RadioMenuItem)radio).setDisable(download.isDone()));
			this.removeSelectedCMItem.setDisable(this.downloadList.getSelectionModel().getSelectedIndices().size()<2);
			this.removeSingleCMItem.setDisable(false);
			this.chnageDirCMItem.setDisable(download.isDone());
			this.setPriorities(downloadValues);
		}else {
			this.playCMItem.setText(ObservableResourceBundle.getLocalizedString("start"));
			this.stopCMItem.setText(ObservableResourceBundle.getLocalizedString("stop"));
			this.playCMItem.setDisable(true);
			this.pauseCMItem.setDisable(true);
			this.stopCMItem.setDisable(true);
			this.cancelCMItem.setDisable(true);
			this.openFileCMItem.setDisable(true);
			this.openFolderCMItem.setDisable(true);
			this.infoCMItem.setDisable(true);
			this.toggleGroup.getToggles().forEach(radio->((RadioMenuItem)radio).setDisable(true));
			this.undefindedPriority.setSelected(true);
			this.removeSelectedCMItem.setDisable(true);
			this.removeSingleCMItem.setDisable(true);
			this.chnageDirCMItem.setDisable(true);
		}
		
		this.removeCancelledCMItem.setDisable(this.downloadList.getItems().stream().noneMatch(item->item.getDownload().isInterrupted()));
		this.removeDoneCMItem.setDisable(this.downloadList.getItems().stream().noneMatch(item->item.getDownload().isDone()));
		this.removeCompletedCMItem.setDisable(this.downloadList.getItems().stream().noneMatch(item->item.getDownload().isCompleted()));
	}

	private void setPriorities(DownloadValues downloadValues) {		
		Download.Priority currentPriority = downloadValues.getDownload().getPriority();

		for(Toggle menuItem : toggleGroup.getToggles()) {	
			Download.Priority priority = (Priority) menuItem.getUserData();
	
			if(priority.equals(currentPriority)) menuItem.setSelected(true);
						
			menuItem.selectedProperty().addListener(new ChangeListener<Boolean>() {
				@Override
				public void changed(ObservableValue<? extends Boolean> selected, Boolean oldValue, Boolean newValue) {
					if(newValue) {
						if(detectValues() != null) detectValues().setPriority(priority.getValue());
						menuItem.selectedProperty().removeListener(this);
					}
				}
			});
		}
				
		downloadValues.getPriority().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> value, Number oldValue, Number newValue) {
				Thread priorityTask = new Thread(()->{
					for(Toggle radioItem : toggleGroup.getToggles()){
						Download.Priority priority = (Priority) radioItem.getUserData();
						
						if(newValue.intValue() < 0) return;
						if(priority.getValue() == newValue.intValue()) {
							radioItem.setSelected(true);
							return;
						}
					}					
				});
				
				priorityTask.setDaemon(true);
				priorityTask.setName("PriorityTask");
				priorityTask.start();
				downloadValues.getPriority().removeListener(this);
			}			
		});
	}		

	protected DownloadValues detectValues() {		
		DownloadValues downloadValues = null;
		
		if(this.downloadList.getSelectionModel().getSelectedItems().size() > 0) {
			downloadValues = this.downloadList.getItems().get(this.downloadList.getSelectionModel().getFocusedIndex());
		}
		
		return downloadValues;
	}

	public void changePath(ActionEvent event) {
		DownloadValues downloadValues = this.detectValues();
		Download download = downloadValues.getDownload();
		
		FileChooser fileChooser = new FileChooser();
		
		String append = "." + download.getMedia().getExt() + DownloadTask.DOWNLOAD_EXTENTION;
				
		FileChooser.ExtensionFilter fileExtensions = new FileChooser.ExtensionFilter("ZDownload (*." + download.getMedia().getExt() + ", *" + append + ")", "*." + download.getMedia().getExt(), "*" + append);
		
		fileChooser.getExtensionFilters().add(fileExtensions);				
		fileChooser.setInitialDirectory(download.getDestination().getParentFile());		
		fileChooser.setInitialFileName("");

		File newDest = fileChooser.showSaveDialog(this.root.getOwnerWindow());
		
		if(newDest != null) {
			if(download.isDone()) {
				/*
				 *DOWNLOAD COMPLETATO 
				 */
				
				return;
			}
			if(!newDest.getAbsolutePath().endsWith(DownloadTask.DOWNLOAD_EXTENTION)) {
				if(!newDest.exists())
					newDest = Download.finalOutFile(new File(newDest.getAbsolutePath()));
				else {
					newDest.delete();
					newDest = new File(newDest.getAbsolutePath() + DownloadTask.DOWNLOAD_EXTENTION);
				}
			}
				
			download.setNewDestination(newDest);
			downloadValues.setName(newDest.getName().replaceAll(DownloadTask.DOWNLOAD_EXTENTION, ""));
			downloadValues.setPath(newDest.getParent());
			
			DownloadManager.storeDownlads();
		}
	}
}
