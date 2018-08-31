package com.zstreaming.browser.http;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.net.ssl.HttpsURLConnection;

import com.zstreaming.browser.LoggerManager;
import com.zstreaming.browser.WebBrowser;
import com.zstreaming.browser.http.cookies.Cookie;
import com.zstreaming.download.Download;
import com.zstreaming.download.DownloadManager;

public class Connection {
	
	private final static LoggerManager LOGGER = WebBrowser.loggerManager;

	private URL url;
	private HttpResponse response;
	private HttpRequest request;
	private String bodyRequest;
	
	private HttpURLConnection connection; 
	private List<HttpResponse> sessionHistory;

	private int read_timeout;
	private int connection_timeout;
	private boolean use_caches;
	private boolean follow_redirect;
	
	private int responseCode;

	private String contentType;
	private String responseMessage;

	private Map<String, List<String>> headerResponse;

	private long start;
	
	private final static HttpMethod POST = HttpMethod.POST;
	//private final static HttpMethod GET = HttpMethod.GET;

	
	public Connection(HttpRequest request) {
		this.request = request;
		this.url = request.getUrl();
		this.bodyRequest = request.getBody();
		this.sessionHistory = Collections.synchronizedList(new ArrayList<>());
		this.follow_redirect = true;
		this.responseCode = -1;
	}
	
	public void setTimeout(int read, int connection) {
		this.read_timeout = read;
		this.connection_timeout = connection;
	}
	
	protected void setUseCaches(boolean cache) {
		this.use_caches = cache;
	}
	
	protected void setFollowRedirect(boolean follow_redirect) {
		this.follow_redirect = follow_redirect;
	}	

	public int getResponseCode() {
		return this.responseCode;
	}	
	
	public String getResponseMessage() {
		return responseMessage;
	}
	
	public int connect() throws ProtocolException, IOException {			
		LOGGER.info("Avvio richiesta all'indirizzo: \"" + url + "\".");

		if(url == null) return -1;
		
		if(url.getProtocol().equals("https")) {
			this.connection = (HttpsURLConnection) url.openConnection();
		}else {
			this.connection = (HttpURLConnection) url.openConnection();
		}		
		
		this.setOptions();
		
		this.start = System.currentTimeMillis();
		
		if(Objects.nonNull(this.bodyRequest) && this.connection.getDoOutput()) this.sendQuery();
		
		this.checkResponseHeader();
		
		this.contentType = this.connection.getContentType();
		this.headerResponse = this.connection.getHeaderFields();
		this.url = this.connection.getURL();
		
		return responseCode;
	}
	
	private void checkResponseHeader() throws IOException {
		
		int code = this.responseCode = this.connection.getResponseCode();
		String message = this.responseMessage = this.connection.getResponseMessage();

		switch(code) {
			case 201:
				//Created
			case 202:
				//Accepted
			case 203:
				//Non-Authoritative Information
			case 204:
				//No Content
			case 205:
				//Reset Content
			case 206:
				//Partial Content
			case 200:
				LOGGER.info("Connessione effettuata. Response code: \"" + code + " " + message  + "\".");
				break;
			case 300:
				//Multiple Choices
			case 301:
				//Moved Permanently
			case 302:
				if(this.follow_redirect) {
					String location = this.connection.getHeaderField("Location");
										
					if(location == null || location.isEmpty()) {
						LOGGER.error("Redirection not success.", null);
						throw new IOException();
					}
					
					URL newURL = new URL(location);
					
					LOGGER.info("Reindirizzamento all'URL :\"" + location + "\"");
					
					this.redirect(newURL);
				}	
				break;
			case 400:
				LOGGER.error("Error 400. The request is badly formed.", null);
				break;
			case 404:
				LOGGER.error("Error 404 Page Not Found", null);
				break;
			case 500:
				LOGGER.error("Error 500 Internal Server Error", null);
				break;
			case 501:
			case 502:
			case 503:
			case 504:
	
				break;
		}
		
	}

