package com.mad.remindmehere.geofence;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.JobIntentService;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingEvent;
import com.mad.remindmehere.R;
import com.mad.remindmehere.activity.RemindersMapsActivity;

import java.util.List;

/**
 * Listens for geofence transition changes
 * Receives geofence transition events from Location Services through intents that contain the transition type and geofence id of the triggered geofence
 * Creates notification for user as output
 */
public class GeofenceTransitionsJobIntentService extends JobIntentService {

    //Constants
    public static final String TAG = "GeofenceTransition";
    public static final String CHANNEL_ID = "Reminders";
    public static final int JOB_ID = 0;
    public static final int PENDING_INTENT_REQUEST_ID = 8;
    public static final String GROUP_KEY_REMINDER = "com.mad.remindmehere.service.REMINDER";

    /**
     * Method for enqueuing work into this JobIntentService
     * @param context
     * @param intent
     */
    public static void enqueueWork(Context context, Intent intent) {
        enqueueWork(context, GeofenceTransitionsJobIntentService.class, JOB_ID, intent);
    }

    /**
     * Handles incoming intents sent by Location Services
     * @param intent
     */
    @Override
    protected void onHandleWork(Intent intent) {
        //Create new GeofencingEvent object from intent
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        //If geofencingEvent has an error
        if (geofencingEvent.hasError()) {
            //Log the geofence error via getting the error code and converting into readable text via method
            String errorMessage = getErrorString(geofencingEvent.getErrorCode());
            Log.e(TAG, errorMessage);
            return;
        }

        //Get the geofence transition type
        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        //Check if the geofence transition was an entrance of a geofence
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
            //Create a list to store the triggered geofences
            List<Geofence> triggeredGeofences = geofencingEvent.getTriggeringGeofences();
            //Log geofence entered for debugging purposes
            Log.d(TAG, getString(R.string.geofence_entry));
            //For each loop to go through each triggered geofence
            for (Geofence g : triggeredGeofences) {
                //Create string to store geofence's request id
                String nameDesc = g.getRequestId();
                //Create string array to split the request id into name and description for the reminder's geofence
                String[] reminderDetails = nameDesc.split(getString(R.string.geofence_delimiter));
                //Check if string array size is greater than 1
                if (reminderDetails.length > 1) {
                    //Log that a notification has been set for debugging purposes
                    Log.d(TAG, getString(R.string.geofence_notification));
                    //Method call to send a notification passing in the reminder's name and description as arguments
                    sendNotification(reminderDetails[0], reminderDetails[1]);
                }
                //Else
                else {
                    //Log that a notification has been set for debugging purposes
                    Log.d(TAG, getString(R.string.geofence_notification));
                    //Method call to send a notification passing in the reminder's name and nothing for description
                    sendNotification(reminderDetails[0], "");
                }
            }
        }
    }

    /**
     * Returns converts error code by returning error string
     * @param errorCode
     * @return
     */
    private String getErrorString(int errorCode) {
        switch (errorCode) {
            case GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE:
                return getString(R.string.geofence_unavailable);
            case GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES:
                return getString(R.string.geofence_exceeded);
            case GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS:
                return getString(R.string.geofence_pendingintent);
            default:
                return getString(R.string.geofence_error);
        }
    }

    /**
     * Method that posts a notification setting the title and text of the notification with the passed parameters
     * @param name
     * @param desc
     */
    public void sendNotification(String name, String desc) {
        //Create new intent that sends users to RemindersMapsActivity when users click on the notification
        Intent notificationIntent = new Intent(getApplicationContext(), RemindersMapsActivity.class);
        //Create pendingintent containing the activity
        PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), PENDING_INTENT_REQUEST_ID, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        //Create new NotificationCompat.Builder object, passing in context and notification channel id
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                //Set notification icon to custom drawable
                .setSmallIcon(R.drawable.ic_remind_notification)
                //Setting notification icon colour
                .setColor(getResources().getColor(R.color.colorPrimary))
                //Setting notification title
                .setContentTitle(name)
                //Setting notification description
                .setContentText(desc)
                //Setting notification pending intent
                .setContentIntent(contentIntent)
                //Setting notification group
                .setGroup(GROUP_KEY_REMINDER)
                //Setting notification to dismiss itself when clicked on
                .setAutoCancel(true)
                //Setting priority of notification
                .setPriority(NotificationCompat.PRIORITY_MAX);

        //Creating new NotificationManagerCompat object
        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(this);
        //Push notification passing in id of notification and built notification
        managerCompat.notify(0, mBuilder.build());
    }
}
