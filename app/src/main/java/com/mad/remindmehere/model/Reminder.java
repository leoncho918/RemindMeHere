package com.mad.remindmehere.model;

import com.google.android.gms.maps.model.LatLng;

public class Reminder {
    private int mId;
    private String mName;
    private String mDescription;
    private LatLng mLatLng;
    private int mRadius;

    public Reminder(int mId, String mName, String mDescription, LatLng mLatLng, int mRadius) {
        this.mId = mId;
        this.mName = mName;
        this.mDescription = mDescription;
        this.mLatLng = mLatLng;
        this.mRadius = mRadius;
    }

    public Reminder() {
    }

    public int getId() {
        return mId;
    }

    public void setId(int mId) {
        this.mId = mId;
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
