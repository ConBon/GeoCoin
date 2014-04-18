package com.bc.geocoin.sync;

abstract class LocationModelBase {

	private String longitude;
	private String latitude;
	private String title;
	private String uuid;
	
	public LocationModelBase(String longitude, String latitude, String title){
		this.longitude = longitude;
		this.latitude = latitude;
		this.title = title;
		
	}
	
	public String GetLongitude(){
		return this.longitude;
	}
	
	public void SetLongitude(String value){
		this.longitude = value;
	}
	
	public String GetLatitude(){
		return this.latitude;
	}
	
	public void SetLatitude(String value){
		this.latitude = value;
	}
	
	public String GetTitle(){
		return this.title;
	}
	
	public void SetTitle(String value){
		this.title = value;
	}
}
