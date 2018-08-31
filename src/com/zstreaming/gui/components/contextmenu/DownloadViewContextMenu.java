package com.zstreaming.gui.components.contextmenu;

import java.io.File;

import com.util.locale.ObservableResourceBundle;
import com.zstreaming.download.Download;
import com.zstreaming.gui.controller.DownloadViewContextMenuController;
import com.zstreaming.gui.download.DownloadValues;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class DownloadViewContextMenu extends ContextMenu {
	
	private DownloadViewContextMenuController controller;
	
	private MenuItem playCMItem, pauseCMItem, stopCMItem, cancelCMItem, openFileCMItem, openFolderCMItem, infoCMItem, removeSelectedCMItem, removeCompletedCMItem, removeSingleCMItem, removeCancelledCMItem, removeDoneCMItem, changePathCMItem;
	private Menu priorityCMitem, removeCMItem;
	private RadioMenuItem undefindedPriority, minPriority, lowPriority, mediumPriority, highPriority, maxPriority;

	private ToggleGroup toggleGroup;
	
	private TableView<DownloadValues> downloadList;
	
	public DownloadViewContextMenu(TableView<DownloadValues> downloadList) {
		this.downloadList = downloadList;
		this.getStyleClass().add("download-view-context-menu");
		this.toggleGroup = new ToggleGroup();
		this.setup();
		this.getItems().addAll(this.playCMItem, this.pauseCMItem, this.stopCMItem, new SeparatorMenuItem(), this.cancelCMItem, new SeparatorMenuItem(), this.priorityCMitem, this.changePathCMItem, new SeparatorMenuItem(), this.openFileCMItem, this.openFolderCMItem, new SeparatorMenuItem(), this.removeCMItem, new SeparatorMenuItem(), this.infoCMItem);
		this.priorityCMitem.getItems().addAll(this.undefindedPriority, this.minPriority, this.lowPriority, this.mediumPriority, this.highPriority, this.maxPriority);
		this.removeCMItem.getItems().addAll(this.removeSingleCMItem, this.removeSelectedCMItem, new SeparatorMenuItem(), this.removeCancelledCMItem, this.removeCompletedCMItem, this.removeDoneCMItem);
		this.controller = new DownloadViewContextMenuController(downloadList, this, this.playCMItem, this.pauseCMItem, this.stopCMItem, this.cancelCMItem, this.removeCMItem, this.openFileCMItem, this.openFolderCMItem, this.infoCMItem, this.priorityCMitem, this.undefindedPriority, this.minPriority, this.lowPriority, this.mediumPriority, this.highPriority, this.maxPriority, this.changePathCMItem, this.removeSelectedCMItem, this.removeCompletedCMItem,this.removeSingleCMItem, this.removeDoneCMItem,this.removeCancelledCMItem, this.toggleGroup);
		this.setActions();
	}
	
	public DownloadViewContextMenuController getController() {
		return this.controller;
	}
	
	private void setup() {
		this.setPlayItem();
		this.setPauseItem();
		this.setStopItem();
		this.setCancelItem();
		this.setChangePathItem();
		this.setRemoveItem();
		this.setRemoveCompletedItem();
		this.setRemoveSelectedItem();
		this.setRemoveSingleItem();
		this.setRemoveCancelledItem();
		this.setRemoveDoneItem();
		this.setOpenFileItem();
		this.setOpenFolderItem();
		this.setInfoItem();
		this.setPriorityItem();
		this.setUndefinedPriority();
		this.setMinPriority();
		this.setLowPriority();
		this.setMediumPriority();
		this.setHighPriority();
		this.setMaxPriority();
	}
	
	private void setItem(Menu menu, String id, String text, String imageURI) {
		menu.setId(id);
		menu.setText(text);
		menu.getStyleClass().add("download_cm_item");
		ImageView imgView = new ImageView(new Image(imageURI));
		imgView.setFitWidth(10);
		imgView.setFitHeight(10);
		menu.setGraphic(imgView);
		menu.setMnemonicParsing(false);
	}
	
	private void setItem(MenuItem menuItem, String id, String text, String imageURI) {
		menuItem.setId(id);
		menuItem.setText(text);
		menuItem.getStyleClass().add("download_cm_item");
		ImageView imgView = new ImageView(new Image(imageURI));
		imgView.setFitWidth(10);
		imgView.setFitHeight(10);
		menuItem.setGraphic(imgView);
		menuItem.setMnemonicParsing(false);
	}
	
	private void setSubItem(RadioMenuItem menuItem, String id, String text, Download.Priority priority) {
		menuItem.setId(id);
		menuItem.getStyleClass().addAll("download_cm_item", "radio_cm_item");
		menuItem.setText(text);
		menuItem.setToggleGroup(toggleGroup);
		menuItem.setMnemonicParsing(false);
		menuItem.setUserData(priority);
	}
	
	private void setPlayItem() {
		this.playCMItem = new MenuItem(); 
		this.setItem(this.playCMItem, "playCMItem", ObservableResourceBundle.getLocalizedString("start"), new File("images/_play.png").toURI().toString());
	}

	private void setPauseItem() {
		this.pauseCMItem = new MenuItem(); 
		this.setItem(this.pauseCMItem, "pauseCMItem", ObservableResourceBundle.getLocalizedString("pause"), new File("images/pause.png").toURI().toString());
	}

	private void setStopItem() {
		this.stopCMItem = new MenuItem(); 
		this.setItem(this.stopCMItem, "stopCMItem", ObservableResourceBundle.getLocalizedString("skip"), new File("images/skip.png").toURI().toString());
	}

	private void setCancelItem() {
		this.cancelCMItem = new MenuItem(); 
		this.setItem(this.cancelCMItem, "cancelCMItem", ObservableResourceBundle.getLocalizedString("interrupt"), new File("images/_stop.png").toURI().toString());
	}
	
	private void setChangePathItem() {
		this.changePathCMItem = new MenuItem();
		this.setItem(this.changePathCMItem, "changePathCMItem", ObservableResourceBundle.getLocalizedString("change.path"), new File("images/change_path.png").toURI().toString());
	}

	private void setRemoveItem() {
		this.removeCMItem = new Menu(); 
		this.setItem(this.removeCMItem, "removeCMItem", ObservableResourceBundle.getLocalizedString("remove"), new File("images/_remove.png").toURI().toString());		
	}
	
	private void setRemoveSelectedItem() {
		this.removeSelectedCMItem = new MenuItem();
		this.setItem(this.removeSelectedCMItem, "removeSelectedCMItem", ObservableResourceBundle.getLocalizedString("remove.all.selected"), new File("images/remove_multi_select.png").toURI().toString());
	}

	private void setRemoveCompletedItem() {
		this.removeCompletedCMItem = new MenuItem();
		this.setItem(this.removeCompletedCMItem, "removeCompletedCMItem", ObservableResourceBundle.getLocalizedString("remove.completed"), new File("images/remove_completed.png").toURI().toString());
	}

	private void setRemoveCancelledItem() {
		this.removeCancelledCMItem = new MenuItem();
		this.setItem(this.removeCancelledCMItem, "removeCancelledCMItem", ObservableResourceBundle.getLocalizedString("remove.interrupted"), new File("images/remove_cancelled.png").toURI().toString());
	}

	private void setRemoveDoneItem() {
		this.removeDoneCMItem = new MenuItem();
		this.setItem(this.removeDoneCMItem, "removeDoneCMItem", ObservableResourceBundle.getLocalizedString("remove.interrupted.completed"), new File("images/remove_done.png").toURI().toString());
	}
	
	private void setRemoveSingleItem() {
		this.removeSingleCMItem = new MenuItem();
		this.setItem(this.removeSingleCMItem, "removeSingleCMItem", ObservableResourceBundle.getLocalizedString("remove.selected"), new File("images/remove_select.png").toURI().toString());
	}
	
	private void setOpenFolderItem() {
		this.openFolderCMItem = new MenuItem(); 
		this.setItem(this.openFolderCMItem, "openFolderCMItem", ObservableResourceBundle.getLocalizedString("open.dest.folder"), new File("images/open_folder.png").toURI().toString());
	}
	
	private void setOpenFileItem() {
		this.openFileCMItem = new MenuItem(); 
		this.setItem(this.openFileCMItem, "openFileCMItem", ObservableResourceBundle.getLocalizedString("open.file"), new File("images/open_file.png").toURI().toString());
	}

	private void setInfoItem() {
		this.infoCMItem = new MenuItem(); 
		this.setItem(this.infoCMItem, "infoCMItem", ObservableResourceBundle.getLocalizedString("media.info"), new File("images/media_info.png").toURI().toString());
	}

	private void setPriorityItem() {
		this.priorityCMitem = new Menu(); 
		this.setItem(this.priorityCMitem, "priorityCMitem", ObservableResourceBundle.getLocalizedString("priority"), new File("images/priority.png").toURI().toString());
	}

	private void setUndefinedPriority() {
		this.undefindedPriority = new RadioMenuItem(); 
		this.setSubItem(this.undefindedPriority, "undefindedPriority",  ObservableResourceBundle.getLocalizedString("no.priority"), Download.Priority.UNDEFINED);		
	}

	private void setMinPriority() {
		this.minPriority = new RadioMenuItem(); 
		this.setSubItem(this.minPriority, "minPriority", ObservableResourceBundle.getLocalizedString("minimum"), Download.Priority.MIN);			
	}

	private void setLowPriority() {
		this.lowPriority = new RadioMenuItem(); 
		this.setSubItem(this.lowPriority, "lowPriority", ObservableResourceBundle.getLocalizedString("low"), Download.Priority.LOW);			
	}

	private void setMediumPriority() {
		this.mediumPriority = new RadioMenuItem(); 
		this.setSubItem(this.mediumPriority, "mediumPriority", ObservableResourceBundle.getLocalizedString("medium"), Download.Priority.MEDIUM);			
	}

	private void setHighPriority() {
		this.highPriority = new RadioMenuItem(); 
		this.setSubItem(this.highPriority, "highPriority", ObservableResourceBundle.getLocalizedString("high"), Download.Priority.HIGH);			
	}

	private void setMaxPriority() {
		this.maxPriority = new RadioMenuItem(); 
		this.setSubItem(this.maxPriority, "maxPriority", ObservableResourceBundle.getLocalizedString("max"), Download.Priority.MAX);		
	}

	private void setActions() {
		this.playCMItem.setOnAction(e->this.controller.play(e));
		this.pauseCMItem.setOnAction(e->this.controller.pause(e));
		this.stopCMItem.setOnAction(e->this.controller.stop(e));
		this.cancelCMItem.setOnAction(e->this.controller.cancel(e));
		this.removeSingleCMItem.setOnAction(e->this.controller.removeSingle(e));
		this.removeCompletedCMItem.setOnAction(e->this.controller.removeCompleted(e));
		this.removeCancelledCMItem.setOnAction(e->this.controller.removeCancelled(e));
		this.removeDoneCMItem.setOnAction(e->this.controller.removeDone(e));
		this.removeSelectedCMItem.setOnAction(e->this.controller.removeSelected(e));
		this.openFolderCMItem.setOnAction(e->this.controller.openFolder(e));
		this.openFileCMItem.setOnAction(e->this.controller.openFile(e));
		this.infoCMItem.setOnAction(e->this.controller.openInfo(e));
		this.changePathCMItem.setOnAction(e->this.controller.changePath(e));
		this.downloadList.setOnContextMenuRequested(e->this.controller.updateContextMenu(e));
	}
}
