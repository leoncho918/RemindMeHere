package com.mad.remindmehere;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.res.ResourcesCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.mad.remindmehere.activity.MainActivity;
import com.mad.remindmehere.adapter.InfoWindowAdapter;
import com.mad.remindmehere.model.Reminder;

import java.util.List;

public class MapFragment extends Fragment implements OnMapReadyCallback {

    private SupportMapFragment mapFragment;
    private GoogleMap mMap;
    private boolean mLocationPermissionGranted;
    private Location mLastKnownLocation;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private Activity mContext;
    public static final int MAP_ZOOM = 16;
    private List<Reminder> mReminders;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reminders_map, container, false);

        if (mapFragment == null) {
            mapFragment = SupportMapFragment.newInstance();
            mapFragment.getMapAsync(this);
        }

        getChildFragmentManager().beginTransaction().replace(R.id.map, mapFragment).commit();

        mLocationPermissionGranted = ((MainActivity)getActivity()).getmLocationPermissionGranted();
        mContext = ((MainActivity)getActivity());
        mReminders = ((MainActivity)getActivity()).getReminders();

        return view;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        InfoWindowAdapter adapter = new InfoWindowAdapter(mContext);
        mMap.setInfoWindowAdapter(adapter);

        updateLocationUI();

        getDeviceLocation(false, true);

        populateRemindersOnMap();
    }

    private void populateRemindersOnMap() {
        for (Reminder r : mReminders) {
            MarkerOptions markerOptions = new MarkerOptions().position(r.getLatLng()).title(r.getName()).snippet(r.getDescription()).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_reminder_marker));
            mMap.addMarker(markerOptions);
            CircleOptions circleOptions = new CircleOptions().center(r.getLatLng()).radius(r.getRadius()).strokeColor(ResourcesCompat.getColor(getResources(), R.color.colorPrimaryDark, null)).fillColor(ResourcesCompat.getColor(getResources(), R.color.colorCircleFill, null));
            mMap.addCircle(circleOptions);
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
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(mContext);
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
                            Toast.makeText(mContext, R.string.toast_location_unavailable, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }
        catch (SecurityException e) {
            Log.e(MainActivity.TAG, "getDeviceLocation: SecurityException: " + e.getMessage());
        }
    }

    public void myLocation(View view) {
        if (mLocationPermissionGranted) {
            ActivityCompat.requestPermissions(mContext,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    MainActivity.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
        else {
            getDeviceLocation(true, true);
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
}
