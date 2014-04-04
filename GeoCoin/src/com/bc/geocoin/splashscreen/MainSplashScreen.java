package com.bc.geocoin.splashscreen;

import com.bc.geocoin.MainActivity;
import com.bc.geocoin.R;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;

public class MainSplashScreen extends Activity {
	 
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_splash_screen);
         
        // METHOD 1             
         /****** Create Thread that will sleep for 5 seconds *************/        
        Thread background = new Thread() {
            public void run() {
                 
                try {
                    // Thread will sleep for 5 seconds
                    sleep(5*1000);
                     
                    // After 5 seconds redirect to another intent
                    Intent i=new Intent(getBaseContext(), MainActivity.class);
                    startActivity(i);
                     
                    //Remove activity
                    finish();
                     
                } catch (Exception e) {
                 
                }
            }
        };
         
        // start thread
        background.start();
         
        //METHOD 2           
        /*
        new Handler().postDelayed(new Runnable() {
              
            // Using handler with postDelayed called runnable run method
  
            @Override
            public void run() {
                Intent i = new Intent(MainSplashScreen.this, FirstScreen.class);
                startActivity(i);
  
                // close this activity
                finish();
            }
        }, 5*1000); // wait for 5 seconds
        */
    }
     
    @Override
    protected void onDestroy() {
         
        super.onDestroy();
         
    }
}