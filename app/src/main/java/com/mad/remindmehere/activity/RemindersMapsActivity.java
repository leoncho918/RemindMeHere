package com.mad.remindmehere.activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
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
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
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
import com.mad.remindmehere.geofence.Geofencing;
import com.mad.remindmehere.R;
import com.mad.remindmehere.adapter.InfoWindowAdapter;
import com.mad.remindmehere.adapter.ReminderAdapter;
import com.mad.remindmehere.database.ReminderDatabase;
import com.mad.remindmehere.model.Reminder;
import com.mad.remindmehere.service.GeofenceTransitionsJobIntentService;

import java.util.ArrayList;

public class RemindersMapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    public static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    public static final String TAG = "MAD";
    public static final int MAP_ZOOM = 16;
    public static final double DEFAULT_LAT = 37.422;
    public static final double DEFAULT_LNG = -122.084;
    public static final int ADD_REMINDER = 1;
    public static final int LIST_REMINDER = 3;
    private static boolean mLocationPermissionGranted;
    private Location mLastKnownLocation = null;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private DrawerLayout mDrawerLayout;
    private ArrayList<Reminder> mReminders = new ArrayList<Reminder>();
    private ReminderDatabase mReminderDatabase;
    private Geofencing mGeofencing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminders_maps);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            this.getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        }

        NavigationView navigationView = findViewById(R.id.nav_view);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                //Save item id for easy access
                int id = item.getItemId();
                //Close drawer when item is tapped
                mDrawerLayout.closeDrawers();

                if (id == R.id.nav_reminders) {
                    Intent intent = new Intent(RemindersMapsActivity.this, RemindersListActivity.class);
                    startActivityForResult(intent, LIST_REMINDER);
                }

                return false;
            }
        });

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

        InfoWindowAdapter adapter = new InfoWindowAdapter(this);
        mMap.setInfoWindowAdapter(adapter);

        getLocationPermission(RemindersMapsActivity.this, getApplicationContext());

        updateLocationUI();

        getDeviceLocation(false, true);

    }

    @Override
    protected void onResume() {
        super.onResume();
        initialiseDatabase();

        getReminders();

        initialiseGeofencer();

        createNotificationChannel();
    }

    private void initialiseDatabase() {
        mReminderDatabase = ReminderDatabase.getReminderDatabase(getApplicationContext());
    }

    private void getReminders() {
        RefreshRemindersAsyncTask task = new RefreshRemindersAsyncTask();
        task.execute();
    }

    private void initialiseGeofencer() {
        mGeofencing = new Geofencing(getApplicationContext());
    }

    private void populateRemindersOnMap() {
        mMap.clear();
        for (Reminder r : mReminders) {
            MarkerOptions markerOptions = new MarkerOptions().position(new LatLng(r.getLat(), r.getLng())).title(r.getName()).snippet(r.getDescription()).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_reminder_marker));
            mMap.addMarker(markerOptions);
            CircleOptions circleOptions = new CircleOptions().center(new LatLng(r.getLat(), r.getLng())).radius(r.getRadius()).strokeColor(ResourcesCompat.getColor(getResources(), R.color.colorPrimaryDark, null)).fillColor(ResourcesCompat.getColor(getResources(), R.color.colorCircleFill, null));
            mMap.addCircle(circleOptions);
        }
    }

    /**
     * Prompts the user for permission to use the device location.
     */
    public static void getLocationPermission(Activity activity, Context context) {
         //Request location permission, so that app has the location of the device. The result of the permission request is handled by onRequestPermissionsResult.
        if (ContextCompat.checkSelfPermission(context,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(activity,
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
            createLocationDialog(this, RemindersMapsActivity.this);
        }
        //else enable location ui
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
                            if (mLastKnownLocation != null) {
                                moveCamera(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()), isAnimated, moveCamera);
                            }
                        }
                        else {
                            Toast.makeText(RemindersMapsActivity.this, R.string.toast_location_unavailable, Toast.LENGTH_SHORT).show();
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
        getLocationPermission(RemindersMapsActivity.this, getApplicationContext());
        if (!(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
            getDeviceLocation(true, true);
            updateLocationUI();
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
        startActivityForResult(intent, ADD_REMINDER);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADD_REMINDER) {
            if (resultCode == ADD_REMINDER) {
                LatLng latLng = new LatLng(data.getDoubleExtra(AddReminderActivity.LAT, DEFAULT_LAT), data.getDoubleExtra(AddReminderActivity.LNG, DEFAULT_LNG));
                RefreshRemindersAsyncTask task = new RefreshRemindersAsyncTask();
                task.execute();
                moveCamera(latLng, true, true);
            }
        }
        if (requestCode == LIST_REMINDER) {
            if (resultCode == LIST_REMINDER) {
                LatLng latLng = new LatLng(data.getDoubleExtra(ReminderAdapter.LAT, DEFAULT_LAT), data.getDoubleExtra(ReminderAdapter.LNG, DEFAULT_LNG));
                moveCamera(latLng, true, true);
            }
        }
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_desc);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(GeofenceTransitionsJobIntentService.CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private class RefreshRemindersAsyncTask extends AsyncTask<Void, Void, ArrayList<Reminder>> {
        @Override
        protected ArrayList<Reminder> doInBackground(Void... voids) {
            ArrayList<Reminder> reminders = new ArrayList<Reminder>();
            reminders = (ArrayList<Reminder>)mReminderDatabase.reminderDao().getAll();
            return reminders;
        }

        @Override
        protected void onPostExecute(ArrayList<Reminder> reminders) {
            super.onPostExecute(reminders);
            mReminders = reminders;
            populateRemindersOnMap();
            mGeofencing.unRegisterGeofences();
            mGeofencing.updateGeofences(reminders);
            mGeofencing.registerGeofences();
        }
    }
}
