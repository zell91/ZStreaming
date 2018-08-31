package com.util.network;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

import com.zstreaming.statistics.SessionStatistics;

import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;

public class OnlineChecker {

	private static SimpleBooleanProperty connected = new SimpleBooleanProperty();
	private static final String ADDRESS = "qwert.altervista.org";
	private static final int PORT = 80;
	private static Thread onlineCheckerTask;
	
	static {
		SessionStatistics.connectionProperty().bind(OnlineChecker.connected);

		OnlineChecker.onlineCheckerTask = new Thread(()->{
			
			NetworkInterface interf = null;
			
			while(true) {
				
				if(interf != null) {
					try {
						if(!interf.isLoopback() && interf.isUp()) {
							if(!OnlineChecker.isConnected()) {
								OnlineChecker.setConnected(true);
							}
						}else {
							throw new SocketException();
						}
						
					} catch (SocketException e) {
						interf = OnlineChecker.checkNetworkInterface();
						continue;
					}
					
				}else {
					if(OnlineChecker.isConnected()) {
						OnlineChecker.setConnected(false);
					}
					
					interf = OnlineChecker.checkNetworkInterface();
					if(interf != null) continue;
				}
				
				synchronized(OnlineChecker.connected) {
					try {
						OnlineChecker.connected.wait(5000);
					} catch (InterruptedException e) {	}
				}
				
			}
			
		});			
	}

	private OnlineChecker() { }
	
	private static boolean isConnected() {
		return OnlineChecker.connected.get();
	}
	
	public static void setConnected(boolean online) {
		Platform.runLater(()->OnlineChecker.connected.set(online));
	}
	
	public static void start() {
		if(OnlineChecker.onlineCheckerTask.getState().equals(Thread.State.NEW) || OnlineChecker.onlineCheckerTask.isInterrupted()) {				
			OnlineChecker.onlineCheckerTask.setDaemon(true);
			OnlineChecker.onlineCheckerTask.setName("OnlineCheckerTask");
			OnlineChecker.onlineCheckerTask.start();
		}
	}
	
	public static NetworkInterface checkNetworkInterface() {
		Enumeration<NetworkInterface> networkInterfaces = null;		
		
		try {
			networkInterfaces = NetworkInterface.getNetworkInterfaces();
		} catch (SocketException e) {
			return null;
		}
		
		while(networkInterfaces.hasMoreElements()) {
			NetworkInterface interf = networkInterfaces.nextElement();
			
			try {
				if(interf.isUp() && !interf.isLoopback()) {
					
					Enumeration<InetAddress> intefAddresses = interf.getInetAddresses();
					
					while(intefAddresses.hasMoreElements()) {
						try {
							InetAddress interfAddress = intefAddresses.nextElement();
							SocketAddress remoteAddr = new InetSocketAddress(InetAddress.getByName(ADDRESS), PORT);
							SocketAddress localAddr = new InetSocketAddress(interfAddress, 0);
						
							try(Socket socket = new Socket()){
								socket.setKeepAlive(false);
								
								socket.bind(localAddr);
								socket.connect(remoteAddr, 150000);
								
								System.out.println("PING");
								
								return interf;																
							} catch (IOException e) {	}
							
						}catch(UnknownHostException ex) {	}
					}
					
				}
			} catch (SocketException e) {	}		
		}
		
		return null;
	}
	
	public static boolean checkOnline() {
		return checkNetworkInterface() != null;
	}
}
