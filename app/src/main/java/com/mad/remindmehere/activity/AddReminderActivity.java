package com.mad.remindmehere.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatSeekBar;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.mad.remindmehere.R;
import com.mad.remindmehere.database.ReminderDatabase;
import com.mad.remindmehere.geofence.Geofencing;
import com.mad.remindmehere.model.Reminder;

import java.io.IOException;
import java.util.List;

//This activity handles all the functions and behaviour displayed in the activity to add reminders
public class AddReminderActivity extends AppCompatActivity implements OnMapReadyCallback {

    //Variables to store ui elements
    private GoogleMap mMap;
    private LatLng mLatLng;
    private TextView mAddressTv;
    private EditText mNameEt;
    private EditText mDescEt;
    private AppCompatSeekBar mSeekBar;
    private FloatingActionButton mAddFab;
    private Circle mCircle;

    //Variables to store data
    private boolean mLocationPermissionGranted;
    private Location mLastKnownLocation;
    private int mRadius;
    private boolean mNameSet;

    //Variables to store objects
    private ReminderDatabase mReminderDatabase;
    private FusedLocationProviderClient mFusedLocationProviderClient;

    //Constants
    public static final String LAT = "com.mad.RemindMeHere.LAT";
    public static final String LNG = "com.mad.RemindMeHere.LNG";
    public static final int ADD_REMINDER_ZOOM = 18;
    public static final int SELECT_LOCATION_RESULT = 2;

    //Called when the activity is created
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Links xml layout to activity
        setContentView(R.layout.activity_add_reminder);

        //Set the statusbar colour if Android Version is Lollipop or higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            this.getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        }

