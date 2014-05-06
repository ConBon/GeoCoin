package com.bc.geocoin;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import shared.ui.actionscontentview.ActionsContentView;

import com.bc.geocoin.db.*;
import com.bc.geocoin.sync.*;
import com.bc.geocoin.util.ZipUtil;
import com.bc.geocoin.fragments.*;
import com.bc.geocoin.geofence.GeofenceStore;
import com.bc.geocoin.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.app.Activity;
import android.app.Dialog;
//import android.app.DialogFragment;
import android.support.v4.app.DialogFragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class MainActivity extends ActionBarActivity {

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
	private GeofenceStore storage;
	private BroadcastReceiver dataReceiver;
	private JsonParser jsonParser;
	private Map<String, Object> locationMap;
	
    @Override
    protected void onPause() {
      super.onPause();
      unregisterReceiver(dataReceiver);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
        //registerReceiver(dataReceiver, new IntentFilter(DataPullerService.ACTION));
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        
        /**
         * Receiver for DataPullerService
         */
        dataReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
            	
            	Bundle bundle = intent.getExtras();    
            	if(bundle != null){
            		try {
						splitJson(ZipUtil.decompress((byte[])bundle.get("json")));
					} catch (Exception e) {
						e.printStackTrace();
					}            		
            	}
            }
        };
        
        syncFirstTime();   
    }
    
    /**
     * Call JsonParser to split string from url
     * @param json
     */
    public void splitJson(final String json){
    	jsonParser = new JsonParser();
    	 
    	new Thread(new Runnable() {
    	    public void run() {
    	    	locationMap = jsonParser.parseJSON(json);
    	    	int counter = 0;
    	    	for(Map.Entry<String, Object> entry : locationMap.entrySet()){
    				Log.d("args", entry.toString());
    				Log.d("LOCATIONMAP", locationMap.toString());
    				
    				
    				//stop at 30 locations for test purposes
    				if(counter>30) return;
    				counter++;
    				
    				// convert object to map construct
    				Map<String, ?> newMap = jsonParser.parseRecord(entry.getValue());
    				
    				for(Map.Entry<String, ?> property : newMap.entrySet()){
    					Log.d("Main Activity", "Key: " +property.getKey().toString()+" & Value: "+ property.getValue().toString());
    					
    					//dbManager.addDocumentContent(property.getKey(), property.getValue());
    					//dbManager.persistDocument();
    				}
    	    	}
    	    }	
   
    	 }).start();
    }
    
	/**
	 * Setup new or retrieve existing db
	 */
    private void setUpDb() {
    	storage = new GeofenceStore(this.getBaseContext());
    	storage.initialiseDb();
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
    	Intent intent = new Intent(this, DataPullerService.class);
		startService(intent);
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
        case 3:
        	

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
            /*int errorCode = connectionResult.getErrorCode();
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
            }*/
        	return false;
        }
    }

}
