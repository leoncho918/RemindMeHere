package com.mad.remindmehere.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
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

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
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

public class SelectLocationMapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener {

    private GoogleMap mMap;
    private EditText mSearchViewEt;
    private boolean mLocationPermissionGranted;
    private Location mLastKnownLocation;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private LatLng mReminderLatLng;
    private boolean markerSet = false;
    public static final String MARKER_LAT = "com.mad.remindmehere.MARKER_LAT";
    public static final String MARKER_LGN = "com.mad.remindmehere.MARKER_LNG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_location_maps);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_arrow_back));
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

        mMap.setOnMapLongClickListener(this);
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
                    MainActivity.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
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
            getDeviceLocation(false, true);
            updateLocationUI();
        }
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
                            Toast.makeText(SelectLocationMapsActivity.this, R.string.toast_location_unavailable, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }
        catch (SecurityException e) {
            Log.e(MainActivity.TAG, "getDeviceLocation: SecurityException: " + e.getMessage());
        }
    }

    private void moveCamera(LatLng latLng, boolean isAnimated, boolean moveCamera) {
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, MainActivity.MAP_ZOOM);
        if (moveCamera) {
            if (isAnimated) {
                mMap.animateCamera(cameraUpdate);
            }
            if (!isAnimated) {
                mMap.moveCamera(cameraUpdate);
            }
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

    @Override
    public void onMapLongClick(LatLng latLng) {
        addMarker(latLng);
    }

    private void addMarker(LatLng latLng) {
        mMap.clear();
        MarkerOptions marker = new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_reminder_marker));
        mMap.addMarker(marker);
        markerSet = true;
        mReminderLatLng = latLng;
    }

    public void myLocation(View view) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            getLocationPermission();
        }
        else {
            getDeviceLocation(true, true);
            addMarker(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()));
        }
    }

    public void setLocation(View view) {
        if (markerSet) {
            Intent resultIntent = new Intent();
            resultIntent.putExtra(MARKER_LAT, mReminderLatLng.latitude);
            resultIntent.putExtra(MARKER_LGN, mReminderLatLng.longitude);
            setResult(AddReminderActivity.SELECT_LOCATION_RESULT, resultIntent);
            finish();
        }
        else {
            Toast.makeText(SelectLocationMapsActivity.this, R.string.toast_marker, Toast.LENGTH_SHORT).show();
        }
    }
}