        //Linking toolbar from xml layout
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //Set navigation icon to custom drawable
        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_arrow_back));
        //Set on click listener for navigation icon to close activity when pressed
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //Linking ui elements to variables
        mAddressTv = (TextView) findViewById(R.id.location_Tv);
        mNameEt = (EditText) findViewById(R.id.name_Et);
        mDescEt = (EditText) findViewById(R.id.desc_Et);
        mSeekBar = (AppCompatSeekBar) findViewById(R.id.radius_Sb);
        mAddFab = (FloatingActionButton) findViewById(R.id.addReminder_fab);

        //Setting default radius to 10
        mRadius = 10;

        //Setting a textchanged listener
        mNameEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            //Called when text changes in name edittext
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //If the length in edit text is equal to 0 then set mNameSet to false else set it to true
                if (s.length() == 0) {
                    mNameSet = false;
                }
                else {
                    mNameSet = true;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        //Setting seekbar change listener
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            //Called when the seekbar progress changes
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //Update the radius with progress of seekbar and update the circle shown on the map fragment
                mRadius = progress;
                updateCircle();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        //Calling method to start get instance of room database
        initialiseDatabase();
    }

    //Method to get an instance of room database
    private void initialiseDatabase() {
        mReminderDatabase = ReminderDatabase.getReminderDatabase(getApplicationContext());
    }

    //Method to update the size of the circle shown on map fragment based on seekbar progress
    private void updateCircle() {
        //If circle does exist remove it
        if (mCircle != null) {
            mCircle.remove();
        }
        //Create a new circle and add it to map fragment
        CircleOptions circleOptions = new CircleOptions().center(mLatLng).radius(mRadius).strokeColor(ResourcesCompat.getColor(getResources(), R.color.colorPrimaryDark, null)).fillColor(ResourcesCompat.getColor(getResources(), R.color.colorCircleFill, null));
        mCircle = mMap.addCircle(circleOptions);
    }

    //Method converts latitude and longitude and returns an address in the form of a string
    public static String getAddress(LatLng latLng, Context context) {
        //Get instance of geocoder
        Geocoder geocoder = new Geocoder(context);
        //String variable to store address
        String lastAddress = "Couldn't get Address";
        //Try running
        try {
            //List to store all possible addresses returned by geocoder
            List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            //If list size is greater than 0
            if (addresses.size() > 0) {
                //Get first address object
                Address address = addresses.get(0);
                //Set string as address
                lastAddress = address.getAddressLine(0);
            }
        }
        //Catch exception and print stack
        catch (IOException e) {
            e.printStackTrace();
        }
        //Return address string
        return lastAddress;
    }

    //Method is called when the add reminder fab is pressed
    public void addReminder(View view) {
        //Run if the reminder name is set
        if (mNameSet) {
            //Convert latlng variable into two double variables.
            double lat = mLatLng.latitude;
            double lng = mLatLng.longitude;

            //Create a new reminder object
            Reminder newReminder = new Reminder();
            //Set reminder name to user input
            newReminder.setName(mNameEt.getText().toString());
            //Set reminder description to user input
            newReminder.setDescription(mDescEt.getText().toString());
            //Set reminder latitude
            newReminder.setLat(lat);
            //Set reminder longitude
            newReminder.setLng(lng);
            //Set reminder geofence radius
            newReminder.setRadius(mRadius);

            //Create new async task to add new reminder into room database
            AddRemindersAsyncTask task = new AddRemindersAsyncTask();
            //Execute task with new reminder as an argument
            task.execute(newReminder);

            //Create new intent
            Intent resultIntent = new Intent();
            //Insert lat and lng variables into intent
            resultIntent.putExtra(LAT, lat);
            resultIntent.putExtra(LNG, lng);
            //Set the result
            setResult(RemindersMapsActivity.ADD_REMINDER, resultIntent);
            //Close activity
            finish();
        }
        //Else generate a toast to notify the user to set a reminder name
        else {
            Toast toast = Toast.makeText(this, R.string.toast_name_not_set, Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    //Called when the map fragment is ready
    @Override
    public void onMapReady(GoogleMap googleMap) {
        //Set mMap as googleMap
        mMap = googleMap;
        //If app does not have location permission then get permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            RemindersMapsActivity.getLocationPermission(this, getApplicationContext());
        }
        //Else set mLocationPermissionGranted to true and get device location
        else {
            mLocationPermissionGranted = true;
            getDeviceLocation(false);
        }
    }

    //Called when user allows or denies a permission
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //If permission denied create dialog to tell user why permission is needed
        if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
            RemindersMapsActivity.createLocationDialog(this, AddReminderActivity.this);
        }
        //else enable location ui, get location and set boolean to true
        else {
            mLocationPermissionGranted = true;
            getDeviceLocation(true);
            updateUi();
        }
    }

    //Method to configure how users interact with map fragment
    private void updateUi() {
        //Try
        try {
            //Only run if location permission is granted and mLatLng is not null
            if (mLocationPermissionGranted && mLatLng != null) {
                //Show user location on map
                mMap.setMyLocationEnabled(true);
                //Add marker and circle on mLatLng
                addMarker(mLatLng);
                addCircle(mLatLng);
                //Disable scroll gestures, zoom gestures, zoom control, mylocationbutton, maptoolbar, tilt gestures and rotate gestures
                mMap.getUiSettings().setScrollGesturesEnabled(false);
                mMap.getUiSettings().setZoomGesturesEnabled(false);
                mMap.getUiSettings().setZoomControlsEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                mMap.getUiSettings().setMapToolbarEnabled(false);
                mMap.getUiSettings().setTiltGesturesEnabled(false);
                mMap.getUiSettings().setRotateGesturesEnabled(false);
            }
        }
        //Catch exception for when location permission is not granted
        catch (SecurityException e) {
            Log.e(RemindersMapsActivity.TAG, e.getMessage());
        }
    }

    //Method to add marker on map fragment
    private void addMarker(LatLng latLng) {
        MarkerOptions markerOptions = new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_reminder_marker));
        mMap.addMarker(markerOptions);
    }

    //Method to add circle on map fragment
    private void addCircle(LatLng latLng) {
        CircleOptions circleOptions = new CircleOptions().center(latLng).radius(mRadius).strokeColor(ResourcesCompat.getColor(getResources(), R.color.colorPrimaryDark, null)).fillColor(ResourcesCompat.getColor(getResources(), R.color.colorCircleFill, null));
        mCircle = mMap.addCircle(circleOptions);
    }

    //Method to get device location
    private void getDeviceLocation(final boolean isAnimated) {
        //Get instance of fusedlocationproviderclient
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        //Try
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
                            //Set latlng to last known location
                            mLatLng = new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude());
                            //Call method to move camera
                            moveCamera(mLatLng, isAnimated, true);
                            //Call method updateUi
                            updateUi();
                            //Set textview to new address
                            mAddressTv.setText(getAddress(mLatLng, getApplicationContext()));
                        }
                        //Else make toast to tell user that location cannot be found
                        else {
                            Toast.makeText(AddReminderActivity.this, R.string.toast_location_unavailable, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }
        //Catch SercurityException for when location permission is not granted
        catch (SecurityException e) {
            //Print error message to log
            Log.e(RemindersMapsActivity.TAG, e.getMessage());
        }
    }

    //Method to handle camera movement on map fragment
    private void moveCamera(LatLng latLng, boolean isAnimated, boolean moveCamera) {
        //Create new cameraUpdate object with latlng parameters and constant zoom
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, ADD_REMINDER_ZOOM);
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

    //Method called when edit location button is pressed
    public void changeLocation(View view) {
        //Create new intent to start SelectLocationMapsActivity
        Intent intent = new Intent(AddReminderActivity.this, SelectLocationMapsActivity.class);
        //Start activity and wait for result
        startActivityForResult(intent, SELECT_LOCATION_RESULT);
    }

    //Called when result is received
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Check if request code is the same
        if (requestCode == SELECT_LOCATION_RESULT) {
            //Check if resultcode is the same
            if (resultCode == SELECT_LOCATION_RESULT) {
                //Clear all markers and circles from map fragment
                mMap.clear();
                //Get latitude and longitude values from result intent
                double lat = data.getDoubleExtra(SelectLocationMapsActivity.MARKER_LAT, RemindersMapsActivity.DEFAULT_LAT);
                double lng = data.getDoubleExtra(SelectLocationMapsActivity.MARKER_LGN, RemindersMapsActivity.DEFAULT_LNG);
                //Set mLatLng to new LatLng object with doubles lat and lng
                mLatLng = new LatLng(lat, lng);
                //Add marker, circle and move camera to new latlng
                addMarker(mLatLng);
                addCircle(mLatLng);
                moveCamera(mLatLng, false, true);
                //Update mAddressTextView text to show the new address of mLatLng
                mAddressTv.setText(getAddress(mLatLng, getApplicationContext()));
            }
        }
    }

    //Class to add new reminder to room database
    private class AddRemindersAsyncTask extends AsyncTask<Reminder, Void, Void> {
        //Method called when task is executed
        @Override
        protected Void doInBackground(Reminder... reminders) {
            //Add reminder to room database
            mReminderDatabase.reminderDao().addReminder(reminders[0]);
            return null;
        }
    }
}
