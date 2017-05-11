package it.ibashkimi.lockscheduler.model;

import com.google.android.gms.maps.model.LatLng;

import java.util.Locale;


public class PlaceCondition extends Condition {

    private static final String TAG = "PlaceCondition";

    private LatLng place;
    private int radius;
    private String address;

    public PlaceCondition(LatLng place, int radius) {
        super(Type.PLACE);
        this.place = place;
        this.radius = radius;
    }

    public LatLng getPlace() {
        return place;
    }

    public void setPlace(LatLng place) {
        this.place = place;
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof PlaceCondition)) {
            return false;
        }
        PlaceCondition placeCondition = (PlaceCondition) obj;
        return placeCondition.getPlace().equals(place) &&
                placeCondition.getRadius() == radius &&
                placeCondition.getType() == getType();
    }

    @Override
    public String toString() {
        return String.format(Locale.ENGLISH, "PlaceCondition{place=[%f, %f], radius=%d}", place.latitude, place.longitude, radius);
    }
}
