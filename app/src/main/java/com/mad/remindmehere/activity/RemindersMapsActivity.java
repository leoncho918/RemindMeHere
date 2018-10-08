package com.mad.remindmehere.activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.mad.remindmehere.geofence.Geofencing;
import com.mad.remindmehere.R;
import com.mad.remindmehere.adapter.InfoWindowAdapter;
import com.mad.remindmehere.adapter.ReminderAdapter;
import com.mad.remindmehere.database.ReminderDatabase;
import com.mad.remindmehere.model.Reminder;
import com.mad.remindmehere.geofence.GeofenceTransitionsJobIntentService;

import java.util.ArrayList;

//This activity handles all the functions and behaviour in the actitivy that shows all reminders on a map
public class RemindersMapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    //Variables to store ui elements
    private GoogleMap mMap;
    private DrawerLayout mDrawerLayout;
    private SupportMapFragment mMapFragment;
    private EditText mJsonEditText;
    private Button mCancelBtn;
    private Button mAddBtn;
    private Toolbar mToolbar;

    //Variables to store data
    private static boolean mLocationPermissionGranted;
    private Location mLastKnownLocation = null;
    private ArrayList<Reminder> mReminders = new ArrayList<Reminder>();

    //Variables to store objects
    private ReminderDatabase mReminderDatabase;
    private Geofencing mGeofencing;
    private FusedLocationProviderClient mFusedLocationProviderClient;

    //Constants
    public static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    public static final String TAG = "MAD";
    public static final int MAP_ZOOM = 16;
    public static final double DEFAULT_LAT = 37.422;
    public static final double DEFAULT_LNG = -122.084;
    public static final int ADD_REMINDER = 1;
    public static final int LIST_REMINDER = 3;
    public static final int TOAST_OFFSET = 0;

    //Called when the activity is created
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Links xml layout to activity
        setContentView(R.layout.activity_reminders_maps);

        //Set the statusbar colour if Android Version is Lollipop or higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            this.getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        }

        //Linking components from xml
        mJsonEditText = (EditText) findViewById(R.id.json_Et);
        mAddBtn = (Button) findViewById(R.id.addReminder_btn);
        mCancelBtn = (Button) findViewById(R.id.cancel_btn);
        //Set on click listeners for buttons
        mAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addJsonReminders();
            }
        });
        mCancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideJsonEt();
                Activity activity = RemindersMapsActivity.this;
                InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
                //Find the currently focused view, so we can grab the correct window token from it.
                View view = activity.getCurrentFocus();
                //If no view currently has focus, create a new one, just so we can grab a window token from it
                if (view == null) {
                    view = new View(activity);
                }
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        });
        //Linking toolbar from xml layout
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        //Setting toolbar as the support action bar
        setSupportActionBar(mToolbar);
        //Linking support action bar in xml file with variable
        ActionBar actionBar = getSupportActionBar();
        //Enabling button to go back
        actionBar.setDisplayHomeAsUpEnabled(true);
        //Changing icon for button to custom drawable
        actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mMapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mMapFragment.getMapAsync(this);

        //Create navigation view to link to navigation view in menu resources
        NavigationView navigationView = findViewById(R.id.nav_view);

        //Link DrawerLayout to one in xml layout
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        //On Click Listener for when Navigation item is selected
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                //Save item id for easy access
                int id = item.getItemId();
                //Close drawer when item is tapped
                mDrawerLayout.closeDrawers();

                //Checks what navigation item is selected
                if (id == R.id.nav_reminders) {
                    //Method call to make map fragment visible
                    hideJsonEt();
                    //Create a new intent to start ReminderListActivity
                    Intent intent = new Intent(RemindersMapsActivity.this, RemindersListActivity.class);
                    //Start activity and wait for result
                    startActivityForResult(intent, LIST_REMINDER);
                }
                if (id == R.id.nav_export) {
                    //Method call to make map fragment visible
                    hideJsonEt();
                    //Method call to save json string into clipboard
                    saveRemindersToClipboard(reminderToJsonString());
                    //Show toast to notify user their reminders are saved in clipboard
                    Toast toast = Toast.makeText(getApplicationContext(), getString(R.string.clipboard_reminders), Toast.LENGTH_LONG);
                    //Set text alignment of toast to center by getting layout of toast and getting textview from layout
                    LinearLayout layout = (LinearLayout) toast.getView();
                    if (layout.getChildCount() > 0) {
                        TextView textView = (TextView) layout.getChildAt(0);
                        textView.setGravity(Gravity.CENTER);
                    }
                    //Show toast
                    toast.show();
                }
                if (id == R.id.nav_import) {
                    //Method call to make json Edit Text visible
                    showJsonEt();
                }
                return false;
            }
        });
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        //Set mMap as googleMap
        mMap = googleMap;

        //Create an adapter for the info windows and set the adapter for the map
        InfoWindowAdapter adapter = new InfoWindowAdapter(this);
        mMap.setInfoWindowAdapter(adapter);

        //Method call to get location permission
        getLocationPermission(RemindersMapsActivity.this, getApplicationContext());

        //Method call to set up how the map fragment can be interacted with
        updateLocationUI();

        //Method call to get device location
        getDeviceLocation(false, true);

    }

    //Method called when activity starts/resumes
    @Override
    protected void onResume() {
        super.onResume();
        //Calling method to start get instance of room database
        initialiseDatabase();

        //Calling method to start async task to get reminders stored in database
        getReminders();

        //Calling method to assign instance of geofencer
        initialiseGeofencer();

        //Calling method to create notification channel
        createNotificationChannel();
    }

    //Method to get an instance of room database
    private void initialiseDatabase() {
        mReminderDatabase = ReminderDatabase.getReminderDatabase(getApplicationContext());
    }

    //Method to get reminders from database
    private void getReminders() {
        //Create new RefreshRemindersAsyncTask
        RefreshRemindersAsyncTask task = new RefreshRemindersAsyncTask();
        //Execute the task
        task.execute();
    }

    //Method to create object of geofencer
    private void initialiseGeofencer() {
        mGeofencing = new Geofencing(getApplicationContext());
    }

    //Method to populate all reminders and their geofence radius onto the map
    private void populateRemindersOnMap() {
        mMap.clear();
        for (Reminder r : mReminders) {
            MarkerOptions markerOptions = new MarkerOptions().position(new LatLng(r.getLat(), r.getLng())).title(r.getName()).snippet(r.getDescription()).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_reminder_marker));
            mMap.addMarker(markerOptions);
            CircleOptions circleOptions = new CircleOptions().center(new LatLng(r.getLat(), r.getLng())).radius(r.getRadius()).strokeColor(ResourcesCompat.getColor(getResources(), R.color.colorPrimaryDark, null)).fillColor(ResourcesCompat.getColor(getResources(), R.color.colorCircleFill, null));
            mMap.addCircle(circleOptions);
        }
    }

    //Prompts the user for permission to use the device location.
    public static void getLocationPermission(Activity activity, Context context) {
        //Request location permission, so that app has the location of the device. The result of the permission request is handled by onRequestPermissionsResult.
        //Check if app has location permission
        if (ContextCompat.checkSelfPermission(context,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            //Set boolean to true
            mLocationPermissionGranted = true;
        }
        //Else request for permission
        else {
            ActivityCompat.requestPermissions(activity,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    //Called when user allows or denies a permission
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //If permission denied create dialog to tell user why permission is needed
        if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
            createLocationDialog(this, RemindersMapsActivity.this);
        }
        //else enable location ui, get device location and set boolean to true
        else {
            mLocationPermissionGranted = true;
            getDeviceLocation(true, true);
            updateLocationUI();
        }
    }

    //Method to create dialog to notify user about location permission
    public static void createLocationDialog(final Activity activity, final Context context) {
        //Create alertdialog builder
        AlertDialog.Builder alertDlg = new AlertDialog.Builder(context);
        //Set title and message of alertdialog
        alertDlg.setTitle(R.string.dialog_title).setMessage(R.string.dialog_message);
        //OnClickListener for alertdialog's positive button
        alertDlg.setPositiveButton(R.string.dialog_allow, new DialogInterface.OnClickListener() {
            @Override
            //Called when user clicks on allow button
            public void onClick(DialogInterface dialog, int which) {
                //Get user's permission for location
                getLocationPermission(activity, context);
            }
        });
        //OnClickListener for alertdialog's negative button
        alertDlg.setNegativeButton(R.string.dialog_deny, new DialogInterface.OnClickListener() {
            //Called when the user clicks on deny button
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        //Create alertdialog
        AlertDialog dialog = alertDlg.create();
        //Show alertdialog
        dialog.show();
    }

    //Method to set the maps visibility to gone and set buttons and edittext to visible
    private void showJsonEt() {
        mMapFragment.getView().setVisibility(View.GONE);
        mJsonEditText.setVisibility(View.VISIBLE);
        mAddBtn.setVisibility(View.VISIBLE);
        mCancelBtn.setVisibility(View.VISIBLE);
        mToolbar.setTitle(R.string.title_activity_reminders_maps_json);
    }

    //Method to set the maps visibility to visible and set buttons and edittext to gone
    private void hideJsonEt() {
        mMapFragment.getView().setVisibility(View.VISIBLE);
        mJsonEditText.setVisibility(View.GONE);
        mAddBtn.setVisibility(View.GONE);
        mCancelBtn.setVisibility(View.GONE);
        mJsonEditText.getText().clear();
        mToolbar.setTitle(R.string.title_activity_reminders_maps);
    }

    private void addJsonReminders() {
        String jsonString = mJsonEditText.getText().toString();
        Gson gson = new Gson();
        Reminder reminder;
        ArrayList<Reminder> reminderArrayList = new ArrayList<>();
        String[] jsonStringArray = jsonString.split(getString(R.string.delimiter));
        for (String s : jsonStringArray) {
            try {
                reminder = gson.fromJson(s, Reminder.class);
            }
            catch (JsonSyntaxException e) {
                Toast.makeText(getApplicationContext(), getString(R.string.json_error), Toast.LENGTH_LONG).show();
                Log.e(TAG, e.getMessage());
                return;
            }
            Reminder newReminder = new Reminder();
            newReminder.setDescription(reminder.getDescription());
            newReminder.setLat(reminder.getLat());
            newReminder.setLng(reminder.getLng());
            newReminder.setName(reminder.getName());
            newReminder.setRadius(reminder.getRadius());
            reminderArrayList.add(newReminder);
        }
        hideJsonEt();
        AddRemindersAsyncTask task = new AddRemindersAsyncTask();
        task.execute(reminderArrayList);
    }

    //Method to configure how users interact with map fragment
    private void updateLocationUI() {
        //Check if mMap is null
        if (mMap == null) {
            return;
        }
        try {
            //Only run if location permission is granted
            if (mLocationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                mMap.getUiSettings().setMapToolbarEnabled(false);
            }
            //Disable user location on map, button to get location and set last location to null
            else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                mLastKnownLocation = null;
            }
        }
        //Catch security exception and print stack to log
        catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    //Method to get device location
    private void getDeviceLocation(final boolean isAnimated, final boolean moveCamera) {
        //Get instance of fusedlocationproviderclient
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        try {
            //If location permission is granted
            if (mLocationPermissionGranted) {
                //Get last known location
                Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        //If task is successful
                        if (task.isSuccessful()) {
                            //Set last known location
                            mLastKnownLocation = (Location) task.getResult();
                            //If lastlocation is known
                            if (mLastKnownLocation != null) {
                                //Call method to move camera
                                moveCamera(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()), isAnimated, moveCamera);
                            }
                        }
                        //Else make toast to tell user that location cannot be found
                        else {
                            Toast.makeText(RemindersMapsActivity.this, R.string.toast_location_unavailable, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }
        //Catch SercurityException for when location permission is not granted
        catch (SecurityException e) {
            //Print error message to log
            Log.e(TAG, "getDeviceLocation: SecurityException: " + e.getMessage());
        }
    }

    //Method to handle camera movement on map fragment
    private void moveCamera(LatLng latLng, boolean isAnimated, boolean moveCamera) {
        //Create new cameraUpdate object with latlng parameters and constant zoom
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, MAP_ZOOM);
        //If moveCamera boolean is true
        if (moveCamera) {
            //If isAnimated boolean is true
            if (isAnimated) {
                //Animate map fragment camera movement to new latlng
                mMap.animateCamera(cameraUpdate);
            }
            //If isAnimated boolean is false
            if (!isAnimated) {
                //Move map fragment camera to new latlng
                mMap.moveCamera(cameraUpdate);
            }
        }
    }

    //Method handles getting the device's location called when user clicks on mylocation fab
    public void myLocation(View view) {
        //Method call to make map fragment visible
        hideJsonEt();
        //Calls method to get location permission
        getLocationPermission(RemindersMapsActivity.this, getApplicationContext());
        //If location permission is granted
        if (!(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
            //Call method ot get device location
            getDeviceLocation(true, true);
            //Call method to configure map fragment interactions
            updateLocationUI();
        }
    }

    //Method called when user selects menu item in options
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //Open drawerlayout when home button is clicked
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //Method opens AddReminderActivity for users to add a new reminder and is called by addReminder fab
    public void addReminder(View view) {
        //Method call to make map fragment visible
        hideJsonEt();
        //Create new intent to start AddReminderActivity
        Intent intent = new Intent(RemindersMapsActivity.this, AddReminderActivity.class);
        //Start activity and wait for result
        startActivityForResult(intent, ADD_REMINDER);
    }

    //Called when result is received
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Check which activity results are coming from
        if (requestCode == ADD_REMINDER) {
            if (resultCode == ADD_REMINDER) {
                //Create new LatLng object to store lat and lng double variables
                LatLng latLng = new LatLng(data.getDoubleExtra(AddReminderActivity.LAT, DEFAULT_LAT), data.getDoubleExtra(AddReminderActivity.LNG, DEFAULT_LNG));
                //Calling method to start async task to get reminders stored in database
                getReminders();
                //Call method to move camera to new latlng
                moveCamera(latLng, true, true);
            }
        }
        //Check which activity results are coming from
        if (requestCode == LIST_REMINDER) {
            if (resultCode == LIST_REMINDER) {
                //Create new LatLng object to store lat and lng double variables
                LatLng latLng = new LatLng(data.getDoubleExtra(ReminderAdapter.LAT, DEFAULT_LAT), data.getDoubleExtra(ReminderAdapter.LNG, DEFAULT_LNG));
                //Call method to move camera to new latlng
                moveCamera(latLng, true, true);
            }
        }
    }

    //Method to create notification channel
    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //Get name and description of channel and declare channel importance
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_desc);
            int importance = NotificationManager.IMPORTANCE_MAX;
            //Create new notification channel object with channel id, name, importance as arguments
            NotificationChannel channel = new NotificationChannel(GeofenceTransitionsJobIntentService.CHANNEL_ID, name, importance);
            //Set channel description
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private String reminderToJsonString() {
        Gson gson = new Gson();
        String stringJson = "";
        if (mReminders.size() > 0) {
            stringJson = gson.toJson(mReminders.get(0));
        }
        if (mReminders.size() > 1) {
            for (int i = 1; i < mReminders.size(); i++) {
                stringJson = stringJson + getString(R.string.delimiter) + gson.toJson(mReminders.get(i));
            }
        }
        return stringJson;
    }

    private void saveRemindersToClipboard(String jsonString) {
        ClipboardManager manager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(getString(R.string.clipboard_name), jsonString);
        manager.setPrimaryClip(clip);
    }

    //Class to retrieve all reminders from room database
    private class RefreshRemindersAsyncTask extends AsyncTask<Void, Void, ArrayList<Reminder>> {
        @Override
        protected ArrayList<Reminder> doInBackground(Void... voids) {
            //Create new arraylist for reminders
            ArrayList<Reminder> reminders = new ArrayList<Reminder>();
            //Populate new arraylist with reminders in room database
            reminders = (ArrayList<Reminder>)mReminderDatabase.reminderDao().getAll();
            //Return reminders list
            return reminders;
        }

        @Override
        protected void onPostExecute(ArrayList<Reminder> reminders) {
            super.onPostExecute(reminders);
            //Update reminders list with reminders from database
            mReminders = reminders;
            //Method call to repopulate reminders on map
            populateRemindersOnMap();
            //Get geofencing object to unregister any existing geofences, update reminders list to build new geofences and register them.
            mGeofencing.unRegisterGeofences();
            mGeofencing.updateGeofences(reminders);
            mGeofencing.registerGeofences();
        }
    }

    //Class to add new reminder to room database
    private class AddRemindersAsyncTask extends AsyncTask<ArrayList<Reminder>, Void, Void> {
        //Method called when task is executed
        @Override
        protected Void doInBackground(ArrayList<Reminder>... arrayLists) {
            for (Reminder r : arrayLists[0]) {
                mReminderDatabase.reminderDao().addReminder(r);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            RefreshRemindersAsyncTask task = new RefreshRemindersAsyncTask();
            task.execute();
        }
    }
}
