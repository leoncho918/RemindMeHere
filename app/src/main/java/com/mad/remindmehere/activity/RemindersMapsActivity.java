package com.mad.remindmehere.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
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
import com.mad.remindmehere.R;
import com.mad.remindmehere.model.Reminder;

import java.util.ArrayList;

public class RemindersMapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    public static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    public static final String TAG = "MAD";
    public static final int MAP_ZOOM = 16;
    public static final String LATITUDE = "LAT";
    public static final String LONGITUTE = "LONG";
    public static final int ADD_REMINDER = 1;
    private boolean mLocationPermissionGranted;
    private Location mLastKnownLocation = null;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private DrawerLayout mDrawerLayout;
    private ArrayList<Reminder> mReminders = new ArrayList<Reminder>();
    private EditText mSearchViewEt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminders_maps);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


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
        mMap = googleMap;

        getLocationPermission();

        updateLocationUI();

        getDeviceLocation(false, true);

        populateReminders();

        populateRemindersOnMap();
    }

    private void populateReminders() {
        mReminders.add(new Reminder(0, "Groceries", "Get groceries", new LatLng(-33.915609, 151.040804), 5));
    }

    private void populateRemindersOnMap() {
        for (Reminder r : mReminders) {
            MarkerOptions markerOptions = new MarkerOptions().position(r.getLatLng()).title(r.getName()).snippet(r.getDescription()).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_reminder_marker));
            mMap.addMarker(markerOptions);
            CircleOptions circleOptions = new CircleOptions().center(r.getLatLng()).radius(r.getRadius()*10).strokeColor(ResourcesCompat.getColor(getResources(), R.color.colorPrimaryDark, null)).fillColor(ResourcesCompat.getColor(getResources(), R.color.colorCircleFill, null));
            mMap.addCircle(circleOptions);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Inflating searchview layout
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_view, menu);
        //Getting reference to searchview
        MenuItem searchItem = menu.findItem(R.id.search);
        searchItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.showSoftInput(getCurrentFocus(), InputMethodManager.SHOW_IMPLICIT);
                mSearchViewEt.requestFocus();
                return false;
            }
        });
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        //Expanding searchview when icon is pressed.
        searchView.setIconified(false);
        searchView.setIconifiedByDefault(false);
        //Removing search icon
        int magnifyId = getResources().getIdentifier("android:id/search_mag_icon", null, null);
        ImageView magnifyIcon = (ImageView) searchView.findViewById(magnifyId);
        magnifyIcon.setLayoutParams(new LinearLayout.LayoutParams(0, 0));
        int editTextId = getResources().getIdentifier("android:id/search_src_text", null, null);
        mSearchViewEt = (EditText) searchView.findViewById(editTextId);
        searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(mSearchViewEt.getWindowToken(), 0);
            }
        });
        return true;
    }

    /**
     * Prompts the user for permission to use the device location.
     */
    private void getLocationPermission() {
         //Request location permission, so that app has the location of the device. The result of the permission request is handled by onRequestPermissionsResult.
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    //Called when user allows of denies a permission
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //If permission denied create dialog to tell user why permission is needed
        if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
            createLocationDialog();
        }
        //else enable location ui
        else {
            mLocationPermissionGranted = true;
            getDeviceLocation(true, true);
            updateLocationUI();
        }
    }

    //Method to create dialog to notify user about location permission
    private void createLocationDialog() {
        //Create alertdialog builder
        AlertDialog.Builder alertDlg = new AlertDialog.Builder(this);
        //Set title and message of alertdialog
        alertDlg.setTitle(R.string.dialog_title).setMessage(R.string.dialog_message);
        //OnClickListener for alertdialog's positive button
        alertDlg.setPositiveButton(R.string.dialog_allow, new DialogInterface.OnClickListener() {
            @Override
            //Called when user clicks on allow button
            public void onClick(DialogInterface dialog, int which) {
                //Get user's permission for location
                getLocationPermission();
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

    //Method to handle
    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        try {
            if (mLocationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                mMap.getUiSettings().setMapToolbarEnabled(false);
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                mLastKnownLocation = null;
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private void getDeviceLocation(final boolean isAnimated, final boolean moveCamera) {
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        try {
            if (mLocationPermissionGranted) {
                Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            mLastKnownLocation = (Location) task.getResult();
                            moveCamera(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()), isAnimated, moveCamera);
                        }
                        else {
                            Toast.makeText(RemindersMapsActivity.this, R.string.location_unavailable, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }
        catch (SecurityException e) {
            Log.e(TAG, "getDeviceLocation: SecurityException: " + e.getMessage());
        }
    }

    private void moveCamera(LatLng latLng, boolean isAnimated, boolean moveCamera) {
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, MAP_ZOOM);
        if (moveCamera) {
            if (isAnimated) {
                mMap.animateCamera(cameraUpdate);
            }
            if (!isAnimated) {
                mMap.moveCamera(cameraUpdate);
            }
        }
    }

    public void myLocation(View view) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            getLocationPermission();
        }
        else {
            getDeviceLocation(true, true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void addReminder(View view) {
        Intent intent = new Intent(RemindersMapsActivity.this, AddReminderActivity.class);
        intent.putExtra(LATITUDE, mLastKnownLocation.getLatitude());
        intent.putExtra(LONGITUTE, mLastKnownLocation.getLongitude());
        startActivityForResult(intent, ADD_REMINDER);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Reminder newReminder = new Reminder();
        if (requestCode == 1) {
            if (resultCode == ADD_REMINDER) {
                newReminder.setId(mReminders.size());
                newReminder.setName(data.getStringExtra(AddReminderActivity.NAME));
                newReminder.setDescription(data.getStringExtra(AddReminderActivity.DESCRIPTION));
                newReminder.setLatLng(new LatLng(data.getDoubleExtra(AddReminderActivity.LAT, 37.422), data.getDoubleExtra(AddReminderActivity.LNG, -122.084)));
                newReminder.setRadius(data.getIntExtra(AddReminderActivity.RADIUS, 1));
                mReminders.add(newReminder);
                mMap.clear();
                populateRemindersOnMap();
            }
        }
    }
}
