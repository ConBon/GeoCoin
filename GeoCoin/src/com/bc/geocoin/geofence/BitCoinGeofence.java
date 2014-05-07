package com.bc.geocoin.geofence;

import com.google.android.gms.location.Geofence;

/**
 * A single Geofence object, defined by its center and radius.
 */
public class BitCoinGeofence {
        // Instance variables
        private final String mId;
        private final double mLatitude;
        private final double mLongitude;
        private final float mRadius;
        private long mExpirationDuration;
        private int mTransitionType;
        private final String mName;
        private final String mCity;
        private final String mAddress;
        private final String mWeb;
        private final String mPhone;
        private final String mIcon;

    /**
     * @param geofenceId The Geofence's request ID
     * @param latitude Latitude of the Geofence's center.
     * @param longitude Longitude of the Geofence's center.
     * @param radius Radius of the geofence circle.
     * @param expiration Geofence expiration duration
     * @param transition Type of Geofence transition.
     * @param name of the geofence target.
     * @param city of the geofence target.
     * @param address of the geofence target.
     * @param web address of the geofence target.
     * @param phone number of the geofence target..
     * @param icon type of the geofence target
     */
    public BitCoinGeofence(
            String geofenceId,
            double latitude,
            double longitude,
            float radius,
            long expiration,
            int transition,
            String name,
            String city,
            String address,
            String web,
            String phone,
            String icon) {
        // Set the instance fields from the constructor
        this.mId = geofenceId;
        this.mLatitude = latitude;
        this.mLongitude = longitude;
        this.mRadius = radius;
        this.mExpirationDuration = expiration;
        this.mTransitionType = transition;
        this.mName = name;
        this.mCity = city;
        this.mAddress = address;
        this.mWeb = web;
        this.mPhone = phone;
        this.mIcon = icon;
    }
    // Instance field getters
    public String getId() {
        return mId;
    }
    public double getLatitude() {
        return mLatitude;
    }
    public double getLongitude() {
        return mLongitude;
    }
    public float getRadius() {
        return mRadius;
    }
    public long getExpirationDuration() {
        return mExpirationDuration;
    }
    public int getTransitionType() {
        return mTransitionType;
    }
    public String getName() {
        return mName;
    }
    public String getCity() {
        return mCity;
    }
    public String getAddress() {
        return mAddress;
    }
    public String getWeb() {
        return mWeb;
    }
    public String getPhone() {
        return mPhone;
    }
    public String getIcon() {
        return mIcon;
    }
    /**
     * Creates a Location Services Geofence object from a
     * SimpleGeofence.
     *
     * @return A Geofence object
     */
    public Geofence toGeofence(String id) {
        // Build a new Geofence object
        return new Geofence.Builder()
                .setRequestId(id)
                .setTransitionTypes(mTransitionType)
                .setCircularRegion(
                        getLatitude(), getLongitude(), getRadius())
                .setExpirationDuration(mExpirationDuration)
                .build();
    }
}
