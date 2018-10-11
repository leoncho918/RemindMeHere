package com.mad.remindmehere.geofence;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.mad.remindmehere.R;
import com.mad.remindmehere.model.Reminder;

import java.util.ArrayList;
import java.util.List;

/**
 * ResultCallback class to simplify the registration, updating and unregistration of geofences
 */
public class Geofencing implements ResultCallback {
    //Variables to store data
    private List<Geofence> mGeofenceList;
    private PendingIntent mGeofencePendingIntent;
    private Context mContext;
    private GeofencingClient mClient;
    //Constants
    public static final String TAG = "Geofence";
    public static final int DWELL_TIME = 15000;

    /**
     * Constructor for this class, takes context as a argument
     * @param mContext
     */
    public Geofencing(Context mContext) {
        this.mGeofenceList = new ArrayList<>();
        this.mGeofencePendingIntent = null;
        this.mContext = mContext;
        this.mClient = LocationServices.getGeofencingClient(mContext);
    }

    /**
     * Method to register all geofences in geofencelist with location services
     */
    public void registerGeofences() {
        //Check if geofencelist has geofences or has been initalised
        if (!(mGeofenceList == null || mGeofenceList.size() == 0)) {
            //Try
            try {
                //Register all geofences with location services and add a on success listener
                mClient.addGeofences(getGeofencingRequest(), getGeofencePendingIntent())
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            //Called when geofences are register successfully
                            @Override
                            public void onSuccess(Void aVoid) {
                                //Log that geofences are registered for debugging purposes
                                Log.d(TAG, mContext.getString(R.string.geofence_success));
                            }
                        })
                        //add on failure listener
                        .addOnFailureListener(new OnFailureListener() {
                            //Called when geofences fail to register
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                //Log that geofences failed to register for debugging purposes
                                Log.d(TAG, mContext.getString(R.string.geofence_fail));
                                //Notify user that geofences haven't been registered
                                Toast.makeText(mContext, mContext.getString(R.string.geofence_fail), Toast.LENGTH_LONG);
                            }
                        });
            }
            //Catch security exception for when location permission hasn't been granted
            catch (SecurityException e) {
                //Log exception message
                Log.e(TAG, e.getMessage());
            }
        }
    }

    /**
     * Method to unregister all geofences that have been registered with location services
     */
    public void unRegisterGeofences() {
        //Try
        try {
            //Unregister all geofences with location services and add a on success listener
            mClient.removeGeofences(getGeofencePendingIntent())
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        //Called when geofences are unregistered successfully
                        @Override
                        public void onSuccess(Void aVoid) {
                            //Log that geofences are unregistered for debugging purposes
                            Log.d(TAG, mContext.getString(R.string.geofence_remove_success));
                        }
                    })
                    //add on failure listener
                    .addOnFailureListener(new OnFailureListener() {
                        //Called when geofences fail to unregister
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //Log that geofences failed to unregister for debugging purposes
                            Log.d(TAG, mContext.getString(R.string.geofence_remove_fail));
                            //Notify user that geofences haven't been unregistered
                            Toast.makeText(mContext, mContext.getString(R.string.geofence_remove_fail), Toast.LENGTH_LONG);
                        }
                    });
        }
        //Catch security exception for when location permission hasn't been granted
        catch (SecurityException e) {
            //Log exception message
            Log.e(TAG, e.getMessage());
        }
    }

    /**
     * Method takes a list of reminders and updates it's geofences list based on the reminders list
     * @param reminders
     */
    public void updateGeofences(List<Reminder> reminders) {
        //Reinitialise geofence list
        mGeofenceList = new ArrayList<>();
        //For each loop to go through each reminder
        for (Reminder r : reminders) {
            //Create a new geofence
            Geofence geofence = new Geofence.Builder()
                    //Set request id of geofence to a combination of reminder name and description
                    .setRequestId(r.getName() + mContext.getString(R.string.geofence_delimiter) + r.getDescription())
                    //Set the geofence to never expire
                    .setExpirationDuration(Geofence.NEVER_EXPIRE)
                    //Set the location and size of the circular geofence
                    .setCircularRegion(r.getLat(), r.getLng(), (float)r.getRadius())
                    //Set the loitering time for dwelling in a geofence
                    .setLoiteringDelay(DWELL_TIME)
                    //Set transition types to entering and dwelling
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_DWELL)
                    //Build geofence
                    .build();
            //Add geofence into list
            mGeofenceList.add(geofence);
        }
    }

    /**
     * Method returns a geofencingrequest
     * @return
     */
    private GeofencingRequest getGeofencingRequest() {
        //Create a new geofencerequest builder
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        //Set the initial trigger for geofencingrequest to entering
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        //Add geofence list to builder
        builder.addGeofences(mGeofenceList);
        //Return built geofencingrequest
        return builder.build();
    }

    /**
     * Method creates a new pending intent to be returned if one does not exist else it returns the existing pending intent
     * @return
     */
    private PendingIntent getGeofencePendingIntent() {
        //Logging the creation of pending intent for debugging purposes
        Log.d(TAG, mContext.getString(R.string.geofence_createPending));
        //Check if geofencependingintent isn't null
        if (mGeofencePendingIntent != null) {
            //Return already created pending intent
            return mGeofencePendingIntent;
        }
        //Create new intent with context and broadcastreceiver as arguments
        Intent intent = new Intent(mContext, GeofenceBroadcastReceiver.class);
        //Create a new broadcast pending intent
        mGeofencePendingIntent = PendingIntent.getBroadcast(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        //Return pending intent
        return mGeofencePendingIntent;
    }

    /**
     * Called when result is ready
     * @param result
     */
    @Override
    public void onResult(@NonNull Result result) {
        //Log the result status
        Log.e(TAG, String.format(mContext.getString(R.string.geofence_error_adding_removing), result.getStatus().toString()));
    }
}
