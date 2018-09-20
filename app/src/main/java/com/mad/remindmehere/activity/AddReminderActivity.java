package com.mad.remindmehere.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
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
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.mad.remindmehere.R;
import com.mad.remindmehere.model.Reminder;

import java.io.IOException;
import java.util.List;

public class AddReminderActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private LatLng mLatLng;
    private TextView mAddressTv;
    private EditText mNameEt;
    private EditText mDescEt;
    private TextView mRadiusTv;
    private AppCompatSeekBar mSeekBar;
    private FloatingActionButton mAddFab;
    private boolean mLocationPermissionGranted;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private Location mLastKnownLocation;
    private int mRadius;
    private boolean mNameSet;
    public static final String NAME = "com.mad.RemindMeHere.NAME";
    public static final String DESCRIPTION = "com.mad.RemindMeHere.DESCRIPTION";
    public static final String LAT = "com.mad.RemindMeHere.LAT";
    public static final String LNG = "com.mad.RemindMeHere.LNG";
    public static final String RADIUS = "com.mad.RemindMeHere.RADIUS";
    public static final int ADD_REMINDER_ZOOM = 17;
    public static final int SELECT_LOCATION_RESULT = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_reminder);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mAddressTv = (TextView) findViewById(R.id.location_Tv);
        mNameEt = (EditText) findViewById(R.id.name_Et);
        mDescEt = (EditText) findViewById(R.id.desc_Et);
        mRadiusTv = (TextView) findViewById(R.id.radius_Tv);
        mSeekBar = (AppCompatSeekBar) findViewById(R.id.radius_Sb);
        mAddFab = (FloatingActionButton) findViewById(R.id.addReminder_fab);

        mRadiusTv.setText(getString(R.string.radius_initialTv));
        mRadius = 1;

        mNameEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (count == 0) {
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

        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mRadiusTv.setText(getString(R.string.radius_textview) + progress);
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

    private void updateCircle() {
        mMap.clear();
        MarkerOptions markerOptions = new MarkerOptions().position(mLatLng).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_reminder_marker));
        mMap.addMarker(markerOptions);
        CircleOptions circleOptions = new CircleOptions().center(mLatLng).radius(mRadius*10).strokeColor(ResourcesCompat.getColor(getResources(), R.color.colorPrimaryDark, null)).fillColor(ResourcesCompat.getColor(getResources(), R.color.colorCircleFill, null));
        mMap.addCircle(circleOptions);
    }

    private String getAddress(LatLng latLng) {
        Geocoder geocoder = new Geocoder(this);
        String lastAddress = "Couldn't get Address";
        try {
            List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            if (addresses.size() > 0) {
                Address address = addresses.get(0);
                lastAddress = address.getAddressLine(0);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lastAddress;
    }

    public void addReminder(View view) {
        if (mNameSet) {
            double lat = mLatLng.latitude;
            double lng = mLatLng.longitude;
            Intent resultIntent = new Intent();
            resultIntent.putExtra(NAME, mNameEt.getText().toString());
            resultIntent.putExtra(DESCRIPTION, mDescEt.getText().toString());
            resultIntent.putExtra(LAT, lat);
            resultIntent.putExtra(LNG, lng);
            resultIntent.putExtra(RADIUS, mRadius);

            setResult(RemindersMapsActivity.ADD_REMINDER, resultIntent);
            finish();
        }
        else {
            Toast toast = Toast.makeText(this, R.string.toast_name_not_set, Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            getLocationPermission();
        }
        else {
            mLocationPermissionGranted = true;
        }
        if (mLocationPermissionGranted) {
            getDeviceLocation();
        }
    }

    private void getLocationPermission() {
        //Request location permission, so that app has the location of the device. The result of the permission request is handled by onRequestPermissionsResult.
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    RemindersMapsActivity.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    //Called when user allows of denies a permission
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //If permission denied create dialog to tell user why permission is needed
        if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
        }
        //else enable location ui
        else {
            mLocationPermissionGranted = true;
            getDeviceLocation();
            updateUi();
        }
    }

    private void updateUi() {
        try {
            if (mLocationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
                addMarker(mLatLng);
                addCircle(mLatLng);
                mMap.getUiSettings().setScrollGesturesEnabled(false);
                mMap.getUiSettings().setZoomGesturesEnabled(false);
                mMap.getUiSettings().setZoomControlsEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                mMap.getUiSettings().setMapToolbarEnabled(false);
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
        CircleOptions circleOptions = new CircleOptions().center(latLng).radius(mRadius*10).strokeColor(ResourcesCompat.getColor(getResources(), R.color.colorPrimaryDark, null)).fillColor(ResourcesCompat.getColor(getResources(), R.color.colorCircleFill, null));
        mMap.addCircle(circleOptions);
    }

    private void getDeviceLocation() {
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        try {
            if (mLocationPermissionGranted) {
                Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            mLastKnownLocation = (Location) task.getResult();
                            mLatLng = new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude());
                            moveCamera(mLatLng, false, true);
                            updateUi();
                            mAddressTv.setText(getAddress(mLatLng));
                        }
                        else {
                            Toast.makeText(AddReminderActivity.this, R.string.toast_location_unavailable, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }
        catch (SecurityException e) {
            Log.e(RemindersMapsActivity.TAG, e.getMessage());
        }
    }

    private void moveCamera(LatLng latLng, boolean isAnimated, boolean moveCamera) {
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, ADD_REMINDER_ZOOM);
        if (moveCamera) {
            if (isAnimated) {
                mMap.animateCamera(cameraUpdate);
            }
            if (!isAnimated) {
                mMap.moveCamera(cameraUpdate);
            }
        }
    }

    public void changeLocation(View view) {
        Intent intent = new Intent(AddReminderActivity.this, SelectLocationMapsActivity.class);
        startActivityForResult(intent, SELECT_LOCATION_RESULT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SELECT_LOCATION_RESULT) {
            if (resultCode == SELECT_LOCATION_RESULT) {
                mMap.clear();
                double lat = data.getDoubleExtra(SelectLocationMapsActivity.MARKER_LAT, RemindersMapsActivity.DEFAULT_LAT);
                double lng = data.getDoubleExtra(SelectLocationMapsActivity.MARKER_LGN, RemindersMapsActivity.DEFAULT_LNG);
                mLatLng = new LatLng(lat, lng);
                addMarker(mLatLng);
                addCircle(mLatLng);
                moveCamera(mLatLng, false, true);
                mAddressTv.setText(getAddress(mLatLng));
            }
        }
    }
}
