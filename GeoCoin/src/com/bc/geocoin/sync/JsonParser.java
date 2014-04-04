package com.bc.geocoin.sync;

import java.util.HashMap;
import java.util.Map;

import org.json.*;

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
	
	public Map<String, Object> parse(String jsonString){
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
}
