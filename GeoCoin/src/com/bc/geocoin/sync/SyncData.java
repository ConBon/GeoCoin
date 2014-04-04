package com.bc.geocoin.sync;

public class SyncData {

	private static String url = "http://www.coinmap.org/data/data-overpass-bitcoin.json";
	
	public static void main(String[] args) throws Exception{
		URLReader urlReader = new URLReader(url);
	}

}
