package com.bc.geocoin.db;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.couchbase.lite.*;
import com.couchbase.lite.util.Log;
import com.bc.geocoin.sync.*;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

public class DbManager extends Service implements IDbManager{

	private final String TAG = "GeoCoin";
	private final String dbname = "geocoin_db";
	private Manager manager;
	private Database database;
	private Map<String, Object> docContent;
	private SimpleDateFormat dateFormatter;
	private Calendar calendar;
	private String timestamp;
	private Document document;
	private URLReader urlReader;
	private String docId;
	
	@Override
	public IBinder onBind(Intent intent) {
				 
		 Log.d(TAG, "Begin Geocoin db service");
		 	
		return null;//new Intent(action, uri);
	}


	@Override
	public void GetManager() {
		 try {
		     manager = new Manager(getApplicationContext().getFilesDir(), Manager.DEFAULT_OPTIONS);
		 } catch (IOException e) {
		     Log.e(TAG, "Cannot create manager object");
		     return;
		 }		
	}

	@Override
	public void GetDb() {
		 // create a name for the database and make sure the name is legal	 
		 if (!Manager.isValidDatabaseName(dbname)) {
		     Log.e(TAG, "Unacceptable database name");
		     return;
		 }

		 // create a new database		 
		 try {
		     database = manager.getDatabase(dbname);
		 } catch (CouchbaseLiteException e) {
		     Log.e(TAG, "Cannot retrieve database");
		     return;
		 }	
	}

	@Override
	public void GetTimestamp() {
		 dateFormatter = new SimpleDateFormat("yyyy-MM-dd’T'HH:mm:ss.SSS'Z'");
		 calendar = GregorianCalendar.getInstance();
		 timestamp = dateFormatter.format(calendar.getTime());		
	}

	@Override
	public void CreateDocument() {
		 docContent.put("message", "Hello Couchbase Lite");
		 docContent.put("creationDate", timestamp);

		 // display the data for the new document
		 Log.d(TAG, "docContent=" + String.valueOf(docContent));		
	}

	@Override
	public String PersistDocument() {
		 // create an empty document
		 document = database.createDocument();

		 // write the document to the database
		 try {
		     document.putProperties(docContent);
		 } catch (CouchbaseLiteException e) {
		     Log.e(TAG, "Cannot write document to database", e);
		 }

		// save the ID of the new document
		docId = document.getId();
		return docId;
	}

	@Override
	public Document GetDocument(String docId) {
		// retrieve the document from the database
		Document retrievedDocument = database.getDocument(docId);

		// display the retrieved document
		Log.d(TAG, "retrievedDocument=" + String.valueOf(retrievedDocument.getProperties()));
		Log.d(TAG, "End db_test");
		return retrievedDocument;
	}
}
