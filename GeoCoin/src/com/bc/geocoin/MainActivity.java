package com.bc.geocoin;

import shared.ui.actionscontentview.ActionsContentView;

import com.bc.geocoin.db.*;
import com.bc.geocoin.sync.*;
import com.bc.geocoin.fragments.*;
import com.bc.geocoin.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.os.Build;

public class MainActivity extends ActionBarActivity {

	private ActionsContentView viewActionsContentView;
	private GoogleMap map;
	private IDbManager dbManager;
	private IUrlReader urlReader;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
              
        viewActionsContentView = (ActionsContentView) findViewById(R.id.actionsContentView);
        
        final ListView viewActionsList = (ListView) findViewById(R.id.actions);
        
        final String[] values = new String[] { "Home", "About", "Settings" };
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
            android.R.layout.simple_list_item_1, android.R.id.text1, values);
        
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
        
    }
    
    private void showFragment(int position) {
        final Fragment f;
        switch (position) {
        case 0:
          setUpMapIfNeeded();
          return;
        case 1:
          f = new AboutFragment();
          break;
        case 2:
          f = new SettingsFragment();
          break;

        default:
          return;
        }
        getSupportFragmentManager().beginTransaction().replace(R.id.content, f).commit();

        viewActionsContentView.showContent();
      }

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (map == null) {
            // Try to obtain the map from the SupportMapFragment.
            map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
            // Check if we were successful in obtaining the map.
            if (map != null) {
                setUpMap();
            }
        }
        viewActionsContentView.showContent();
    }
    
    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        map.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
       
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {       
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch(id){
        case R.id.action_about:
        	showFragment(1);
        case R.id.action_settings:
        	showFragment(2);
        	break;
        case R.id.action_refresh:
        	
        	break;
        }
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

}
