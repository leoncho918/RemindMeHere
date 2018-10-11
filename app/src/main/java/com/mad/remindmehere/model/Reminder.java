package com.mad.remindmehere.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;

/**
 * Class for a Reminders
 */
@Entity(tableName = "reminders")
public class Reminder {
    @NonNull
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
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

    /**
     * Constructor for a Reminder
     */
    public Reminder() {
    }

    /**
     * Returns the reminder id as a int
     * @return
     */
    public int getId() {
        return mId;
    }

    /**
     * Sets the reminder id to given parameter
     * @param mId
     */
    public void setId(int mId) {
        this.mId = mId;
    }

    /**
     * Returns the name of the reminder as a string
     * @return
     */
    public String getName() {
        return mName;
    }

    /**
     * Sets the reminder name to the given parameter
     * @param mName
     */
    public void setName(String mName) {
        this.mName = mName;
    }

    /**
     * Returns the description of the reminder as a string
     * @return
     */
    public String getDescription() {
        return mDescription;
    }

    /**
     * Sets the reminder description to the given parameter
     * @param mDescription
     */
    public void setDescription(String mDescription) {
        this.mDescription = mDescription;
    }

    /**
     * Returns the latitude of the reminder as a double
     * @return
     */
    public double getLat() {
        return mLat;
    }

    /**
     * Sets the reminder latitude to the given parameter
     * @param mLat
     */
    public void setLat(double mLat) {
        this.mLat = mLat;
    }

    /**
     * Returns the longitude of the reminder as a double
     * @return
     */
    public double getLng() {
        return mLng;
    }

    /**
     * Sets the reminder longitude to the given parameter
     * @param mLng
     */
    public void setLng(double mLng) {
        this.mLng = mLng;
    }

    /**
     * Returns the radius of the reminder as an int
     * @return
     */
    public int getRadius() {
        return mRadius;
    }

    /**
     * Sets the reminder radius to the given parameter
     * @param mRadius
     */
    public void setRadius(int mRadius) {
        this.mRadius = mRadius;
    }
}
