package com.zstreaming.plugins.shortlink;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Base64;

import com.util.regex.Regex;
import com.zstreaming.browser.http.HttpResponse;
import com.zstreaming.plugins.Link;

@Link(name = "adfly", urls = {"adf.ly", "j.gs", "q.gs", "ay.gy", "zo.ee", "babblecase.com", "riffhold.com", "microify.com", "pintient.com", "tinyium.com", "atominik.com", "bluenik.com", "bitigee.com", "atomcurve.com", "picocurl.com", "tinyical.com", "casualient.com", "battleate.com", "mmoity.com", "simizer.com", "dataurbia.com", "viahold.com", "coginator.com", "cogismith.com", "kaitect.com", "yoalizer.com", "kibuilder.com", "kimechanic.com", "chathu.apkmania.co", "alien.apkmania.co", "adf.acb.im", "packs.redmusic.pl", "packs2.redmusic.pl", "dl.android-zone.org", "out.unionfansub.com", "sostieni.ilwebmaster21.com", "fuyukai-desu.garuda-raws.net", "queuecosm.bid", "threadsphere.bid", "restorecosm.bid", "clearload.bid"})
public class Adfly extends ShortLink{
	
	final String[] urls = {"adf.ly", "j.gs", "q.gs", "ay.gy", "zo.ee", "babblecase.com", "riffhold.com", "microify.com", "pintient.com", "tinyium.com", "atominik.com", "bluenik.com", "bitigee.com", "atomcurve.com", "picocurl.com", "tinyical.com", "casualient.com", "battleate.com", "mmoity.com", "simizer.com", "dataurbia.com", "viahold.com", "coginator.com", "cogismith.com", "kaitect.com", "yoalizer.com", "kibuilder.com", "kimechanic.com", "chathu.apkmania.co", "alien.apkmania.co", "adf.acb.im", "packs.redmusic.pl", "packs2.redmusic.pl", "dl.android-zone.org", "out.unionfansub.com", "sostieni.ilwebmaster21.com", "fuyukai-desu.garuda-raws.net", "queuecosm.bid", "threadsphere.bid", "restorecosm.bid", "clearload.bid"};
	
	private CharSequence content;
	private HttpResponse response;
	
	public Adfly() {
		super();
		this.hoster = "adfly";
	}
	
	@Override
	protected void offlineCheck() throws InterruptedException {
		super.offlineCheck();
		this.response = this.browser.getResponse();	
		this.content = response.getContent();
		if(response.getURL().getPath().matches("/suspended")) this.online = false;
		return;
	}

	@Override
	public boolean hasCaptcha() {
		return false;
	}

	@Override
	public void decrypt() throws InterruptedException  {
		
		if(this.response.getURL().toString().contains("/locked")) {
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				if(Thread.currentThread().isInterrupted()) return;
			}
			this.browser.sendRequest();
			this.offlineCheck();
			this.decrypt();
			return;
		}
		
		String encode = null, decode = null;

		try {
			encode = this.getEncode(content);
			decode = this.decode(encode);
		} catch (NullPointerException | ArrayIndexOutOfBoundsException | UnsupportedEncodingException e) {
			return;
		}
		
		try {
			this.finalLink = new URL(decode);
		} catch (MalformedURLException e) {	}				
	}

	private String getEncode(CharSequence content) throws NullPointerException, ArrayIndexOutOfBoundsException{
		
		String x = "", y = "";
		
		final String REGEX = "var\\s+ysmm\\s*=\\s*[\"\'][^\"\']*";		
		Regex regex = new Regex(REGEX, content);
				
		String ysmm = regex.match().toString().split("[\"\']")[1];
		
		for(int i=0;i<ysmm.length();i++){			
			if(i%2==0)
				x += ysmm.charAt(i);
			else
				y = ysmm.charAt(i) + y;
		}
				
		return x+y;
	}
	
	private String decode(String encode) throws NullPointerException, UnsupportedEncodingException {
		
		String[] raw = encode.split("");
		
		for(int i=0;i<raw.length;i++){
			
			if(raw[i].matches("\\d")){
				
				for(int n=i+1;n<raw.length;n++){					
					if(raw[n].matches("\\d")){						
						final int val = Integer.parseInt(raw[i]) ^ Integer.parseInt(raw[n]);
						
						if(val < 10) raw[i] = val + "";						
						i = n;
						
						break;
					}					
				}
			}			
		}
		
		String preDecode = "";
		
		for(String s: raw) preDecode += s;	

		byte[] data = Base64.getDecoder().decode(preDecode.getBytes("UTF-8"));

		String decodeURL = new String(data, "UTF-8");
		decodeURL = decodeURL.substring(16, decodeURL.length() - 16);
		
		return decodeURL;
	}

}
