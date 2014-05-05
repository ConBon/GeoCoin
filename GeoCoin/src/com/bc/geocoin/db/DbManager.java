package com.bc.geocoin.db;

import android.app.Service;
import android.content.Context;
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

public class DbManager implements IDbManager{

	private final String TAG = "GeoCoin";
	private final String dbname = "geocoindb";
	private Manager manager;
	private Database database;
	private Map<String, Object> docContent;
	private SimpleDateFormat dateFormatter;
	private Calendar calendar;
	private String timestamp;
	private Document document;
	private String docId;
	private Context context;
	
	public DbManager(Context context){
		this.context = context;
		//initialise docContent
		 docContent = new HashMap<String, Object>();
	}

	@Override
	public void getManager() {
		 try {
		     manager = new Manager(context.getFilesDir(), Manager.DEFAULT_OPTIONS);
		 } catch (IOException e) {
		     Log.e(TAG, "Cannot create manager object");
		     return;
		 }		
	}

	@Override
	public boolean createDb() {
		
		 getManager();
		 // create a name for the database and make sure the name is legal	 
		 if (!Manager.isValidDatabaseName(dbname)) {
		     Log.e(TAG, "Unacceptable database name");
		     return false;
		 }
		
		 // create a new database		 
		 try {
		     database = manager.getDatabase(dbname);
		 } catch (CouchbaseLiteException e) {
		     Log.e(TAG, "Cannot retrieve database");
		     return false;
		 }	
		 return true;
	}

	@Override
	public void getTimestamp() {
		 dateFormatter = new SimpleDateFormat("yyyy-MM-dd''HH:mm:ss.SSS'Z'");
		 calendar = GregorianCalendar.getInstance();
		 timestamp = dateFormatter.format(calendar.getTime());		
	}

	@Override
	public void addDocumentContent(String key, Object value) {
		 docContent.put(key, value);
		 getTimestamp();
		 docContent.put("creationDate", timestamp);

		 // display the data for the new document
		 Log.d(TAG, "docContent=" + String.valueOf(docContent));		
	}

	@Override
	public String persistDocument() {
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
		Log.d("DbManager", "document saved with id: "+ docId);
		//clear variable
		docContent.clear();
		return docId;
	}

	@Override
	public Document getDocument(String docId) {
		// retrieve the document from the database
		Document retrievedDocument = database.getDocument(docId);

		// display the retrieved document
		Log.d(TAG, "retrievedDocument=" + String.valueOf(retrievedDocument.getProperties()));
		Log.d(TAG, "End db_test");
		return retrievedDocument;
	}
}
