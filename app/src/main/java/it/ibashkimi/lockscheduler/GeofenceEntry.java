package it.ibashkimi.lockscheduler;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by indrit on 23/11/16.
 */

public class GeofenceEntry {
    private String key;
    private LatLng latLng;
    private int radius;

    public GeofenceEntry(String key, LatLng latLng) {
        this.key = key;
        this.latLng = latLng;
    }

    public String getKey() {
        return key;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public int getRadius() {
        return radius;
    }
}
