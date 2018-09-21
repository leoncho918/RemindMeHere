package com.mad.remindmehere.activity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.support.v7.widget.Toolbar;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.mad.remindmehere.ListFragment;
import com.mad.remindmehere.R;
import com.mad.remindmehere.MapFragment;
import com.mad.remindmehere.model.Reminder;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private GoogleMap mMap;
    public static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    public static final String TAG = "MAD";

    public static final String LATITUDE = "com.mad.remindmehere.LATITUDE";
    public static final String LONGITUTE = "com.mad.remindmehere.LONGITUDE";
    public static final double DEFAULT_LAT = 37.422;
    public static final double DEFAULT_LNG = -122.084;
    public static final int ADD_REMINDER = 1;
    private boolean mLocationPermissionGranted;
    private Location mLastKnownLocation = null;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private DrawerLayout mDrawer;
    private Toolbar mToolbar;
    private NavigationView mNavView;
    private ArrayList<Reminder> mReminders = new ArrayList<Reminder>();

    private ActionBarDrawerToggle drawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        }

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        mNavView = (NavigationView) findViewById(R.id.nav_view);
        mNavView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, mDrawer, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);

        mDrawer.addDrawerListener(toggle);
        toggle.syncState();

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment, new MapFragment()).commit();
            mNavView.setCheckedItem(R.id.nav_map);
        }

        populateReminders();
    }

    private void populateReminders() {
        mReminders.add(new Reminder(0, "Groceries", "Get groceries", new LatLng(-33.915609, 151.040804), 5));
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_map:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment, new MapFragment()).commit();
                break;
            case R.id.nav_reminders:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment, new ListFragment()).commit();
                break;
        }

        mDrawer.closeDrawer(GravityCompat.START);

        return true;
    }

    @Override
    public void onBackPressed() {
        if (mDrawer.isDrawerOpen(GravityCompat.START)) {
            mDrawer.closeDrawer(GravityCompat.START);
        }
        else {
            super.onBackPressed();
        }
    }

    /**
     * Prompts the user for permission to use the device location.
     */
    public void getLocationPermission() {
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

    public boolean getmLocationPermissionGranted() {
        return mLocationPermissionGranted;
    }

    public List<Reminder> getReminders() {
        return mReminders;
    }
}
