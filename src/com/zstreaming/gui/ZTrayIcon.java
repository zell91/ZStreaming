package com.zstreaming.gui;

import java.awt.AWTException;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;

import com.util.locale.ObservableResourceBundle;

import javafx.application.Platform;
import javafx.stage.Stage;

public class ZTrayIcon extends TrayIcon{
	
	private static final String ICON = "";
	private PopupMenu popupMenu;
	private MenuItem showItem;
	private MenuItem exitItem;
	private Stage primaryStage;
		
	public ZTrayIcon(Stage primaryStage) {
		super(Toolkit.getDefaultToolkit().getImage(ICON));
		
		this.primaryStage = primaryStage;
		this.createPopupMenu();
		this.setMenuActions();
		
		this.setPopupMenu(this.popupMenu);
	}
	
	private void createPopupMenu() {
		this.popupMenu = new PopupMenu();
		this.showItem = new MenuItem(ObservableResourceBundle.getLocalizedString("show"));
		this.exitItem = new MenuItem(ObservableResourceBundle.getLocalizedString("exit"));
		
		this.popupMenu.add(this.showItem);
		this.popupMenu.addSeparator();
		this.popupMenu.add(this.exitItem);	
	}


	private void setMenuActions() {
		this.showItem.addActionListener(e->{
			Platform.runLater(()->{
				this.primaryStage.show();
				Platform.setImplicitExit(true);
			});
			SystemTray.getSystemTray().remove(this);
		});
		this.addActionListener(this.showItem.getActionListeners()[0]);
		this.exitItem.addActionListener(e->System.exit(0));		
	}
	
	public void show() {		
		if(SystemTray.isSupported()) {			
			this.showItem.setLabel(ObservableResourceBundle.getLocalizedString("show"));
			this.exitItem.setLabel(ObservableResourceBundle.getLocalizedString("exit"));
			
			Platform.setImplicitExit(false);
			Platform.runLater(()->this.primaryStage.close());
						
			try {
				SystemTray.getSystemTray().add(this);
			} catch (AWTException e) {
				e.printStackTrace();
			}
		}else
			throw new IllegalArgumentException("This operation cannot be performed on the current system");				
	}
	

}
