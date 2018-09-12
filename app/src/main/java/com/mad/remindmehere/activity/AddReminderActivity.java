package com.mad.remindmehere.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.mad.remindmehere.R;

public class AddReminderActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private LatLng mMarkerLatLng = null;
    private LatLng mLatLng;
    private TextView mAddressTf;
    private EditText mNameEt;
    private EditText mDescEt;
    private FloatingActionButton mAddFab;
    private boolean mNameSet;

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

        Intent intent = getIntent();
        double latitude = intent.getExtras().getDouble(RemindersMapsActivity.LATITUDE);
        double longitude = intent.getExtras().getDouble(RemindersMapsActivity.LONGITUTE);
        mLatLng = new LatLng(latitude, longitude);

        mAddressTf = (TextView) findViewById(R.id.location_Tv);
        mNameEt = (EditText) findViewById(R.id.name_Et);
        mDescEt = (EditText) findViewById(R.id.desc_Et);
        mAddFab = (FloatingActionButton) findViewById(R.id.addReminder_fab);

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
    }

    public void addReminder(View view) {
        if (mNameSet) {

        }
        else {
            Toast toast = Toast.makeText(this, R.string.toast_name_not_set, Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setScrollGesturesEnabled(false);
        mMap.getUiSettings().setZoomGesturesEnabled(false);
        mMap.getUiSettings().setZoomControlsEnabled(false);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.getUiSettings().setMapToolbarEnabled(false);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

        }
        mMap.setMyLocationEnabled(true);
        MarkerOptions markerOptions = new MarkerOptions().position(mLatLng).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_reminder_marker));
        mMap.addMarker(markerOptions);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(mLatLng, RemindersMapsActivity.MAP_ZOOM);
        mMap.moveCamera(cameraUpdate);
    }
}
