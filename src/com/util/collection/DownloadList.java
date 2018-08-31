package com.util.collection;

import java.util.ArrayList;
import java.util.Comparator;

import com.zstreaming.download.Download;
import com.zstreaming.download.DownloadTask;
import com.zstreaming.download.DownloadWrapper;

public class DownloadList extends ArrayList<DownloadWrapper>{
	
	private static final long serialVersionUID = 1L;

	private Comparator<DownloadWrapper> comparator = new Comparator<DownloadWrapper>() {
		@Override
		public int compare(DownloadWrapper arg0, DownloadWrapper arg1) {

			int result = 0;
			
			Download download0 = arg0.getDownload();
			Download download1 = arg1.getDownload();
			
			if(download0.isDone() && download1.isDone()) return 1;
			if(download0.isDone() && !download1.isDone()) result = 1;
			if(download0.isStopped() && !download1.isStopped()) result = 1;
			
			if((download0.isQueued() || download0.isPaused()) && (download1.isQueued() || download1.isPaused())) {
				result = -(Integer.compare(download0.getPriority().getValue(), download1.getPriority().getValue()));
				if(result == 0) result = 1;
			}else if((download0.isQueued() || download0.isPaused()) && !(download1.isQueued() || download1.isPaused())) {
				result = -1;
			}else {
				result = 1;
			}
							
			return result;
		}		
	};
	
	public DownloadList() {
		super();
	}

	public DownloadList getSortedList(){
		return this.getSortedList(null);
	}
	
	public DownloadList getSortedList(Download download) {
		DownloadList cloned = null;
		
		if(download != null) {
			cloned = this.getAllExcept(download);
		}else {
			cloned = (DownloadList) this.clone();
		}
		
		cloned.sort(this.comparator);		
		cloned.removeIf(item->!item.getDownload().isQueued() && !item.getDownload().isPaused());
		
		return cloned;
	}

	public boolean contains(Download download) {
		return this.stream().anyMatch(dw->dw.getDownload().equals(download));
	}
	
	public boolean contains(DownloadTask task) {
		return this.stream().anyMatch(dw->dw.getTask().equals(task));
	}

	public DownloadWrapper get(Download download) {
		return this.stream().filter(dw->dw.getDownload().equals(download)).findFirst().orElse(null);
	}
	
	public DownloadWrapper get(DownloadTask task) {
		return this.stream().filter(dw->dw.getTask().equals(task)).findFirst().orElse(null);
	}
	
	public DownloadWrapper get(DownloadWrapper downloadWrapper) {
		return this.stream().filter(dw->dw.equals(downloadWrapper)).findFirst().orElse(null);
	}

	public synchronized DownloadTask getTask(Download download) {
		if(this.contains(download))
			return this.get(download).getTask();
		
		return null;
	}
	
	public synchronized Download getDownload(DownloadTask task) {
		if(this.contains(task))
			return this.get(task).getDownload();
		
		return null;
	}
	
	public synchronized void remove(Download download) {
		this.remove(this.get(download));
	}

	public DownloadList getAllExcept(Download download){
		DownloadList _list = ((DownloadList) this.clone());
		_list.remove(download);
		return _list;
	}

}


