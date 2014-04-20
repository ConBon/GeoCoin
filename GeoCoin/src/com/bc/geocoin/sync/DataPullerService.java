package com.bc.geocoin.sync;

import java.net.*;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.json.*;

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
    private JsonParser jsonParser;
    private Map<String, Object> locationMap;
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
		
		jsonParser = new JsonParser();
		locationMap = new HashMap<String, Object>();
		
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

        locationMap = jsonParser.parseJSON(json);   
        
        sendBackServiceResult(locationMap);
	}

	private void sendBackServiceResult(Map<String, Object> value) {
		for(Entry<String, Object> entry : value.entrySet()){
			Intent intent = new Intent(ACTION);
			//Log.d("DataPullerService", "initial entry: "+entry.getValue().toString());
			// convert object to map construct
			Map<String, ?> newMap = jsonParser.parseRecord(entry.getValue());
			
			for(Entry<String, ?> property : newMap.entrySet()){
				intent.putExtra(property.getKey().toString(), property.getValue().toString());
				Log.d("DataPullerService", "Intent- Key: " +property.getKey().toString()+" & Value: "+ property.getValue().toString());
			}
			sendBroadcast(intent);
		}
		
	}
	
}
