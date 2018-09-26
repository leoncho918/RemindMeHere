package com.mad.remindmehere.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatSeekBar;
import android.support.v7.widget.Toolbar;
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

public class EditRemindersActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private LatLng mLatLng;
    private TextView mAddressTv;
    private EditText mNameEt;
    private EditText mDescEt;
    private AppCompatSeekBar mSeekBar;
    private FloatingActionButton mAddFab;
    private Circle mCircle;

    private String mName;
    private String mDesc;
    private double mLat;
    private double mLng;
    private int mRadius;
    private boolean mLocationPermissionGranted;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private Location mLastKnownLocation;

    public static final int DEFAULT_RADIUS = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_reminders);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            this.getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        }

        mAddressTv = (TextView) findViewById(R.id.location_Tv);
        mNameEt = (EditText) findViewById(R.id.name_Et);
        mDescEt = (EditText) findViewById(R.id.desc_Et);
        mSeekBar = (AppCompatSeekBar) findViewById(R.id.radius_Sb);
        mAddFab = (FloatingActionButton) findViewById(R.id.addReminder_fab);

        getData();

        fillData();

        startSeekBarListener();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.title_activity_edit_reminders) + mName);
        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_arrow_back));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                finish();
            }
        });

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void getData() {
        Intent intent = getIntent();
        mName = intent.getStringExtra(RemindersListActivity.NAME);
        mDesc = intent.getStringExtra(RemindersListActivity.DESC);
        mLat = intent.getDoubleExtra(RemindersListActivity.LAT, RemindersMapsActivity.DEFAULT_LAT);
        mLng = intent.getDoubleExtra(RemindersListActivity.LNG, RemindersMapsActivity.DEFAULT_LNG);
        mRadius = intent.getIntExtra(RemindersListActivity.RADIUS, DEFAULT_RADIUS);
    }

    private void fillData() {
        mNameEt.setText(mName);
        mDescEt.setText(mDesc);
        mSeekBar.setProgress(mRadius);
        mLatLng = new LatLng(mLat, mLng);
        AddReminderActivity.getAddress(mLatLng, getApplicationContext());
    }

    private void startSeekBarListener() {
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
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
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            RemindersMapsActivity.getLocationPermission(this, getApplicationContext());
        }
        else {
            mLocationPermissionGranted = true;
            updateUi();
            moveCamera(mLatLng, false, true);
        }
    }

    private void moveCamera(LatLng latLng, boolean isAnimated, boolean moveCamera) {
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, AddReminderActivity.ADD_REMINDER_ZOOM);
        if (moveCamera) {
            if (isAnimated) {
                mMap.animateCamera(cameraUpdate);
            }
            if (!isAnimated) {
                mMap.moveCamera(cameraUpdate);
            }
        }
    }

    private void updateUi() {
        try {
            if (mLocationPermissionGranted && mLatLng != null) {
                mMap.setMyLocationEnabled(true);
                addMarker(mLatLng);
                addCircle(mLatLng);
                mMap.getUiSettings().setScrollGesturesEnabled(false);
                mMap.getUiSettings().setZoomGesturesEnabled(false);
                mMap.getUiSettings().setZoomControlsEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                mMap.getUiSettings().setMapToolbarEnabled(false);
                mMap.getUiSettings().setTiltGesturesEnabled(false);
                mMap.getUiSettings().setRotateGesturesEnabled(false);
            }
        }
        catch (SecurityException e) {
            Log.e(RemindersMapsActivity.TAG, e.getMessage());
        }
    }

    private void addMarker(LatLng latLng) {
        MarkerOptions markerOptions = new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_reminder_marker));
        mMap.addMarker(markerOptions);
    }

    private void addCircle(LatLng latLng) {
        CircleOptions circleOptions = new CircleOptions().center(latLng).radius(mRadius).strokeColor(ResourcesCompat.getColor(getResources(), R.color.colorPrimaryDark, null)).fillColor(ResourcesCompat.getColor(getResources(), R.color.colorCircleFill, null));
        mCircle = mMap.addCircle(circleOptions);
    }

    private void updateCircle() {
        if (mCircle != null) {
            mCircle.remove();
        }
        CircleOptions circleOptions = new CircleOptions().center(mLatLng).radius(mRadius).strokeColor(ResourcesCompat.getColor(getResources(), R.color.colorPrimaryDark, null)).fillColor(ResourcesCompat.getColor(getResources(), R.color.colorCircleFill, null));
        mCircle = mMap.addCircle(circleOptions);
    }

    public void changeLocation(View view) {
        Intent intent = new Intent(EditRemindersActivity.this, SelectLocationMapsActivity.class);
        intent.putExtra(AddReminderActivity.LAT, mLat);
        intent.putExtra(AddReminderActivity.LNG, mLng);
        startActivityForResult(intent, AddReminderActivity.SELECT_LOCATION_RESULT);
    }

    public void editReminder(View view) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AddReminderActivity.SELECT_LOCATION_RESULT) {
            if (resultCode == AddReminderActivity.SELECT_LOCATION_RESULT) {
                mMap.clear();
                double lat = data.getDoubleExtra(SelectLocationMapsActivity.MARKER_LAT, RemindersMapsActivity.DEFAULT_LAT);
                double lng = data.getDoubleExtra(SelectLocationMapsActivity.MARKER_LGN, RemindersMapsActivity.DEFAULT_LNG);
                mLatLng = new LatLng(lat, lng);
                addMarker(mLatLng);
                addCircle(mLatLng);
                moveCamera(mLatLng, false, true);
                mAddressTv.setText(AddReminderActivity.getAddress(mLatLng, getApplicationContext()));
            }
        }
    }
}
