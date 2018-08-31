package com.zstreaming.download;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.NoRouteToHostException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import com.util.network.OnlineChecker;
import com.zstreaming.browser.WebBrowser;
import com.zstreaming.browser.http.HttpRequest;
import com.zstreaming.browser.http.SimpleRequestHeader;
import com.zstreaming.download.exception.DownloadException;
import com.zstreaming.gui.download.DownloadValues;
import com.zstreaming.media.Media;
import com.zstreaming.plugins.controller.URLController;

import javafx.collections.ObservableList;

public class DownloadTask extends Thread {
	
	public static final String DOWNLOAD_EXTENTION = ".zdownload";
	private Download download;
	private WebBrowser wb;
	private File dest;
	private Progress progress;
	private Media media;
	private FileOutputStream os;

		
	private DownloadValues downloadValues;
	private Collection<DownloadValues> observableDownloadList;
	
	protected DownloadTask(DownloadValues downloadValues, Collection<DownloadValues> observableDownloadList) {
		this(downloadValues.getDownload());
		this.downloadValues = downloadValues;
		this.observableDownloadList = observableDownloadList;
	}
	
	public void setOutputStream(FileOutputStream os) {
		if(this.os != null) {
			try {
				this.os.close();
			} catch (IOException e) { }
		}
		this.os = os;
	}
	
	public FileOutputStream getOutputStream() {
		return os;
	}
	
	protected DownloadTask(Download download) {
		this.setDaemon(true);
		this.setName("DownloadTASK => " + download.getMedia().getMRL());
		this.wb = download.getWebBrowser();
		this.download = download;
		this.dest = download.getDestination();
		this.progress = download.getProgress();
		this.media = download.getMedia();
	}	
	
	public void setDownloadValues(DownloadValues downloadValues) {
		this.downloadValues = downloadValues;
	}
	
	public DownloadValues getDownloadValues() {
		return downloadValues;
	}
		
	public Collection<DownloadValues> getObservableDownloadList() {
		return observableDownloadList;
	}
	
	public void setObservableDownloadList(ObservableList<DownloadValues> observableDownloadList) {
		this.observableDownloadList = observableDownloadList;
	}

	protected Download getDownload() {
		return download;
	}
	
	@Override
	public void run() {
		try {	
			if(this.download.isDone()) throw new DownloadException(this.download.getState());			
			
			long range = 0;
			
			if(this.download.isPaused()) {
				this.download.setState(Download.State.WAITING);
				if(!DigitalSignature.isSigned(this.download)) throw new IOException();
				this.progress.setCurrentSize(this.dest.length() - DigitalSignature.DATA_LENGTH);
				this.progress.setRemainingSize(this.dest.length());
				range = this.progress.getCurrentSize();
				this.os = new FileOutputStream(this.dest, true);
			} else {
				this.download.setState(Download.State.WAITING);
				this.os = new FileOutputStream(this.dest);
				DigitalSignature.addSignature(os);
				this.download.setStartDate(LocalDateTime.now());
				this.downloadValues.getActivity().setStartActivity(this.download.getStartDate());
			}

			if(this.wb.getSession() != null) {
				long sessionDuration = this.wb.getSession().getSessionDuration();
									
				if(sessionDuration < 15000) {					
					long time = 15000 - sessionDuration;
				
					while(time>0) {
						this.downloadValues.setState(String.format("%s %ds", this.downloadValues.getBundleFactory().getString("wait"), (long)Math.ceil(time/1000)));
						time -=1000;
						synchronized(this.wb.getSession()) {	
							this.wb.getSession().wait(1000);
						}	
					}
					
					this.downloadValues.setState("waiting", Locale.getDefault());
				}		
			}	
					
			SimpleRequestHeader header = new SimpleRequestHeader();
			if(range > 0) header.addRequestProperty("Range", "bytes=" + range + "-");
			
			try(DataOutputStream out = new DataOutputStream(os)){
				if(this.media.isChunked()) {
					List<URL> mrls = this.download.getSegments();
					HttpRequest[] requests = new HttpRequest[mrls.size()];
					for(int i=0;i<mrls.size();i++) {
						requests[i] = new HttpRequest(mrls.get(i), header);
					}
					this.wb.download(requests, out, this.download);	
				}else {
					HttpRequest request = null;
					try {
						URL url = this.media.getMRL();
						if(url == null) throw new DownloadException(this.download.getState());
						request = new HttpRequest(url, header);
						if(!this.download.isActive()) throw new InterruptedException();
						this.wb.download(request, out, this.download);
					} catch (NullPointerException e) {						
						throw new InterruptedException();
					} catch (DownloadException e) {
						if(this.download.isWaitinig()) {
							URLController controller = new URLController(download.getMedia().getSource().toExternalForm(), this.wb);
							controller.run();
							Media media = controller.getMedia();
							if(media == null || media.getMRL() == null) {
								this._interrupt();
								throw new InterruptedException();
							}
							this.download.setMedia(media);
							request = new HttpRequest(media.getMRL(), header);
							if(!this.download.isActive()) throw new InterruptedException();
							this.wb.download(request, out, this.download);
						}
					}catch(NoRouteToHostException e) {
						throw new UnknownHostException();
					}
				}
			}
			
			this.download.setState(Download.State.COMPLETED);
			DownloadManager.getDownloads().get(this.download).setTask(null);

		} catch (FileNotFoundException e) {
			this._interrupt();
			WebBrowser.loggerManager.error("Non è possibile creare il file nel percorso specificato", e);
		} catch(UnknownHostException | SocketTimeoutException | SocketException e) {
			this.download.setState(Download.State.PAUSED);
			DownloadManager.getDownloads().get(this.getDownload()).setTask(null);
			if(OnlineChecker.checkOnline()) {
				DownloadManager.resume(this.downloadValues, this.observableDownloadList);
			}		
		} catch (IOException e) {
			this.download.setState(Download.State.PAUSED);
			DownloadManager.getDownloads().get(this.getDownload()).setTask(null);
			if(OnlineChecker.checkOnline()) {
				DownloadManager.resume(this.downloadValues, this.observableDownloadList);
			}		
		} catch(InterruptedException e) {
		}catch(DownloadException e) {
			e.printStackTrace();
		}finally {
			this.setOutputStream(null);	
			this.downloadValues.getActivity().setLastActivity(this.download.getDestination().lastModified());
			if(this.download.isInterrupted()) this.download.getDestination().delete();
			if(!DownloadManager.isStopped()) DownloadManager.startNext(this.observableDownloadList, this.download);
		}
	}
			
	protected void pause() {
		this.interrupt();
		this.download.setState(Download.State.PAUSED);
	}
	
	protected void _stop() {
		this.interrupt();
		this.download.setState(Download.State.STOPPED);
	}
	
	protected void _interrupt() {
		this.interrupt();
		this.download.setState(Download.State.INTERRUPTED);
	}

	public boolean isStartable() {
		return DownloadManager.inLimitActive() && !DownloadManager.isStopped();
	}	
}