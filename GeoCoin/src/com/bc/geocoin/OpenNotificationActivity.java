package com.bc.geocoin;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

/**
 * Activity started upon clicking drawer notification
 * @author Conal
 *
 */
public class OpenNotificationActivity extends Activity{
	
	TextView tv;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.geofence_notification_view);
        
        tv = (TextView)findViewById(R.id.textView2);
        Intent intent = getIntent();
        String id = intent.getStringExtra("GEOFENCE_ID");
        
        tv.setText(tv.getText()+id);
        
	}

}
