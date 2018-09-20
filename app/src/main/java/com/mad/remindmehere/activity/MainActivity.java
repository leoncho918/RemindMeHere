package com.mad.remindmehere.activity;

import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.support.v7.widget.Toolbar;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.GoogleMap;
import com.mad.remindmehere.ListFragment;
import com.mad.remindmehere.R;
import com.mad.remindmehere.MapFragment;
import com.mad.remindmehere.model.Reminder;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private GoogleMap mMap;
    public static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    public static final String TAG = "MAD";
    public static final int MAP_ZOOM = 16;
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
}
