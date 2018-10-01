package com.mad.remindmehere.service;

import android.app.IntentService;
import android.app.Notification;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingEvent;
import com.google.android.gms.location.places.GeoDataClient;
import com.mad.remindmehere.R;
import com.mad.remindmehere.activity.RemindersMapsActivity;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class GeofenceTransitionsIntentService extends IntentService {

    public static final String TAG = "GeofenceTransition";
    public static final String CHANNEL_ID = "Reminders";
    public static final String NAME = "GeofenceIntentService";

    public GeofenceTransitionsIntentService() {
        super(NAME);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            String errorMessage = getErrorString(geofencingEvent.getErrorCode());
            Log.e(TAG, errorMessage);
            return;
        }

        int geofenceTransition = geofencingEvent.getGeofenceTransition();
        Log.d(TAG, "Geofence Triggered");

        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
            List<Geofence> triggeredGeofences = geofencingEvent.getTriggeringGeofences();

            sendNotification(Integer.parseInt(triggeredGeofences.get(0).getRequestId()));
        }
    }

    private String getErrorString(int errorCode) {
        switch (errorCode) {
            case GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE:
                return "Geofence not available";
            case GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES:
                return "geofence too many_geofences";
            case GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS:
                return "geofence too many pending_intents";
            default:
                return "geofence error";
        }
    }

    public void sendNotification(int id) {
        Intent notificationIntent = new Intent(getApplicationContext(), RemindersMapsActivity.class);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_remind_notification)
                .setContentTitle("Reminder")
                .setContentText("You have a new reminder")
                .setPriority(NotificationCompat.PRIORITY_MAX);

        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(this);
        managerCompat.notify(id, mBuilder.build());
    }
}
