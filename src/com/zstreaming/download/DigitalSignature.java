package com.zstreaming.download;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

import com.zstreaming.gui.download.DownloadValues;

public class DigitalSignature {
	
	final static String SIGNATURE = "ZStreaming by zell91";
	private final static byte[] DATA_SIGN = SIGNATURE.getBytes(Charset.forName("UTF-8"));
	public final static int DATA_LENGTH = DATA_SIGN.length;
	
	private DigitalSignature() { }
		
	public static void addSignature(OutputStream out) throws IOException {
		out.write(DATA_SIGN);
	}
	
	public static File removeSignature(DownloadValues downloadValues) {
		File zdownDest = downloadValues.getDownload().getDestination();
		File dest = zdownDest.getName().contains(DownloadTask.DOWNLOAD_EXTENTION) ? new File(zdownDest.getParent(), zdownDest.getName().substring(0, zdownDest.getName().lastIndexOf("."))) : zdownDest;
		
		int read = 0;
		double workDone = 0;

		byte[] buffer = new byte[(int) Math.min(1132462, zdownDest.length() - DATA_LENGTH)];
		
		try(FileInputStream in = new FileInputStream(zdownDest);
			FileOutputStream out = new FileOutputStream(dest)){
			
			in.skip(DATA_LENGTH);
						
			while((read = in.read(buffer, 0, buffer.length)) != -1){
				out.write(buffer, 0, read);
				downloadValues.setProgress(workDone+=read/downloadValues.getSize().getRealSize());
			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return zdownDest;
		} catch (IOException e) {
			e.printStackTrace();
			return zdownDest;
		}

		zdownDest.delete();
				
		return dest;			
	}
	
	@Override
	public String toString() {
		return "Signature: " + SIGNATURE;
	}

	public static boolean isSigned(Download download) throws FileNotFoundException, IOException {
		
		String sign = "";
		
		try(FileInputStream in = new FileInputStream(download.getDestination())){
			int read = 0;
			int r = 0;
			byte[] buffer = new byte[DATA_LENGTH];
			
			while((read += r = in.read(buffer, 0, buffer.length)) <= DATA_LENGTH) {
				if(r == -1) break;
				sign += new String(buffer, "UTF-8");
			}
		}
				
		return sign.equals(SIGNATURE);
	}

}
