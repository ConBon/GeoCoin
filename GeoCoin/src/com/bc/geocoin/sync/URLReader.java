package com.bc.geocoin.sync;

import java.net.*;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

import org.json.*;

public class URLReader implements IUrlReader{
    
	// put in config? 
	// coinmap.org url
	private final String urlString = "http://www.coinmap.org/data/data-overpass-bitcoin.json";
	private URL url;
	private String inputLine;
    private String json;
    private JsonParser jsonParser;
    private Map<String, Object> locationMap;
    
	public URLReader(){
		
		try {
			url = new URL(urlString);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		jsonParser = new JsonParser();
		locationMap = new HashMap<String, Object>();	
	}
	
	public Map<String, Object> PullDataFromUrl(){
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

        locationMap = jsonParser.parse(json);   
        
        return locationMap;
	}
	
}
