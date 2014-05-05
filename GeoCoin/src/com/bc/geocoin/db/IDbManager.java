package com.bc.geocoin.db;

import com.couchbase.lite.Document;

public interface IDbManager {
	void getManager();
	boolean createDb();
	void getTimestamp();
	void addDocumentContent(String key, Object value);
	String persistDocument();
	Document getDocument(String docId);
}
