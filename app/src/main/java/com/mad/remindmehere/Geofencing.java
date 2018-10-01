package com.mad.remindmehere;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.mad.remindmehere.database.ReminderDatabase;
import com.mad.remindmehere.model.Reminder;
import com.mad.remindmehere.service.GeofenceTransitionsIntentService;

import java.util.ArrayList;
import java.util.List;

public class Geofencing implements ResultCallback {

    private List<Geofence> mGeofenceList;
    private PendingIntent mGeofencePendingIntent;
    private Context mContext;
    private GeofencingClient mClient;
    public static final String TAG = "Geofence";

    public Geofencing(Context mContext) {
        this.mGeofenceList = new ArrayList<>();
        this.mGeofencePendingIntent = null;
        this.mContext = mContext;
        this.mClient = LocationServices.getGeofencingClient(mContext);
    }

    public void registerGeofences() {
        if (!(mGeofenceList == null || mGeofenceList.size() == 0)) {
            try {
                mClient.addGeofences(getGeofencingRequest(), getGeofencePendingIntent())
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.e(TAG, "Geofences Registered");
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e(TAG, "Geofences Failed Registration");
                            }
                        });
            } catch (SecurityException e) {
                Log.e(TAG, e.getMessage());
            }
        }
    }

    public void unRegisterGeofences() {
        try {
            mClient.removeGeofences(getGeofencePendingIntent())
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.e(TAG, "Geofences Removed");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e(TAG, "Geofences Failed Removal");
                        }
                    });
        }
        catch (SecurityException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    public void updateGeofences(List<Reminder> reminders) {
        mGeofenceList = new ArrayList<>();
        for (Reminder r : reminders) {
            Geofence geofence = new Geofence.Builder()
                    .setRequestId(r.getId() + "")
                    .setExpirationDuration(Geofence.NEVER_EXPIRE)
                    .setCircularRegion(r.getLat(), r.getLng(), (float)r.getRadius())
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                    .build();
            mGeofenceList.add(geofence);
        }
    }

    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER | GeofencingRequest.INITIAL_TRIGGER_EXIT);
        builder.addGeofences(mGeofenceList);
        return builder.build();
    }

    private PendingIntent getGeofencePendingIntent() {
        Log.e(TAG, "Intent Called");
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(mContext, GeofenceTransitionsIntentService.class);
        mGeofencePendingIntent = PendingIntent.getService(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return mGeofencePendingIntent;
    }

    @Override
    public void onResult(@NonNull Result result) {
        Log.e(TAG, String.format("Error adding/removing geofence : %s", result.getStatus().toString()));
    }
}
