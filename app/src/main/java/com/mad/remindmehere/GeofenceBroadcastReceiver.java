package com.mad.remindmehere;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.mad.remindmehere.service.GeofenceTransitionsJobIntentService;

public class GeofenceBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        GeofenceTransitionsJobIntentService.enqueueWork(context, intent);
    }
}
