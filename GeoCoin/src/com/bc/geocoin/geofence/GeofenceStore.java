package com.bc.geocoin.geofence;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
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

/**
 * Storage for geofence values, implemented in SharedPreferences.
 */
public class GeofenceStore {
	// Keys for flattened geofences
	public static final String KEY_LATITUDE = "com.example.android.geofence.KEY_LATITUDE";
	public static final String KEY_LONGITUDE = "com.example.android.geofence.KEY_LONGITUDE";
	public static final String KEY_RADIUS = "com.example.android.geofence.KEY_RADIUS";
	public static final String KEY_NAME = "com.example.android.geofence.KEY_NAME";
	public static final String KEY_ADDRESS = "com.example.android.geofence.KEY_ADDRESS";
	public static final String KEY_EXPIRATION_DURATION = "com.example.android.geofence.KEY_EXPIRATION_DURATION";
	public static final String KEY_TRANSITION_TYPE = "com.example.android.geofence.KEY_TRANSITION_TYPE";
	// The prefix for flattened geofence keys
	public static final String KEY_PREFIX = "com.example.android.geofence.KEY";

	private final String TAG = "GeoCoin";
	/*
	 * Couchbase db variables
	 */
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

	/*
	 * Invalid values, used to test geofence storage when retrieving geofences
	 */
	public static final long INVALID_LONG_VALUE = -999l;
	public static final float INVALID_FLOAT_VALUE = -999.0f;
	public static final int INVALID_INT_VALUE = -999;

	// Create the SharedPreferences storage
	public GeofenceStore(Context context) {
		this.context = context;
		// initialise docContent
		docContent = new HashMap<String, Object>();
	}

	public boolean initialiseDb() {
		try {
			manager = new Manager(context.getFilesDir(),
					Manager.DEFAULT_OPTIONS);
		} catch (IOException e) {
			Log.e(TAG, "Cannot create manager object");
		}
		
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

	public void getTimestamp() {
		dateFormatter = new SimpleDateFormat("yyyy-MM-dd''HH:mm:ss.SSS'Z'");
		calendar = GregorianCalendar.getInstance();
		timestamp = dateFormatter.format(calendar.getTime());
	}

	/**
	 * Returns a stored geofence by its id
	 * 
	 * @param id 
	 * 		The ID of a stored geofence
	 * @return A geofence defined by its center and radius. See
	 */
	public BitCoinGeofence getGeofence(String id) {	
		// retrieve the document from the database
		Document retrievedDocument = database.getDocument(id);
		Map<String, Object> properties = retrievedDocument.getProperties();

		double lat = (!properties.get(KEY_LATITUDE).equals(null)) ? 
				(double) properties.get(KEY_LATITUDE) : null;
		double lng = (!properties.get(KEY_LONGITUDE).equals(null)) ?
				(double) properties.get(KEY_LONGITUDE) : null;
		float radius = (!properties.get(KEY_RADIUS).equals(null)) ?
				(float) properties.get(KEY_RADIUS) : null;
		String name = (!properties.get(KEY_NAME).equals(null)) ?
				(String) properties.get(KEY_NAME) : null;
		String address = (!properties.get(KEY_ADDRESS).equals(null)) ?
				(String) properties.get(KEY_ADDRESS) : null;
		long expirationDuration = (!properties.get(KEY_EXPIRATION_DURATION).equals(null)) ?
				(long) properties.get(KEY_EXPIRATION_DURATION) : null;
		int transitionType = (!properties.get(KEY_TRANSITION_TYPE).equals(null)) ?
				(int) properties.get(KEY_TRANSITION_TYPE) : null;

		// display the retrieved document
		Log.d(TAG, "retrievedDocument="
						+ String.valueOf(retrievedDocument.getProperties()));
		// Return a true Geofence object
		return new BitCoinGeofence(id, lat, lng, radius, name, address,
				expirationDuration, transitionType);
	}

	/**
	 * Save a geofence.
	 * 
	 * @param geofence
	 *            The SimpleGeofence containing the values you want to save in
	 *            SharedPreferences
	 */
	public String saveGeofence(BitCoinGeofence geofence) {
		
		// Write the Geofence values to a document
		docContent.put(KEY_LATITUDE, (float) geofence.getLatitude());
		docContent.put(KEY_LONGITUDE, (float) geofence.getLongitude());
		docContent.put(KEY_RADIUS, geofence.getRadius());
		docContent.put(KEY_NAME, geofence.getName());
		docContent.put(KEY_ADDRESS, geofence.getAddress());
		docContent.put(KEY_EXPIRATION_DURATION,
				geofence.getExpirationDuration());
		docContent.put(KEY_TRANSITION_TYPE, geofence.getTransitionType());
		
		getTimestamp();
		docContent.put("creationDate", timestamp);

		// display the data for the new document
		Log.d(TAG, "docContent=" + String.valueOf(docContent));

		// Commit the changes
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
		Log.d("DbManager", "document saved with id: " + docId);
		// clear variable
		docContent.clear();
		return docId;
	}

	public void clearGeofence(String id) {
		try {
			database.deleteLocalDocument(id);
		} catch (CouchbaseLiteException e) {
			Log.d(TAG, "Error occurred deleting item from database");
			e.printStackTrace();
		}
	}
}
