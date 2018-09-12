package com.mad.remindmehere.model;

import com.google.android.gms.maps.model.LatLng;

public class Reminder {
    private String mName;
    private String mDescription;
    private LatLng mLatLng;
    private int mRadius;

    public Reminder(String mName, String mDescription, LatLng mLatLng, int mRadius) {
        this.mName = mName;
        this.mDescription = mDescription;
        this.mLatLng = mLatLng;
        this.mRadius = mRadius;
    }

    public String getName() {
        return mName;
    }

    public void setName(String mName) {
        this.mName = mName;
    }

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String mDescription) {
        this.mDescription = mDescription;
    }

    public LatLng getLatLng() {
        return mLatLng;
    }

    public void setLatLng(LatLng mLatLng) {
        this.mLatLng = mLatLng;
    }

    public int getRadius() {
        return mRadius;
    }

    public void setRadius(int mRadius) {
        this.mRadius = mRadius;
    }
}
