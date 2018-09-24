package com.mad.remindmehere.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.ColorInt;

import com.google.android.gms.maps.model.LatLng;

@Entity
public class Reminder {
    @PrimaryKey(autoGenerate = true)
    private int mId;

    @ColumnInfo(name = "name")
    private String mName;

    @ColumnInfo(name = "desc")
    private String mDescription;

    @ColumnInfo(name = "lat")
    private double mLat;

    @ColumnInfo(name = "lng")
    private double mLng;

    @ColumnInfo(name = "radius")
    private int mRadius;

    public Reminder(int mId, String mName, String mDescription, double lat, double lng, int mRadius) {
        this.mId = mId;
        this.mName = mName;
        this.mDescription = mDescription;
        this.mLat = lat;
        this.mLng = lng;
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

    public double getLat() {
        return mLat;
    }

    public void setLat(double mLat) {
        this.mLat = mLat;
    }

    public double getLng() {
        return mLng;
    }

    public void setLng(double mLng) {
        this.mLng = mLng;
    }

    public int getRadius() {
        return mRadius;
    }

    public void setRadius(int mRadius) {
        this.mRadius = mRadius;
    }
}
