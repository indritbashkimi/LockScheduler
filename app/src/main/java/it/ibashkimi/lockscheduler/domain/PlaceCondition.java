package it.ibashkimi.lockscheduler.domain;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;


public class PlaceCondition extends Condition {

    private static final String TAG = "PlaceCondition";

    private LatLng place;
    private int radius;

    public PlaceCondition(String name, LatLng place, int radius) {
        super(Type.PLACE, name);
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

    public String toJson() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("type", getType());
            jsonObject.put("name", getName());
            jsonObject.put("true", isTrue());
            jsonObject.put("latitude", place.latitude);
            jsonObject.put("longitude", place.longitude);
            jsonObject.put("radius", radius);
        } catch (JSONException e) {
            Log.d(TAG, "toJson: cannot create json");
            e.printStackTrace();
        }
        return jsonObject.toString();
    }

    public static PlaceCondition parseJson(String json) throws JSONException {
        JSONObject jsonObject = new JSONObject(json);
        int type = jsonObject.getInt("type");
        String name = jsonObject.getString("name");
        double latitude = jsonObject.getDouble("latitude");
        double longitude = jsonObject.getDouble("longitude");
        int radius = jsonObject.getInt("radius");
        boolean isTrue = jsonObject.getBoolean("true");
        PlaceCondition placeCondition = new PlaceCondition(name, new LatLng(latitude, longitude), radius);
        placeCondition.setTrue(isTrue);
        return placeCondition;
    }
}
