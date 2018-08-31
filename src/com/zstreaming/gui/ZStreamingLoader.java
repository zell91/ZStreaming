package com.zstreaming.gui;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import com.util.locale.ObservableResourceBundle;
import com.zstreaming.browser.WebBrowser;
import com.zstreaming.download.Download;
import com.zstreaming.download.DownloadListener;
import com.zstreaming.download.DownloadManager;
import com.zstreaming.download.DownloadTask;
import com.zstreaming.download.DownloadWrapper;
import com.zstreaming.gui.components.MediaListButton;
import com.zstreaming.gui.components.MediaListButtonBuilder;
import com.zstreaming.gui.controller.ZController;
import com.zstreaming.gui.download.DownloadValues;
import com.zstreaming.history.MediaHistory;
import com.zstreaming.launcher.ZStreaming;
import com.zstreaming.media.MediaList;
import com.zstreaming.media.MediaListLoader;
import com.zstreaming.search.SearchEngine;
import com.zstreaming.statistics.DisabledChartCounter;
import com.zstreaming.statistics.SessionStatistics;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class ZStreamingLoader {
	
	private ZController controller;
	
	private WebBrowser webBrowser;	
	private DownloadManager downloadManager;	
	private SearchEngine searchEngine;
	private DisabledChartCounter chartCounter;
	private MediaHistory mediaHistory;
	private ResourceBundle bundle;
	
	private List<String> parameters;
	
	public ZStreamingLoader(List<String> parameters) {
		this.parameters = parameters;
	}
	
	public boolean isAutorun() {
		return !this.parameters.isEmpty() && this.parameters.get(0).equals("-autorun");
	}
	
	public SearchEngine getSearchEngine() {
		return searchEngine;
	}
	
	public WebBrowser getWebBrowser() {
		return webBrowser;
	}
	
	public DownloadManager getDownloadManager() {
		return downloadManager;
	}
	
	public MediaHistory getMediaHistory() {
		return this.mediaHistory;
	}

	public void load() {
		ZStreaming.getSettingManager().load();
		String lang = ZStreaming.getSettingManager().getSettings().get("lang");
		Locale.setDefault(new Locale(lang.split("_")[0], lang.split("_")[1]));
		
		this.bundle =  ResourceBundle.getBundle(ObservableResourceBundle.BUNDLE_PATH + lang + "/" + lang, Locale.getDefault());
		this.webBrowser = new WebBrowser();
		this.searchEngine = new SearchEngine();
		this.chartCounter = new DisabledChartCounter();
		this.mediaHistory = MediaHistory.load(new File(ZStreaming.getSettingManager().getSettings().get("history.path")));	
		if(this.mediaHistory == null) {
			this.mediaHistory = new MediaHistory();
		}
	}

	private ObservableList<DownloadValues> loadDownloads() {
		SessionStatistics.setState("loading.download.list");
		ObservableList<DownloadValues> list = FXCollections.observableArrayList();
		for(DownloadWrapper downloadWrapper : DownloadManager.downloads) {
			Download download = downloadWrapper.getDownload();			
			downloadWrapper.getDownload().setWebBrowser(this.webBrowser.clone());		
			DownloadValues values = new DownloadValues(download);
			values.setName(download.getDestination().getName().split(DownloadTask.DOWNLOAD_EXTENTION)[0]);
			download.setState(download.getState());
			list.add(values);
		
			download.stateProperty().addListener(new DownloadListener(values, list, this.chartCounter));

			if(download.inPendingFinalization()){				
				download.setState(Download.State.UNDEFINED);
				download.setState(Download.State.COMPLETED);			
			}
			
			values.setProgressValues();

			SessionStatistics.setState(String.format("%s %s...", download.getDestination().getName(), ObservableResourceBundle.getLocalizedString("loaded")), false);
		}
		
		SessionStatistics.setState("loading.download.list");

		return list;
	}
	
	private ObservableList<MediaListButton> loadLists() {
		SessionStatistics.setState("loading.media.lists");

		ObservableList<MediaListButton> list = FXCollections.observableArrayList();
		
		MediaListLoader mediaListLoader = new MediaListLoader();
		mediaListLoader.load();
		
		List<MediaList> mediaLists = mediaListLoader.getLoadList();
		
		for(MediaList mediaList : mediaLists) {
			list.add(MediaListButtonBuilder.build(mediaList));
		}
		
		list.sort((x,y)->Integer.compare(x.getMediaList().getIndex(), y.getMediaList().getIndex()));
		
		return list;
	}
	
	public ZController getController() {
		return this.controller;
	}

	public void init(Stage stage) throws IOException {	
		FXMLLoader fxml = new FXMLLoader(this.getClass().getResource("./fxml/zstreaming.fxml"), this.bundle);
		Parent root = fxml.load();
		this.controller = fxml.getController();
		this.controller.setModel(this.webBrowser, this.mediaHistory, this.searchEngine, this.chartCounter);
		this.controller.setPrimaryStage(stage);		
		this.controller.initDownloadList(this.loadDownloads());
		this.controller.initListSection(this.loadLists());		
		this.controller.initHistory();
		SessionStatistics.setState("download.list.loaded");
		SessionStatistics.setState("opening");
		
		Scene scene = new Scene(root,1280, 720);
		scene.setFill(null);
		stage.setTitle("ZStreaming by zell91");
		stage.initStyle(StageStyle.DECORATED);
		((BorderPane)root).setBackground(null);
		stage.sizeToScene();
		
		stage.setOnShown(e->this.controller.mainSection());
		
		Platform.runLater(()->{
			stage.setScene(scene);
			this.controller.onClose();

			if(this.isAutorun()) {
				this.controller.showWithOptions();
			}else
				stage.show();
		});
	}
	
	

	
	
}
