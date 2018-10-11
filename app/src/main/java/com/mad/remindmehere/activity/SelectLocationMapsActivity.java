package com.mad.remindmehere.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.mad.remindmehere.R;
import com.mad.remindmehere.model.Reminder;

public class SelectLocationMapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener {

    //Variables to store ui widgets
    private GoogleMap mMap;

    //Variables to store data
    private LatLng mLatLng;
    private boolean mLocationPermissionGranted;
    private Location mLastKnownLocation;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private LatLng mReminderLatLng;
    private boolean markerSet = false;
    private boolean firstRun = true;

    //Constants
    public static final String MARKER_LAT = "com.mad.remindmehere.MARKER_LAT";
    public static final String MARKER_LGN = "com.mad.remindmehere.MARKER_LNG";
    public static final int PLACE_AUTOCOMPLETE_REQUESTCODE = 4;

    /**
     * Method is called when activity is first created
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Links xml layout to activity
        setContentView(R.layout.activity_select_location_maps);
        //Set the statusbar colour if Android Version is Lollipop or higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            this.getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        }
        //Linking toolbar from xml layout
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //Setting toolbar as the support action bar
        setSupportActionBar(toolbar);
        //Setting icon for navigation to custom drawable
        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_arrow_back));
        //On Click Listener for when Navigation item is selected
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
        //Get intent from previous activity and save lat and lng double variables.
        Intent intent = getIntent();
        double lat = intent.getDoubleExtra(AddReminderActivity.LAT, RemindersMapsActivity.DEFAULT_LAT);
        double lng = intent.getDoubleExtra(AddReminderActivity.LNG, RemindersMapsActivity.DEFAULT_LNG);
        //If lat lng variables aren't default values then assign mLatLng to new LatLng variable of double values
        if (lat != RemindersMapsActivity.DEFAULT_LAT && lng != RemindersMapsActivity.DEFAULT_LNG) {
            mLatLng = new LatLng(lat, lng);
        }
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     * @param googleMap
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        getLocationPermission();

        updateLocationUI();

        getDeviceLocation(false, true);

        mMap.setOnMapLongClickListener(this);
    }

    /**
     * Method that creates an intent for PlaceAutocomplete which searches for a location based or name or address and returns the latlng value of the selected place
     */
    private void startPlaceAutoComplete() {
        //Try
        try {
            //Create new intent to start PlaceAutocomplete overlay
            Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY).build(SelectLocationMapsActivity.this);
            //Start activity and wait for a result
            startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUESTCODE);
        }
        //Catch GooglePlayServicesNotAvailableException
        catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }
        //Catch GooglePlayServicesRepairableException
        catch (GooglePlayServicesRepairableException e) {
            e.printStackTrace();
        }
    }

    /**
     * Called when result is received
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //Check if request code is the same
        if (requestCode == PLACE_AUTOCOMPLETE_REQUESTCODE) {
            //Switch statement
            switch (resultCode) {
                //If result code is the same as RESULT_OK
                case RESULT_OK:
                    //Create a new place object for the place searched by the user
                    Place place = PlaceAutocomplete.getPlace(SelectLocationMapsActivity.this, data);
                    //Create new LatLng variable to store selected location's latlng value
                    LatLng searchedLagLng = place.getLatLng();
                    //Add marker at the location
                    addMarker(searchedLagLng);
                    //Move camera to that location
                    moveCamera(searchedLagLng, true, true);
                    //stop
                    break;
                //If result code is the same as PlaceAutocomplete.RESULT_ERROR
                case PlaceAutocomplete.RESULT_ERROR:
                    //Create new status object to store PlactAutocomplete status
                    Status status = PlaceAutocomplete.getStatus(SelectLocationMapsActivity.this, data);
                    //Log the status message
                    Log.i(RemindersMapsActivity.TAG, status.getStatusMessage());
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Prompts the user for permission to use the device location if permission isn't granted
     */
    private void getLocationPermission() {
        //Request location permission, so that app has the location of the device. The result of the permission request is handled by onRequestPermissionsResult.
        //Check if app has location permission
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        }
        //Else request for permission
        else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    RemindersMapsActivity.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    /**
     * Called when user allows of denies a permission
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //If permission denied create dialog to tell user why permission is needed
        if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
            RemindersMapsActivity.createLocationDialog(SelectLocationMapsActivity.this, SelectLocationMapsActivity.this);
        }
        //else enable location ui, get device location and set boolean to true
        else {
            mLocationPermissionGranted = true;
            getDeviceLocation(true, true);
            updateLocationUI();
        }
    }

    /**
     * Method to configure how users interact with map fragment if location permission is granted
     */
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

    /**
     * Method to get device location and move the map camera to the location
     * @param isAnimated
     * @param moveCamera
     */
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
                            //If current location known and this method has only run once
                            if (mLatLng == null || !firstRun) {
                                //Call method to move camera to last known location
                                moveCamera(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()), isAnimated, moveCamera);
                            }
                            //Else move camera to last known location, add marker and set boolean firstRun to false
                            else {
                                moveCamera(mLatLng, false, true);
                                addMarker(mLatLng);
                                firstRun = false;
                            }
                        }
                        //Else make toast to tell user that location cannot be found
                        else {
                            Toast.makeText(SelectLocationMapsActivity.this, R.string.toast_location_unavailable, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }
        //Catch SercurityException for when location permission is not granted
        catch (SecurityException e) {
            Log.e(RemindersMapsActivity.TAG, "getDeviceLocation: SecurityException: " + e.getMessage());
        }
    }

    /**
     * Method to handle camera movement on map fragment
     * @param latLng
     * @param isAnimated
     * @param moveCamera
     */
    private void moveCamera(LatLng latLng, boolean isAnimated, boolean moveCamera) {
        //Create new cameraUpdate object with latlng parameters and constant zoom
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, RemindersMapsActivity.MAP_ZOOM);
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

    /**
     * Method called when options in toolbar are created
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Inflating searchview layout
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_menu, menu);
        //Getting search menuitem
        MenuItem searchItem = menu.findItem(R.id.search);
        //Setting onmenuitemclicklistener
        searchItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            //Method called when menuitem is clicked
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                //Method call to start intent to PlaceAutocomplete overlay
                startPlaceAutoComplete();
                //Return false
                return false;
            }
        });
        //Return true
        return true;
    }

    /**
     * Method that is called when the user long clicks the map which then sets a marker at the location that is long clicked
     * @param latLng
     */
    @Override
    public void onMapLongClick(LatLng latLng) {
        //Method call to add marker at location
        addMarker(latLng);
    }

    /**
     * Method to clear map add new marker on map fragment
     * @param latLng
     */
    private void addMarker(LatLng latLng) {
        //Clear map of previous markers
        mMap.clear();
        //Create new MarkerOptions object with custom drawable and latlng location
        MarkerOptions marker = new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_reminder_marker));
        //Add marker onto map
        mMap.addMarker(marker);
        //Set boolean to true showing marker has been placed
        markerSet = true;
        //Set mReminderLatLng variable to marker latlng
        mReminderLatLng = latLng;
    }

    /**
     * Method is called when user clicks on mylocation fab, if location permission is granted device location is retrieved and a marker is placed on that location.
     * @param view
     */
    public void myLocation(View view) {
        //If location permission isn't granted then request permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            getLocationPermission();
        }
        //Else get device location and add marker at the location
        else {
            getDeviceLocation(true, true);
            addMarker(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()));
        }
    }

    /**
     * Method is called when user clicks on setLocation fab, checks if a marker has been set and puts the lat and lng variables into the intent and closes the activity.
     * Else a toast is generated notifying how to place a marker
     * @param view
     */
    public void setLocation(View view) {
        //If a marker has been set
        if (markerSet) {
            //Create a new intent to send data back to previous activity
            Intent resultIntent = new Intent();
            //Put latitude and longitude values into intent
            resultIntent.putExtra(MARKER_LAT, mReminderLatLng.latitude);
            resultIntent.putExtra(MARKER_LGN, mReminderLatLng.longitude);
            //Set result, adding result code and the result intent
            setResult(AddReminderActivity.SELECT_LOCATION_RESULT, resultIntent);
            //Close the activity
            finish();
        }
        //Else generate a toast telling user how to place marker on the map
        else {
            Toast.makeText(SelectLocationMapsActivity.this, R.string.toast_marker, Toast.LENGTH_SHORT).show();
        }
    }
}
