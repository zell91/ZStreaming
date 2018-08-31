package com.zstreaming.browser;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.util.network.OnlineChecker;
import com.zstreaming.browser.http.Connection;
import com.zstreaming.browser.http.HttpRequest;
import com.zstreaming.browser.http.HttpResponse;
import com.zstreaming.browser.http.Session;
import com.zstreaming.browser.http.cookies.CookieManager;
import com.zstreaming.browser.view.ViewBrowser;
import com.zstreaming.download.Download;
import com.zstreaming.download.exception.DownloadException;
import com.zstreaming.statistics.SessionStatistics;

public class WebBrowser implements Serializable {

	private static final long serialVersionUID = 3294113415569884321L;
	
	public final static String DEFAULT_USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; WOW64; rv:56.0) Gecko/20100101 Firefox/56.0";	
	public final static String CHARSET_ENCODING = "UTF-8";
	
	public final static int READ_TIMEOUT_DEFAULT = 15000;
	public final static int CONNECTION_TIMEPUT_DEFAULT = 30000;
	
	public final static boolean USE_CACHES_DEFAULT = false;
		
	public final static LoggerManager loggerManager = new LoggerManager();
	public final static CookieManager cookieManager = new CookieManager();
	public final static List<HttpResponse> history = Collections.synchronizedList(new ArrayList<>());
	
	private int read_timeout;
	private int connection_timeout;
	
	private boolean use_caches;
	private boolean isConnected;

	private Session session;
	
	private transient HttpRequest request;
	private transient Connection connection;
	private transient ViewBrowser viewBrowser;
	
	public WebBrowser(ViewBrowser viewBrowser) {
		WebBrowser.cookieManager.loadFromFileSystem();
		this.read_timeout = READ_TIMEOUT_DEFAULT;
		this.connection_timeout = CONNECTION_TIMEPUT_DEFAULT;
		this.use_caches = USE_CACHES_DEFAULT;
		this.viewBrowser = viewBrowser;
	}
	
	public WebBrowser() {
		this(WebBrowser.initViewBrowser());
	}
	
	private static ViewBrowser initViewBrowser() {
		return ViewBrowser.createBrowser();
	}

	public boolean onSession() {
		if(this.request == null) return false;		
		return this.isConnected;
	}
	
	public void setTimeout(int read, int connection) {
		this.read_timeout = read;
		this.connection_timeout = connection;
	}
	
	public void setUseCaches(boolean cache) {
		this.use_caches = cache;
	}

	public void setRequest(HttpRequest request) {
		this.request = request;
	}
	
	public HttpRequest getRequest() {
		return request;
	}
	
	public ViewBrowser getSessionView() {
		return viewBrowser;
	}

	public void setViewBrowser(ViewBrowser viewBrowser) {
		this.viewBrowser = viewBrowser;

	}
	
	private void setSession(Session session) {
		this.session = session;
	}	
	
	private void setConnection(Connection connection) {
		this.connection = connection;
	}
	
	public WebBrowser clone() {
		WebBrowser webBrowser = new WebBrowser(this.viewBrowser);
		webBrowser.setTimeout(this.read_timeout, this.connection_timeout);
		webBrowser.setUseCaches(this.use_caches);
		webBrowser.setRequest(this.request);
		webBrowser.setSession(this.session);
		webBrowser.setConnection(this.connection);
		
		return webBrowser;
	}

	public synchronized Session sendRequest() {
		if(!this.isConnected) {
			if(this.request != null) {
				this.connection = new Connection(this.request);
				this.connection.setTimeout(this.read_timeout, this.connection_timeout);
				try {
					this.connection.connect();
				} catch (UnknownHostException e) {
					loggerManager.error("Fatal Error: Impossibile connettersi all' host \"" + this.request.getUrl() + "\".", e);
					SessionStatistics.setState("connection.failed");
					OnlineChecker.checkOnline();
				} catch (IOException e) {
					loggerManager.error("Fatal Error: Connessione all'host \"" + this.request.getUrl() + "\" rifiutata.", e);
					SessionStatistics.setState("connection.failed");
					OnlineChecker.checkOnline();
				}finally {
					this.isConnected = this.connection.getResponseCode() != -1;
					if(!this.isConnected) return null;
				}
				
				this.session = new Session(this.connection.getURL(), this.connection.getHeaderResponse(), this.connection.getResponseCode(), this.connection.getResponseMessage());
				this.session.setEndSession(this.request.getRequestHeader().getRequestHeader().get("Connection").equals("close") ? System.currentTimeMillis() : -1);
				return this.session;	
			}else {
				return null;
			}
		}else {
			if(this.connection != null)	this.connection.disconnect();
			this.setConnected(false);
			return this.session = this.sendRequest();
		}
	}
	
	public synchronized Session sendRequest(HttpRequest request) {		
		this.request = request;
		return this.session = this.sendRequest();
	}
		
	public synchronized HttpResponse getResponse() {
		try {
			return this.getResponse(0);
		} catch (InterruptedException e) { return null; }
	}
	
	public HttpResponse getResponse(int wait) throws InterruptedException {
		
		if(wait > 0) Thread.sleep(wait);
		
		if(this.session != null) {
			if(this.session.getSessionDuration() > 15000) {
				loggerManager.warning("Sessione scaduta. Inizio nuova sessione.", null);
				this.session = this.sendRequest();
			}
			
			try {
				return this.connection.readResponse();
			} catch (IOException e) {
				loggerManager.error("Fatal Error: La connessione è stata interrotta.", e);
			}finally {
				this.setConnected(false);
			}
		}
		
		return null;
	}
	
	private void setConnected(boolean connected) {
		this.isConnected = connected;
	}

	public Session getSession() {
		return session;
	}
	
	public void download(HttpRequest request, OutputStream outputStream, Download download) throws UnknownHostException, IOException, InterruptedException, DownloadException {
		this.session = this.sendRequest(request);
		if(this.session == null) throw new UnknownHostException();
		if(this.session.getContentType().equals(download.getMedia().getMimeType())) {
			this.connection.copyBytes(outputStream, download);
		} else {
			throw new DownloadException(download.getState());
		}
			
			
	}
	
	public void download(HttpRequest[] requests, OutputStream outputStream, Download download) throws UnknownHostException, IOException, InterruptedException, DownloadException {
		for(HttpRequest request : requests) {
			this.download(request, outputStream, download);
		}
	}

}
