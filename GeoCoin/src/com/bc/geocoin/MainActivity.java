package com.bc.geocoin;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import shared.ui.actionscontentview.ActionsContentView;

import com.bc.geocoin.sync.*;
import com.bc.geocoin.util.ZipUtil;
import com.bc.geocoin.fragments.*;
import com.bc.geocoin.geofence.BitCoinGeofence;
import com.bc.geocoin.geofence.GeofenceStore;
import com.bc.geocoin.geofence.ReceiveTransitionsIntentService;
import com.bc.geocoin.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationClient.OnAddGeofencesResultListener;
import com.google.android.gms.location.LocationStatusCodes;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.app.Activity;
import android.app.Dialog;
import android.app.PendingIntent;
//import android.app.DialogFragment;
import android.support.v4.app.DialogFragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender.SendIntentException;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class MainActivity extends ActionBarActivity implements
									ConnectionCallbacks,
									OnConnectionFailedListener,
									OnAddGeofencesResultListener {

    // Global constants
    /*
     * Define a request code to send to Google Play services
     * This code is returned in Activity.onActivityResult
     */
    private final static int
            CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
	private final static String PREFS_NAME = "MyPrefsFile";
	
	/*
     * Use to set an expiration time for a geofence. After this amount
     * of time Location Services will stop tracking the geofence.
     */
    private static final long SECONDS_PER_HOUR = 60;
    private static final long MILLISECONDS_PER_SECOND = 1000;
    private static final long GEOFENCE_EXPIRATION_IN_HOURS = 12;
    private static final long GEOFENCE_EXPIRATION_TIME =
            GEOFENCE_EXPIRATION_IN_HOURS *
            SECONDS_PER_HOUR *
            MILLISECONDS_PER_SECOND;
	
	private SharedPreferences settings;
	private ActionsContentView viewActionsContentView;
	private GoogleMap map;
	private JsonParser jsonParser;
	private Map<String, Object> locationMap;
	
	// Internal List of Geofence objects
    List<Geofence> geofenceList;
	// Persistent storage for geofences
    private GeofenceStore geofenceStorage;
    
    // Holds the location client
    private LocationClient locationClient;
    // Stores the PendingIntent used to request geofence monitoring
    private PendingIntent geofenceRequestIntent;
    // Defines the allowable request types.
    public enum REQUEST_TYPE {ADD, REMOVE_INTENT}
    private REQUEST_TYPE requestType;
    // Flag that indicates if a request is underway.
    private boolean inProgress;
    
    BitCoinGeofence bcGeofence;
    String docId;
    double lat;
	double lon;
	String name;
    
	 /**
     * Receiver for DataPullerService
     */
    private BroadcastReceiver dataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) { 
        	Log.d("MainActivity", "onReceive broadcast");
        	int i = 0;
        	Bundle bundle = intent.getExtras();    
        	if(bundle != null){
        		try {
        			if(i<1){
        				splitJson(ZipUtil.decompress((byte[])bundle.get("json")));
        				i++;
        			}
				} catch (Exception e) {
					e.printStackTrace();
				}            		
        	}
        }
    };        
	
    @Override
    protected void onPause() {
      super.onPause();
      unregisterReceiver(dataReceiver);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
        registerReceiver(dataReceiver, new IntentFilter(DataPullerService.ACTION));
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //registerReceiver(dataReceiver, new IntentFilter(DataPullerService.ACTION));
        setContentView(R.layout.activity_main);
        
        /*
         * debug classLoader errors
         * 
         * Thread.currentThread().setContextClassLoader(new ClassLoader() {
            @Override
            public Enumeration<URL> getResources(String resName) throws IOException {
                Log.i("Debug", "Stack trace of who uses " +
                        "Thread.currentThread().getContextClassLoader()." +
                        "getResources(String resName):", new Exception());
                return super.getResources(resName);
            }
        });*/
        
        settings = getSharedPreferences(PREFS_NAME, 0);
        // Instantiate a new geofence storage area
        geofenceStorage = new GeofenceStore(this);
        // Instantiate the current List of geofences
        geofenceList = new ArrayList<Geofence>(); 
        // Start with the request flag set to false
        inProgress = false;
              
        viewActionsContentView = (ActionsContentView) findViewById(R.id.actionsContentView);
        
        final ListView viewActionsList = (ListView) findViewById(R.id.actions);
        
        final String[] values = new String[] { "Refresh", "Settings", "About" };
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
            android.R.layout.simple_list_item_2, android.R.id.text1, values);
        
        viewActionsList.setAdapter(adapter);
        viewActionsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
          @Override
          public void onItemClick(AdapterView<?> adapter, View v, int position,
              long flags) {
            showFragment(position);
          }
        });       

        //if (savedInstanceState == null) {
        	showFragment(0);
        //}
        	
        setUpDb();	
        
        
        syncFirstTime();   
    }
    
    /**
     * Call JsonParser to split string from url
     * @param json
     */
    public void splitJson(final String json){
    	Log.d("MainActivity", "splitJson");
    	jsonParser = new JsonParser();
    	 
    	new Thread(new Runnable() {
    	    public void run() {
    	    	locationMap = jsonParser.parseJSON(json);
    	    	int counter = 0;
		    
    	    	for(Map.Entry<String, Object> entry : locationMap.entrySet()){
    				Log.d("args", entry.toString());
		
    				
    				//control number of locations created for test purposes
    				if(counter>10){return;}
    				counter++;
    				
    				/**
	    	    	 * Test Data for geofences
	    	    	 */
    				 new Thread(new Runnable() {
    			    	    public void run() {
    			    	    	
    			    	    	Log.d("MainActivity", "Create test geofence object");
    			    	    	BitCoinGeofence BCBGeofence = new BitCoinGeofence(
    			    	    			54.581707, -5.937531, 200, 
    			    		            GEOFENCE_EXPIRATION_TIME,
    			    		            // This geofence records only entry transitions
    			    		            Geofence.GEOFENCE_TRANSITION_ENTER,
    			    		            "Bernard Crossland Building", "Belfast", "Malone Rd", "http://www.qub.ac.uk/", "02890000000", "bitcoin");
    			    	    	String BCBid = geofenceStorage.saveGeofence(BCBGeofence);
    			    	    	BCBGeofence.setId(BCBid);
    			    	    	geofenceList.add(BCBGeofence.toGeofence(BCBid));
    			    	    	
    			    	    	runOnUiThread(new Runnable() {

    			                    @Override
    			                    public void run() {
    			                    	/*
    			                    	 * Add test marker to map
    			                    	 */
    			                    	setUpMap(54.581707, -5.937531, "Bernard Crossland Building");
    			                    	
    			            		    Log.d("MainActivity", "runOnUiThread: setUpMap");
    			                    }
    			                });
    			    	    }
    			    	}).start();    
    			    	    	
    			    // convert object to map construct
    				Map<String, Object> newMap = jsonParser.parseRecord(entry.getValue());
    				createGeofence(newMap);
    				newMap.clear();
    	    	}
    	    	addGeofences();
    	    }	
   
    	 }).start();
    }
    
	/**
	 * Setup new or retrieve existing db
	 */
    private void setUpDb() {
    	geofenceStorage = new GeofenceStore(this.getBaseContext());
    	geofenceStorage.initialiseDb();
	}
    
    /**
     * Sync bitcoin locations and save to db on first run
     */
    private void syncFirstTime() {
		if(settings.getBoolean("first_time_open", true)){
			Log.d("MainActivity", "First time application run");
			settings.edit().putBoolean("first_time_open", false).commit();
				
			sync();
		}
	}

    /**
     * Start service to pull data from url in background
     */
    private void sync(){
    	Log.d("Sync", "Creating intent and starting service");
    	Intent intent = new Intent(this, DataPullerService.class);
		startService(intent);
    }
    
    /**
     * Get the geofence parameters for each geofence from the UI
     * and add them to a List.
     */
    public void createGeofence(Map<String, Object> data) {
        /*
         * Create an internal object to store the data.
         */
    	lat = 0;
    	lon = 0;
    	float radius = settings.getFloat("GEOFENCE_RADIUS", 200);
    	name ="";
    	String city ="";
    	String address ="";
    	String web ="";
    	String phone ="";
    	String icon ="";
    	
    	for(Map.Entry<String, Object> property : data.entrySet()){
    		switch(property.getKey()){
    		case "lat":
    			lat = (Double) property.getValue();
    			break;
    		case "lon":
    			lon = (Double) property.getValue();
    			break;
    		case "title": 
    			name = (String) property.getValue();
    			break;
    		case "addr":
    			address = (String) property.getValue();
    			break;
    		case "web":
    			web = (String) property.getValue();
    			break;
    		case "phone":
    			phone = (String) property.getValue();
    			break;
    		case "icon":
    			icon = (String) property.getValue();
    			break;
    		default: break;  				
    		}
    	}
    	bcGeofence = new BitCoinGeofence(
	            lat, lon, radius, 
	            GEOFENCE_EXPIRATION_TIME,
	            // This geofence records only entry transitions
	            Geofence.GEOFENCE_TRANSITION_ENTER,
	            name, city, address, web, phone, icon);

	    new Thread(new Runnable() {
    	    public void run() {	
    	    	
    	    	// Store this flat version
    		    docId = geofenceStorage.saveGeofence(bcGeofence);
    		    bcGeofence.setId(docId);
    		    geofenceList.add(bcGeofence.toGeofence(docId));
    		    Log.d("MainActivity", "Inside new thread: to create and store geofence");
    		    
    		    runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                    	/*
                    	 * Add test marker to map
                    	 */
                    	setUpMap(54.581707, -5.937531, "Bernard Crossland Building");
                    	//place a marker on the map
                    	setUpMap(lat, lon, name);
            		    Log.d("MainActivity", "runOnUiThread: setUpMap");
                    }
                });
    	    }
    	}).start();    
    }
    
    /**
     * Create a PendingIntent that triggers an IntentService in your
     * app when a geofence transition occurs.
     */
    private PendingIntent getTransitionPendingIntent() {
        // Create an explicit Intent
        Intent intent = new Intent(this,
                ReceiveTransitionsIntentService.class);
        /*
         * Return the PendingIntent
         */
        return PendingIntent.getService(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }
    
    /**
     * Show fragment based on click result of actioncontentview menu
     * @param position
     */
	private void showFragment(int position) {
        final Fragment f;
        switch (position) {
        case 0:
        	sync();
        	//viewActionsContentView.showContent();
        	return;
        case 1:
        	f = new SettingsFragment();
        	break;
        case 2:
        	f = new AboutFragment();
        	break;
        default:
          return;
        }
        getSupportFragmentManager().beginTransaction().replace(R.id.content, f).commit();

        viewActionsContentView.showContent();
      }
	
	/**
	 * Setup map fragment
	 */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (map == null) {
            // Try to obtain the map from the SupportMapFragment.
            map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
            // Check if we were successful in obtaining the map.
            if (map != null) {
                //setUpMap();
            }
        }
    }
    
    /**
     * This is where we can add markers or lines, add listeners or move the camera. 
     */
    private void setUpMap(double lat, double lng, String title) {   	
        map.addMarker(new MarkerOptions().position(new LatLng(lat, lng)).title(title));
        Log.d("MainActivity", "Adding marker '"+title+"' to map at lat: "+lat+" long: "+lng);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {       
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
       /* int id = item.getItemId();
        switch(id){
		}*/
        return super.onOptionsItemSelected(item);
    }
    
    /**
     *  Define a DialogFragment that displays the error dialog
     */
    public static class ErrorDialogFragment extends DialogFragment {
        // Global field to contain the error dialog
        private Dialog mDialog;
        
        // Default constructor. Sets the dialog field to null
        public ErrorDialogFragment() {
            super();
            mDialog = null;
        }
        
        // Set the dialog to display
        public void setDialog(Dialog dialog) {
            mDialog = dialog;
        }
        
        // Return a Dialog to the DialogFragment.
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return mDialog;
        }      
    }
    
    /**
     * Handle results returned to the FragmentActivity
     * by Google Play services
     */
     @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        // Decide what to do based on the original request code
        switch (requestCode) {
            case CONNECTION_FAILURE_RESOLUTION_REQUEST :
            /*
             * If the result code is Activity.RESULT_OK, try
             * to connect again
             */
                switch (resultCode) {
                    case Activity.RESULT_OK :
                    /*
                     * Try the request again
                     */
                    break;
                }
        }
    }
    
     /**
      * Check if GooglePlayServices is available
      * @return
      */
    private boolean servicesConnected() {
        // Check that Google Play services is available
        int resultCode =
                GooglePlayServicesUtil.
                        isGooglePlayServicesAvailable(this);
        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {
            // In debug mode, log the status
            Log.d("Geofence Detection",
                    "Google Play services is available.");
            // Continue
            return true;
        // Google Play services was not available for some reason
        } else {
            // Get the error code
            int errorCode = ConnectionResult.yI.getErrorCode();
            		//connectionResult.getErrorCode();
            // Get the error dialog from Google Play services
            Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(
                    errorCode,
                    this,
                    CONNECTION_FAILURE_RESOLUTION_REQUEST);

            // If Google Play services can provide an error dialog
            if (errorDialog != null) {
                // Create a new DialogFragment for the error dialog
                ErrorDialogFragment errorFragment =
                        new ErrorDialogFragment();
                // Set the dialog in the DialogFragment
                errorFragment.setDialog(errorDialog);
                // Show the error dialog in the DialogFragment
                errorFragment.show(
                        getSupportFragmentManager(),
                        "Geofence Detection");
            }
        	return false;
        }
    }

	
    /**
     * Start a request to remove geofences by calling
     * LocationClient.connect()
     */
    public void removeGeofences(PendingIntent requestIntent) {
        // Record the type of removal request
        requestType = REQUEST_TYPE.REMOVE_INTENT;
        /*
         * Test for Google Play services after setting the request type.
         * If Google Play services isn't present, the request can be
         * restarted.
         */
        if (!servicesConnected()) {
            return;
        }
        // Store the PendingIntent
        geofenceRequestIntent = requestIntent;
        /*
         * Create a new location client object. Since the current
         * activity class implements ConnectionCallbacks and
         * OnConnectionFailedListener, pass the current activity object
         * as the listener for both parameters
         */
        locationClient = new LocationClient(this, this, this);
        // If a request is not already underway
        if (!inProgress) {
            // Indicate that a request is underway
            inProgress = true;
            // Request a connection from the client to Location Services
            locationClient.connect();
        } else {
            /*
             * A request is already underway. You can handle
             * this situation by disconnecting the client,
             * re-setting the flag, and then re-trying the
             * request.
             */
        }
    }
    /**
     * Start a request for geofence monitoring by calling
     * LocationClient.connect().
     */
    public void addGeofences() {
        // Start a request to add geofences
        requestType = requestType.ADD;
        /*
         * Test for Google Play services after setting the request type.
         * If Google Play services isn't present, the proper request
         * can be restarted.
         */
        if (!servicesConnected()) {
            return;
        }
        /*
         * Create a new location client object. Since the current
         * activity class implements ConnectionCallbacks and
         * OnConnectionFailedListener, pass the current activity object
         * as the listener for both parameters
         */
        locationClient = new LocationClient(this, this, this);
        // If a request is not already underway
        if (!inProgress) {
            // Indicate that a request is underway
            inProgress = true;
            // Request a connection from the client to Location Services
            locationClient.connect();
            Log.d("AddGeofences", "LocationClient connecting: awaiting callback");
        } else {
            /*
             * A request is already underway. You can handle
             * this situation by disconnecting the client,
             * re-setting the flag, and then re-trying the
             * request.
             */
        }
    }
    @Override
	public void onAddGeofencesResult(int statusCode, String[] geofenceRequestIds) {
    	// If adding the geofences was successful
        if (LocationStatusCodes.SUCCESS == statusCode) {
            /*
             * Handle successful addition of geofences here.
             * You can send out a broadcast intent or update the UI.
             * geofences into the Intent's extended data.
             */
        	for(String id : geofenceRequestIds){
        		//ListIterator<Geofence>geofenceList.listIterator().
        		Log.d("AddGeoFencesSuccessful", "ID: "+id);
        	}
        	
        } else {
        // If adding the geofences failed
            /*
             * Report errors here.
             * You can log the error using Log.e() or update
             * the UI.
             */
        }
        // Turn off the in progress flag and disconnect the client
        inProgress = false;
        locationClient.disconnect();
		
	}


    /**
     * When the request to remove geofences by PendingIntent returns,
     * handle the result.
     *
     *@param statusCode the code returned by Location Services
     *@param requestIntent The Intent used to request the removal.
     */
    public void onRemoveGeofencesByPendingIntentResult(int statusCode,
            PendingIntent requestIntent) {
        // If removing the geofences was successful
        if (statusCode == LocationStatusCodes.SUCCESS) {
            /*
             * Handle successful removal of geofences here.
             * You can send out a broadcast intent or update the UI.
             * geofences into the Intent's extended data.
             */
        } else {
        // If adding the geocodes failed
            /*
             * Report errors here.
             * You can log the error using Log.e() or update
             * the UI.
             */
        }
        /*
         * Disconnect the location client regardless of the
         * request status, and indicate that a request is no
         * longer in progress
         */
        inProgress = false;
        locationClient.disconnect();
    }
    @Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		// Turn off the request flag
        inProgress = false;
        /*
         * If the error has a resolution, start a Google Play services
         * activity to resolve it.
         */
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(
                        this,
                        CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (SendIntentException e) {
                // Log the error
                e.printStackTrace();
                Log.d("Main_Activity", e.getMessage());
            }
        // If no resolution is available, display an error dialog
        } else {
            // Get the error code
            int errorCode = connectionResult.getErrorCode();
            // Get the error dialog from Google Play services
            Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(
                    errorCode,
                    this,
                    CONNECTION_FAILURE_RESOLUTION_REQUEST);
            // If Google Play services can provide an error dialog
            if (errorDialog != null) {
                // Create a new DialogFragment for the error dialog
                ErrorDialogFragment errorFragment =
                        new ErrorDialogFragment();
                // Set the dialog in the DialogFragment
                errorFragment.setDialog(errorDialog);
                // Show the error dialog in the DialogFragment
                errorFragment.show(
                        getSupportFragmentManager(),
                        "Geofence Detection");
            }
        }
		
	}
	

	@Override
	public void onConnected(Bundle arg0) {
		switch (requestType) {
        case ADD :
            // Get the PendingIntent for the request
            PendingIntent transitionPendingIntent =
                    getTransitionPendingIntent();
            // Send a request to add the current geofences
            locationClient.addGeofences(
                    geofenceList, transitionPendingIntent, this);
            Log.d("OnConnected", "Request to add geofences sent");
            break;
        case REMOVE_INTENT:
        	
        	break;
		}	
		
	}

	@Override
	public void onDisconnected() {
        // Turn off the request flag
        inProgress = false;
        // Destroy the current location client
        locationClient = null;
		
	}
}
