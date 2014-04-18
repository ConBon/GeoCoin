package com.bc.geocoin.db;

import com.couchbase.lite.Document;

public interface IDbManager {
	void GetManager();
	void GetDb();
	void GetTimestamp();
	void CreateDocument();
	String PersistDocument();
	Document GetDocument(String docId);
}
