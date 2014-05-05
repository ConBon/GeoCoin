package com.bc.geocoin.geofence;

import android.app.PendingIntent.CanceledException;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.Geofence;

public class BootCompleteReceiver extends WakefulBroadcastReceiver
{
    private static final String TAG = "BootCompleteReceiver";

    @Override
    public void onReceive(Context context, Intent intent)
    {
        //Do what you want/Register Geofences
    }
}