	public HttpResponse readResponse() throws IOException {	
		if(this.responseCode == -1 || this.request.getRequestHeader().getRequestHeader().get("Connection").equals("close")) return null;
				
		if(this.response == null) {
			this.response = new HttpResponse(this.start);
			this.response.setURL(this.connection.getURL());
			this.response.setCode(this.connection.getResponseCode());
			this.response.setResponseMessage(this.connection.getResponseMessage());
			this.response.setResponseHeader(this.connection.getHeaderFields());	
			this.response.setContentType(connection.getContentType());
			List<Cookie> cookies = WebBrowser.cookieManager.load(this.response.getURL());
			this.response.setCookies(cookies);
			
			this.sessionHistory.add(this.response);
			WebBrowser.history.add(this.response);

			InputStream is = null;
			
			try {
				is = this.connection.getInputStream();
			}catch(IOException ex) {
				is = this.connection.getErrorStream();
			}
			
			if(is == null) {
				LOGGER.error("Impossibile leggere la risorsa.", null);
				this.response.setEndSession(System.currentTimeMillis());
				return this.response;
			}
			
			this.response.setContent(this.readContent(is));
			this.response.setEndSession(System.currentTimeMillis());
		}
		
		return this.response;
	}
	
	private synchronized void setOptions() throws ProtocolException {
		this.connection.setRequestMethod(this.request.getMethod().toString());
		this.connection.setDoOutput(this.request.getMethod().equals(POST));
		this.connection.setDoInput(true);
		this.connection.setConnectTimeout(this.connection_timeout);
		this.connection.setReadTimeout(this.read_timeout);
		this.connection.setUseCaches(this.use_caches);
		this.connection.setRequestProperty("User-agent", this.request.getUserAgent());
		this.request.getRequestHeader().set(this.connection);
		this.connection.addRequestProperty("Cookie", this.request.getCookies());
	}
	
	private StringBuilder readContent(InputStream is) throws IOException {		
		StringBuilder content = new StringBuilder();
		
		try(BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"))){			
			String line;			
			while(Objects.nonNull(line = reader.readLine())) content.append(line + "\r\n");			
		}
										
		return content;		
	}	

	public void redirect(URL url) throws IOException {	
		this.request.setUrl(url);
		this.setRequest(this.request);	
		this.connect();		
	}
	
	private void setRequest(HttpRequest request) {
		this.request = request;
		this.url = this.request.getUrl();
	}

	private void sendQuery() throws IOException {
		try(BufferedWriter output = new BufferedWriter(new OutputStreamWriter(this.connection.getOutputStream(), "UTF-8"))) {			
			output.write(this.bodyRequest);			
		}
	}

	public String getContentType() {
		return this.contentType;
	}

	public void disconnect() {
		this.connection.disconnect();
	}

	public Map<String, List<String>> getHeaderResponse() {
		return headerResponse;
	}

	public URL getURL() {
		return url;
	}
	
	public void copyBytes(OutputStream outputStream, Download download)  throws UnknownHostException, IOException, InterruptedException{		
		try(DataInputStream in = new DataInputStream(this.connection.getInputStream())){
			int read = 0;

			byte[] buffer = new byte[(int) Math.min(8*1024, download.getMedia().getSize().getRealSize())];

			download.setState(Download.State.IN_PROGRESS);

			while((read = in.read(buffer, 0, buffer.length)) != -1){
				
				if(download.getNewDestination() != null) {
					if(download.getNewDestination().equals(download.getDestination())){
						download.setNewDestination(null);
					}else {
						download.setState(Download.State.WAITING);
						try {
							outputStream.close();
							Files.move(download.getDestination().toPath(), download.getNewDestination().toPath(), StandardCopyOption.REPLACE_EXISTING);
							download.setDestination(download.getNewDestination());
							download.setNewDestination(null);
							DownloadManager.storeDownlads();
							if(Thread.currentThread().isInterrupted()) throw new InterruptedException();
							FileOutputStream os = new FileOutputStream(download.getDestination(), true);
							outputStream = new DataOutputStream(os); 
							DownloadManager.getDownloads().getTask(download).setOutputStream(os);
							download.setState(Download.State.IN_PROGRESS);
						}catch(java.nio.file.FileSystemException ex) {
							ex.printStackTrace();
						}
					}
				}
				
				outputStream.write(buffer, 0, read);
				download.getProgress().setCurrentSize(download.getProgress().getCurrentSize() + read);
				
				if(Thread.currentThread().isInterrupted()) throw new InterruptedException();
			}			
		}
	}
}
