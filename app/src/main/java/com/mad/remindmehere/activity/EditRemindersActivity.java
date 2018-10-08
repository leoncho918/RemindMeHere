package com.mad.remindmehere.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import com.mad.remindmehere.model.Reminder;

public class EditRemindersActivity extends AppCompatActivity implements OnMapReadyCallback {

    //Variables to store ui elements
    private GoogleMap mMap;
    private TextView mAddressTv;
    private EditText mNameEt;
    private EditText mDescEt;
    private AppCompatSeekBar mSeekBar;
    private FloatingActionButton mAddFab;
    private Circle mCircle;

    //Variables to store data
    private String mName;
    private String mDesc;
    private double mLat;
    private double mLng;
    private int mRadius;
    private int mPosition;
    private boolean mLocationPermissionGranted;
    private boolean mNameSet;
    private LatLng mLatLng;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private Location mLastKnownLocation;
    private ReminderDatabase mReminderDatabase;

    //Constants
    public static final int DEFAULT_RADIUS = 10;
    public static final int DEFAULT_POSITION = 1;

    //Called when the activity is created
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Links xml layout to activity
        setContentView(R.layout.activity_edit_reminders);
        //Set the statusbar colour if Android Version is Lollipop or higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            this.getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        }
        //Linking ui elements to variables
        mAddressTv = (TextView) findViewById(R.id.location_Tv);
        mNameEt = (EditText) findViewById(R.id.name_Et);
        mDescEt = (EditText) findViewById(R.id.desc_Et);
        mSeekBar = (AppCompatSeekBar) findViewById(R.id.radius_Sb);
        mAddFab = (FloatingActionButton) findViewById(R.id.addReminder_fab);

        //Method call to get data from intent
        getData();
        //Method call to fill in data from intent
        fillData();
        //Method call to set a new SeekBarListener
        startSeekBarListener();

        startTextChangedListener();

        //Linking toolbar from xml layout
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //Setting the title of toolbar
        toolbar.setTitle(getString(R.string.title_activity_edit_reminders) + mName);
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
    }

    //Method to get reminder data from previous intent
    private void getData() {
        Intent intent = getIntent();
        mName = intent.getStringExtra(RemindersListActivity.NAME);
        mDesc = intent.getStringExtra(RemindersListActivity.DESC);
        mLat = intent.getDoubleExtra(RemindersListActivity.LAT, RemindersMapsActivity.DEFAULT_LAT);
        mLng = intent.getDoubleExtra(RemindersListActivity.LNG, RemindersMapsActivity.DEFAULT_LNG);
        mRadius = intent.getIntExtra(RemindersListActivity.RADIUS, DEFAULT_RADIUS);
        mPosition = intent.getIntExtra(RemindersListActivity.POSITION, DEFAULT_POSITION);
    }

    //Method to fill in reminder data into edittext boxes, seekbar and map
    private void fillData() {
        mNameEt.setText(mName);
        mDescEt.setText(mDesc);
        mSeekBar.setProgress(mRadius);
        mLatLng = new LatLng(mLat, mLng);
        //Method call to method in another activity
        AddReminderActivity.getAddress(mLatLng, getApplicationContext());
    }

    //Method to create a seekbar listener
    private void startSeekBarListener() {
        //Creating OnSeekBarChangeListener
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            //Called when the seekbar progress changes
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mRadius = progress;
                updateCircle();
            }
            //Called when seekbar starts tracking users finger
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }
            //Called when seekbar stops tracking users finger
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    //Method to create text changed listener
    private void startTextChangedListener() {
        //Setting a textchanged listener
        mNameEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

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
    }

    //Called when activity resumes
    @Override
    protected void onResume() {
        super.onResume();
        //Method call to get reminder database instance
        initialiseDatabase();
    }

    //Method to get instance of reminder database
    private void initialiseDatabase() {
        mReminderDatabase = ReminderDatabase.getReminderDatabase(getApplicationContext());
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
        //Else set mLocationPermissionGranted to true, setup map ui and move camera to reminder location
        else {
            mLocationPermissionGranted = true;
            updateUi();
            moveCamera(mLatLng, false, true);
        }
    }

    //Method to handle camera movement on map fragment
    private void moveCamera(LatLng latLng, boolean isAnimated, boolean moveCamera) {
        //Create new cameraUpdate object with latlng parameters and constant zoom from AddReminderActivity
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, AddReminderActivity.ADD_REMINDER_ZOOM);
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

    //Method called when edit location button is pressed
    public void changeLocation(View view) {
        //Create new intent to pass reminder latitude and longitude coordinates to start SelectLocationMapsActivity
        Intent intent = new Intent(EditRemindersActivity.this, SelectLocationMapsActivity.class);
        intent.putExtra(AddReminderActivity.LAT, mLat);
        intent.putExtra(AddReminderActivity.LNG, mLng);
        startActivityForResult(intent, AddReminderActivity.SELECT_LOCATION_RESULT);
    }

    //Method is called when the edit reminder fab is pressed
    public void editReminder(View view) {
        if (mNameSet) {
            //Get all text from edittext and seekbar
            mName = mNameEt.getText().toString();
            mDesc = mDescEt.getText().toString();
            mRadius = mSeekBar.getProgress();
            //Convert latlng variable into two double variables.
            mLat = mLatLng.latitude;
            mLng = mLatLng.longitude;

            //Create new reminder object and fill in variables of new reminder object
            Reminder updatedReminder = new Reminder();
            updatedReminder.setName(mName);
            updatedReminder.setDescription(mDesc);
            updatedReminder.setLat(mLat);
            updatedReminder.setLng(mLng);
            updatedReminder.setRadius(mRadius);

            //Create new async task to add new reminder into room database
            AddRemindersAsyncTask task = new AddRemindersAsyncTask();
            //Execute task with new reminder as an argument
            task.execute(updatedReminder);

            //Create new intent
            Intent resultIntent = new Intent();
            //Insert original reminder position in recyclerview to confirm reminder has been edited
            resultIntent.putExtra(RemindersListActivity.POSITION, mPosition);
            //Set the result
            setResult(RemindersListActivity.UPDATE_REMINDER, resultIntent);

            //Close activity
            finish();
        }
        //Else generate a toast to notify the user to set a reminder name
        else {
            Toast toast = Toast.makeText(this, R.string.toast_name_not_set, Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    //Called when result is received
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Check if request code is the same
        if (requestCode == AddReminderActivity.SELECT_LOCATION_RESULT) {
            //Check if resultcode is the same
            if (resultCode == AddReminderActivity.SELECT_LOCATION_RESULT) {
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
                mAddressTv.setText(AddReminderActivity.getAddress(mLatLng, getApplicationContext()));
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
