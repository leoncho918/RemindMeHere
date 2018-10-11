package com.mad.remindmehere.geofence;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Receiver for geofence transition changes
 * Receives geofence transition events from location services and creates a
 * GeofenceTransitionsJobIntentService that will handle the intent in the background
 */
public class GeofenceBroadcastReceiver extends BroadcastReceiver {
    /**
     * Called when intent is received
     * @param context
     * @param intent
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        //Enqueues a GeofenceTransitionsJobIntentService passing the context and intent as parameters
        GeofenceTransitionsJobIntentService.enqueueWork(context, intent);
    }
}
