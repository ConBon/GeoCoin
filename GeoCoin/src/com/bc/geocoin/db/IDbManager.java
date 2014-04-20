package com.bc.geocoin.db;

import java.util.Map;

import com.couchbase.lite.Document;

public interface IDbManager {
	void getManager();
	boolean createDb();
	void getTimestamp();
	void addDocumentContent(String key, String value);
	String persistDocument();
	Document getDocument(String docId);
}
