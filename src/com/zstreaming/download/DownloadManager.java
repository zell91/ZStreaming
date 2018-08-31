package com.zstreaming.download;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.util.collection.DownloadList;
import com.zstreaming.download.exception.DownloadException;
import com.zstreaming.gui.download.DownloadValues;
import com.zstreaming.launcher.ZStreaming;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;

public class DownloadManager {
	
	public static DownloadList downloads = DownloadManager.loadDownloads();
		
	private static SimpleIntegerProperty activeProperty = new SimpleIntegerProperty();	
	private static SimpleBooleanProperty stopProperty = new SimpleBooleanProperty(true);

	private DownloadManager() { }

	public synchronized static boolean isStopped() {
		return DownloadManager.stopProperty.get();
	}
	
	public synchronized static void setStopped(boolean stopped) {
		DownloadManager.stopProperty.set(stopped);
	}

	@SuppressWarnings("unchecked")
	private static DownloadList loadDownloads() {
		DownloadList list = new DownloadList();
		List<Download> _list =  Collections.synchronizedList(new ArrayList<>());
		try(ObjectInputStream os = new ObjectInputStream(new FileInputStream(new File(ZStreaming.getSettingManager().getSettings().get("zdownloads.store"))))){
			_list = (List<Download>)os.readObject();
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		Iterator<Download> iter = _list.iterator();
		
		while(iter.hasNext()) {
			Download d = iter.next();
			
			if(d.getNewDestination() != null) {
				if(d.getDestination().exists()){
					try {
						Files.move(d.getDestination().toPath(), d.getNewDestination().toPath(), StandardCopyOption.REPLACE_EXISTING);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				
				if(d.isQueued()) {
					d.setDestination(d.getNewDestination());
					d.setNewDestination(null);
				}
			}
						
			if(!d.getDestination().exists() && !d.isQueued() && !d.isStopped()) {
				d.setState(Download.State.INTERRUPTED);
			}else if(d.isActive() && d.getDestination().length() != 0) {
				d.setState(Download.State.PAUSED);
				d.getProgress().setCurrentSize(d.getDestination().length());
			}else if(d.isActive() && d.getDestination().length() <= 0) {
				d.setDestination(new File(d.getDestination().getParent(), d.getMedia().getName()));
				d.setState(Download.State.UNDEFINED);
			}
						
			list.add(new DownloadWrapper(d));
		}
				
		return list;
	}
		
	public static DownloadTask createDownloadTaskOnPlatform(DownloadValues downloadValues, Collection<DownloadValues> observableDownloadList) {
		DownloadTask task = new DownloadTask(downloadValues, observableDownloadList);
		if(!DownloadManager.getDownloads().contains(downloadValues.getDownload())) DownloadManager.getDownloads().add(new DownloadWrapper(downloadValues.getDownload()));
		DownloadManager.storeDownlads();
		
		return task;
	}
	
	public static void start(DownloadTask downloadTask) {
		try {
			if(!downloadTask.getState().equals(Thread.State.NEW)) throw new DownloadException(downloadTask.getDownload().getState());
			DownloadManager.getDownloads().get(downloadTask.getDownload()).setTask(downloadTask);
			downloadTask.start();
		} catch (DownloadException e) {
			System.out.println("NO");
		}
	}
	

	public static void resume(DownloadValues downloadValues, Collection<DownloadValues> observableDownloadList) {
		DownloadTask downloadTask = DownloadManager.createDownloadTaskOnPlatform(downloadValues, observableDownloadList);
		
		try {
			if(downloadTask.getDownload().isDone() || downloadTask.getDownload().isActive()) throw new DownloadException(downloadValues.getDownload().getState());
			DownloadManager.start(downloadTask);
		} catch (DownloadException e) {
			System.out.println("Errore di stato:\n" +  downloadValues.getDownload().getDestination() + " " + e.getMessage());
		}
	}
	
	public static void pause(DownloadTask downloadTask) {
		try {
			if(!downloadTask.getDownload().isActive() || downloadTask.getDownload().isPaused()) throw new DownloadException(downloadTask.getDownload().getState());
			DownloadManager.getDownloads().get(downloadTask.getDownload()).setTask(null);
			downloadTask.pause();
		} catch (DownloadException e) {
			System.out.println("Errore di stato:\n" +  downloadTask.getDownload().getDestination() + " " + e.getMessage());
		}
	}
	
	public static void stop(DownloadTask downloadTask) {
		try {
			if(downloadTask.getDownload().isDone() || downloadTask.getDownload().isStopped()) { throw new DownloadException(downloadTask.getDownload().getState()); }
			DownloadManager.getDownloads().get(downloadTask.getDownload()).setTask(null);
			downloadTask._stop();
		} catch (DownloadException e) {
			System.out.println("Errore di stato:\n" +  downloadTask.getDownload().getDestination() + " " + e.getMessage());
		}
	}
	
	public static void interrupt(DownloadTask downloadTask) {
		try { 
			if(downloadTask.getDownload().isDone()) { throw new DownloadException(downloadTask.getDownload().getState()); }
			DownloadManager.getDownloads().get(downloadTask.getDownload()).setTask(null);
			downloadTask._interrupt();
		} catch (DownloadException e) {
			System.out.println("Errore di stato:\n" +  downloadTask.getDownload().getDestination() + " " + e.getMessage());
		}
	}

	public static void storeDownlads() {		
		Thread storeDownloadTask = new Thread(()->{
			List<Download> downloads = Collections.synchronizedList(new ArrayList<>());
			DownloadManager.getDownloads().forEach(d->downloads.add(d.getDownload()));
			try(ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(new File(ZStreaming.getSettingManager().getSettings().get("zdownloads.store"))))){
				os.writeObject(downloads);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
		storeDownloadTask.setName("Store Download TASK");
		storeDownloadTask.start();
	}

	public static BooleanProperty stopProperty() {
		return DownloadManager.stopProperty;
	}
	
	public synchronized static DownloadList getDownloads() {
		return DownloadManager.downloads;
	}
	
	public static SimpleIntegerProperty activeProperty() {
		return DownloadManager.activeProperty;
	}
	
	public synchronized static int getActive() {
		return DownloadManager.activeProperty.get();
	}
	
	public static void setActive(int active) {
		DownloadManager.activeProperty.set(active);
	}
	
	public synchronized static void updateActive() {
		DownloadManager.setActive((int) DownloadManager.getDownloads().stream().filter(dw->dw.getTask() != null).count());
	}

	public synchronized static void decrementActive() {
		DownloadManager.setActive(DownloadManager.getActive() - 1);
		if(DownloadManager.getActive() < 0) DownloadManager.setActive(0);
	}
	public synchronized static void incrementActive() {
		DownloadManager.setActive(DownloadManager.getActive() + 1);
		if(DownloadManager.getActive() > 5) throw new IllegalArgumentException();
	}
	
	public synchronized static boolean inLimitActive() {
		return DownloadManager.getActive() < Integer.parseInt(ZStreaming.getSettingManager().getSettings().get("max.download"));
	}
	
	public synchronized static DownloadValues startNext(Collection<DownloadValues> observableDownloadList, Download except) {
		DownloadList sortedList = DownloadManager.getDownloads().getSortedList(except);
		if(sortedList.size() > 0 && DownloadManager.inLimitActive()) {
			DownloadValues newValues = observableDownloadList.stream().filter(item->item.getDownload().equals(sortedList.get(0).getDownload())).findFirst().orElse(null);
			
			if(newValues != null) {
				Download download = newValues.getDownload();				

				if(download.isPaused()) {
					DownloadManager.resume(newValues, observableDownloadList);
				}else if(download.isQueued()) {
					DownloadTask task = DownloadManager.createDownloadTaskOnPlatform(newValues, observableDownloadList);
					DownloadManager.start(task);
				}	

				return newValues;
			}
		}		
		return null;
	}
	
	public synchronized static DownloadValues startNext(Collection<DownloadValues> observableDownloadList) {
		return DownloadManager.startNext(observableDownloadList, null);
	}
}