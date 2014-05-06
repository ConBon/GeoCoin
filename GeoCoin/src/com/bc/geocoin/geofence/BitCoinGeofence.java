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
        private final String mName;
        private final String mAddress;
        private long mExpirationDuration;
        private int mTransitionType;

    /**
     * @param geofenceId The Geofence's request ID
     * @param latitude Latitude of the Geofence's center.
     * @param longitude Longitude of the Geofence's center.
     * @param radius Radius of the geofence circle.
     * @param name of the geofence target.
     * @param address of the geofence target.
     * @param expiration Geofence expiration duration
     * @param transition Type of Geofence transition.
     */
    public BitCoinGeofence(
            String geofenceId,
            double latitude,
            double longitude,
            float radius,
            String name,
            String address,
            long expiration,
            int transition) {
        // Set the instance fields from the constructor
        this.mId = geofenceId;
        this.mLatitude = latitude;
        this.mLongitude = longitude;
        this.mRadius = radius;
        this.mName = name;
        this.mAddress = address;
        this.mExpirationDuration = expiration;
        this.mTransitionType = transition;
    }
    // Instance field getters
    public double getLatitude() {
        return mLatitude;
    }
    public double getLongitude() {
        return mLongitude;
    }
    public float getRadius() {
        return mRadius;
    }
    public String getName() {
        return mName;
    }
    public String getAddress() {
        return mAddress;
    }
    public long getExpirationDuration() {
        return mExpirationDuration;
    }
    public int getTransitionType() {
        return mTransitionType;
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
