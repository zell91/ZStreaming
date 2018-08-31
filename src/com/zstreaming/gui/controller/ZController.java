package com.zstreaming.gui.controller;

import java.awt.Desktop;
import java.awt.SystemTray;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import com.util.locale.LocaleItem;
import com.util.locale.ObservableResourceBundle;
import com.util.network.URLValidator;
import com.util.size.Size;
import com.util.size.SizePerSecond;
import com.util.system.Response;
import com.util.system.Response.Result;
import com.util.system.SystemCMDManager;
import com.util.time.TimeAdjuster;
import com.zstreaming.browser.WebBrowser;
import com.zstreaming.download.Download;
import com.zstreaming.download.DownloadActivity;
import com.zstreaming.download.DownloadListener;
import com.zstreaming.download.DownloadManager;
import com.zstreaming.download.DownloadTask;
import com.zstreaming.download.DownloadWrapper;
import com.zstreaming.gui.ZTrayIcon;
import com.zstreaming.gui.components.CheckSelectionGroup;
import com.zstreaming.gui.components.EditTextField;
import com.zstreaming.gui.components.ExitAlert;
import com.zstreaming.gui.components.Fieldset;
import com.zstreaming.gui.components.FormListButton;
import com.zstreaming.gui.components.FoundItem;
import com.zstreaming.gui.components.ImageOptimizer;
import com.zstreaming.gui.components.MediaListButton;
import com.zstreaming.gui.components.MediaListButtonBuilder;
import com.zstreaming.gui.components.MediaListFormBuilder;
import com.zstreaming.gui.components.MediaListView;
import com.zstreaming.gui.components.PriorityIndicator;
import com.zstreaming.gui.components.RescanListView;
import com.zstreaming.gui.components.SelectionForm;
import com.zstreaming.gui.components.SettingButton;
import com.zstreaming.gui.components.StatisticsChart;
import com.zstreaming.gui.components.WaitingBar;
import com.zstreaming.gui.components.contextmenu.DownloadViewContextMenu;
import com.zstreaming.gui.components.contextmenu.TableHeaderContextMenu;
import com.zstreaming.gui.download.DownloadTableRow;
import com.zstreaming.gui.download.DownloadValues;
import com.zstreaming.history.FilterHistory;
import com.zstreaming.history.FilterHistory.Type;
import com.zstreaming.history.HistoryEntry;
import com.zstreaming.history.HistoryFactory;
import com.zstreaming.history.MediaHistory;
import com.zstreaming.launcher.ZStreaming;
import com.zstreaming.media.Media;
import com.zstreaming.media.MediaList;
import com.zstreaming.media.OnlineMediaChecker;
import com.zstreaming.plugins.controller.URLScannerTask;
import com.zstreaming.search.SearchEngine;
import com.zstreaming.settings.SettingsFactory;
import com.zstreaming.settings.SettingsManager;
import com.zstreaming.statistics.DisabledChartCounter;
import com.zstreaming.statistics.SessionStatistics;

import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WritableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.RadioButton;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Separator;
import javafx.scene.control.Slider;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTreeCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;
import javafx.util.Duration;

public class ZController implements Initializable {
	
	private WebBrowser webBrowser;
	protected SearchEngine searchEngine;
	protected DisabledChartCounter chartCounter;
	protected MediaHistory mediaHistory;
	protected ObservableResourceBundle bundleFactory = new ObservableResourceBundle();
	
	protected List<? extends Node> listVBox;
	protected List<TextArea> listTextInfo = new ArrayList<>();
	
	private ObservableList<DownloadValues> observableDownloadList;
	
	public final ExecutorService scannerPool = Executors.newFixedThreadPool(1);
	
	private ZTrayIcon trayIcon;
	
	private Stage primaryStage;
	
	@FXML
	protected BorderPane root;
		
	@FXML
	protected GridPane headerBar;
	
	@FXML
	protected HBox menuWrapper;
	
	@FXML
	protected MenuBar menuBar;
		
	public void setModel(WebBrowser webBrowser, MediaHistory history, SearchEngine searchEngine, DisabledChartCounter chartCounter) {
		this.webBrowser = webBrowser;
		this.mediaHistory = history;
		this.searchEngine = searchEngine;
		this.chartCounter = chartCounter;
		this.chartCounter.checkEnabledChart(this.listCharts);
	}
	
	public void setPrimaryStage(Stage primaryStage) {
		this.primaryStage = primaryStage;
		this.trayIcon = new ZTrayIcon(this.primaryStage);
	}	
	
	@Override
	public void initialize(URL url, ResourceBundle bundle) {
		this.listVBox = this.mediaInfoWrapper.getChildren().stream().filter(s-> s instanceof VBox).collect(Collectors.toList());
		this.listVBox.forEach(vbox->listTextInfo.add(((TextArea) ((VBox)vbox).getChildren().get(1))));
		this.bundleFactory.setBundle(bundle);
	
		this.initSettingSection();
		this.setSplitSize();
		this.setInfo();
		this.setResultList();
		this.setHeightTextInfo();
		this.addTabButton();
		this.initStateBar();
		this.createStatisticsCharts();
		this.initTreeItems();
		this.setMediaListViewSelectionMode();
		this.initDownloadDirChooser();
		
		this.scanSecBtnLbl.textProperty().bind(this.bundleFactory.getStringBindings("scan.sec.btn"));
		this.downSecBtnLbl.textProperty().bind(this.bundleFactory.getStringBindings("down.sec.btn"));
		this.listSecBtnLbl.textProperty().bind(this.bundleFactory.getStringBindings("list.sec.btn"));
		this.statSecBtnLbl.textProperty().bind(this.bundleFactory.getStringBindings("stat.sec.btn"));
		this.searchSecBtnLbl.textProperty().bind(this.bundleFactory.getStringBindings("search.sec.btn"));
		this.settingsSecBtnLbl.textProperty().bind(this.bundleFactory.getStringBindings("setting.sec.btn"));
		this.historySecBtnLbl.textProperty().bind(this.bundleFactory.getStringBindings("history.sec.btn"));
		
		this.urlLabel.textProperty().bind(this.bundleFactory.getStringBindings("url.scan.label").concat(":"));
		this.urlButton.textProperty().bind(this.bundleFactory.getStringBindings("url.scan.button"));
		this.mediaNameLbl.textProperty().bind(this.bundleFactory.getStringBindings("name").concat(":"));
		this.mediaSizeLbl.textProperty().bind(this.bundleFactory.getStringBindings("size").concat(":"));
		this.mediaSourceLbl.textProperty().bind(this.bundleFactory.getStringBindings("source").concat(":"));
		this.mediaHosterLbl.textProperty().bind(this.bundleFactory.getStringBindings("hoster").concat(":"));
		this.mediaStateLbl.textProperty().bind(this.bundleFactory.getStringBindings("state").concat(":"));
		this.downloadDirLbl.textProperty().bind(this.bundleFactory.getStringBindings("save.path.label").concat(":"));
		this.downloadDirBtn.textProperty().bind(this.bundleFactory.getStringBindings("browse.button"));
		this.openFolderBtn.textProperty().bind(this.bundleFactory.getStringBindings("open.folder"));
		this.clearNotFoundBtn.textProperty().bind(this.bundleFactory.getStringBindings("remove.not.found"));
		this.clearBtn.textProperty().bind(this.bundleFactory.getStringBindings("clean.list"));
		
		this.nameCol.textProperty().bind(this.bundleFactory.getStringBindings("name"));
		this.stateCol.textProperty().bind(this.bundleFactory.getStringBindings("state"));
		this.progressCol.textProperty().bind(this.bundleFactory.getStringBindings("progress"));
		this.speedCol.textProperty().bind(this.bundleFactory.getStringBindings("speed"));
		this.workDoneCol.textProperty().bind(this.bundleFactory.getStringBindings("downloaded"));
		this.sizeCol.textProperty().bind(this.bundleFactory.getStringBindings("size"));
		this.sizeRemainCol.textProperty().bind(this.bundleFactory.getStringBindings("remaining"));
		this.timeRemainCol.textProperty().bind(this.bundleFactory.getStringBindings("remaining.time"));
		this.priorityCol.textProperty().bind(this.bundleFactory.getStringBindings("priority"));
		this.hosterCol.textProperty().bind(this.bundleFactory.getStringBindings("hoster"));
		this.pathCol.textProperty().bind(this.bundleFactory.getStringBindings("save.path"));
		this.infoTab.textProperty().bind(this.bundleFactory.getStringBindings("info"));
		this.statTab.textProperty().bind(this.bundleFactory.getStringBindings("statistics"));
		this.progressSectText.textProperty().bind(this.bundleFactory.getStringBindings("_progress"));
		this.tansferWrapper.titleProperty().bind(this.bundleFactory.getStringBindings("transfer"));
		this.mediaWrapper.titleProperty().bind(this.bundleFactory.getStringBindings("general"));
		this.actDurationLblHead.textProperty().bind(this.bundleFactory.getStringBindings("duration.activity").concat(":"));
		this.speedLblHead.textProperty().bind(this.bundleFactory.getStringBindings("speed").concat(":"));
		this.downloadedLblHead.textProperty().bind(this.bundleFactory.getStringBindings("downloaded").concat(":"));
		this.remainingSizeLblHead.textProperty().bind(this.bundleFactory.getStringBindings("remaining").concat(":"));
		this.remainingTimeLblHead.textProperty().bind(this.bundleFactory.getStringBindings("remaining.time").concat(":"));
		this.lastActLblHead.textProperty().bind(this.bundleFactory.getStringBindings("last.activity").concat(":"));
		this.priorityLblHead.textProperty().bind(this.bundleFactory.getStringBindings("priority").concat(":"));
		this.startActLblHead.textProperty().bind(this.bundleFactory.getStringBindings("creation.date").concat(":"));
		this.nameInfoLblHead.textProperty().bind(this.bundleFactory.getStringBindings("name").concat(":"));
		this.mimeInfoLblHead.textProperty().bind(this.bundleFactory.getStringBindings("mime").concat(":"));
		this.sourceInfoLblHead.textProperty().bind(this.bundleFactory.getStringBindings("source").concat(":"));
		this.sizeInfoLblHead.textProperty().bind(this.bundleFactory.getStringBindings("size").concat(":"));
		this.segInfoLblHead.textProperty().bind(this.bundleFactory.getStringBindings("segments").concat(":"));
		this.pathInfoLblHead.textProperty().bind(this.bundleFactory.getStringBindings("save.path").concat(":"));
		
		this.backBtn.textProperty().bind(this.bundleFactory.getStringBindings("back"));
		this.headListLbl.textProperty().bind(this.bundleFactory.getStringBindings("list.sec.btn"));
		this.addListBtn.textProperty().bind(this.bundleFactory.getStringBindings("add.list"));
		this.selectionListBtn.textProperty().bind(this.bundleFactory.getStringBindings("selection"));
		this.editListBtn.textProperty().bind(this.bundleFactory.getStringBindings("edit.list"));
		this.delListBtn.textProperty().bind(this.bundleFactory.getStringBindings("del.list"));
		this.placeholderList.textProperty().bind(this.bundleFactory.getStringBindings("no.list"));
		this.mediaListLbl.textProperty().bind(this.bundleFactory.getStringBindings("content.list"));
		this.removeMediaBtn.textProperty().bind(this.bundleFactory.getStringBindings("remove"));
		this.onlineCheckBtn.textProperty().bind(this.bundleFactory.getStringBindings("check.available"));
		this.originalNameMediaText.textProperty().bind(this.bundleFactory.getStringBindings("name").concat(":"));
		this.sizeMediaText.textProperty().bind(this.bundleFactory.getStringBindings("size").concat(":"));
		this.mimeMediaText.textProperty().bind(this.bundleFactory.getStringBindings("mime").concat(":"));
		this.extMediaText.textProperty().bind(this.bundleFactory.getStringBindings("ext").concat(":"));
		this.sourceMediaText.textProperty().bind(this.bundleFactory.getStringBindings("source").concat(":"));
		this.hosterMediaText.textProperty().bind(this.bundleFactory.getStringBindings("hoster").concat(":"));
		this.segmentMediaText.textProperty().bind(this.bundleFactory.getStringBindings("segments").concat(":"));
		this.lastScannerText.textProperty().bind(this.bundleFactory.getStringBindings("last.scan").concat(":"));
		this.availableText.textProperty().bind(this.bundleFactory.getStringBindings("available").concat(":"));
		this.descriptionMedia.promptTextProperty().bind(this.bundleFactory.getStringBindings("no.description"));
		this.descriptionMedia.titleProperty().bind(this.bundleFactory.getStringBindings("description"));
		this.emptyListLbl.textProperty().bind(this.bundleFactory.getStringBindings("empty.list"));
		
		this.historyHeadLbl.textProperty().bind(this.bundleFactory.getStringBindings("history.sec.btn"));
		this.filterSection.textProperty().bind(this.bundleFactory.getStringBindings("search.filters"));
		this.searchWithinLbl.textProperty().bind(this.bundleFactory.getStringBindings("search.within"));
		this.stateFilterLbl.textProperty().bind(this.bundleFactory.getStringBindings("state"));
		this.sizeFilterLbl.textProperty().bind(this.bundleFactory.getStringBindings("size"));
		this.nameCheck.textProperty().bind(this.bundleFactory.getStringBindings("name"));
		this.stateFilterLbl.textProperty().bind(this.bundleFactory.getStringBindings("state"));
		this.queryHistory.promptTextProperty().bind(this.bundleFactory.getStringBindings("search.history"));
		this.emptyHistoryLbl.textProperty().bind(this.bundleFactory.getStringBindings("no.elements"));
		this.delSelHistoryBtn.textProperty().bind(this.bundleFactory.getStringBindings("delete.selected"));
		this.removeDate.textProperty().bind(this.bundleFactory.getStringBindings("delete"));
		
		this.totTreeItem.valueProperty().bind(this.bundleFactory.getStringBindings("current.session"));
		this.actTotItemHead.textProperty().bind(this.bundleFactory.getStringBindings("active.down").concat(":"));
		this.totCompletedHead.textProperty().bind(this.bundleFactory.getStringBindings("completed.down").concat(":"));
		this.totDownloadedHead.textProperty().bind(this.bundleFactory.getStringBindings("downloaded.data").concat(":"));
		this.totSpeedHead.textProperty().bind(this.bundleFactory.getStringBindings("speed").concat(":"));
		this.downloaded0Head.textProperty().bind(this.bundleFactory.getStringBindings("downloaded.data").concat(":"));
		this.downloaded1Head.textProperty().bind(this.bundleFactory.getStringBindings("downloaded.data").concat(":"));
		this.downloaded2Head.textProperty().bind(this.bundleFactory.getStringBindings("downloaded.data").concat(":"));
		this.downloaded3Head.textProperty().bind(this.bundleFactory.getStringBindings("downloaded.data").concat(":"));
		this.speed0Head.textProperty().bind(this.bundleFactory.getStringBindings("speed").concat(":"));
		this.speed1Head.textProperty().bind(this.bundleFactory.getStringBindings("speed").concat(":"));
		this.speed2Head.textProperty().bind(this.bundleFactory.getStringBindings("speed").concat(":"));
		this.speed3Head.textProperty().bind(this.bundleFactory.getStringBindings("speed").concat(":"));
		this.remain0Head.textProperty().bind(this.bundleFactory.getStringBindings("remaining.data").concat(":"));
		this.remain1Head.textProperty().bind(this.bundleFactory.getStringBindings("remaining.data").concat(":"));
		this.remain2Head.textProperty().bind(this.bundleFactory.getStringBindings("remaining.data").concat(":"));
		this.remain3Head.textProperty().bind(this.bundleFactory.getStringBindings("remaining.data").concat(":"));
		this.remainTime0Head.textProperty().bind(this.bundleFactory.getStringBindings("remaining.time").concat(":"));
		this.remainTime1Head.textProperty().bind(this.bundleFactory.getStringBindings("remaining.time").concat(":"));
		this.remainTime2Head.textProperty().bind(this.bundleFactory.getStringBindings("remaining.time").concat(":"));
		this.remainTime3Head.textProperty().bind(this.bundleFactory.getStringBindings("remaining.time").concat(":"));
		this.mainChart.titleProperty().bind(this.bundleFactory.getStringBindings("current.session"));
		
		this.generalBtn.textProperty().bind(this.bundleFactory.getStringBindings("general"));
		this.listsBtn.textProperty().bind(this.bundleFactory.getStringBindings("lists"));
		this.connectionBtn.textProperty().bind(this.bundleFactory.getStringBindings("connection"));
		this.langWrapper.titleProperty().bind(this.bundleFactory.getStringBindings("lang"));
		this.langLbl.textProperty().bind(this.bundleFactory.getStringBindings("lang.ui"));
		this.startOptionsWrapper.titleProperty().bind(this.bundleFactory.getStringBindings("startup.options"));
		this.startupCheck.textProperty().bind(this.bundleFactory.getStringBindings("startup"));
		this.startMinCheck.textProperty().bind(this.bundleFactory.getStringBindings("start.minimize"));
		this.startTrayCheck.textProperty().bind(this.bundleFactory.getStringBindings("start.icon.tray"));
		this.startupCheck.textProperty().bind(this.bundleFactory.getStringBindings("startup"));
		this.exitOptionsWrapper.titleProperty().bind(this.bundleFactory.getStringBindings("exit.options"));
		this.exitConfirmCheck.textProperty().bind(this.bundleFactory.getStringBindings("exit.confirm"));
		this.exitRequestCheck.textProperty().bind(this.bundleFactory.getStringBindings("exit.request"));
		this.exitTrayIconCheck.textProperty().bind(this.bundleFactory.getStringBindings("exit.tray.request"));
		this.logLbl.textProperty().bind(this.bundleFactory.getStringBindings("log.path"));
		this.openLogFolder.textProperty().bind(this.bundleFactory.getStringBindings("open.folder"));

		this.generalDownOptions.titleProperty().bind(this.bundleFactory.getStringBindings("general.options"));
		this.removalDownOptions.titleProperty().bind(this.bundleFactory.getStringBindings("removal.options"));
		this.saveDownOptions.titleProperty().bind(this.bundleFactory.getStringBindings("store.options"));
		this.statisticsOptions.titleProperty().bind(this.bundleFactory.getStringBindings("statistics.options"));
		this.simultaneousLbl.textProperty().bind(this.bundleFactory.getStringBindings("max.simultaneous.download"));
		this.modeAutoStartup.textProperty().bind(this.bundleFactory.getStringBindings("mode.out.startup"));
		this.confirmDelDownCheck.textProperty().bind(this.bundleFactory.getStringBindings("confirm.remove.download"));
		this.deleteAutoCheck.textProperty().bind(this.bundleFactory.getStringBindings("delete.auto"));
		this.onlyCompleteCheck.textProperty().bind(this.bundleFactory.getStringBindings("only.completed"));
		this.onlyInterruptedCheck.textProperty().bind(this.bundleFactory.getStringBindings("only.interrupted"));
		this.downPathLbl.textProperty().bind(this.bundleFactory.getStringBindings("save.down.path"));
		this.openDownFolder.textProperty().bind(this.bundleFactory.getStringBindings("open.folder"));
		this.downStoreLbl.textProperty().bind(this.bundleFactory.getStringBindings("store.down.path"));
		this.openDownStoreFolder.textProperty().bind(this.bundleFactory.getStringBindings("open.folder"));
		this.enableDisableLbl.textProperty().bind(this.bundleFactory.getStringBindings("enable.disable").concat(":"));
		this.mainChartCheck.textProperty().bind(this.bundleFactory.getStringBindings("main.chart"));
		this.delayText.textProperty().bind(this.bundleFactory.getStringBindings("delay.update.charts").concat(":"));
		this.defaultsListOptions.titleProperty().bind(this.bundleFactory.getStringBindings("defaults.options"));
		this.listPathDefaultLbl.textProperty().bind(this.bundleFactory.getStringBindings("list.path.default"));
		this.defaultIconListLbl.textProperty().bind(this.bundleFactory.getStringBindings("default.icon.list"));
		this.defaultNameListLbl.textProperty().bind(this.bundleFactory.getStringBindings("default.name.list"));
		this.resetDefaultNameList.textProperty().bind(this.bundleFactory.getStringBindings("reset"));
		this.storeConnectionWrapper.titleProperty().bind(this.bundleFactory.getStringBindings("store.options"));
		this.cookieLbl.textProperty().bind(this.bundleFactory.getStringBindings("cookie.path"));
		this.openCookieFolder.textProperty().bind(this.bundleFactory.getStringBindings("open.folder"));
		this.historyLbl.textProperty().bind(this.bundleFactory.getStringBindings("history.path"));
		this.openHistoryFolder.textProperty().bind(this.bundleFactory.getStringBindings("open.folder"));
		this.resetStatisticsBtn.textProperty().bind(this.bundleFactory.getStringBindings("reset.statistics"));
		
		this.storePlayerWrapper.titleProperty().bind(this.bundleFactory.getStringBindings("store.options"));
		this.playerPathLbl.textProperty().bind(this.bundleFactory.getStringBindings("player.path"));
		this.openPlayerFolderBtn.textProperty().bind(this.bundleFactory.getStringBindings("open.folder"));

		this.restoreConnBtn.textProperty().bind(this.bundleFactory.getStringBindings("restore.default"));
		this.restoreListBtn.textProperty().bind(this.bundleFactory.getStringBindings("restore.default"));
		this.restoreDownBtn.textProperty().bind(this.bundleFactory.getStringBindings("restore.default"));
		this.restoreGenBtn.textProperty().bind(this.bundleFactory.getStringBindings("restore.default"));
		this.restorePlayerBtn.textProperty().bind(this.bundleFactory.getStringBindings("restore.default"));

		this.searchSecBtn.setTooltip(new Tooltip("This feature is not available yet"));
	}

