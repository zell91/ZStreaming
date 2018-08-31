package com.util.system;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import com.util.system.Response.Result;

public class SystemCMDManager {
	
	private Process cmd;
	
	private BufferedWriter writer;
	private BufferedReader reader;
	private BufferedReader error;

	public SystemCMDManager() {}
	
	public Process getProcess() {
		return this.cmd;
	}
	
	public void newProcess() throws IOException {
			if(this.cmd == null || !this.cmd.isAlive()) {
				Runtime runtime = Runtime.getRuntime();
				this.cmd = runtime.exec("cmd");
			}
		
			this.error = new BufferedReader(new InputStreamReader(this.cmd.getErrorStream(), "UTF-8"));
			this.reader = new BufferedReader(new InputStreamReader(this.cmd.getInputStream(), "UTF-8"));
			this.writer = new BufferedWriter(new OutputStreamWriter(this.cmd.getOutputStream(), "UTF-8"));			
	}
	
	public Response exec(String command) {		
		if(this.cmd == null || !this.cmd.isAlive()) {
			throw new RuntimeException("Process not initialized");
		}
		
		String error = "";
		String resp = "";
		
		try {
			this.writer.write(command + "\r\n");
			this.writer.write("exit\r\n");
			this.writer.flush();
			
			String line;
			
			while((line = this.error.readLine()) != null) {
				error += line + "\r\n";
			}

			while((line = this.reader.readLine()) != null) {
				resp += line + "\r\n";
			}

			if(error.isEmpty()) error = null;
			if(resp.isEmpty()) resp = null;

			if(error != null) 
				throw new IOException();
			else
				return new Response(Result.SUCCESS, resp);
		}catch(IOException ex) {
			return new Response(Result.ERROR, error);
		}finally {
			try {
				this.error.close();
			} catch (IOException e) { }
			try {
				this.reader.close();
			} catch (IOException e) { }
			try {
				this.writer.close();
			} catch (IOException e) { }
		}
	}
	
	
	
}
