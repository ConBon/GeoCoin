package com.bc.geocoin.sync;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.json.*;

import com.google.gson.GsonBuilder;

public class JsonParser {
	int one;
	private JSONArray jsonArray;
	private JSONObject jsonObject;
	private Map<String, Object> map;
	
	public JsonParser(){
		jsonArray = new JSONArray();
		jsonObject = new JSONObject();
		map = new HashMap<String, Object>();
	}
	
	public JSONObject getJSONObject(){
		return jsonObject;
	}
	
	public Map<String, Object> parseJSON(String jsonString){
		try {
			jsonArray = new JSONArray(jsonString);
		} catch (JSONException e) {
			e.printStackTrace();
		}
			
		for(int i=0; i<jsonArray.length(); i++){
			jsonObject = jsonArray.optJSONObject(i);
			map.put(Integer.toString(i), (Object)jsonObject);
		}
			
		return map;
	}
	
	/**
	 *  Parse single record from json to map at bottom level
	 * @param obj
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Object> parseRecord(Object obj){		
		GsonBuilder builder = new GsonBuilder();
		Map<String, Object> gsonMap = new HashMap<String, Object>();
	    String str = obj.toString();//result.getValue().toString();
		gsonMap = (Map<String, Object>) builder.create().fromJson(str, Object.class);
		return gsonMap;
	}
}