	private void initDownloadDirChooser() {
		File dir = new File(ZStreaming.getSettingManager().getSettings().get(SettingsManager.DOWNLOAD_PATH));
		if(!dir.isDirectory()) {
			dir = new File("");
			ZStreaming.getSettingManager().storeSettings(SettingsManager.DOWNLOAD_PATH, dir.getAbsolutePath());
		}

		this.downloadDirTxt.setText(dir.getAbsolutePath());
		
		this.downloadDirTxt.hoverProperty().addListener((observable, oldValue, newValue)->downloadDirBtn.pseudoClassStateChanged(PseudoClass.getPseudoClass("hover"), newValue));
		this.downloadDirTxt.focusedProperty().addListener((observable, oldValue, newValue)->downloadDirBtn.pseudoClassStateChanged(PseudoClass.getPseudoClass("focused"), newValue));
		
		this.downloadDirBtn.hoverProperty().addListener((observable, oldValue, newValue)->downloadDirTxt.pseudoClassStateChanged(PseudoClass.getPseudoClass("hover"), newValue));
		this.downloadDirBtn.focusedProperty().addListener((observable, oldValue, newValue)->downloadDirTxt.pseudoClassStateChanged(PseudoClass.getPseudoClass("focused"), newValue));		
	}
	
	private void setSplitSize() {
		this.downloadPane.heightProperty().addListener((observable, oldValue, newValue)->this.downloadPane.setDividerPositions(this.getDividerPositions(newValue.doubleValue())));
		this.tabPaneDownload.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue)->this.setTabPaneHeight(newValue));
		
		this.tabPaneDownload.heightProperty().addListener((observable, oldValue, newValue)->{
			if(this.tabPaneDownload.getMinHeight() > 0 && newValue.doubleValue() >= this.tabPaneDownload.getMinHeight())
				this.tabPaneDownload.setPrefHeight(newValue.doubleValue());
		});
		
		this.infoContent.heightProperty().addListener((observable, oldValue, newValue)->{
			if(newValue.doubleValue() > 273) 
				this.tabPaneDownload.setMaxHeight(84.0 + newValue.doubleValue());
		});
	}
	
	private void addTabButton() {
		this.tabPaneDownload.skinProperty().addListener((observable, oldValue, newValue)->{
			StackPane header = (StackPane)this.tabPaneDownload.lookup(".tab-header-background");
			this.hideTabBtn = new Button();
			Text text = new Text("»");
			text.setId("hideTabText");
			this.hideTabBtn.setGraphic(text);
			this.hideTabBtn.setId("hideTabBtn");
			StackPane.setAlignment(this.hideTabBtn, Pos.BOTTOM_RIGHT);
			header.getChildren().add(this.hideTabBtn);	
			this.hideTabBtn.setOnAction(e->this.hideTab(e));
		});	
	}

	private void setInfo() {
		for(Node node : listVBox) {
			VBox infoWrap = (VBox) node;
			TextArea textArea = (TextArea) infoWrap.getChildren().get(1);
			textArea.textProperty().addListener(new ChangeListener<String>() {				
				@Override
				public void changed(ObservableValue<? extends String> arg0, String arg1, String arg2) {
					if(arg2.length() == 0) return;
					((Text)textArea.lookup((".text"))).boundsInParentProperty().addListener(new ChangeListener<Bounds>(){
						@Override
						public void changed(ObservableValue<? extends Bounds> arg0, Bounds arg1, Bounds arg2) {
							textArea.setPrefHeight(arg2.getMaxY() + 15);
							adjustInfoSize(textArea);
						}				
					});		
					textArea.setPrefHeight(((Text)textArea.lookup((".text"))).boundsInParentProperty().get().getMaxY() + 15);
					adjustInfoSize(textArea);
				}				
			});
		}
	}

	private void setResultList() {			
		this.resultList.getItems().addListener(new ListChangeListener<FoundItem>() {
			@Override
			public void onChanged(Change<? extends FoundItem> value) {
				clearBtn.setDisable(resultList.getItems().stream().noneMatch(item->item.isNotFound() || item.isFound()));
				clearNotFoundBtn.setDisable(resultList.getItems().stream().noneMatch(item->item.isNotFound()));
			}
		});
		
		this.resultList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue)->this.setResults(newValue));
	}

	protected void setHeightTextInfo() {		
		for(Node node : this.listTextInfo) {
			TextArea textArea = (TextArea)node;
			textArea.setPrefHeight(30);
			((VBox)textArea.getParent()).setPrefHeight(75);
			
		}
	}
		
	public void onClose() {
		this.root.getScene().getWindow().setOnCloseRequest(e->{
			Toggle toggle = this.extOptionsGroup.getSelectedToggle();
			
			if(toggle.equals(this.exitConfirmCheck)) {
				ExitAlert exitAlert = new ExitAlert();
				
				exitAlert.initOwner(this.primaryStage);
				
				ExitAlert.Result result = exitAlert.showAndGetResult();

				switch(result) {
					case EXIT:
						this.pauseAll();
						
						if(exitAlert.rememberChoice()) 
							this.exitRequestCheck.setSelected(true);
						
						this.scannerPool.shutdownNow();
						break;
					case TRAY_ICON:
						this.trayIcon.show();

						if(exitAlert.rememberChoice()) 
							this.exitTrayIconCheck.setSelected(true);
						break;
					case CANCEL:
					default:
						e.consume();
						break;
				}
			}else if(toggle.equals(this.exitTrayIconCheck)) {
				this.trayIcon.show();
				return;
			}else if(toggle.equals(this.exitRequestCheck)) {
				this.pauseAll();
				this.scannerPool.shutdownNow();
			}
		});
	}
	
	protected void adjustInfoSize(TextArea target) {
		VBox infoWrap = (VBox) target.getParent();
				
		Timeline timeline = new Timeline();
		
		double height = target.getPrefHeight();		
		
		WritableValue<Double> writable = new WritableValue<Double>() {			
			@Override
			public Double getValue() {
				return target.getHeight();
			}

			@Override
			public void setValue(Double value) {
				target.setPrefHeight(value);
				infoWrap.setMaxHeight(value + 45);
				infoWrap.setPrefHeight(infoWrap.getMaxHeight());
			}			
		};
				
		KeyFrame k1 = new KeyFrame(Duration.millis(200), new KeyValue(writable, height));

		timeline.getKeyFrames().addAll(k1);
		timeline.play();
	}
		
	/*
	 * TOOLBAR
	 */
		
	@FXML
	protected ToggleButton autoModeSwitch;
	
	@FXML
	public void autoMode(ActionEvent event) {
		if(this.autoModeSwitch.isSelected()) 
			this.playAll();
		else 
			this.pauseAll();		
	}
	
	public void playAll() {
		if(!SessionStatistics.isConnected()) return;
		
		Thread playAllTask = new Thread(()->{
			DownloadValues downloadValues = null;
						
			while((downloadValues = DownloadManager.startNext(this.observableDownloadList)) != null) {
				
				Download download = downloadValues.getDownload();
				
				synchronized(download) {
					while(!download.isActive()){
						try {
							download.wait(500);
							if(!SessionStatistics.isConnected()) {
								this.autoModeSwitch.setSelected(false);
								return;
							}
						} catch (InterruptedException e) {	}
					}
				}	
			}
			DownloadManager.setStopped(false);
		});

		playAllTask.setName("Play-ALL: 0 active downlaods");
		playAllTask.setDaemon(true);
		playAllTask.start();
	}
	
	public void pauseAll() {
		DownloadManager.setStopped(true);

		Thread pauseAllTask = new Thread(()->{			
			for(DownloadWrapper downloadWrapper : DownloadManager.getDownloads()) {
				if(downloadWrapper.getTask() != null) DownloadManager.pause(downloadWrapper.getTask());
				
				Download download = downloadWrapper.getDownload();
				
				synchronized(download) {
					while(download.isActive()){
						try {
							download.wait(500);
						} catch (InterruptedException e) {	}
					}
				}
			}
		});
	
		pauseAllTask.setName("Pause-ALL");
		pauseAllTask.setDaemon(true);
		pauseAllTask.start();
	}
	
		
	/*
	 * SECTIONS
	 */
	
	
	@FXML
	protected VBox leftSide;
	
	@FXML
	protected Button scanSecBtn, downSecBtn, listSecBtn, statSecBtn, searchSecBtn, settingsSecBtn, historySecBtn;
	
	@FXML
	protected Label scanSecBtnLbl, downSecBtnLbl, listSecBtnLbl, statSecBtnLbl, searchSecBtnLbl, settingsSecBtnLbl, historySecBtnLbl, mediaNameLbl, mediaSizeLbl, mediaSourceLbl, mediaHosterLbl, mediaStateLbl;
		
	public void mainSection() {
		this.changeSection(this.urlScannerPane, this.scanSecBtn);
	}
	
	@FXML
	public void scannerSection(ActionEvent event) {
		this.changeSection(this.urlScannerPane, (Button)event.getSource());
	}

	@FXML
	public void downloadSection(ActionEvent event) {
		this.changeSection(this.downloadPane, (Button)event.getSource());
	}
	
	@FXML
	public void myListSection(ActionEvent event) {
		this.changeSection(this.myListPane, (Button)event.getSource());
	}
	
	@FXML
	public void historySection(ActionEvent event) {		
		this.changeSection(this.historyPane, (Button)event.getSource());
		this.dateView.getSelectionModel().select(0);
		this.historyScrollWrapper.setVvalue(0.0);
		this.historyFactory.setSearchMode(false);
		this.historyFactory.setSelectionMode(false);
	}
	
	@FXML
	public void statSection(ActionEvent event) {
		this.changeSection(this.statPane, (Button)event.getSource());
	}
	
	@FXML
	public void searchSection(ActionEvent event) {
		//this.changeSection(this.searchPane, (Button)event.getSource());
	}
	
	@FXML
	public void settingSection(ActionEvent event) {
		this.changeSection(this.settingsPane, (Button)event.getSource());
	}	
	
	protected void changeSection(Node section, Button target) {
		if(this.root.getCenter().equals(section)) return;		
		
		this.backToSettings(new ActionEvent(this.settingBackTitleBtn, this.settingBackTitleBtn));
		this.closeMediaList();
		this.root.setCenter(section);
		this.cancelForm(true);
		
		if(target != null) {
			this.leftSide.getChildren().stream().filter(child->child.getStyleClass().contains("section_selected")).findFirst().get().getStyleClass().remove("section_selected");
			target.getStyleClass().add("section_selected");
		}
		
		ZStreaming.gcClean(1000);
	}	
	
	
	/*
	 * ANALAYZE URL SECTIOON
	 */
	
	@FXML
	protected GridPane urlScannerPane;
	
	@FXML
	protected HBox urlWrapper, dirChooser;
	
	@FXML
	protected Label urlLabel, downloadDirLbl;
	
	@FXML
	protected TextField urlTextField, downloadDirTxt;
	
	@FXML
	protected Button urlButton, clearNotFoundBtn, clearBtn, downloadDirBtn, openFolderBtn;
		
	@FXML
	protected HBox loadingWrapper, boxBtn;
	
	@FXML
	protected ListView<FoundItem> resultList;
	
	@FXML
	protected VBox mediaInfoWrapper;
	
	@FXML
	protected TextArea name, source, size, hoster, state;
	
	@FXML
	public void enterPressed(KeyEvent event) {
		if(event.getCode().equals(KeyCode.ENTER)){
			if(event.getTarget() instanceof Button)
				((Button)event.getTarget()).fireEvent(new ActionEvent(event.getTarget(), event.getTarget()));
			else {
				((Node)event.getSource()).lookup("Button").fireEvent(new ActionEvent(event.getTarget(), event.getTarget()));
			}
		}		
	}
	
	@FXML
	public void analyzeURL(ActionEvent event) {			
		String url = this.urlTextField.getText();
		
		if(url.isEmpty()) {
		/*
		 * 
		 * MESSAGGIO "URL VUOTO"
		 * 
		 */
			return;
		}
		
		this.urlTextField.clear();
		
		FoundItem foundItem = this.findInList(url);

		if(foundItem != null) {
			this.resultList.getSelectionModel().select(foundItem);
			/*
			 * 
			 * MESSAGGIO "GIA ESISTENTE";
			 * 
			 */
			return;
		}
		
		foundItem = new FoundItem(url);
		
		SessionStatistics.setState("queue", "...");
		
		foundItem.setResultList(this.resultList);
		
		this.resultList.getSelectionModel().select(foundItem);
		this.resultList.scrollTo(foundItem);
		
		URLScannerTask scannerTask = new URLScannerTask(foundItem.getSource(), this.webBrowser.clone());
			
		foundItem.nameProperty().bind(scannerTask.nameProperty());
		foundItem.sourceProperty().bind(scannerTask.sourceProperty());
		foundItem.getNameTooltip().textProperty().bind(scannerTask.nameProperty());
		foundItem.stateProperty().bind(Bindings.createStringBinding(()->this.bundleFactory.getString(scannerTask.stateTextProperty().get()), scannerTask.stateTextProperty()));

		final FoundItem _foundItem = foundItem; 
		
		foundItem.setOnStopAction(e->this.stopSearch(e, scannerTask, _foundItem));
		foundItem.setOnRemoveItemAction(e->this.removeFoundItem(e, scannerTask, _foundItem));

		this.startSearch(foundItem, scannerTask);
	}
	
	private void startSearch(FoundItem foundItem, URLScannerTask scannerTask) {		
		scannerTask.onDoneProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				if(newValue) {
					URLScannerTask.State state = scannerTask._getState();
					switch(state) {
						case SUCCESSED: 
							Media media = scannerTask.getMedia();

							foundItem.setMedia(media);
							foundItem.foundLayout();
							foundItem.setOnDownloadAction(e->addDownload(e, media, scannerTask.getBrowser()));
							foundItem.setOnStreamingAction(e->streamingFromResultList(e));
							foundItem.setOnAddToListAction(e->showAddToListPane(e, media));
							
							SessionStatistics.setState("scan.completed");
							break;
						case STOPPED:
							SessionStatistics.setState("interrupted.operation");
						case FAILED:
							scannerTask.setStateText("plugin.not.found");
							foundItem.notFoundLayout();
							break;
						default:
							return;		
					}
					
					Media media = scannerTask.getMedia() == null ? new Media() : scannerTask.getMedia();

					media.setAvalaible(scannerTask.isFound());

					try {
						if(media.getSource() == null) {
							media.setSource(URLValidator.validateURL(foundItem.getSource()));
						}
						mediaHistory.addEntry(URLValidator.validateURL(foundItem.getSource()).toString(), media , state);
					} catch (MalformedURLException e1) {}					
					
					resultList.getSelectionModel().clearSelection();
					resultList.getSelectionModel().select(foundItem);
					
					scannerTask.onDoneProperty().removeListener(this);
				}
			}
		});
		
		this.scannerPool.submit(scannerTask);		
	}
	
	public void stopSearch(ActionEvent event, URLScannerTask scannerTask, FoundItem foundItem) {
		((Button)event.getSource()).setDisable(true);
		this.stopScannerTask(scannerTask, foundItem);
	}

	public void removeFoundItem(ActionEvent event, URLScannerTask scannerTask, FoundItem foundItem) {
		this.stopScannerTask(scannerTask, foundItem);
		this.resultList.getItems().remove(foundItem);
		ZStreaming.gcClean(2000);
	}	
		
	public void addDownload(ActionEvent event, Media media, WebBrowser browser) {
		DownloadValues downloadValues = this.createDownloadValues(media, browser);		
		this.changeSection(this.downloadPane, this.downSecBtn);
		
		if(downloadValues != null) {
			if(!this.observableDownloadList.contains(downloadValues)) {
				this.observableDownloadList.add(downloadValues);

				SessionStatistics.setState("add.to.download.list");

				DownloadTask task = DownloadManager.createDownloadTaskOnPlatform(downloadValues, observableDownloadList);
				
				if(task.isStartable()) {
					DownloadManager.start(task);
				}
			}
			
			this.downloadTableView.getSelectionModel().clearSelection();
			this.downloadTableView.getSelectionModel().select(downloadValues);
			this.downloadTableView.scrollTo(downloadValues);
		}
	}
	
	private DownloadValues createDownloadValues(Media media, WebBrowser browser) {
		DownloadValues downloadValues = null;
		
		if(media != null) {
			
			if((downloadValues = (DownloadValues) this.observableDownloadList.stream().filter(dv->dv.getDownload().getMedia().getSource().sameFile(media.getSource())).findFirst().orElse(null)) != null){
				/*
				 * MESSAGGIO "DOWNLOAD GIA' PRESENTE"
				 * 	
				 */
				
			}else {
				String path = ZStreaming.getSettingManager().getSettings().get(SettingsManager.DOWNLOAD_PATH);
				Download download = new Download(media, browser, path);
				
				downloadValues = new DownloadValues(download);
				download.stateProperty().addListener(new DownloadListener(downloadValues, this.observableDownloadList, this.chartCounter));
			}
		}

		return downloadValues;
	}
	
	private void stopScannerTask(URLScannerTask scannerTask, FoundItem foundItem) {
		scannerTask.interrupt();		
		foundItem.getStyleClass().add("stopped");
	}
	
	
	@FXML
	public void outResultList(MouseEvent event){
		try {
			if(this.resultList.getItems().size() > 0 && ((Node)event.getTarget()).getParent().getParent().getParent().getParent() instanceof ListView){
				this.resultList.getSelectionModel().clearSelection();
			}
		}catch(NullPointerException ex) {}
	}
		
	@FXML
	public void clearAll(ActionEvent event) {		
		List<FoundItem> list = new ArrayList<FoundItem>();		
		
		for(FoundItem item : this.resultList.getItems()) {
			if(item.getStyleClass().contains("found") || item.getStyleClass().contains("not_found")) 
				list.add(item);	
		}
		
		this.resultList.getItems().removeAll(list);
		
		if(this.resultList.getItems().size() == 0) this.urlTextField.requestFocus();
		
		ZStreaming.gcClean(1000);
	}
	
	@FXML
	public void clearNotFound(ActionEvent event) {
		List<FoundItem> list = new ArrayList<FoundItem>();		
		
		for(FoundItem item : this.resultList.getItems()) {
			if(item.isNotFound())
				list.add(item);	
		}
		
		this.resultList.getItems().removeAll(list);
		
		if(this.resultList.getItems().size() == 0) this.urlTextField.requestFocus();
		
		ZStreaming.gcClean(1000);
	}
	
	private void setResults(FoundItem foundItem) {
		this.clearBtn.setDisable(this.resultList.getItems().stream().noneMatch(item->item.isFound() || item.isNotFound()));
		this.clearNotFoundBtn.setDisable(this.resultList.getItems().stream().noneMatch(item->item.isNotFound()));
		
		if(foundItem != null && this.resultList.getItems().contains(foundItem)) {
			Media media = foundItem.getMedia();
			
			if(media != null) {
				this.name.setText(media.getName() == null ? "-" : media.getName());
				this.size.setText(media.getSize().getRealSize() < 1 ? "-" : media.getSize().optimizeSize());
				this.source.setText(media.getSource() == null ? foundItem.getSource() : media.getSource().toString());
				this.hoster.setText(media.getHoster() == null ? "-" : media.getHoster());
				this.state.setText(media.getSource() == null ? "-" : "Online");
			}else {
				this.name.setText("-");
				this.size.setText("-");
				this.source.setText(foundItem.getSource());
				this.hoster.setText("-");
				this.state.setText("-");
			}
		}else {
			this.name.setText("");
			this.size.setText("");
			this.source.setText("");
			this.hoster.setText("");
			this.state.setText("");
		}
	}
	
	private FoundItem findInList(String url) {
		return this.resultList.getItems().stream().filter(item->item.getSource().equals(url) && !item.isStopped()).findFirst().orElse(null);
	}
	
	public void showAddToListPane(ActionEvent event, Media media) {
		if(media != null) {
			Stage addToListStage = new Stage();
			List<MediaListButton> list = new ArrayList<>();
			this.listContainerItems.stream().filter(item->item instanceof MediaListButton).forEach(item->list.add((MediaListButton) item));
			
			MediaListView mediaListView = new MediaListView(list);
			
			mediaListView.setNewListAction(e->{
				this.addNewList(e, media);
				mediaListView.closeWindow();				
			});
			
			mediaListView.setOnAction(e->{
				this.addToList(mediaListView.getSelectedMediaList(), media);
				mediaListView.closeWindow();
			});
			
			mediaListView.setOnCloseRequest(e->mediaListView.closeWindow());
			
			addToListStage.setScene(new Scene(mediaListView, 400, 580));
			addToListStage.getScene().setFill(null);
			addToListStage.initStyle(StageStyle.TRANSPARENT);
			addToListStage.initModality(Modality.APPLICATION_MODAL);
			addToListStage.initOwner(this.root.getScene().getWindow());
			addToListStage.centerOnScreen();
			addToListStage.sizeToScene();
			
			mediaListView.showWindow(addToListStage);
		}
	}


	public void addNewList(ActionEvent event, Media media) {
		if(media != null) {	
			try {			
				this.showAddForm();
				this.changeSection(this.myListPane, this.listSecBtn);
				this.formAddListBtn.setContentMedialist(media);
			} catch (IOException e) { 
				e.printStackTrace();
			}
		}
	}
	
	private void addToList(MediaList mediaList, Media media) {
		if(mediaList != null && media != null) {
			List<Media> mediaListContent = mediaList.getContent();
			
			final Media _media = media;
			
			if(mediaListContent.stream().anyMatch(m->m.sameMedia(_media))) {
				/*
				 * 
				 * DIALOG WINDOW MEDIALIST ALREADY CONTAINS MEDIA
				 * 
				 */
				System.out.println("Media già esistente");
				if(media.equals(media))	media = media.clone();
			}
			
			mediaList.addMedia(media.getMediaList() != null ? media.clone() : media);
			mediaList.store();
		}
	}
	
	@FXML
	public void streamingFromResultList(ActionEvent event) {
		Media media = this.resultList.getSelectionModel().getSelectedItem().getMedia();
		
		this.streaming(media);
	}
	
	private void streaming(Media media) {
		
		final String playerKey = "player.default";
		String playerLocation = ZStreaming.getSettingManager().getSettings().get(playerKey);
		
		if(playerLocation == null) {
			FileChooser chooser = new FileChooser();
			ExtensionFilter filter = new ExtensionFilter("Executable type (*.exe)", "*.exe");

			chooser.setSelectedExtensionFilter(filter);
			chooser.setTitle("player.file.chooser");
			chooser.setInitialDirectory(new File("").getAbsoluteFile());
			
			File player = null;
			
			if((player = chooser.showOpenDialog(this.primaryStage)) != null)
				playerLocation = player.toString();
			else
				return;
			
			Alert alert = new Alert(AlertType.CONFIRMATION, String.format("%s \"%s\"?", ObservableResourceBundle.getLocalizedString("player.choose"), playerLocation), ButtonType.YES, ButtonType.NO);
			alert.setHeaderText(null);
			alert.getDialogPane().setPrefSize(500, 100);
			
			alert.initOwner(this.primaryStage);
			
			Optional<ButtonType> result = alert.showAndWait();
			
			if(result.get().equals(ButtonType.YES)) {
				ZStreaming.getSettingManager().storeSettings(playerKey, playerLocation);
				this.playerPathText.setText(playerLocation);
			}
		}		
		
		ProcessBuilder pb = new ProcessBuilder(playerLocation, media.getMRL().toString());
		
		try {							
			pb.start();	        
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@FXML
	public void browse(ActionEvent event) {		
		this.chooseDir(this.downloadDirTxt, SettingsManager.DOWNLOAD_PATH);
	}

	@FXML
	public void openDownloadFolder(ActionEvent event) {
		this.openFolder(this.downloadDirTxt.getText(), SettingsManager.DOWNLOAD_PATH);
	}
	
	/*
	 * DOWNLOAD SECTION
	 */
	
	
	@FXML
	protected SplitPane downloadPane;
	
	@FXML
	protected TableView<DownloadValues> downloadTableView;
		
	@FXML
	protected TableColumn<DownloadValues, Integer> indexCol;
	
	@FXML
	protected TableColumn<DownloadValues, Label> nameCol, stateCol, sizeCol, speedCol, workDoneCol, sizeRemainCol, timeRemainCol;
	
	@FXML
	protected TableColumn<DownloadValues, Text> hosterCol, pathCol;
	
	@FXML
	protected TableColumn<DownloadValues, StackPane> progressCol;
		
	@FXML
	protected TableColumn<DownloadValues, PriorityIndicator> priorityCol;
		
	@FXML
	protected Label actDurationLblHead, actDurationLbl, speedLblHead, speedLbl, downloadedLblHead, downloadedLbl, remainingTimeLblHead, remainingTimeLbl,
					remainingSizeLbl, remainingSizeLblHead, lastActLbl, lastActLblHead, priorityLbl, priorityLblHead, startActLbl, startActLblHead,
					nameInfoLbl, nameInfoLblHead, mimeInfoLbl, mimeInfoLblHead, sourceInfoLbl, sourceInfoLblHead, sizeInfoLbl, sizeInfoLblHead,
					segInfoLbl, segInfoLblHead, pathInfoLbl, pathInfoLblHead, progPercent;
			
	@FXML
	protected VBox infoContent, infoTabContent, downloadListContainer;
	
	@FXML
	protected TabPane tabPaneDownload;
	
	@FXML
	protected HBox progressSect;
	
	@FXML
	protected ProgressBar progBar;
	
	@FXML
	protected Tab infoTab, statTab;
	
	@FXML
	protected Fieldset tansferWrapper, mediaWrapper;
	
	@FXML
	protected Label progressSectText;
	
	protected Button hideTabBtn;
	
	public void initDownloadList(ObservableList<DownloadValues> observableDownloadList) {
		this.observableDownloadList = observableDownloadList;
		this.downloadTableView.setItems(this.observableDownloadList);		
		SessionStatistics.bind(this.observableDownloadList);		
		
		this.resetInfoDownload();
		
		this.downloadTableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);	
		DownloadViewContextMenu downloadContextMenu = new DownloadViewContextMenu(this.downloadTableView);
		downloadContextMenu.getController().setObservableBundleFactory(this.bundleFactory);
		this.downloadTableView.setContextMenu(downloadContextMenu);		

		this.indexCol.setCellValueFactory(cellData->cellData.getValue().getIndex());
		this.nameCol.setCellValueFactory(new PropertyValueFactory<>("nameLabel"));
		this.stateCol.setCellValueFactory(new PropertyValueFactory<>("stateLabel"));
		this.progressCol.setCellValueFactory(new PropertyValueFactory<>("progressWrapper"));
		this.sizeCol.setCellValueFactory(new PropertyValueFactory<>("sizeLabel"));
		this.speedCol.setCellValueFactory(new PropertyValueFactory<>("speedLabel"));
		this.workDoneCol.setCellValueFactory(new PropertyValueFactory<>("workDoneLabel"));
		this.sizeRemainCol.setCellValueFactory(new PropertyValueFactory<>("sizeRemainLabel"));
		this.timeRemainCol.setCellValueFactory(new PropertyValueFactory<>("timeRemainLabel"));
		this.priorityCol.setCellValueFactory(new PropertyValueFactory<>("priorityIndicator"));
		this.hosterCol.setCellValueFactory(new PropertyValueFactory<>("hosterText"));
		this.pathCol.setCellValueFactory(new PropertyValueFactory<>("pathText"));
		
		this.nameCol.setComparator((x,y)->x.getText().compareTo(y.getText()));
		this.stateCol.setComparator((x,y)->x.getText().compareTo(y.getText()));
		this.progressCol.setComparator((x,y)->Double.compare(((SimpleDoubleProperty)x.getUserData()).get(), ((SimpleDoubleProperty)y.getUserData()).get()));
		this.speedCol.setComparator((x,y)->Double.compare(((SizePerSecond)x.getUserData()).getRealSize(), ((SizePerSecond)y.getUserData()).getRealSize()));
		this.sizeCol.setComparator((x,y)->Double.compare(((Size)x.getUserData()).getRealSize(), ((Size)y.getUserData()).getRealSize()));
		this.workDoneCol.setComparator((x,y)->Double.compare(((Size)x.getUserData()).getRealSize(), ((Size)y.getUserData()).getRealSize()));
		this.sizeRemainCol.setComparator((x,y)->Double.compare(((Size)x.getUserData()).getRealSize(), ((Size)y.getUserData()).getRealSize()));
		this.timeRemainCol.setComparator((x,y)->Long.compare(((TimeAdjuster)x.getUserData()).getSeconds(), ((TimeAdjuster)y.getUserData()).getSeconds()));
		this.priorityCol.setComparator((x,y)->Double.compare(((SimpleDoubleProperty)x.getUserData()).get(), ((SimpleDoubleProperty)y.getUserData()).get()));
		this.hosterCol.setComparator((x,y)->x.getText().compareTo(y.getText()));
		this.pathCol.setComparator((x,y)->x.getText().compareTo(y.getText()));
						
		this.downloadTableView.getItems().addListener(new ListChangeListener<DownloadValues>() {
			@Override
			public void onChanged(Change<? extends DownloadValues> values) {
				if(observableDownloadList.size() > 0) {
					//refreshDownloadView();
					observableDownloadList.forEach(item->item.setIndex(observableDownloadList.indexOf(item) + 1));
				}
			}
		});
	
		this.downloadTableView.setRowFactory(rowFactory->{
			return new DownloadTableRow();
		});
		
		TableHeaderContextMenu headerContextMenu = new TableHeaderContextMenu(this.downloadTableView);
		
		this.downloadTableView.skinProperty().addListener((observable, oldSkin, newSkin)->{
			Node header = this.downloadTableView.lookup(".column-header-background");
			 header.setOnContextMenuRequested(e->{
				headerContextMenu.show(header, e.getScreenX(),  e.getScreenY());
				e.consume();
			}); 				
			ZStreaming.gcClean(5000);
		});

		this.updateSelectionListener();
	}
		
	protected void refreshDownloadView() {
		this.downloadTableView.refresh();
		ZStreaming.gcClean(1000);
	}
	
	private void updateSelectionListener() {
		this.downloadTableView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue)->{			
			this.bindInfoDownload(newValue);
			this.bindStatDownload(newValue);
		});
	}
	
	@FXML
	public void outDownloadList(MouseEvent event) {
		if(((Node)event.getTarget()).getParent() instanceof TableRow) {
			@SuppressWarnings("unchecked")
			TableRow<DownloadValues> row = ((TableRow<DownloadValues>)((Node)event.getTarget()).getParent());
			if(!row.isEmpty()) return;
		}else
			return;
		
		this.downloadTableView.getSelectionModel().clearSelection();
	}
	
	private void hideTab(ActionEvent event) {		
		Text text = ((Text)this.hideTabBtn.getGraphic());

		if(text.getText().equals("«")) {
			text.setText("»");
			this.tabPaneDownload.setMaxHeight(-1.0);
			this.setTabPaneHeight(this.tabPaneDownload.getSelectionModel().getSelectedItem());
			this.downloadPane.setDividerPositions(this.getDividerPositions(this.downloadPane.getHeight()));
			this.tabPaneDownload.getTabs().forEach(tab->tab.setStyle("-fx-scale-x:1.0;"));
			this.downloadPane.lookup(".split-pane-divider").setDisable(false);
		}else if(text.getText().equals("»")) {
			text.setText("«");
			this.tabPaneDownload.setMaxHeight(29.0);
			this.tabPaneDownload.setMinHeight(-1.0);
			this.tabPaneDownload.getTabs().forEach(tab->tab.setStyle("-fx-scale-x:.0;"));
			this.downloadPane.lookup(".split-pane-divider").setDisable(true);
		}
	}
	
	/*
	 * 
	 * Model
	 * 
	 */
	private double[] getDividerPositions(double height) {
		double[] dividers = null;
		double sp2Height = this.tabPaneDownload.getPrefHeight();
		if(sp2Height > 0 && this.tabPaneDownload.getMaxHeight() > 0) {
			double div = sp2Height/height;
			dividers = new double[] {1.0 - div, div};
		}else {
			dividers = new double[] {.46, .54};
		}		
		return dividers;
	}
	
	/*
	 * 
	 * Model
	 * 
	 */
	private void setTabPaneHeight(Tab selectedTab) {	
		if(this.infoTab.equals(selectedTab)) {
			this.tabPaneDownload.setMinHeight(76.0);
			this.tabPaneDownload.setMaxHeight(84.0 + Math.max(273.0, this.infoContent.getHeight()));
		}else if(this.statTab.equals(selectedTab)) {
			this.tabPaneDownload.setMinHeight(215.0);
			this.tabPaneDownload.setMaxHeight(400.0);
		}		
	}


		/*
		 *InfoTab
		 */
		
	public void bindInfoDownload(DownloadValues downloadValues) {
		if(downloadValues != null) {
			if(downloadTableView.getSelectionModel().getSelectedIndices().size() > 1) return;
			
			int index = this.observableDownloadList.indexOf(downloadValues);	

			this.progressListener(downloadValues, index);
			this.speedListener(downloadValues, index);
			this.workDoneListener(downloadValues, index);
			this.remainingTimeListener(downloadValues, index);
			this.remainingSizeListener(downloadValues, index);
			this.priorityListener(downloadValues, index);
			this.mediaInfo(downloadValues, index);
			
			downloadValues.getActivity().bindActivity(this.downloadTableView.getSelectionModel(), index, this.startActLbl, this.lastActLbl, this.actDurationLbl);
		}else
			this.resetInfoDownload();
	}
	

	private void progressListener(DownloadValues downloadValues, int index) {
		this.progBar.getStyleClass().removeAll("completed");

		if(downloadValues.getDownload().isInterrupted()) {
			this.progBar.setProgress(0.0);
			this.progPercent.setText("-");
		}else {
			if(downloadValues.getDownload().isCompleted() && !this.progBar.getStyleClass().contains("completed")) this.progBar.getStyleClass().add("completed");
			this.progBar.setProgress(downloadValues.getProgress().get());
			this.progPercent.setText(String.format("%.1f", (downloadValues.getProgress().get()*100)) + "%");
		}
		
		downloadValues.getProgress().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> progress, Number oldValue, Number newValue) {
				if(downloadTableView.getSelectionModel().isSelected(index)) {
					if(newValue.doubleValue() < 0.0) {
						progBar.setProgress(0.0);
						progPercent.setText("0.0%");
					}else{						
						if(downloadValues.getDownload().isCompleted() && !progBar.getStyleClass().contains("completed")) progBar.getStyleClass().add("completed");						
						progBar.setProgress(newValue.doubleValue());
						progPercent.setText(String.format("%.1f", (downloadValues.getProgress().get()*100)) + "%");
					}
				}else
					downloadValues.getProgress().removeListener(this);
			}
		});				
	}

	private void speedListener(DownloadValues downloadValues, int index) {		
		this.speedLbl.setText(" -");
		
		downloadValues.getSpeed().sizeProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				String val = newValue.isEmpty() ? " -" : newValue;
				
				if(downloadTableView.getSelectionModel().isSelected(index))
					speedLbl.setText(val);		
				else
					downloadValues.getSpeed().sizeProperty().removeListener(this);
			}			
		});		
	}

	private void remainingTimeListener(DownloadValues downloadValues, int index) {
		String value = downloadValues.getDownload().isDone() ? " -" : " ∞";
		
		this.remainingTimeLbl.setText(value);
		
		downloadValues.getTimeRemain().timeProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				String val = downloadValues.getDownload().isDone() ? " -" : !downloadValues.getDownload().isActive() ? " ∞" : newValue;

				if(downloadTableView.getSelectionModel().isSelected(index))
					remainingTimeLbl.setText(val);
				else
					downloadValues.getTimeRemain().timeProperty().removeListener(this);
			}
			
		});
	}
	
	private void workDoneListener(DownloadValues downloadValues, int index) {
		String value = downloadValues.getDownload().isCompleted() ? downloadValues.getSize().optimizeSize() : downloadValues.getWorkDone() == null || downloadValues.getWorkDone().optimizeSize().isEmpty() ? " -" : downloadValues.getWorkDone().optimizeSize();
		
		this.downloadedLbl.setText(value);
		
		downloadValues.getWorkDone().sizeProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				String val = newValue.isEmpty() ? " -" : newValue;

				if(downloadTableView.getSelectionModel().isSelected(index))
					downloadedLbl.setText(val);	
				else
					downloadValues.getWorkDone().sizeProperty().removeListener(this);
			}			
		});
	}
	
	private void remainingSizeListener(DownloadValues downloadValues, int index) {		
		String value = downloadValues.getSizeRemain() != null && !downloadValues.getSizeRemain().optimizeSize().isEmpty() ?  downloadValues.getSizeRemain().optimizeSize() : " -";
				
		this.remainingSizeLbl.setText(value);
		
		downloadValues.getSizeRemain().sizeProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				String val = newValue.isEmpty() ? " -" : newValue;

				if(downloadTableView.getSelectionModel().isSelected(index))
					remainingSizeLbl.setText(val);	
				else
					downloadValues.getSizeRemain().sizeProperty().removeListener(this);
			}			
		});
	}	

	private void priorityListener(DownloadValues downloadValues, int index) {		
		this.priorityLbl.setText(downloadValues.getPriority().get() < 0 ? " -" : downloadValues.getDownload().getPriority().getName());
		
		downloadValues.getPriority().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				String val = newValue.intValue() < 0 ? " -" : Download.Priority.valueOf(newValue.intValue()).getName();

				if(downloadTableView.getSelectionModel().isSelected(index))
					priorityLbl.setText(val);
				else
					downloadValues.getPriority().removeListener(this);
			}			
		});
	}

	private void mediaInfo(DownloadValues downloadValues, int index) {		
		if(downloadTableView.getSelectionModel().isSelected(index)) {			
			Media media = downloadValues.getDownload().getMedia();						
			this.nameInfoLbl.setText(media.getName());
			this.mimeInfoLbl.setText(media.getMimeType());
			this.sourceInfoLbl.setText(media.getSource().toExternalForm());
			this.sizeInfoLbl.setText(media.getSize().optimizeSize());
			this.segInfoLbl.setText(" -");//SEGMENTS
			this.pathInfoLbl.setText(downloadValues.getDownload().isCompleted() ? downloadValues.getDownload().getDestination().getAbsolutePath() : downloadValues.getDownload().getDestination().getAbsolutePath().split(DownloadTask.DOWNLOAD_EXTENTION)[0]);
			downloadValues.getNameLabel().textProperty().addListener((observable, oldValue, newValue)->	this.pathInfoLbl.setText(downloadValues.getPath() + "\\" +  downloadValues.getName()));
			downloadValues.getPathText().textProperty().addListener((observable, oldValue, newValue)->	this.pathInfoLbl.setText(downloadValues.getPath() + "\\" +  downloadValues.getName()));
		}
	}
	
	protected void resetInfoDownload() {
		this.tabPaneDownload.setMaxHeight(this.infoTab.isSelected() ? 357.0 : 400.0);
		this.progBar.getStyleClass().remove("completed");
		this.progPercent.setText("-");
		this.progBar.setProgress(0.0);
		this.actDurationLbl.setText(" -");
		this.speedLbl.setText(" -");
		this.downloadedLbl.setText(" -");
		this.remainingTimeLbl.setText(" -");
		this.remainingSizeLbl.setText(" -");
		this.lastActLbl.setText(" -");
		this.priorityLbl.setText(" -");
		this.startActLbl.setText(" -");
		this.nameInfoLbl.setText(" -");
		this.mimeInfoLbl.setText(" -");
		this.sourceInfoLbl.setText(" -");
		this.sizeInfoLbl.setText(" -");
		this.segInfoLbl.setText(" -");
		this.pathInfoLbl.setText(" -");
	}
	
	
		/*
		 *Statistics Tab 
		 */
	
	@FXML
	protected VBox statTabContent;
	
	protected StatisticsChart singleChart;
	
	private void bindStatDownload(DownloadValues downloadValues) {
		if(this.singleChart.isBound()) this.singleChart.unbind();

		if(downloadValues != null && DownloadActivity.isSingleChartActive()) {
			if(this.downloadTableView.getSelectionModel().getSelectedIndices().size() > 1) return;
			
			int index = this.observableDownloadList.indexOf(downloadValues);	
			
			if(this.downloadTableView.getSelectionModel().isSelected(index)) {
				this.singleChart.bind(downloadValues.getActivity());
			}				
		}
	}
	
	/*
	 * STATISTICS SECTION
	 */
	
	@FXML
	protected SplitPane statPane;
	
	@FXML
	protected VBox statistcsTreeContainer;
	
	@FXML
	protected GridPane statisticsContainer;
			
	@FXML
	protected TreeView<String> treeStat;
	
	@FXML
	protected TreeItem<String> totTreeItem, actTotItem, totCompleted, totDownloaded, totSpeed,
								down0, down1, down2, down3, 
								downloaded0, downloaded1, downloaded2, downloaded3,
								remain0, remain1, remain2, remain3,
								speed0, speed1, speed2, speed3,
								remainTime0, remainTime1, remainTime2, remainTime3;
	
	@FXML
	protected Label actTotItemHead, totCompletedHead, totDownloadedHead, totSpeedHead,
					down0Head, down1Head, down2Head, down3Head, 
					downloaded0Head, downloaded1Head, downloaded2Head, downloaded3Head,
					remain0Head, remain1Head, remain2Head, remain3Head,
					speed0Head, speed1Head, speed2Head, speed3Head,
					remainTime0Head, remainTime1Head, remainTime2Head, remainTime3Head;
		
	protected StatisticsChart mainChart, chart0, chart1, chart2, chart3;
	
	private StatisticsChart[] listCharts;
	
	private Thread mainChartTask;
	
	private void createStatisticsCharts() {
		this.singleChart = new StatisticsChart(6, true, true, this.bundleFactory);
		this.mainChart = new StatisticsChart("Sessione corrente", 6, true, false, this.bundleFactory);
		this.chart0 = new StatisticsChart("Download #0", 5, false, true, this.bundleFactory);
		this.chart1 = new StatisticsChart("Download #1", 5, false, true, this.bundleFactory);
		this.chart2 = new StatisticsChart("Download #2", 5, false, true, this.bundleFactory);
		this.chart3 = new StatisticsChart("Download #3", 5, false, true, this.bundleFactory);
		
		this.chart0.setTickUnitX(Integer.MAX_VALUE);
		this.chart1.setTickUnitX(Integer.MAX_VALUE);
		this.chart2.setTickUnitX(Integer.MAX_VALUE);
		this.chart3.setTickUnitX(Integer.MAX_VALUE);
		
		this.mainChart.setId("mainChart");
		this.chart0.setId("chart0");
		this.chart1.setId("chart1");
		this.chart2.setId("chart2");
		this.chart3.setId("chart3");
		this.singleChart.setId("singleChart");

		this.listCharts = new StatisticsChart[] {this.chart0, this.chart1, this.chart2, this.chart3};
				
		GridPane.setRowIndex(this.chart0, 0);
		GridPane.setColumnIndex(this.chart0, 0);
		
		GridPane.setRowIndex(this.chart1, 0);
		GridPane.setColumnIndex(this.chart1, 1);

		GridPane.setRowIndex(this.chart2, 1);
		GridPane.setColumnIndex(this.chart2, 0);

		GridPane.setRowIndex(this.chart3, 1);
		GridPane.setColumnIndex(this.chart3, 1);

		GridPane.setRowIndex(this.mainChart, 2);
		GridPane.setColumnIndex(this.mainChart, 0);
		GridPane.setColumnSpan(this.mainChart, 2);

		
		this.statTab.setContent(this.singleChart);
		this.statisticsContainer.getChildren().addAll(this.listCharts);
		this.statisticsContainer.getChildren().add(this.mainChart);
							
		if(this.mainChartCheck.isSelected()) {
			this.activeMainChart();
		}
	}
		
	private void initTreeItems() {
		Callback<TreeView<String>, TreeCell<String>> cellFactory = TextFieldTreeCell.forTreeView();

		this.treeStat.setCellFactory((TreeView<String> treeView) -> {
		    TreeCell<String> cell = cellFactory.call(treeView);
		    cell.treeItemProperty().addListener((observable, oldValue, newValue) -> {
		        if (newValue != null) {
			    	if(newValue.equals(this.totTreeItem) ) {
			    		if(!cell.getStyleClass().contains("bold-text"))
			    			cell.getStyleClass().add("bold-text");
			    	}else {
			    		cell.getStyleClass().removeAll("bold-text");
			    	}			    	
		        }		           
		    });
		    
		    cell.prefWidthProperty().bind(this.statistcsTreeContainer.widthProperty().subtract(10));
		    		    
		    cell.setOnMouseClicked(e->{
		    	if(cell.isEmpty()) this.treeStat.getSelectionModel().clearSelection();
		    });
		    
		    return cell;
		});
						
		this.bindStatTreeItems();
	}
	
	private void bindStatTreeItems() {	
		DownloadManager.activeProperty().addListener((observable, oldValue, newValue)->{
			long waitCount = DownloadManager.getDownloads().stream().filter(dw->dw.getDownload().isWaitinig()).count();	
			String waiting =  waitCount > 0 ? " (" + waitCount + " in attesa)" : "";
			
			if(!waiting.equals(this.actTotItem.getValue().replaceFirst(String.format("%d", DownloadManager.getActive()), ""))){
				this.actTotItem.setValue(DownloadManager.getActive() + waiting);
			}			
		});
		
		this.mainChart.getFirstSeries().getData().addListener(new ListChangeListener<Object>() {
			@Override
			public void onChanged(Change<? extends Object> value) {
				long waitCount = DownloadManager.getDownloads().stream().filter(dw->dw.getDownload().isWaitinig()).count();				
				String waiting =  waitCount > 0 ? " (" + waitCount + " in attesa)" : "";
				
				if(!waiting.equals(actTotItem.getValue().replaceFirst(String.format("%d", DownloadManager.getActive()), ""))){					
					actTotItem.setValue(DownloadManager.getActive() + waiting);
				}	
			}			
		});
		
		SessionStatistics.stateProperty().addListener((observable, oldValue, newValue)->{
			if(newValue != null && newValue.toLowerCase().contains(this.bundleFactory.getString("completed")) && !newValue.toLowerCase().contains("..."))
				this.totCompleted.setValue(String.format("%d", Integer.parseInt(this.totCompleted.getValue()) + 1));
		});
		
		this.totSpeed.valueProperty().bind(SessionStatistics.speedProperty());
		this.totDownloaded.valueProperty().bind(SessionStatistics.workDoneProperty());
		
		this.bindItemDownload(this.down0, this.chart0, "#0");
		this.bindItemDownload(this.down1, this.chart1, "#1");
		this.bindItemDownload(this.down2, this.chart2, "#2");
		this.bindItemDownload(this.down3, this.chart3, "#3");
	}
		
	
	private void bindItemDownload(TreeItem<String> treeItem, StatisticsChart chart, String index) {
		
		try {
			if(treeItem.getChildren().size() != 4) throw new IllegalArgumentException();
			
			TreeItem<String> workDone = treeItem.getChildren().get(0);
			TreeItem<String> remainSize = treeItem.getChildren().get(1);
			TreeItem<String> remainTime = treeItem.getChildren().get(2);
			TreeItem<String> speed = treeItem.getChildren().get(3);
			
			Label nameLbl = ((Label)treeItem.getGraphic().lookup(".tree-value-head"));

			treeItem.getGraphic().disableProperty().bind(chart.disableProperty());
			
			treeItem.valueProperty().bind(Bindings.when(chart.titleProperty().isNotEqualTo("Download "+ index)).then(chart.titleProperty()).otherwise(""));
			nameLbl.textProperty().bind(Bindings.when(chart.titleProperty().isEqualTo("Download " + index)).then(chart.titleProperty()).otherwise(index.substring(1) + ":"));
			
			chart.boundProperty().addListener((observable, oldValue, newValue)->{
				if(newValue)  {
					DownloadValues values = this.observableDownloadList.stream().filter(downloadValues->downloadValues.getActivity().equals(chart.getActivityBound())).findFirst().orElse(null);
					if(values != null) {
						workDone.valueProperty().bind(values.getWorkDone().sizeProperty());
						remainSize.valueProperty().bind(values.getSizeRemain().sizeProperty());
						speed.valueProperty().bind(values.getSpeed().sizeProperty());
						remainTime.valueProperty().bind(values.getTimeRemain().timeProperty());
					}
				}else {
					workDone.valueProperty().unbind();
					workDone.setValue("0 byte");
					remainSize.valueProperty().unbind();
					remainSize.setValue("0 byte");
					speed.valueProperty().unbind();
					speed.setValue("0 byte/s");
					remainTime.valueProperty().unbind();
					remainTime.setValue("-");
					
				}
			});			
			
		}catch(IllegalArgumentException e) {
			e.printStackTrace();
		}
	}
	
	public void activeMainChart() {
		this.mainChartTask = new Thread(()->{
			try {
				while(!Thread.currentThread().isInterrupted()) {
					long del = 0L;
					
					try {
						del = Long.parseLong(ZStreaming.getSettingManager().getSettings().get("update.chart.delay"));
					}catch(NumberFormatException ex) { 
						del = 5;
					}
					
					final long delay = del;
					
					Platform.runLater(()->this.mainChart.getFirstSeries().getData().add(new Data<Number, Number>(delay, SessionStatistics.getBpsOptimizer().getRealSize())));

					
					synchronized(Thread.currentThread()) {
						Thread.currentThread().wait(delay*1000);
					}
									
					Platform.runLater(()->this.mainChart.getFirstSeries().getData().forEach(data->data.setXValue(data.getXValue().doubleValue() - (double) delay)));
				}
			}catch(InterruptedException e) { }
		});
				
		this.mainChartTask.setDaemon(true);
		this.mainChartTask.setName("MainChartTask");
		this.mainChartTask.start();
	}
	
	public void deactiveMainChart() {
		this.mainChartTask.interrupt();
		this.mainChartTask = null;
		ZStreaming.gcClean(1500);
	}
	
	public void resetChart(StatisticsChart... charts) {
		for(StatisticsChart chart : charts) {
			chart.getFirstSeries().getData().clear();		
		}
		ZStreaming.gcClean(1000);
	}

	
	/*
	 * STATE BAR 
	 */
	
	@FXML
	protected HBox stateBar;

	@FXML
	protected GridPane bottom;
	
	@FXML
	protected Region bottomLeft;
	
	@FXML
	protected Label stateFooter, modeFooter, downFooter, speedFooter, connFooter;
	
	private void initStateBar() {
		this.stateFooter.textProperty().bind(SessionStatistics.stateProperty());
		this.downFooter.textProperty().bind(Bindings.concat("Down: ", SessionStatistics.workDoneProperty()));
		this.speedFooter.textProperty().bind(Bindings.concat("Speed: ", SessionStatistics.speedProperty()));		
		
		DownloadManager.stopProperty().addListener((observable, oldValue, newValue)->{
			this.autoModeSwitch.setSelected(!newValue);
			
			Platform.runLater(()->{
				if(newValue) {
					this.modeFooter.setText("Mode: Manual");
					((ImageView)this.modeFooter.getGraphic()).setImage(new Image(new File("images/manual.png").toURI().toString()));
				} else {
					this.modeFooter.setText("Mode: Auto   ");
					((ImageView)this.modeFooter.getGraphic()).setImage(new Image(new File("images/auto.png").toURI().toString()));					
				}
			});
		});
		
		SessionStatistics.connectionProperty().addListener((observable, oldValue, newValue)->{
			if(newValue) {
				this.connFooter.setText("Online");
				 ((ImageView)this.connFooter.getGraphic()).setImage(new Image(new File("images/online.png").toURI().toString()));
				 SessionStatistics.setState("ready");
			}else {
				this.pauseAll();
				this.connFooter.setText("Offline");
				 ((ImageView)this.connFooter.getGraphic()).setImage(new Image(new File("images/offline.png").toURI().toString()));
				 SessionStatistics.setState("conn.not.available");
			}
		});		
	}
	
	
	/*
	 * MYLIST SECTION
	 */
	
	@FXML
	protected VBox myListPane;
	
	@FXML
	protected Label headListLbl;
	
	@FXML
	protected Button delListBtn;
	
	@FXML
	protected ToggleButton addListBtn, editListBtn, selectionListBtn;
	
	@FXML
	protected ScrollPane listContainerScroll;
	
	@FXML
	protected FlowPane listContainer;	
	
	@FXML
	protected Text placeholderList;
	
	@FXML
	protected ToolBar toolbarListPane;
		
	protected ObservableList<Node> listContainerItems;
	
	private CheckSelectionGroup checkGroup;	
	private ToggleGroup toggleBtnGroup = new ToggleGroup();
	
	private MediaListFormBuilder formAddListBtn;
	private MediaListFormBuilder formEditListBtn;
	private SelectionForm formSelectedListBtn;
	
	private List<CheckBox> selectedList;
	
	public void initListSection(ObservableList<MediaListButton> mediaListList) {
		this.toggleBtnGroup.getToggles().addAll(this.addListBtn, this.editListBtn, this.selectionListBtn);
		this.listContainerItems = this.listContainer.getChildren();
		this.checkGroup = new CheckSelectionGroup();
		this.selectionListBtn.setDisable(this.listContainerItems.stream().noneMatch(item->item instanceof MediaListButton));

		if(mediaListList.size() > 0) this.listContainerItems.remove(this.placeholderList);
		
		this.listContainerItems.addListener(new ListChangeListener<Node>() {
			@Override
			public void onChanged(Change<? extends Node> value) {
				if(value.next() && value.wasAdded()) {
					for(Node n : value.getAddedSubList()) {
						if(!(n instanceof MediaListButton)) break;
						MediaListButton mediaListButton = (MediaListButton) n;
						
						mediaListButton.setCheckSelectionGroup(checkGroup);	
						mediaListButton.setOnMousePressed(e->selectionModeFromMediaBtn(e, mediaListButton));
						mediaListButton.setOnAction(e->{
							if(checkGroup.getSelectionMode()) {
								if(!(e.getTarget() instanceof CheckBox)) {
									mediaListButton.setSelected(!mediaListButton.isSelected());
								}
							} else 
								showList(e);
						});

						dragAndDropMediaBtn(mediaListButton);
					}
				}else if(value.wasRemoved()) {
					for(Node n : value.getRemoved()) {
						if(n instanceof MediaListButton) {
							((MediaListButton)n).removeCheckGroup();
						}
					}
				}
				if(value.getList().stream().noneMatch(item->item instanceof MediaListButton)) {
					selectionListBtn.setDisable(true);
				}else {
					selectionListBtn.setDisable(false);
				}
			}
		});
				
		this.formAddListBtn = new MediaListFormBuilder(this.bundleFactory.getString("add"));
		this.formEditListBtn = new MediaListFormBuilder(this.bundleFactory.getString("edit"));
		this.formSelectedListBtn = new SelectionForm(this.checkGroup);
		
		this.formAddListBtn.nameHeadTextProperty().bind(this.bundleFactory.getStringBindings("name").concat(":"));
		this.formEditListBtn.nameHeadTextProperty().bind(this.bundleFactory.getStringBindings("name").concat(":"));
		this.formAddListBtn.iconHeadTextProperty().bind(this.bundleFactory.getStringBindings("icon").concat(":"));
		this.formEditListBtn.iconHeadTextProperty().bind(this.bundleFactory.getStringBindings("icon").concat(":"));
		this.formAddListBtn.previewHeadTextProperty().bind(this.bundleFactory.getStringBindings("preview").concat(":"));
		this.formEditListBtn.previewHeadTextProperty().bind(this.bundleFactory.getStringBindings("preview").concat(":"));
		this.formAddListBtn.submitTextProperty().bind(this.bundleFactory.getStringBindings("add"));
		this.formEditListBtn.submitTextProperty().bind(this.bundleFactory.getStringBindings("edit"));
		this.formAddListBtn.abortTextProperty().bind(this.bundleFactory.getStringBindings("abort"));
		this.formEditListBtn.abortTextProperty().bind(this.bundleFactory.getStringBindings("abort"));
		this.formAddListBtn.resetTextProperty().bind(this.bundleFactory.getStringBindings("reset"));
		this.formEditListBtn.resetTextProperty().bind(this.bundleFactory.getStringBindings("reset"));
		this.formSelectedListBtn.selectionBtnTextProperty().bind(this.bundleFactory.getStringBindings("select.all"));
		this.formSelectedListBtn.deselectionBtnTextProperty().bind(this.bundleFactory.getStringBindings("deselect.all"));
		
		this.formAddListBtn.setOnAction(e->this.addMediaList(e));
		this.formEditListBtn.setOnAction(e->this.editMediaList(e));
		this.formAddListBtn.setOnCancel(e->this.cancelForm(this.formAddListBtn, this.addListBtn, true));
		this.formEditListBtn.setOnCancel(e->this.cancelForm(this.formEditListBtn, this.editListBtn, true));
		this.formAddListBtn.setOnIconChooser(e->this.iconChooser(this.formAddListBtn));
		this.formEditListBtn.setOnIconChooser(e->this.iconChooser(this.formEditListBtn));

		this.addListBtn.disableProperty().bind(this.selectionListBtn.selectedProperty());
		this.checkGroup.selectionModeProperty().bind(this.selectionListBtn.selectedProperty());
		this.delListBtn.disableProperty().bind(this.checkGroup.selectedGroupSizeProperty().lessThan(1));
		this.editListBtn.disableProperty().bind(this.checkGroup.selectedGroupSizeProperty().isNotEqualTo(1).and(Bindings.not(this.editListBtn.selectedProperty())));
		this.listContainerItems.addAll(mediaListList);
	}
	
	private void dragAndDropMediaBtn(MediaListButton mediaListBtn) {
		mediaListBtn.setOnDragDetected(e->{
			if(!this.checkGroup.getSelectionMode()) {
				Dragboard db = mediaListBtn.startDragAndDrop(TransferMode.ANY);
				ClipboardContent clipboard = new ClipboardContent();
				
				Image image = new Image("file:" + mediaListBtn.getMediaList().getIconPath(), 100.0, 100.0, true, true, false);
				
				if(image != null) {
					clipboard.putImage(image);
					db.setContent(clipboard);
				}							
				e.consume();
			}
		});
		
		mediaListBtn.setOnDragOver(e->{
			if(e.getDragboard().hasImage()){
				e.acceptTransferModes(TransferMode.COPY_OR_MOVE);
			}			
			e.consume();
		});
		
		mediaListBtn.setOnDragEntered(e->{
			if(!e.getGestureSource().equals(mediaListBtn) && e.getDragboard().hasImage()){
				MediaListButton source = (MediaListButton) e.getGestureSource();
				
				int indexSource = source.getMediaList().getIndex();
				int indexTarget = mediaListBtn.getMediaList().getIndex();
				
				if(indexSource > indexTarget) {
					mediaListBtn.setStyle("-fx-border-width:0 0 0 1.5;-fx-border-color:rgba(50,50,80,1);-fx-border-radius:0;");
				}else {
					mediaListBtn.setStyle("-fx-border-width:0 1.5 0 0;-fx-border-color:rgba(50,50,80,1);-fx-border-radius:0;");
				}
				
			}			
			e.consume();
		});
		
		mediaListBtn.setOnDragExited(e->{
			mediaListBtn.setStyle(null);
			e.consume();
		});		
		
		mediaListBtn.setOnDragDropped(e->{
			Dragboard db = e.getDragboard();	
			
			boolean success = false;
			
			if(!mediaListBtn.equals(e.getGestureSource()) && db.hasImage()) {
				MediaListButton source = (MediaListButton)e.getGestureSource();
				MediaListButton target = (MediaListButton)e.getGestureTarget();

				this.listContainerItems.remove(source);				
				int indexTarget = target.getMediaList().getIndex();				
				this.listContainerItems.add(indexTarget, source);
				
				success = true;
			}

			
			e.setDropCompleted(success);
		});
		
		mediaListBtn.setOnDragDone(e->{
			this.listIndexUpdate();
			this.listContainerItems.stream().filter(item->item instanceof MediaListButton).forEach(item->{
				for(PseudoClass pseudoClass : item.getPseudoClassStates()) {
					item.pseudoClassStateChanged(pseudoClass, false);
				}
			});	
		});
	}	

	@FXML
	public void updateSelectedListBtn(MouseEvent event) {
		this.selectedList = this.checkGroup.getSelectedGroup();
	}
	
	@FXML
	public void addList(ActionEvent event) {
		try {
			if(this.closeOtherForms((ToggleButton)event.getSource())) {
				this.showAddForm();
			}
		} catch (IOException e) { }
	}
	
	private void showAddForm() throws IOException {
		this.listContainer.setDisable(true);
		this.formAddListBtn.getMediaList().setPath(null);
		this.formAddListBtn.getMediaList().pathGenerate();
		this.formAddListBtn.reset();
		this.myListPane.getChildren().add(2, this.formAddListBtn);
	}
	
	@FXML
	public void showEditForm(ActionEvent event) {
		if(this.closeOtherForms((ToggleButton)event.getSource())) {
			if(selectedList.size() == 1) {
				this.listContainer.setDisable(true);
				MediaListButton mediaListBtn = (MediaListButton) this.listContainerItems.stream().filter(item->((MediaListButton)item).getCheckBox().equals(this.selectedList.get(0))).findFirst().orElse(null);
								
				if(mediaListBtn != null) {
					this.formEditListBtn.setMediaList(mediaListBtn.getMediaList());
				}
				
				this.myListPane.getChildren().add(2, this.formEditListBtn);
			}
		}
	}

	@FXML
	public void showSelectionForm(ActionEvent event) {
		if(this.closeOtherForms((ToggleButton)event.getSource())) {
			this.myListPane.getChildren().add(2, this.formSelectedListBtn);
		}		
	}
	
	
	public void addMediaList(ActionEvent event) {				
		try {						
			if(!MediaListFormBuilder.isValidName(this.formAddListBtn.getName(), this.listContainerItems)) {
				/*
				 * DIALOG WINDOWS NOT VALID NAME
				 * 
				 */
				return;
			}			
			
			if(this.formAddListBtn.getName().isEmpty()) {
				/*
				 * DIALOG WINDOWS EMPTY
				 * 
				 */
				return;
			}
			
			MediaList mediaList = this.formAddListBtn.getResult();
			
			if(this.listContainerItems.contains(this.placeholderList)) {
				this.listContainerItems.remove(this.placeholderList);
			}
			
			mediaList.pathGenerate();
			mediaList.setIconPath(new File(mediaList.getPath().getParentFile().getAbsoluteFile(), "icon/" + new File(this.formAddListBtn.getIconText()).getName()).getAbsolutePath());
			mediaList.store();
	
			try {
				this.formAddListBtn.getImagePreview().store(new File(mediaList.getIconPath()));
				if(this.formAddListBtn.getContentMediaList() != null) {
					mediaList.addMedia(this.formAddListBtn.getContentMediaList());
					this.formAddListBtn.resetContentMedialist();
				}
				this.listContainerItems.add(0, MediaListButtonBuilder.build(mediaList));				
				this.listIndexUpdate();
			}catch(IOException e) {
				mediaList.delete(mediaList.getPath(), new File(mediaList.getIconPath()));
				/*
				 * DIALOG WINDOWS IMAGE NOT VALID
				 * 
				 */
				return;
			}
			
			this.cancelForm(this.formAddListBtn, this.addListBtn, false);
		}catch(IOException ex) {
			ex.printStackTrace();
		}
	}
	
	
	private void listIndexUpdate() {
		Thread indexUpdateTask = new Thread(()->{
			for(Node node : this.listContainerItems) {
				MediaListButton mediaListButton = (MediaListButton) node;
				int index = this.listContainerItems.indexOf(mediaListButton);
				mediaListButton.getMediaList().setIndex(index);
			}
		});
		
		indexUpdateTask.setDaemon(false);
		indexUpdateTask.setName("IndexUpdateTask");		
		
		synchronized(indexUpdateTask) {
			indexUpdateTask.start();
		}		
	}

	public void editMediaList(ActionEvent event) {				
		try {
			if(!MediaListFormBuilder.isValidName(this.formEditListBtn.getName(), this.listContainerItems, this.formEditListBtn.getMediaList().getName())) {
				/*
				 * DIALOG WINDOWS NOT VALID NAME
				 * 
				 */
				return;
			}
			
			if(this.formEditListBtn.getName().isEmpty()) {
				/*
				 * DIALOG WINDOWS EMPTY
				 * 
				 */
				return;
			}			

			MediaListButton mediaListBtn = (MediaListButton) this.listContainerItems.stream().filter(item->((MediaListButton)item).getMediaList().equals(this.formEditListBtn.getMediaList())).findFirst().orElse(null);

			if(mediaListBtn != null) {
				MediaList mediaList = mediaListBtn.getMediaList();	
								
				if(mediaList.getName().equals(this.formEditListBtn.getName())){
					mediaList.delete();
				}else if(mediaList.getName().equalsIgnoreCase(this.formEditListBtn.getName())){
					mediaList.delete();
					mediaList.setName(this.formEditListBtn.getName());
					mediaList.pathGenerate();
				}else {
					mediaList.setName(this.formEditListBtn.getName());
					mediaList.pathGenerate();
				}					
				
				File iconPath = new File(mediaList.getPath().getParentFile().getAbsoluteFile(), "icon/" + new File(this.formEditListBtn.getIconText()).getName());
				mediaList.setIconPath(iconPath.toString());
				mediaList.setSourceIcon(new File(this.formEditListBtn.getIconText()));
				mediaList.store();
				try {
					this.formEditListBtn.getImagePreview().store(iconPath);
					mediaListBtn.getImageIcon().load(iconPath, 100.0, true);
				}catch(IOException e) {
					mediaList.delete();
					/*
					 * DIALOG WINDOWS IMAGE NOT VALID
					 * 
					 */				
					return;
				}
			}
			
			this.cancelForm(this.formEditListBtn, this.editListBtn, false);
		}catch(IOException ex) {
			ex.printStackTrace();
		}
	}
	
	public void cancelForm(FormListButton formListButton, ToggleButton source, boolean confirm) {
		if(confirm) {
			/*
			 * DIALOG WINDOWS CONFIRM
			 * 
			 */
		}		
		
		formListButton.hide();
		source.setSelected(false);
		this.listContainer.setDisable(false);
	}
	
	public void cancelForm(boolean confirm) {
		Node node = this.myListPane.getChildren().get(2);
		ToggleButton button = (ToggleButton) this.toggleBtnGroup.getSelectedToggle();
		
		if(button != null) {
			if(node instanceof FormListButton) {
				this.cancelForm((FormListButton) node, button, node instanceof MediaListFormBuilder);
			}else{
				return;
			}	
		}
	}
	
	private void iconChooser(MediaListFormBuilder form) {
		FileChooser fileChooser = new FileChooser();
		ExtensionFilter extensionFilter = new ExtensionFilter("Image type (*.png, *.jpg, *.gif, *.tiff, *.ico)", "*.png", "*.jpg", "*.gif", "*.tiff", "*.ico");

		fileChooser.setInitialDirectory(new File(form.getIconText()).getParentFile());
		fileChooser.getExtensionFilters().add(extensionFilter);
		
		File icon = fileChooser.showOpenDialog(this.root.getScene().getWindow());
		
		if(icon != null) {
			form.setImageURL(icon.getAbsolutePath());
		}
	}

	
	
	private boolean closeOtherForms(ToggleButton toggleBtn) {
		if(this.myListPane.getChildren().get(2) instanceof FormListButton) {
			((FormListButton) this.myListPane.getChildren().get(2)).hide();
		}
		
		this.listContainer.setDisable(false);

		if(toggleBtn == null) return false;

		return toggleBtn.isSelected();
	}
		
	public void selectionModeFromMediaBtn(MouseEvent event, MediaListButton mediaListBtn) {
		if(!this.checkGroup.getSelectionMode()) {			
			Thread waitTask = new Thread(()->{
				try {
					synchronized(Thread.currentThread()) {
						Thread.currentThread().wait(500);
					}
					Platform.runLater(()->{
						if(this.myListPane.getChildren().contains(this.listContainerScroll))
							this.selectionListBtn.fire();						
					});
				} catch (InterruptedException e1) {	}																
			});			
												
			mediaListBtn.setOnMouseReleased(e->{
				if(waitTask.isAlive()) waitTask.interrupt();
			});

			mediaListBtn.setOnMouseDragged(ev->{
				if(waitTask.isAlive()) waitTask.interrupt();
			});
						
			waitTask.setDaemon(true);
			waitTask.start();
		}
	}
		
	@FXML
	public void deleteList(ActionEvent event) {
		if(this.checkGroup.getSelectedGroupSize() > 0) {
			/*
			 * 
			 * SHOW DIALOG WINDOW CONFIRM TO DELETE MEDIALIST
			 * 
			 */

			List<Node> selected = this.listContainerItems.stream().filter(mediaListBtn->this.checkGroup.getSelectedGroup().contains(((MediaListButton)mediaListBtn).getCheckBox())).collect(Collectors.toList());
			
			this.cancelForm(this.formSelectedListBtn, this.selectionListBtn, false);

			for(Node node : selected) {
				if(node instanceof MediaListButton) {
					MediaListButton mediaListBtn = (MediaListButton) node;
					mediaListBtn.getMediaList().delete(mediaListBtn.getMediaList().getPath(), mediaListBtn.getIcon());
				}
			}
			
			this.listContainerItems.removeAll(selected);
			
			if(this.listContainerItems.isEmpty()) {
				this.listContainerItems.add(this.placeholderList);
			}
		}
	}	
	@FXML
	public void closeSelected(MouseEvent event) {
		if(this.checkGroup.getSelectionMode()) {
			this.selectionListBtn.fire();
		}
	}
	
	/*
	 * MediaList
	 * 
	 */
	
	@FXML
	protected HBox mediaList;
	
	@FXML
	private VBox mediaListInfoWrapper, checkResultWrapper;
		
	@FXML
	private GridPane mediaListContentWrapper, mediaInfoContent;
	
	@FXML
	private StackPane emptyListGraphic;
	
	@FXML
	protected ToolBar mediaToolBar;

	@FXML
	protected Button backBtn, downMediaBtn, streamMediaBtn, removeMediaBtn, onlineCheckBtn;
	
	@FXML
	protected ImageOptimizer mediaListIcon;
	
	@FXML
	protected Label mediaListName, mediaListSize, mediaListLbl;
	
	@FXML
	protected ListView<Media> mediaListView;
	
	@FXML
	protected Separator mediaListSeparator;
	
	@FXML
	protected EditTextField titleMedia, descriptionMedia;
	
	@FXML
	protected Label emptyListLbl, originalNameMedia, sizeMedia, mimeMedia, extMedia, sourceMedia, hosterMedia, segmentMedia, lastScanner, available;
	
	@FXML
	protected Text originalNameMediaText, sizeMediaText, mimeMediaText, extMediaText, sourceMediaText, hosterMediaText, segmentMediaText, lastScannerText, availableText;
	
	@FXML
	protected ImageView onlineCheckGraphic;

	private void setMediaListViewSelectionMode() {	
		this.checkResultWrapper = this.createCheckResultWrapper();
		this.emptyListGraphic = this.createEmptyListGraphic();
		this.mediaList.getChildren().set(2, this.emptyListGraphic);

		final DataFormat dataFormat = new DataFormat("media");
	
		this.mediaListView.setCellFactory(listView->{
			ListCell<Media> cell = new ListCell<>();		
			
			cell.emptyProperty().addListener((observable, oldValue, newValue)->{
				if(!cell.isEmpty()) {
					cell.setText(cell.getItem().getCustomName());
					this.dragAndDropMediaListCell(cell, dataFormat);						
				}else {
					cell.setText(null);
					cell.setOnDragDetected(null);
					cell.setOnDragOver(null);
					cell.setOnDragEntered(null);
					cell.setOnDragExited(null);
					cell.setOnDragDropped(null);
					cell.setOnDragDone(null);
				}
			});
			
			return cell;
		});			

		this.mediaListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue)->{
			this.mediaListViewWrapText(newValue);

			if(newValue != null) {
				if(!this.mediaList.getChildren().contains(this.mediaListInfoWrapper)) {
					this.mediaList.getChildren().set(2, this.mediaListInfoWrapper);
				}
				this.titleMedia.setDisable(false); 
				this.descriptionMedia.setDisable(false);
				this.mediaListInfoWrapper.setDisable(false);
				
				this.titleMedia.setText(newValue.getCustomName());
				this.originalNameMedia.setText(newValue.getName());
				this.sizeMedia.setText(newValue.getSize().optimizeSize());
				this.mimeMedia.setText(newValue.getMimeType());
				this.extMedia.setText(newValue.getExt().toUpperCase());
				this.sourceMedia.setText(newValue.getSource().toExternalForm());
				this.hosterMedia.setText(newValue.getHoster() != null ? newValue.getHoster() : this.bundleFactory.getString("no.hoster"));
				this.segmentMedia.setText(newValue.isChunked() ? String.format("%d", newValue.getMRLs().length) : "No");
				this.lastScanner.setText(newValue.getLastScan());
				this.descriptionMedia.setText(newValue.getDescription() != null ? newValue.getDescription() : "");
				this.available.setText(newValue.isAvalaible() ? "Online" : "Offline");
			}else {
				if(this.mediaList.getChildren().contains(this.mediaListInfoWrapper)) {
					this.mediaList.getChildren().set(2, this.emptyListGraphic);
				}
				this.titleMedia.setDisable(true);
				this.descriptionMedia.setDisable(true);
				this.mediaListInfoWrapper.setDisable(true);
				
				this.titleMedia.clear();
				this.originalNameMedia.setText("");
				this.sizeMedia.setText("");
				this.mimeMedia.setText("");
				this.extMedia.setText("");
				this.sourceMedia.setText("");
				this.hosterMedia.setText("");
				this.segmentMedia.setText("");
				this.lastScanner.setText("");
				this.descriptionMedia.setText("");
				this.available.setText("");
			}
			
			this.mediaList.layout();
			this.mediaListInfoWrapper.layout();
			this.myListPane.layout();		
		});
	}	
	
	private void dragAndDropMediaListCell(ListCell<Media> cell, DataFormat dataFormat) {
		cell.setOnDragDetected(e->{
			Dragboard db = cell.startDragAndDrop(TransferMode.ANY);						
			ClipboardContent clipboard = new ClipboardContent();
			
			Image image = new Image("file:" + new File("images/drag_media.png").getAbsolutePath(), 70.0, 70.0, true, true, false);
			if(image != null) {
				clipboard.putImage(image);
			}
			clipboard.put(dataFormat, cell.getItem());
			db.setContent(clipboard);

			e.consume();
		});
		
		cell.setOnDragOver(e->{
			if(e.getDragboard().hasContent(dataFormat)){
				e.acceptTransferModes(TransferMode.COPY_OR_MOVE);
			}			
			e.consume();
		});
		
		cell.setOnDragEntered(e->{
	         if (!e.getGestureSource().equals(cell) && e.getDragboard().hasContent(dataFormat)) {
	        	 ListCell<?> source = (ListCell<?>) e.getGestureSource();
	        	 
	        	 int indexSource = source.getIndex();
	        	 int indexTarget = cell.getIndex();
	        	 
	        	 if(indexSource<indexTarget) {
		        	 cell.setStyle("-fx-border-color:rgb(12, 90, 148);-fx-border-width:0 0 2 0;");
	        	 }else {
		        	 cell.setStyle("-fx-border-color:rgb(12, 90, 148);-fx-border-width:2 0 0 0;");
	        	 }
	        	 
	         }              
	         e.consume();
		});
		
		cell.setOnDragExited(e->{
			cell.setStyle(null);
			e.consume();
		});						

		cell.setOnDragDropped(e->{
			Dragboard db = e.getDragboard();
			
			ListCell<?> source = (ListCell<?>) e.getGestureSource();
        	ListCell<?> target = (ListCell<?>) e.getGestureTarget();	
        	
			boolean success = false;
			
			if(db.hasContent(dataFormat)) {
				Media mediaSource = (Media)source.getItem();
				Media mediaTarget = (Media)target.getItem();
				
				mediaSource.toBack(mediaTarget);
				success = true;
			}
			
			e.setDropCompleted(success);
			e.consume();
		});
		
		cell.setOnDragDone(e->{
			Media media = this.mediaListView.getSelectionModel().getSelectedItem();
			MediaList mediaList = ((Media)cell.getItem()).getMediaList();
			this.mediaListView.getItems().clear();
			this.initContentView(mediaList.getContent(),media);
		});
	}

	private void mediaListViewWrapText(Media newValue) {
		ListCell<?> cell = (ListCell<?>) this.mediaListView.lookupAll(".cell").stream().filter(node->this.mediaListView.getSelectionModel().getSelectedIndex() == ((ListCell<?>) node).getIndex()).findFirst().orElse(null);
		
		if(cell != null && newValue != null) {
			final String text = newValue.toString();
			final double maxWidth = 50;
			final double maxSpace = cell.getWidth() - cell.getPadding().getLeft() - cell.getPadding().getRight() - 15;
			final double fillSpace = cell.getChildrenUnmodifiable().get(0).getLayoutBounds().getMaxX();
			
			if(fillSpace > maxSpace) {
				Thread moveTextTask = new Thread(()-> {
					int x = 0;
					synchronized(cell) {
						try {
							cell.wait(500);
						} catch (InterruptedException e) {	}
					}
					while(this.mediaListView.getSelectionModel().getSelectedItem() != null && this.mediaListView.getSelectionModel().getSelectedItem().equals(newValue)) {
						try {
							final String _text = text.substring(x);						
							Platform.runLater(()->cell.setText(_text));
						}catch(Exception ex) {
							x = Math.max(0, x-1);
						}
						
						synchronized(cell) {
							try {
								cell.wait(400);
								if(cell.getText().equals(text) && x != 0) break;
							} catch (InterruptedException | NullPointerException e) {	}								
						}
 /*
  * 
  * Exception in thread "MoveTextTask" java.lang.IndexOutOfBoundsException: Index: 0, Size: 1
  * 
  * cell.getChildrenUnmodifiable().get(0)
  * 
  * 
	  */				try {
							if(cell.getChildrenUnmodifiable().get(0).getLayoutBounds().getMaxX() < maxWidth) 
								x = 0;
							else
								x++;			  					
	  					}catch(Exception e) {
	  						x = 0;
	  					}
					}
					
					Platform.runLater(()->cell.setText(text));
				});
				
				moveTextTask.setDaemon(true);
				moveTextTask.setName("MoveTextTask");
				moveTextTask.start();
			}
		}	
	}


	private StackPane createEmptyListGraphic() {		
		StackPane emptyPane = new StackPane();
		
		ImageView emptyList = new ImageView(new Image("file:" + new File("images/select.png").getAbsolutePath(), 30.0, 30.0, true, true, true));
		Label emptyLbl = new Label("0 elementi selezionati");
		
		emptyLbl.textProperty().bind(this.bundleFactory.getStringBindings("no.selected.elements"));
		
		emptyList.setFitWidth(22.0);
		emptyList.setFitHeight(22.0);
		emptyLbl.setWrapText(true);
		emptyLbl.setGraphic(emptyList);
		emptyLbl.setContentDisplay(ContentDisplay.TOP);
		emptyLbl.setOpacity(.6);
		
		emptyPane.getChildren().add(emptyLbl);
		
		HBox.setHgrow(emptyPane, Priority.ALWAYS);
		
		emptyList.getStyleClass().add("empty-list-image");
		emptyPane.getStyleClass().add("empty-list-pane");
		emptyLbl.getStyleClass().add("empty-list-lbl");
		
		return emptyPane;
	}


	@FXML
	public void updateMediaTitle(ActionEvent event) {
		EditTextField textField = (EditTextField) event.getSource();
		
		if(this.mediaListView.getSelectionModel().getSelectedItem() != null) {
			Media selectedMedia = this.mediaListView.getSelectionModel().getSelectedItem();			
			
			if(textField.isEmpty()) {
				textField.setText(selectedMedia.getCustomName());
			}else {
				selectedMedia.setCutomName(textField.getText());
			}
			this.mediaListView.refresh();

			this.updateMedia(selectedMedia);
		}	
	}
	
	@FXML
	public void updateMediaDescription(ActionEvent event) {
		EditTextField textField = (EditTextField) event.getSource();
		
		if(this.mediaListView.getSelectionModel().getSelectedItem() != null) {
			Media selectedMedia = this.mediaListView.getSelectionModel().getSelectedItem();			

			selectedMedia.setDescription(textField.getText());
			
			this.updateMedia(selectedMedia);
		}		
	}
	
	private void updateMedia(Media media) {
		MediaList mediaList = media.getMediaList();
		if(mediaList != null) {
			mediaList.store();
		}		
	}
	
	public void showList(ActionEvent event) {
		MediaListButton source = (MediaListButton) event.getSource();

		if(this.checkGroup.getSelectionMode()) return;
		
		this.toolbarListPane.getItems().setAll(this.backBtn);
		
		Image tmpImg = new Image(source.getMediaList().getSourceIcon().toURI().toString(), true);		
		
		tmpImg.progressProperty().addListener((observable, oldValue, newValue)->{
			if(newValue.doubleValue() == 1.0) {
				double width = tmpImg.getWidth();				
				this.mediaListIcon.load(source.getMediaList().getSourceIcon(), Math.min(385.0, width), true);
			}
		});
		
		this.mediaListIcon.setFitWidth(ImageOptimizer.LOADING_IMAGE.getRequestedWidth());
		this.mediaListIcon.setFitHeight(ImageOptimizer.LOADING_IMAGE.getRequestedHeight());
		this.mediaListIcon.setImage(ImageOptimizer.LOADING_IMAGE);
		this.mediaListName.setText(source.getName());	
		this.mediaListSize.setText(source.getSizeLabel().getText());
		this.myListPane.getChildren().remove(this.listContainerScroll);
		this.myListPane.getChildren().add(this.mediaList);
		this.initContentView(source.getMediaList().getContent());		
	}
	
	private void initContentView(List<Media> mediaList) {
		this.initContentView(mediaList, null);
	}
	
	private void initContentView(List<Media> mediaList, Media selected) {
		ObservableList<Media> mediaListContent = FXCollections.observableArrayList();
		
		for(Media media : mediaList) {
			mediaListContent.add(media);
		}

		this.mediaListView.setItems(mediaListContent);
		
		if(selected == null) 
			this.mediaListView.getSelectionModel().selectFirst();
		else
			this.mediaListView.getSelectionModel().select(selected);
		
		this.mediaListView.refresh();
	}


	@FXML
	public void backToListContainer(ActionEvent event) {
		this.closeMediaList();		
		ZStreaming.gcClean(500);
	}
	
	private void closeMediaList() {
		if(this.toolbarListPane.getItems().contains(this.backBtn)){
			this.toolbarListPane.getItems().setAll(this.addListBtn, this.selectionListBtn, this.editListBtn, this.delListBtn);
			this.myListPane.getChildren().remove(this.mediaList);
			this.myListPane.getChildren().add(this.listContainerScroll);
			this.mediaListIcon.setFitHeight(0.0);
			this.mediaListIcon.setFitWidth(0.0);
			this.mediaListIcon.setImage(null);
			this.mediaListName.setText(null);
			this.mediaListSize.setText(null);
			this.myListPane.lookupAll(".media-list-separator").forEach(separator->((Separator)separator).setMaxWidth(260.0));
			this.mediaListView.getItems().clear();
		}
	}

	@FXML
	public void downloadMediaInList(ActionEvent event) {		
		Thread preDownloadTask = new Thread(()->{
			Media media = this.mediaListView.getSelectionModel().getSelectedItem();
			
			boolean online = media.getLdtLastScan().plusMinutes(60).isAfter(LocalDateTime.now()) ? media.isAvalaible() : this.checkOnlineMedia(media, true);
								
			if(online) {
				Platform.runLater(()->this.addDownload(event, this.mediaListView.getSelectionModel().getSelectedItem(), this.webBrowser.clone()));
			}else {
				/* 
				 * WINDOW DIALOG MEDIA OFFLINE
				 * 
				 */
				System.out.println("Media offline");
			}
		});
		
		preDownloadTask.setName("PreDownloadTask");
		preDownloadTask.setDaemon(true);
		preDownloadTask.start();
	}
	
	@FXML
	public void streamingFromMediaList(ActionEvent event) {
		Thread preStreamTask = new Thread(()->{
			Media media = this.mediaListView.getSelectionModel().getSelectedItem();

			boolean online = media.getLdtLastScan().plusMinutes(60).isAfter(LocalDateTime.now()) ? media.isAvalaible() : this.checkOnlineMedia(media, true);

			if(online) {
				Platform.runLater(()->this.streaming(media));
			}else {
				/* 
				 * WINDOW DIALOG MEDIA OFFLINE
				 * 
				 */
				System.out.println("Media offline");
			}
		});		
		
		preStreamTask.setName("PreStreamTask");
		preStreamTask.setDaemon(true);
		preStreamTask.start();
	}
	
	@FXML
	public void removeMedia(ActionEvent event) {
		Media media = this.mediaListView.getSelectionModel().getSelectedItem();

		if(media != null) {		
			MediaList mediaList = media.getMediaList();

			if(mediaList != null) {
				mediaList.removeMedia(media);
				this.mediaListView.getItems().remove(media);
			}
		}
	}
	
	@FXML
	public void checkOnlineMedia(ActionEvent event) {		
		Media media = this.mediaListView.getSelectionModel().getSelectedItem();
		
		if(media != null) {
			this.checkOnlineMedia(media, false);
		}
	}
	
	
	private Boolean checkOnlineMedia(Media media, boolean wait) {
		MediaList mediaList = media.getMediaList();

		if(mediaList != null) {
			Platform.runLater(()->this.checkInProgressMode());
			this.mediaToolBar.setDisable(true);
			OnlineMediaChecker checker = new OnlineMediaChecker(media, this.webBrowser.clone());
			
			Thread onlineCheckerTask = new Thread(()-> {
				Media newMedia = null;

				if(checker.lazyCheck()){
					media.setLastScan(LocalDateTime.now());		
					media.setAvalaible(true);					
				}else if((newMedia = checker.depthCheck()) != null && newMedia.getMRL() != null) {
					media.setAvalaible(true);
					media.setLastScan(newMedia.getLdtLastScan());
					media.setMRL(newMedia.getMRL());
					if(newMedia.isChunked())
						media.setMRLs(newMedia.getMRLs());
					}else {
						media.setAvalaible(false);
				}
				
				mediaList.store();				
				this.mediaToolBar.setDisable(false);
				
				Platform.runLater(()->{
					this.availableMode(media.isAvalaible());
					synchronized(media) { media.notify();}
				});
			});
			
			onlineCheckerTask.setDaemon(true);
			onlineCheckerTask.setName("OnlineCheckerTask");
			onlineCheckerTask.start();
			
			if(wait) {
				synchronized(media) {
					try {
						media.wait();
					} catch (InterruptedException e) {	}
				}				
				
				Platform.runLater(()->{
					this.mediaListView.getSelectionModel().clearSelection();
					this.mediaListView.getSelectionModel().select(media);
				});
				
				return media.isAvalaible();
			}
			
			return null;
		}
		
		return wait ? false : null;
	}


	private void checkInProgressMode() {		
		this.mediaListInfoWrapper.getChildren().remove(this.mediaInfoContent);
		this.mediaListInfoWrapper.getChildren().add(this.checkResultWrapper);
	}


	private void availableMode(boolean online) {
		this.available.setText(online ? "Online" : "Offline");
		this.onlineCheckBtn.setGraphic(this.onlineCheckGraphic);
		this.mediaListInfoWrapper.getChildren().remove(this.checkResultWrapper);
		this.mediaListInfoWrapper.getChildren().add(this.mediaInfoContent);
	}

	private VBox createCheckResultWrapper() {
		VBox checkResultWrapper = new VBox(8);
		Label waiting = new Label("Verifica disponibiltà in corso...");
		
		waiting.textProperty().bind(this.bundleFactory.getStringBindings("waiting.check.availability"));
		
		checkResultWrapper.setAlignment(Pos.CENTER);
		checkResultWrapper.getChildren().addAll(waiting, new WaitingBar());
		
		checkResultWrapper.setId("checkResultWrapper");		
		
		VBox.setVgrow(checkResultWrapper, Priority.ALWAYS);
		
		return checkResultWrapper;
	}
	
	
	
	/*
	 * HISTORY SECTION
	 */
	
	@FXML
	protected BorderPane historyPane;
	
	@FXML
	protected StackPane historyWrapper;
		
	@FXML
	protected ToolBar historyToolbar;
	
	@FXML
	protected VBox emptyHistory, historyContent, mainCheckWrapper;
	
	@FXML
	protected HBox selButtons, filterWrapper;
	
	@FXML
	protected GridPane headHistory;
	
	@FXML
	protected ScrollPane historyScrollWrapper;
	
	@FXML
	protected MenuButton optionSearch, filterSection;
	
	@FXML
	protected Button delSelHistoryBtn, closeSelHistoryBtn;
		
	@FXML
	private TextField queryHistory;
	
	@FXML
	private CheckBox mainCheck;
	
	@FXML
	protected CheckMenuItem nameCheck, urlCheck, hosterCheck;
	
	@FXML
	protected RadioMenuItem onlineCheck, offlineCheck, minMB, btwOneFiftyMB, btwFiftyTwoFiftyMB, btwTwoFiftyFivHundMB, btwFivHundOneMBGB, greatGB;
	
	@FXML
	protected Label emptyHistoryLbl, stateFilterLbl, sizeFilterLbl, searchWithinLbl, historyHeadLbl, selHistorySize, searchResultLbl;
	
	@FXML
	protected ToggleGroup sizeGroup, stateGroup;
	
	@FXML
	protected TreeView<String> dateView;
	
	@FXML
	protected TreeItem<String> allDate;
	
	@FXML
	protected ContextMenu dateViewContextMenu;
	
	@FXML
	protected MenuItem removeDate;
	
	protected HistoryFactory historyFactory;
			
	public void initHistory() {
		this.historyFactory = new HistoryFactory(this.mediaHistory);
		this.historyContent.getChildren().add(this.historyFactory.getNode());
		this.mainCheckWrapper.visibleProperty().bind(this.historyFactory.selectionModeProperty());
		this.mainCheckWrapper.disableProperty().bind(this.historyFactory.selectionModeProperty().not());
		this.mainCheckWrapper.prefHeightProperty().bind(this.mainCheckWrapper.minHeightProperty());
		this.historyFactory.pageHistoryTextProperty().bind(this.bundleFactory.getStringBindings("page"));
		this.mainCheck.textProperty().bind(this.bundleFactory.getStringBindings("select.all"));
		
		this.filterButtons(this.nameCheck, this.urlCheck, this.hosterCheck);
		this.filterButtons(this.sizeGroup, this.stateGroup);
		
		this.queryHistory.textProperty().addListener((observable, oldValue, newValue)->{
			if(newValue != null && !newValue.trim().isEmpty()) {
				if(!oldValue.trim().equals(newValue.trim())) {
					this.historyFactory.setSearchMode(true);
					this.requestPageWithFilters();
				}
			}else {
				this.historyFactory.setSearchMode(false);
				this.requestPageWithFilters();
			}
		});
		
		this.historyFactory.selectedProperty().addListener((observable, oldValue, newValue)->{			
			if(!newValue) {
				int checkSize = 0;
				int selectedSize = 0;
				
				for(Node n : this.historyFactory.getNode().getChildren()) {
					if(n instanceof GridPane) {
						GridPane wrap = (GridPane) n;
						CheckBox check = (CheckBox) wrap.getChildren().get(0);
						if(check.isSelected()) {
							selectedSize++;
						}
						checkSize++;
					}
				}
				
				this.delSelHistoryBtn.setDisable(selectedSize == 0);
				this.selHistorySize.setText(String.format("%d %s", selectedSize, this.bundleFactory.getString("selected.elements")));
				this.mainCheck.setSelected(selectedSize == checkSize);
			}	
		});
		
		this.historyFactory.selectionModeProperty().addListener((observable, oldValue, newValue)->{
			this.scrollTranslate(newValue);
			
			if(newValue) {
				this.selHistorySize.setText(String.format("0 %s", this.bundleFactory.getString("selected.elements")));
				this.mainCheckWrapper.setMaxWidth(((GridPane)this.historyFactory.getNode().getChildren().get(1)).getWidth());
				this.selButtons.setVisible(true);
				this.selButtons.setDisable(false);
				this.selButtons.toFront();
				this.historyWrapper.setStyle("-fx-background-color:rgba(200,200,200), rgb(0, 89, 179, .2)");

			}else {
				this.selButtons.setVisible(false);
				this.selButtons.setDisable(true);
				this.historyWrapper.setStyle(null);
			}
		});
		
		this.historyFactory.searchModeProperty().addListener((observable, oldValue, newValue)->{
			this.queryHistory.pseudoClassStateChanged(PseudoClass.getPseudoClass("searching"), newValue);
			if(!newValue)			
				this.queryHistory.clear();

		});
		
		if(this.historyFactory.getPages().isEmpty()) {
			this.historyWrapper.getChildren().setAll(this.selButtons, this.emptyHistory);
		}
				
		this.historyFactory.getNode().getChildren().addListener(new ListChangeListener<Node>() {
			@Override
			public void onChanged(Change<? extends Node> value) {
				if(value.next() && value.wasAdded()) {
					List<? extends Node> addedSubList = value.getAddedSubList();					
					for(Node node : addedSubList) {
						addHistoryRescanActions(node);
					}
				}
				if(value.getList().stream().noneMatch(n->n instanceof GridPane)) {					
					if(!historyWrapper.getChildren().contains(emptyHistory))
						historyWrapper.getChildren().setAll(emptyHistory, selButtons);
				}else {
					if(!historyWrapper.getChildren().contains(historyScrollWrapper))
						historyWrapper.getChildren().setAll(historyScrollWrapper, selButtons);
				}
			}			
		});
		
		this.initDateView();
	}

	private void filterButtons(ToggleGroup... groups) {		
		for(ToggleGroup group : groups) {
			for(Toggle toggle : group.getToggles()) {
				toggle.selectedProperty().addListener((observable, oldValue, newValue)->{
					RadioMenuItem radio = (RadioMenuItem)toggle;
					if(newValue) {
						Button btn = new Button(radio.getText());
						btn.setOnAction(e->toggle.setSelected(false));
						
						if(!this.filterWrapper.getChildren().contains(btn)) {
							btn.getStyleClass().add("filter-btn");
							this.filterWrapper.getChildren().add(btn);
						}
					}else {
						Button btn = (Button) this.filterWrapper.getChildren().stream().filter(n->n instanceof Button).filter(n->((Button)n).getText().equals(radio.getText())).findFirst().orElse(null);
						if(btn != null) {
							this.filterWrapper.getChildren().remove(btn);
						}
					}
					this.requestPageWithFilters();
				});				
			}
		}	
	}
	
	private void filterButtons(CheckMenuItem...checkBox) {
		for(CheckMenuItem check : checkBox) {
			check.selectedProperty().addListener((observable, oldValue, newValue)->{
				if(newValue) {
					Button btn = new Button(check.getText());
					btn.setOnAction(e->check.setSelected(false));
					
					if(!this.filterWrapper.getChildren().contains(btn)) {
						btn.getStyleClass().add("filter-btn");
						this.filterWrapper.getChildren().add(btn);
					}
				}else {
					Button btn = (Button) this.filterWrapper.getChildren().stream().filter(n->n instanceof Button).filter(n->((Button)n).getText().equals(check.getText())).findFirst().orElse(null);
					if(btn != null) {
						this.filterWrapper.getChildren().remove(btn);
					}
				}
				this.requestPageWithFilters();
			});
		}
	}


	private void scrollTranslate(boolean newValue) {
		Timeline timeline = new Timeline();
		
		WritableValue<Double> writable = new WritableValue<Double>() {
			@Override
			public Double getValue() {
				return StackPane.getMargin(historyScrollWrapper).getTop();
			}

			@Override
			public void setValue(Double value) {
				StackPane.setMargin(historyScrollWrapper, new Insets(value, 0, 0, 0));				
			}			
		};
		
		timeline.setAutoReverse(false);
		timeline.setCycleCount(1);

		KeyFrame keyFrame = new KeyFrame(Duration.millis(200), new KeyValue(writable, newValue ? this.selButtons.getHeight() : 0.0));
		
		timeline.getKeyFrames().add(keyFrame);

		if(newValue) 
			this.mainCheckWrapper.setMinHeight(-1);
		else
			this.mainCheckWrapper.setMinHeight(0.0);
				
		timeline.play();
	}


	private void initDateView() {
		this.allDate = new TreeItem<String>("Cronologia");
		this.allDate.valueProperty().bind(this.bundleFactory.getStringBindings("history.sec.btn"));
		this.allDate.setExpanded(true);
		this.dateView.setRoot(this.allDate);
		this.fillDateView();
		
		this.dateView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue)->{
			if(newValue != null) {
				this.historyFactory.setSearchMode(false);
				this.historyFactory.setSelectionMode(false);				
				this.requestPageWithFilters();
			}
		});
		
		this.mediaHistory.changeHistoryProperty().addListener((observable, oldValue, newValue)->{
			if(newValue) {
				this.requestPageWithFilters();
				if(this.mediaHistory.getHistory().size() != this.allDate.getChildren().size()) {
					this.fillDateView();
				}
			}
		});
	}

	private void addHistoryRescanActions(Node node) {
		if(node.getStyleClass().contains("history-entry-wrapper")) {
			GridPane wrapper = (GridPane) node;
			
			Node n = wrapper.lookup(".history-rescan-btn");
			
			if(n instanceof Button) {
				Button rescan = (Button)n;
				
				if(rescan.getOnAction() == null) {
					rescan.setOnAction(e->this.rescanURL(e, wrapper));
				}
			}
		}
	}


	private void fillDateView() {
		this.allDate.getChildren().clear();
		
		if(this.dateView.getRoot() == null) {
			this.dateView.setRoot(this.allDate);
		}
		
		for(LocalDate day : mediaHistory.getHistory().keySet()) {
			String formatDate = null;
			if(day.equals(LocalDate.now())){
				formatDate = this.bundleFactory.getString("today");
			}else if(day.plusDays(1).equals(LocalDate.now())) {
				formatDate = this.bundleFactory.getString("yesterday");
			}else {				
				formatDate = String.format("%02d %s %d", day.getDayOfMonth(), day.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault()), day.getYear());
			}
			
			TreeItem<String> item = new TreeItem<String>(formatDate);
			
			item.valueProperty().bind(Bindings.createStringBinding(()->String.format("%02d %s %d", day.getDayOfMonth(), day.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault()), day.getYear()), this.bundleFactory.getResourcesProperty()));
			
			this.allDate.getChildren().add(item);
		}
		
		if(allDate.getChildren().isEmpty()) {
			this.dateView.setRoot(null);
		}
	}


	@FXML
	public void closeHistorySelectionMode(ActionEvent event){
		this.historyFactory.setSelectionMode(false);
	}
	
	@FXML
	public void removeSelHistoryEntries(ActionEvent event){
		/*
		 * 
		 * WINDOW DIALO CONFIRM TO CANCEL
		 * 
		 * 
		 */
		
		new Thread(()-> {
			List<HistoryEntry> entries = new ArrayList<>();

			for(Node n : this.historyFactory.getNode().getChildren()) {
				if(n instanceof GridPane) {
					GridPane wrap = (GridPane) n;
					CheckBox check = (CheckBox) wrap.getChildren().get(0);				
					if(check.isSelected()) {					
						HistoryEntry entry = (HistoryEntry) check.getUserData();
						entries.add(entry);
					}
				}
			}
			
			this.mediaHistory.removeAll(entries);
			this.delSelHistoryBtn.setDisable(true);
			this.historyFactory.setSelectionMode(false);
		}).start();
	}
	
	@FXML	
	public void removeHistoryEntries(ActionEvent event){
		this.mediaHistory.clean();
	}
		
	private void requestPageWithFilters() {
		String query = this.queryHistory.textProperty().get() != null ? this.queryHistory.textProperty().get().trim() : "";
		
		TreeSet<FilterHistory.Type> _types = new TreeSet<>((x,y)->Integer.compare(x.getValue(), y.getValue()));
			
		if(this.nameCheck.isSelected()) _types.add(Type.NAME);
		if(this.urlCheck.isSelected()) _types.add(Type.URL);
		if(this.hosterCheck.isSelected()) _types.add(Type.HOSTER);
		if(this.onlineCheck.isSelected()) _types.add(Type.SUCCESSED);
		if(this.offlineCheck.isSelected()) _types.add(Type.FAILED);

		if(this.minMB.isSelected()) _types.add(Type.MIN_UNO_MB);
		if(this.btwOneFiftyMB.isSelected()) _types.add(Type.UNO_MB_50_MB);
		if(this.btwFiftyTwoFiftyMB.isSelected()) _types.add(Type.CINQUANTA_MB_DUECENTOCINQUANTA_MB);
		if(this.btwTwoFiftyFivHundMB.isSelected()) _types.add(Type.DUECENTOCINQUANTA_MB_CINQUECENTO_MB);
		if(this.btwFivHundOneMBGB.isSelected()) _types.add(Type.CINQUECENTO_MB_UNO_GB);
		if(this.greatGB.isSelected()) _types.add(Type.GREAT_UNO_GB);
				
		if((_types.contains(Type.SUCCESSED) || _types.contains(Type.FAILED)) && !(_types.contains(Type.NAME) || _types.contains(Type.URL) || _types.contains(Type.HOSTER))) {
			if(!_types.contains(Type.FAILED))
				_types.add(Type.NAME);
			_types.add(Type.URL);			
		}
		
		if((_types.contains(Type.MIN_UNO_MB) || _types.contains(Type.UNO_MB_50_MB) || _types.contains(Type.CINQUANTA_MB_DUECENTOCINQUANTA_MB) || 
			_types.contains(Type.DUECENTOCINQUANTA_MB_CINQUECENTO_MB) || _types.contains(Type.CINQUECENTO_MB_UNO_GB) ||_types.contains(Type.GREAT_UNO_GB)) && 
			!(_types.contains(Type.NAME) || _types.contains(Type.URL) || _types.contains(Type.HOSTER))) {
			
			if(!_types.contains(Type.FAILED))
				_types.add(Type.NAME);
			_types.add(Type.URL);
		}
				
		TreeItem<String> item = this.dateView.getSelectionModel().getSelectedItem();
		
		if(item != null && !item.equals(this.dateView.getRoot())) {
			_types.add(Type.DATE);
			
			if(!(_types.contains(Type.NAME) || _types.contains(Type.URL) || _types.contains(Type.HOSTER)) && !query.isEmpty()) {
				if(!_types.contains(Type.FAILED))
					_types.add(Type.NAME);
				_types.add(Type.URL);
			}
		}

		FilterHistory.Type[] types = _types.toArray(new FilterHistory.Type[_types.size()]);
		FilterHistory filter = null;

		if(types.length == 0) {
			filter = new FilterHistory(query);
		}else if(item != null && !item.equals(this.dateView.getRoot())) {
			String dateFormatted = this.dateView.getSelectionModel().getSelectedItem().getValue();
			LocalDate date = null;
			
			if(dateFormatted.equals(this.bundleFactory.getString("today")))
				date = LocalDate.now();
			else if(dateFormatted.equals(this.bundleFactory.getString("yesterday")))
				date = LocalDate.now().minusDays(1);
			else {
				date = this.parseDateFormatted(dateFormatted);
			}
			
			if(date!= null) {
				filter = new FilterHistory(query, date, types);
			}else
				return;
		}else {
			filter = new FilterHistory(query, types);
		}		
		
		this.historyFactory.requestPage(1, filter);		
	}
	
	private LocalDate parseDateFormatted(String dateFormatted) {
		LocalDate date = null;
		String[] _date = dateFormatted.split(" ");
		String pattern = "dd MMM yyyy";

		if(_date.length == 3) {
			dateFormatted = String.format("%s %s %s", _date[0], _date[1].substring(0, 3), _date[2]);
			date = LocalDate.parse(dateFormatted, DateTimeFormatter.ofPattern(pattern, Locale.getDefault()));			
		}
		
		if(date == null) throw new NullPointerException("String conversion failed");
		
		return date;
	}
	
	@FXML
	public void closeHistorySearch(MouseEvent event) {		
		if(this.historyFactory.isSearchMode() && event.getPickResult().getIntersectedPoint().getX() > this.queryHistory.getWidth() - this.queryHistory.getPadding().getRight()) {
			this.historyFactory.setSearchMode(false);
		}
	}
	
	@FXML
	public void changeCursor(MouseEvent event) {		
		Region region = (Region) event.getSource();
		
		if(event.getPickResult().getIntersectedPoint().getX() > region.getWidth() - region.getPadding().getRight()) {
			region.setCursor(this.historyFactory.isSearchMode() ? Cursor.HAND : Cursor.DEFAULT);
		}else
			region.setCursor(Cursor.TEXT);		
	}
		
	public void selectAllHistory(ActionEvent event) {
		for(Node n : this.historyFactory.getNode().getChildren()) {
			if(n instanceof GridPane) {
				GridPane wrap = (GridPane) n;
				CheckBox check = (CheckBox) wrap.getChildren().get(0);
				check.setSelected(this.mainCheck.isSelected());
			}
		}
		
		this.historyFactory.changeSelect();
	}	
	


	public void rescanURL(ActionEvent event, GridPane wrapper) {
		HistoryEntry entry = (HistoryEntry) wrapper.getChildren().get(0).getUserData();		
		this.showRescanListView(entry);
	}


	private void showRescanListView(HistoryEntry entry) {
		Stage rescanChooseStage = new Stage();
		
		RescanListView rescanListView = new RescanListView(entry);
		
		rescanListView.setOnAction(e->this.rescan(rescanListView));		
		rescanListView.setOnCloseRequest(e->rescanListView.closeWindow());
		
		rescanChooseStage.setScene(new Scene(rescanListView));		
		rescanChooseStage.getScene().setFill(null);
		rescanChooseStage.initStyle(StageStyle.TRANSPARENT);
		rescanChooseStage.initModality(Modality.APPLICATION_MODAL);
		rescanChooseStage.initOwner(this.root.getScene().getWindow());
		rescanChooseStage.centerOnScreen();
		
		rescanListView.showWindow(rescanChooseStage);
	}


	private void rescan(RescanListView rescanListView) {
		String url = rescanListView.getListView().getSelectionModel().getSelectedItem();
		
		if(url != null) {
			this.changeSection(this.urlScannerPane, this.scanSecBtn);
			this.urlTextField.setText(url);
			this.analyzeURL(new ActionEvent(this.urlButton, this.urlButton));
			rescanListView.closeWindow();
		}		
	}
	

	public void dateContextMenuInitializate(ContextMenuEvent event) {
		Node node = event.getPickResult().getIntersectedNode();
		TreeCell<?> cell = null;
		
		if(node.lookup(".tree-cell") instanceof TreeCell) {
			cell = (TreeCell<?>) node.lookup(".tree-cell");
		}else if(node.getParent().lookup(".tree-cell") instanceof TreeCell) {
			cell = (TreeCell<?>) node.getParent().lookup(".tree-cell");
		}
				
		if(cell.getItem() != null) {
			this.removeDate.setDisable(false);	
		} else {
			this.dateViewContextMenu.hide();
			this.removeDate.setDisable(true);
			event.consume();
		}
	}
	
	@FXML
	public void removeDate(ActionEvent event) {
		/*
		 * 
		 * WINDOW DIALO CONFIRM TO CANCEL
		 * 
		 * 
		 */
		TreeItem<String> item = this.dateView.getSelectionModel().getSelectedItem();
		
		if(item != null) {			
			String dateFormatted = item.getValue();
			
			if(dateFormatted != null) {
				LocalDate date = null;
				
				if(dateFormatted.equals(this.bundleFactory.getString("history.sec.btn"))) {
					this.mediaHistory.clean();
				
				}else{
					if(dateFormatted.equals(this.bundleFactory.getString("today")))
						date = LocalDate.now();
					else if(dateFormatted.equals(this.bundleFactory.getString("yesterday")))
						date = LocalDate.now().minusDays(1);
					else {
						date = this.parseDateFormatted(dateFormatted);
					}
					
					if(date != null) {
						if(this.mediaHistory.getHistory().containsKey(date)) {
							this.mediaHistory.removeAll(new ArrayList<>(this.mediaHistory.getHistory().get(date)));
						}
					}
				}
			}
		}		
	}
	
	

	/*
	 * SETTINGS SECTION
	 */
	
	@FXML
	protected Pane settingsPane;
	
	@FXML
	protected FlowPane settingContent;
	
	@FXML
	protected SettingButton generalBtn, downloadBtn,  listsBtn, connectionBtn, playerBtn;
	
	@FXML
	protected Fieldset langWrapper, startOptionsWrapper, exitOptionsWrapper, storeConnectionWrapper, removalDownOptions, saveDownOptions, generalDownOptions, statisticsOptions, defaultsListOptions, storePlayerWrapper;
	
	@FXML
	protected ToolBar settingBackWrapper;
	
	@FXML
	protected Pane generalSettings, downloadSettings, listSettings, connectionSettings, playerSettings;
	
	@FXML
	protected ScrollPane scrollSettingContent;
	
	@FXML
	protected ComboBox<LocaleItem> langBox;

	@FXML
	protected ComboBox<Integer> simultaneousBox;
	
	@FXML
	protected CheckBox startupCheck, confirmDelDownCheck, modeAutoStartup, deleteAutoCheck, mainChartCheck, singleChartCheck;
	
	@FXML
	protected RadioButton startMinCheck, startTrayCheck, exitConfirmCheck, exitRequestCheck, exitTrayIconCheck, onlyCompleteCheck, onlyInterruptedCheck;
	
	@FXML
	protected ToggleGroup startupGroup, extOptionsGroup, autoRemoveGroup;
	
	@FXML
	protected Button settingBackTitleBtn, openLogFolder, openDownFolder, openDownStoreFolder, openListPathFolder, openIconListFolder, openCookieFolder, openHistoryFolder, restoreConnBtn, restoreListBtn, restoreDownBtn, restoreGenBtn, resetDefaultNameList, resetStatisticsBtn, openPlayerFolderBtn, restorePlayerBtn;	
	
	@FXML
	protected Label settingLbl, langLbl, logLbl, simultaneousLbl, downPathLbl, downStoreLbl, delayUpdateLbl, enableDisableLbl, listPathDefaultLbl, defaultIconListLbl, defaultNameListLbl, cookieLbl, historyLbl, playerPathLbl;
	
	@FXML
	protected Text delayText;
	
	@FXML
	protected Slider delaySlider;
	
	@FXML
	protected TextField logPathText, downPathText, downStoreText, defaultListPathText, defaultIconListText, defaultNameListText, cookiePathText, historyPathText, playerPathText;
	
	@FXML
	protected ImageView settingImage;

	private void initSettingSection() {
		this.langBox.getItems().addAll(new LocaleItem(Locale.UK), new LocaleItem(Locale.ITALY));
		for(int i=1; i<5;i++) this.simultaneousBox.getItems().add(i);
		
		this.loadPathSettings();
		this.loadLangSettings();
		this.loadBooleanSettings(this.startupCheck, "startup", false, this.startMinCheck, this.startTrayCheck);		
		this.loadGroupSettings("startup.sub", this.startupGroup, this.startupCheck, -1);
		this.loadGroupSettings("exit.options", this.extOptionsGroup, null, 0);
		this.loadMaxSimultaneousSettings();
		this.loadBooleanSettings(this.modeAutoStartup, "auto.mode", false);
		this.loadBooleanSettings(this.deleteAutoCheck, "auto.delete", false, this.onlyCompleteCheck, this.onlyInterruptedCheck);
		this.loadGroupSettings("auto.delete.filter", this.autoRemoveGroup, this.deleteAutoCheck, -1);
		this.loadBooleanSettings(this.confirmDelDownCheck, "confirm.interrupt", false);
		this.loadDelayUpdateChartSettings();
		this.loadBooleanSettings(this.mainChartCheck, "main.chart.enable", true);
		this.loadBooleanSettings(this.singleChartCheck, "single.charts.enable", true);
		this.defaultNameListText.setText(ZStreaming.getSettingManager().getSettings().get("default.list.name"));
		
		this.startTrayCheck.setDisable(!SystemTray.isSupported() || !this.startupCheck.isSelected());
		
		this.defaultNameListText.textProperty().addListener((observalbe, oldValue, newValue)->{
			if(newValue != null && !newValue.isEmpty())
				ZStreaming.getSettingManager().storeSettings("default.list.name", newValue);
			else
				ZStreaming.getSettingManager().storeSettings("default.list.name", "MyList");
		});
		
		this.defaultNameListText.focusedProperty().addListener((observable, oldValue, newValue)->{
			if(!newValue) {
				String defaultName = ZStreaming.getSettingManager().getSettings().get("default.list.name");
				this.defaultNameListText.setText(defaultName);
				this.formAddListBtn.getMediaList().setName(defaultName);
			}
		});

		SessionStatistics.connectionProperty().addListener((observable, oldValue, newValue)->{
			boolean _autoMode = false;
			
			try {
				int value = Integer.parseInt(ZStreaming.getSettingManager().getSettings().get("auto.mode"));

				if(value < 0 || value > 1) {
					throw new NumberFormatException();
				}
				
				_autoMode = value > 0;
			}catch(NumberFormatException ex) { };

			if(newValue && _autoMode)
				this.playAll();
		});
		
		this.historyPathText.textProperty().addListener((observable, oldValue, newValue)->{
			try {
				Files.move(new File(oldValue).toPath(), new File(newValue).toPath(), StandardCopyOption.REPLACE_EXISTING);
				this.mediaHistory.setPath(new File(newValue));
			} catch (IOException e) {
				e.printStackTrace();
				/*
				 *
				 * WINDOW DIALOG IMPOSSIBILE
				 * 
				 */
				ZStreaming.getSettingManager().storeSettings("history.path", oldValue);
				this.historyPathText.setText(oldValue);
			}
		});
		
		this.defaultListPathText.textProperty().addListener((observable, oldValue, newValue)->{			
			File oldPath = new File(oldValue, MediaList.NAME_ROOT_FOLDER);
			File newPath = new File(newValue, MediaList.NAME_ROOT_FOLDER);
			
			if(oldPath.isDirectory()) {
				try {
					if(!newPath.exists()) {
						Files.move(oldPath.toPath(), newPath.toPath(), StandardCopyOption.ATOMIC_MOVE);
					}
				} catch (IOException e) {
					/*
					 * 
					 * 
					 */
					ZStreaming.getSettingManager().storeSettings("list.path", oldValue);
					this.defaultListPathText.setText(oldValue);
				}
			}
		});
		
		this.playerPathText.textProperty().addListener((observable, oldValue, newValue)->{
			this.openPlayerFolderBtn.setDisable(newValue == null || newValue.trim().isEmpty());
		});
	}

	private void loadDelayUpdateChartSettings() {
		int value;
		
		try {
			value = Integer.parseInt(ZStreaming.getSettingManager().getSettings().get("update.chart.delay"));
			
			if(value < 0)
				throw new NumberFormatException();			
		}catch(NumberFormatException ex) { 
			value = 5;
		}
		
		this.delaySlider.setValue(value);		
		this.delayUpdateLbl.textProperty().bind(this.delaySlider.valueProperty().asString("%.0f sec"));		
		this.delaySlider.disableProperty().bind(this.singleChartCheck.selectedProperty().or(this.mainChartCheck.selectedProperty()).not());
		
		this.delaySlider.valueChangingProperty().addListener((observable, oldValue, newValue)->{
			ZStreaming.getSettingManager().storeSettings("update.chart.delay", String.format("%.0f", this.delaySlider.valueProperty().get()));
		});
	}

	private void loadMaxSimultaneousSettings() {
		int value = 4;
		
		try {
			value = Integer.parseInt(ZStreaming.getSettingManager().getSettings().get("max.download"));
		}catch(NumberFormatException ex) { }
		
		this.simultaneousBox.setValue(value);		
		this.simultaneousBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue)->{
			ZStreaming.getSettingManager().storeSettings("max.download", String.format("%d", newValue));
		});		
	}

	private void loadLangSettings() {
		this.langBox.setValue(this.langBox.getItems().stream().filter(item->item.getLocale().equals(Locale.getDefault())).findFirst().orElse(new LocaleItem(null)));
		
		this.langBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue)->{
			this.bundleFactory.setResources(newValue.getLocale());
			ZStreaming.getSettingManager().storeSettings("lang", newValue.getLocale().toString());
			this.settingBackTitleBtn.setText(this.bundleFactory.getString("general"));
		});
	}
	
	private void loadBooleanSettings(CheckBox check, String setting, boolean safeBool, Node...subs) {
		boolean bool = safeBool;
				
		try {
			int value = Integer.parseInt(ZStreaming.getSettingManager().getSettings().get(setting));
			
			if(value < 0 || value > 1)
				throw new NumberFormatException();
			
			bool = value > 0;
		}catch(NumberFormatException ex) { }
		
		check.setSelected(bool);
		
		for(Node sub : subs) {
			sub.setDisable(!check.isSelected());
		}
		
		check.selectedProperty().addListener((observable, oldValue, newValue)->{
			ZStreaming.getSettingManager().storeSettings(setting, String.format("%d", newValue ? 1 : 0));
			
			for(Node sub : subs) {
				sub.setDisable(!newValue);
				
				if(!newValue && sub instanceof ToggleButton){
					((Toggle)sub).setSelected(false);
				}
			}		
		});
	}
	
	private void loadGroupSettings(String setting, ToggleGroup group, CheckBox sup, int safeValue) {
		try {
			int value = Integer.parseInt(ZStreaming.getSettingManager().getSettings().get(setting));
			
			if(value > group.getToggles().size() - 1 || value < 0 || (sup != null && !sup.isSelected()))
				throw new NumberFormatException();
			group.getToggles().get(value).setSelected(true);		
		}catch(NumberFormatException  ex) {
			ZStreaming.getSettingManager().storeSettings(setting, String.format("%d", safeValue));
			group.getToggles().forEach(toggle->toggle.setSelected(false));
		}
		
		group.selectedToggleProperty().addListener((observable, oldValue, newValue)->{
			if(newValue != null) {
				ZStreaming.getSettingManager().storeSettings(setting, String.format("%d", group.getToggles().indexOf(newValue)));				
			}else {
				ZStreaming.getSettingManager().storeSettings(setting, String.format("%d", safeValue));
			}
		});
	}	
	
	private void loadPathSettings() {
		this.logPathText.setText(ZStreaming.getSettingManager().getSettings().get("log.path"));
		this.cookiePathText.setText(ZStreaming.getSettingManager().getSettings().get("cookie.path"));
		this.downPathText.textProperty().bindBidirectional(this.downloadDirTxt.textProperty());
		this.downStoreText.setText(ZStreaming.getSettingManager().getSettings().get("zdownloads.store"));
		this.defaultListPathText.setText(ZStreaming.getSettingManager().getSettings().get("list.path"));
		this.defaultIconListText.setText(ZStreaming.getSettingManager().getSettings().get("list.image.url"));
		this.historyPathText.setText(ZStreaming.getSettingManager().getSettings().get("history.path"));
		this.playerPathText.setText(ZStreaming.getSettingManager().getSettings().get("player.default"));
	}
	
	@FXML
	public void resetDefaultNameList(ActionEvent event) {
		final String defaultName = "MyList";
	
		this.defaultNameListText.setText(defaultName);
		this.formAddListBtn.getMediaList().setName(defaultName);
	}
	
	@FXML
	public void changeListsPath(MouseEvent event) {
		if(event.getPickResult().getIntersectedPoint().getX() > this.defaultListPathText.getWidth() - this.defaultListPathText.getPadding().getRight()) {
			this.chooseDir(this.defaultListPathText, "list.path");
		}
	}
	
	@FXML
	public void changeHistoryPath(MouseEvent event) {	
		if(event.getPickResult().getIntersectedPoint().getX() > this.historyPathText.getWidth() - this.historyPathText.getPadding().getRight()) {
			this.chooseDir(this.historyPathText, "history.path", "history.history");			
		}
	}
	
	@FXML
	public void changeCookiePath(MouseEvent event) {	
		if(event.getPickResult().getIntersectedPoint().getX() > this.cookiePathText.getWidth() - this.cookiePathText.getPadding().getRight()) {
			this.chooseDir(this.cookiePathText, "cookie.path");
		}
	}
	
	@FXML
	public void changeIconListsPath(MouseEvent event) {
		if(event.getPickResult().getIntersectedPoint().getX() > this.defaultIconListText.getWidth() - this.defaultIconListText.getPadding().getRight()) {
			ExtensionFilter filter = new ExtensionFilter("Image type (*.png, *.jpg, *.gif, *.tiff, *.ico)", "*.png", "*.jpg", "*.gif", "*.tiff", "*.ico");
			this.chooseFile(this.defaultIconListText, "list.image.url", filter);
		}
	}

	@FXML
	public void changeDownloadListStorePath(MouseEvent event) {	
		if(event.getPickResult().getIntersectedPoint().getX() > this.downStoreText.getWidth() - this.downStoreText.getPadding().getRight()) {
			File oldFile = new File(ZStreaming.getSettingManager().getSettings().get("zdownloads.store"));
			this.chooseDir(this.downStoreText, "zdownloads.store", "zdownloads.zsav");
			File newFile = new File(this.downStoreText.getText());
			
			if(!oldFile.equals(newFile)) {
				oldFile.delete();
				DownloadManager.storeDownlads();
			}
			
		}
	}
	
	@FXML
	public void changeDownloadPath(MouseEvent event) {	
		if(event.getPickResult().getIntersectedPoint().getX() > this.downPathText.getWidth() - this.downPathText.getPadding().getRight()) {
			this.chooseDir(this.downPathText, "download.path");
		}
	}
	
	@FXML
	public void changeLogPath(MouseEvent event) {		
		if(event.getPickResult().getIntersectedPoint().getX() > this.logPathText.getWidth() - this.logPathText.getPadding().getRight()) {
			
			this.chooseDir(this.logPathText, "log.path");
			
			try {
				WebBrowser.loggerManager.loadFileHandler();
				
			} catch (IOException | SecurityException e) {
				System.out.println("Creation log file failed");
				/*
				 * 
				 * WINDOW DIALOG Impossible Creation log file failed
				 * 
				 */
			}
		}
	}
	
	@FXML
	public void changePlayerPath(MouseEvent event) {		
		if(event.getPickResult().getIntersectedPoint().getX() > this.playerPathText.getWidth() - this.playerPathText.getPadding().getRight()) {
			ExtensionFilter filter = new ExtensionFilter("Executable type (*.exe)", "*.exe");
			this.chooseFile(this.playerPathText, "player.default", filter);
		}
	}
	
	@FXML
	public void openHistoryFolder(ActionEvent event) {
		this.openFolder(this.historyPathText.getText(), "history.path");
	}
	
	@FXML
	public void openListPathFolder(ActionEvent event) {
		this.openFolder(this.defaultListPathText.getText(), "list.path");
	}
	
	@FXML
	public void openIconListFolder(ActionEvent event) {
		this.openFolder(this.defaultIconListText.getText(), "list.image.url");
	}
	
	@FXML
	public void openCookieFolder(ActionEvent event) {
		this.openFolder(this.cookiePathText.getText(), "cookie.path");
	}
	
	@FXML
	public void openLogFolder(ActionEvent event) {
		this.openFolder(this.logPathText.getText(), "log.path");
	}
	
	@FXML
	public void openPlayerFolder(ActionEvent event) {		
		if(this.playerPathText.getText() != null && !this.playerPathText.getText().isEmpty()) {
			this.openFolder(this.playerPathText.getText(), "player.default");
		}
	}
	
	@FXML
	public void openStoreDownloadListFolder(ActionEvent event) {
		this.openFolder(this.downStoreText.getText(), "zdownloads.store");
	}	
	
	@FXML
	public void showGeneral(ActionEvent event) {
		this.showSetting(this.generalSettings, ((SettingButton)event.getSource()));
	}
	
	@FXML
	public void showDownload(ActionEvent event) {
		this.showSetting(this.downloadSettings, ((SettingButton)event.getSource()));
	}
	
	@FXML
	public void showLists(ActionEvent event) {
		this.showSetting(this.listSettings, ((SettingButton)event.getSource()));		
	}
	
	@FXML
	public void showConnection(ActionEvent event) {
		this.showSetting(this.connectionSettings, ((SettingButton)event.getSource()));		
	}
	
	@FXML
	public void showPlayer(ActionEvent event) {
		this.showSetting(this.playerSettings, (SettingButton)event.getSource());
	}

	private void showSetting(Pane pane, SettingButton button) {
		SettingButton[] buttons = new SettingButton[] {generalBtn, downloadBtn,  listsBtn, connectionBtn, playerBtn};
		
		long delay = buttons.length*70;
		
		for(SettingButton btn : buttons) {
			btn.setDisable(true);
			TranslateTransition translateButton = new TranslateTransition(Duration.millis(delay), btn);
			FadeTransition fadeButton = new FadeTransition(Duration.millis(delay), btn);

			fadeButton.setFromValue(1.0);
			fadeButton.setToValue(0.0);
			fadeButton.setDelay(Duration.millis(delay*1.5));
			
			translateButton.setFromX(0.0);	
			translateButton.setToX(this.settingContent.getWidth() - btn.getLayoutX() - btn.getWidth());
			translateButton.setDelay(Duration.millis(delay));
			
			delay -= 70;

			if(btn.equals(this.generalBtn)) {
				FadeTransition fadeSection = new FadeTransition(Duration.millis(50), this.settingContent);
				
				translateButton.setOnFinished(e->fadeSection.play());
				fadeSection.setFromValue(1.0);
				fadeSection.setToValue(0.5);
				
				fadeSection.setOnFinished(e->{
					pane.setScaleX(0.0);
					pane.setScaleY(0.0);
					pane.setTranslateX(this.settingContent.getWidth() - this.leftSide.getWidth());
					
					this.scrollSettingContent.setContent(pane);
					
					Timeline timeline = new Timeline();
					
					WritableValue<Double> writable = new WritableValue<Double>() {
						@Override
						public Double getValue() {
							return pane.getScaleX();
						}				
						@Override
						public void setValue(Double value) {
							pane.setScaleX(value);
							pane.setScaleY(value);
							pane.setTranslateX(value);
						}									
					};
					
					timeline.setOnFinished(ev->{
						this.settingBackWrapper.setScaleY(1.0);
						this.settingBackWrapper.setVisible(true);
						this.settingBackTitleBtn.setText(button.getText());
					});
					
					timeline.getKeyFrames().add(new KeyFrame(Duration.millis(100), new KeyValue(writable, 1.0)));
					timeline.setDelay(Duration.millis(100));
					timeline.play();
					
				});				
			}			
			fadeButton.play();
			translateButton.play();			
		}
	}
	
	@FXML
	public void backToSettings(ActionEvent event){
		if(!this.scrollSettingContent.getContent().equals(this.settingContent)) {
			this.settingBackWrapper.setVisible(false);
			this.settingBackWrapper.setScaleY(0.0);
			this.settingBackTitleBtn.setText(this.bundleFactory.getString("setting.sec.btn"));

			Pane pane = (Pane) this.scrollSettingContent.getContent();			
			pane.setDisable(true);
			
			Timeline timeline = new Timeline();
			
			WritableValue<Double> writable = new WritableValue<Double>() {
				@Override
				public Double getValue() {
					return pane.getScaleX();
				}				
				@Override
				public void setValue(Double value) {
					pane.setScaleX(value);
					pane.setScaleY(value);
					pane.setTranslateX(value);
				}									
			};
			
			timeline.setOnFinished(e->{
				SettingButton[] buttons = new SettingButton[] {generalBtn, downloadBtn,  listsBtn, connectionBtn, playerBtn};
				this.scrollSettingContent.setContent(this.settingContent);
				pane.setDisable(false);
				
				FadeTransition fadeSection = new FadeTransition(Duration.millis(300), this.settingContent);
				
				fadeSection.setToValue(1.0);

				long delay = 0;

				for(SettingButton btn : buttons) {
					btn.setDisable(false);
					FadeTransition fadeButton = new FadeTransition(Duration.millis(200), btn);
					TranslateTransition tranlateButton = new TranslateTransition(Duration.millis(200), btn);
					
					delay += 70;
					
					tranlateButton.setToX(.0);
					tranlateButton.setDelay(Duration.millis(delay));
					
					fadeButton.setFromValue(0.0);
					fadeButton.setToValue(1.0);
					
					tranlateButton.play();
					fadeButton.play();
				}
				
				fadeSection.play();			
			});
			
			timeline.getKeyFrames().add(new KeyFrame(Duration.millis(200), new KeyValue(writable, 0.0)));
			timeline.play();
		}
	}
	
	@FXML
	public void toggleSelection(MouseEvent event) {
		Toggle toggle = (Toggle)event.getSource();
		
		boolean selected = toggle.isSelected();
		
		((Region)toggle).setOnMouseReleased(e->{
			if(selected) {
				toggle.setSelected(false);
			}
		});
	}
	
	@FXML
	public void limitNameText(KeyEvent event) {
		if(this.defaultNameListText.getText().length() - this.defaultNameListText.getSelectedText().length() >= MediaListFormBuilder.NAME_MAX_LENGTH) {
			event.consume();
		}
	}
	
	@FXML
	public void restoreGeneralSettings(ActionEvent event) {
		SettingsFactory.restoreGeneral();
		
		this.langBox.setValue(LocaleItem.getDefault());
		this.startupCheck.setSelected(false);
		this.exitConfirmCheck.setSelected(true);
	}
	
	@FXML
	public void restoreDownloadSettings(ActionEvent event) {		
		File oldFile = new File(ZStreaming.getSettingManager().getSettings().get("zdownloads.store"));
		
		SettingsFactory.restoreDownload();

		this.simultaneousBox.setValue(4);
		this.modeAutoStartup.setSelected(false);
		this.deleteAutoCheck.setSelected(false);
		this.confirmDelDownCheck.setSelected(true);
		this.delaySlider.setValue(5.0);
		this.mainChartCheck.setSelected(true);
		this.singleChartCheck.setSelected(true);
		this.downPathText.setText(SettingsFactory.DOWNLOAD_PATH_VALUE);
		this.downStoreText.setText(SettingsFactory.ZDOWNLOAD_STORE_VALUE);
		
		ZStreaming.getSettingManager().load();
		
		File newFile = new File(ZStreaming.getSettingManager().getSettings().get("zdownloads.store"));
		
		if(!oldFile.equals(newFile)) {
			oldFile.delete();
			DownloadManager.storeDownlads();
		}
	}
	
	@FXML
	public void restoreListsSettings(ActionEvent event) {	
		SettingsFactory.restoreLists();
		
		ZStreaming.getSettingManager().load();
		
		this.defaultListPathText.setText(ZStreaming.getSettingManager().getSettings().get("list.path"));
		this.defaultIconListText.setText(ZStreaming.getSettingManager().getSettings().get("list.image.url"));
		this.resetDefaultNameList(event);
	}	
	
	@FXML
	public void restoreConnectionSettings(ActionEvent event) {	
		SettingsFactory.restoreConnection();
		
		ZStreaming.getSettingManager().load();
		
		this.logPathText.setText(ZStreaming.getSettingManager().getSettings().get("log.path"));
		this.cookiePathText.setText(ZStreaming.getSettingManager().getSettings().get("cookie.path"));
		this.historyPathText.setText(ZStreaming.getSettingManager().getSettings().get("history.path"));
	}
	
	@FXML
	public void restorePlayerSettings(ActionEvent event) {	
		SettingsFactory.clearPlayer();	
		
		ZStreaming.getSettingManager().load();

		this.playerPathText.setText(null);
	}
	
	
	@FXML
	public void onStartup(ActionEvent event) {
		if(this.startupCheck.isSelected()) {
			this.addToStartup();
			this.startTrayCheck.setDisable(!SystemTray.isSupported());
		}else 
			this.removeToStartup();		
	}
	
	private void addToStartup() {
		SystemCMDManager cmd = new SystemCMDManager();

		final String regPath = "HKCU\\SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Run";
		final String value = "ZStreaming";
		final String query = String.format("REG QUERY %s /V %s", regPath, value);
		
		try {				
			final String path = "\"" + new File(ZStreaming.getSettingManager().getSettings().get("root.path"), "zstreaming.exe\" -autorun").toString();				
			final String startup = String.format("REG ADD %s /V %s /T REG_SZ /F /D \"%s\"" , regPath, value, path);
			
			cmd.newProcess();
			if(cmd.exec(query).getResult().equals(Result.ERROR)) {
				cmd.newProcess();
				
				Response resp = cmd.exec(startup);
				
				if(resp.getResult().equals(Result.ERROR))
					throw new IOException(resp.getMessage().toString());
			}	
		}catch(IOException e) {
			/*
			 * 
			 * ERRORE
			 * 
			 * 
			 */
			System.out.println(e.getMessage());
			this.startupCheck.setSelected(false);
		}			
	}


	private void removeToStartup() {
		SystemCMDManager cmd = new SystemCMDManager();
		
		final String regPath = "HKCU\\SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Run";
		final String value = "ZStreaming";
		final String query = String.format("REG QUERY %s /V %s", regPath, value);

		try {				
			final String delete = String.format("REG DELETE %s /V %s /F" , regPath, value);
			
			cmd.newProcess();
			if(!cmd.exec(query).getResult().equals(Result.ERROR)) {
				cmd.newProcess();

				Response resp = cmd.exec(delete);

				if(resp.getResult().equals(Result.ERROR))
					throw new IOException(resp.getMessage().toString());
			}
		}catch(IOException e) {
			/*
			 * 
			 * ERRORE
			 * 
			 * 
			 */
			System.out.println(e.getMessage());
			this.startupCheck.setSelected(true);
		}
	}


	@FXML
	public void f(Event e) {
		System.out.println(((MouseEvent) e).getPickResult().getIntersectedNode());		
		System.out.println(((MouseEvent) e).getPickResult().getIntersectedNode().getParent());
		System.out.println(((MouseEvent) e).getPickResult().getIntersectedNode().getParent().getParent());
		System.out.println(((MouseEvent) e).getPickResult().getIntersectedNode().getParent().getParent().getParent());
		System.out.println(((MouseEvent) e).getPickResult().getIntersectedNode().getParent().getParent().getParent().getParent().getParent());
		System.out.println(((MouseEvent) e).getPickResult().getIntersectedNode().getParent().getParent().getParent().getParent().getParent().getParent());
		
		System.out.println(e);
	}
	
	private void openFolder(String path, String setting) {
		File dir = new File(path);
		
		while(dir.isFile()) {
			dir = dir.getParentFile();
		}
		
		if(!dir.isDirectory()) {
			/*
			 * 
			 * WINDOW DIALOG NOT EXIST
			 * 
			 */
			dir = new File("").getAbsoluteFile();
		}
		if(setting != null) {
			ZStreaming.getSettingManager().storeSettings(setting, dir.toString());
		}
		
		Desktop desktop = Desktop.getDesktop();
		
		try {
			desktop.open(dir);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}	

	private void chooseDir(TextField textField, String setting) {
		this.chooseDir(textField, setting, null);
	}
	
	private void chooseFile(TextField textField, String setting, ExtensionFilter... filter) {
		FileChooser fileChooser = new FileChooser();
		
		fileChooser.getExtensionFilters().addAll(filter);
		
		File path = textField.getText() == null ? null : new File(textField.getText().trim());
		
		if(path == null || !path.toPath().getRoot().toFile().exists()) {
			/*
			 * 
			 * WINDOW DIALOG NOT EXIST
			 * 
			 */
			fileChooser.setInitialDirectory(new File("").getAbsoluteFile());
		}else {
			while(!path.isDirectory()) path  = path.getParentFile();
			
			fileChooser.setInitialDirectory(path);
		}
		
		File newPath = fileChooser.showOpenDialog(this.root.getScene().getWindow());
		
		if(newPath != null) {
			ZStreaming.getSettingManager().storeSettings(setting, newPath.toString());
			textField.setText(newPath.toString());
		}
	}

	
	private void chooseDir(TextField textField, String setting, String file) {		
		DirectoryChooser dirChooser = new DirectoryChooser();
		File path = new File(textField.getText().trim());
				
		if(!path.toPath().getRoot().toFile().exists()) {
			/*
			 * 
			 * WINDOW DIALOG NOT EXIST
			 * 
			 */
			dirChooser.setInitialDirectory(new File("").getAbsoluteFile());
		}else {
			while(!path.isDirectory()) path  = path.getParentFile();
			
			dirChooser.setInitialDirectory(path);
		}
		
		File newPath = dirChooser.showDialog(this.root.getScene().getWindow());
		
		if(newPath != null) {
			newPath = file != null ? new File(newPath, file) : newPath;
			ZStreaming.getSettingManager().storeSettings(setting, newPath.toString());
			textField.setText(newPath.toString());
		}	
	}

	public void showWithOptions() {
		Toggle toggle = this.startupGroup.getSelectedToggle();
		
		if(toggle != null) {
			if(toggle.equals(this.startMinCheck)) {
				this.primaryStage.show();
				this.primaryStage.setIconified(true);
			}else
				this.trayIcon.show();
		}else
			this.primaryStage.show();
	}	
	
	public void enableDisableChart(ActionEvent event) {
		if(event.getSource().equals(this.mainChartCheck)) {
			if(this.mainChartCheck.isSelected())
				this.activeMainChart();
			else
				this.deactiveMainChart();
		}else if(event.getSource().equals(this.singleChartCheck)) {
			if(this.singleChartCheck.isSelected())
				this.activeSingleChart();
			else
				this.deactiveSingleChart();

		}
	}

	private void deactiveSingleChart() {
		for(DownloadValues downloadValues : this.observableDownloadList) {
			if(DownloadManager.getDownloads().getTask(downloadValues.getDownload()) != null) {
				downloadValues.unbindChart(this.chartCounter.getEnabledCharts());
			}
		}		
		if(this.singleChart.isBound()) this.singleChart.unbind();
	}

	private void activeSingleChart() {
		for(DownloadValues downloadValues : this.observableDownloadList) {
			if(DownloadManager.getDownloads().getTask(downloadValues.getDownload()) != null) {
				downloadValues.bindChart(this.chartCounter.getEnabledCharts());
			}
		}
		
		this.bindStatDownload(this.downloadTableView.getSelectionModel().getSelectedItem());
	}
	
	@FXML
	public void resetStatistics(ActionEvent event) {
		SessionStatistics.clear();
		this.bindStatDownload(this.downloadTableView.getSelectionModel().getSelectedItem());
		this.resetChart(this.listCharts);
		this.resetChart(this.mainChart);
	}
}
