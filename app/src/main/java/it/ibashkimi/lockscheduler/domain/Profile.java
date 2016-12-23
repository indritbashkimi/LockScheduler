package it.ibashkimi.lockscheduler.domain;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Locale;

/**
 * @author Indrit Bashkimi (mailto: indrit.bashkimi@studio.unibo.it)
 */

public class Profile implements Parcelable {

    private long id;
    private String name;
    private LatLng place;
    private int radius;
    private boolean enabled = true;
    private LockMode enterLockMode;
    private LockMode exitLockMode;
    private boolean entered;

    public Profile() {
        this(0, "", null, 0);
    }

    public Profile(long id, String name, LatLng place, int radius) {
        this(id, name, place, radius, true, new LockMode(LockMode.LockType.UNCHANGED), new LockMode(LockMode.LockType.UNCHANGED));
    }

    public Profile(long id, String name, LatLng place, int radius, boolean enabled, LockMode enterLockMode, LockMode exitLockMode) {
        this.id = id;
        this.name = name;
        this.place = place;
        this.radius = radius;
        this.enabled = enabled;
        this.enterLockMode = enterLockMode;
        this.exitLockMode = exitLockMode;
    }

    public boolean isEntered() {
        return entered;
    }

    public void setEntered(boolean entered) {
        this.entered = entered;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public LockMode getEnterLockMode() {
        return enterLockMode;
    }

    public void setEnterLockMode(LockMode enterLockMode) {
        this.enterLockMode = enterLockMode;
    }

    public LockMode getExitLockMode() {
        return exitLockMode;
    }

    public void setExitLockMode(LockMode exitLockMode) {
        this.exitLockMode = exitLockMode;
    }

    @Override
    public String toString() {
        return String.format(Locale.ITALIAN, "Profile{id=%d, name=%s, radius=%d}", id, name, radius);
    }


    public JSONObject toJson() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("id", "" + id);
            jsonObject.put("name", name);
            jsonObject.put("enabled", enabled);
            jsonObject.put("latitude", place.latitude);
            jsonObject.put("longitude", place.longitude);
            jsonObject.put("radius", radius);
            jsonObject.put("enterLock", enterLockMode.toJson().toString());
            jsonObject.put("exitLock", exitLockMode.toJson().toString());
            jsonObject.put("entered", entered);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    public static Profile fromJsonString(String json) throws JSONException {
        JSONObject jsonObject = new JSONObject(json);
        Profile profile = new Profile();
        profile.setId(Long.parseLong(jsonObject.getString("id")));
        profile.setName(jsonObject.getString("name"));
        profile.setEnabled(jsonObject.getBoolean("enabled"));
        profile.setPlace(new LatLng(jsonObject.getDouble("latitude"), jsonObject.getDouble("longitude")));
        profile.setRadius(jsonObject.getInt("radius"));
        profile.setEnterLockMode(LockMode.fromJsonString(jsonObject.getString("enterLock")));
        profile.setExitLockMode(LockMode.fromJsonString(jsonObject.getString("exitLock")));
        profile.setEntered(jsonObject.getBoolean("entered"));
        return profile;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(enabled ? 1 : 0);
        dest.writeLong(id);
        dest.writeString(name);
        dest.writeParcelable(place, flags);
        dest.writeInt(radius);
        dest.writeParcelable(enterLockMode, flags);
        dest.writeParcelable(exitLockMode, flags);
        dest.writeInt(entered ? 1 : 0);
    }

    public static final Parcelable.Creator<Profile> CREATOR = new Parcelable.Creator<Profile>() {
        public Profile createFromParcel(Parcel in) {
            return new Profile(in);
        }
        public Profile[] newArray(int size) {
            return new Profile[size];
        }
    };

    private Profile(Parcel in) {
        enabled = in.readInt() == 1;
        id = in.readLong();
        name = in.readString();
        place = in.readParcelable(LatLng.class.getClassLoader());
        radius = in.readInt();
        enterLockMode = in.readParcelable(LockMode.class.getClassLoader());
        exitLockMode = in.readParcelable(LockMode.class.getClassLoader());
        entered = in.readInt() == 1;
    }
}
