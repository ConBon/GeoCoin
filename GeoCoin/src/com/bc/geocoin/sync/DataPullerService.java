package com.bc.geocoin.sync;

import java.net.*;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.json.*;

import com.bc.geocoin.util.ZipUtil;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

public class DataPullerService extends IntentService{
    
	// put in config? 
	// coinmap.org url
	private final String urlString = "http://www.coinmap.org/data/data-overpass-bitcoin.json";
	private URL url;
	private String inputLine;
    private String json;
    
    public static final String ACTION = "com.bc.geocoin";
	
    public DataPullerService(){
    	super("UrlReaderService");
    }
    
    @Override
	protected void onHandleIntent(Intent intent) {
		try {
			url = new URL(urlString);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		pullDataFromUrl();
	}
    
	public void pullDataFromUrl(){
        BufferedReader in;
		try {
			in = new BufferedReader(
			new InputStreamReader(url.openStream()));
			
	        json = "";
	        try {
				while ((inputLine = in.readLine()) != null){
					json += inputLine;
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        in.close();
	        
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
     
        sendBackServiceResult(json);
	}

	/**
	 * Send broadcast back to main activity
	 * @param value
	 */
	private void sendBackServiceResult(String value) {	
		// compress string for sending 
		try {
			byte[] compressedValue = ZipUtil.compress(value);
			Intent intent = new Intent(ACTION);	
			intent.putExtra("json", compressedValue);
			Log.d("DataPullerService", "Sending compressed data back to main activity");
			sendBroadcast(intent);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
	

