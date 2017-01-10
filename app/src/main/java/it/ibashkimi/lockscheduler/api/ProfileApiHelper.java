package it.ibashkimi.lockscheduler.api;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;

import it.ibashkimi.lockscheduler.App;
import it.ibashkimi.lockscheduler.domain.Condition;
import it.ibashkimi.lockscheduler.domain.Profile;
import it.ibashkimi.lockscheduler.domain.TimeCondition;
import it.ibashkimi.lockscheduler.domain.WifiCondition;
import it.ibashkimi.lockscheduler.domain.WifiItem;


public class ProfileApiHelper {
    private static final String TAG = "ProfileApiHelper";

    private SharedPreferences sharedPreferences;
    @Nullable
    private ArrayList<Profile> profiles;


    public ProfileApiHelper(Context context) {
        this.sharedPreferences = context.getSharedPreferences("prefs", Context.MODE_PRIVATE);
    }

    /**
     * Save a new profile and initialize conditions.
     *
     * @param profile Profile to add
     */
    public void addProfile(@NonNull Profile profile) {
        getProfiles().add(profile);
        for (Condition condition : profile.getConditions()) {
            onRegisterCondition(profile, condition);
        }
        saveProfiles();
    }

    /**
     * Return the profile with the given id. Null if there is any.
     *
     * @param profileId Profile id
     * @return Profile with the given id.
     */
    @Nullable
    public Profile getProfileWithId(long profileId) {
        for (Profile profile : getProfiles()) {
            if (profile.getId() == profileId)
                return profile;
        }
        return null;
    }

    public int getIndexOfProfileId(long profileId) {
        for (int i = 0; i < getProfiles().size(); i++) {
            if (getProfiles().get(i).getId() == profileId)
                return i;
        }
        return -1;
    }

    @NonNull
    public ArrayList<Profile> getProfiles() {
        if (profiles == null) {
            profiles = restoreProfiles();
        }
        return profiles;
    }

    public int indexOf(long profileId) {
        for (int i = 0; i < getProfiles().size(); i++) {
            if (getProfiles().get(i).getId() == profileId)
                return i;
        }
        return -1;
    }

    /**
     * Method called on phone reboot
     */
    public void initProfiles() {

    }

    /**
     * Remove a profile and remove conditions.
     *
     * @param profile Profile to remove.
     */
    public void removeProfile(@NonNull Profile profile) {
        /*for (Profile p : getProfiles()) {
            if (p.getId() == profile.getId()) {
                getProfiles().remove(profile);
                break;
            }
        }*/
        getProfiles().remove(profile);
        saveProfiles();
        for (Condition condition : profile.getConditions()) {
            onUnregisterCondition(profile, condition);
        }
    }

    public void swap(int i, int j) {
        Collections.swap(getProfiles(), i, j);
        saveProfiles();
    }

    public void update(@NonNull Profile newProfile) {
        int index = getIndexOfProfileId(newProfile.getId());
        Profile oldProfile = getProfiles().get(index);
        newProfile.setActive(oldProfile.isActive());
        getProfiles().set(index, newProfile);
        saveProfiles();

        // Unregister removed conditions
        for (Condition condition : oldProfile.getConditions()) {
            if (newProfile.getCondition(condition.getType()) == null) {
                onUnregisterCondition(oldProfile, condition);
            }
        }
        // Register newly added conditions and update existing ones.
        for (Condition condition : newProfile.getConditions()) {
            Condition oldCondition = oldProfile.getCondition(condition.getType());
            if (oldCondition == null) {
                onRegisterCondition(newProfile, condition);
            } else {
                condition.setTrue(oldCondition.isTrue());
                onUpdateCondition(newProfile, oldCondition, condition);
            }
        }
        newProfile.notifyUpdated();
    }

    private synchronized ArrayList<Profile> restoreProfiles() {
        Log.d(TAG, "restoreProfiles: ");
        ArrayList<Profile> profiles;
        String jsonArrayRep = sharedPreferences.getString("profiles", "[]");
        Log.d(TAG, "restoreProfiles: " + jsonArrayRep);
        try {
            JSONArray jsonArray = new JSONArray(jsonArrayRep);
            profiles = new ArrayList<>(jsonArray.length());
            for (int i = 0; i < jsonArray.length(); i++) {
                profiles.add(Profile.parseJson(jsonArray.get(i).toString()));
            }
        } catch (JSONException e) {
            Log.d(TAG, "restoreProfiles: uffa");
            e.printStackTrace();
            profiles = new ArrayList<>();
        }
        return profiles;
    }

    public synchronized void saveProfiles() {
        Log.d(TAG, "saveProfiles: len = " + getProfiles().size());
        for (Profile profile : getProfiles()) {
            Log.d(TAG, "saveProfiles: profile: " + profile.toString());
        }
        JSONArray jsonArray = new JSONArray();
        for (Profile profile : getProfiles()) {
            jsonArray.put(profile.toJson());
        }

        sharedPreferences.edit().putString("profiles", jsonArray.toString()).apply();
        Log.d(TAG, "saveProfiles: " + jsonArray.toString());
    }

    private void onRegisterCondition(Profile profile, Condition condition) {
        switch (condition.getType()) {
            case Condition.Type.PLACE:
                App.getGeofenceApiHelper().initGeofences();
                break;
            case Condition.Type.TIME:
                TimeCondition timeCondition = (TimeCondition) condition;
                Calendar c = Calendar.getInstance();
                int dayOfWeek = c.get(Calendar.DAY_OF_WEEK); // dayOfWeek = (dayOfWeek + 5) % 6;
                switch (dayOfWeek) {
                    case Calendar.MONDAY:
                        dayOfWeek = 0;
                        break;
                    case Calendar.TUESDAY:
                        dayOfWeek = 1;
                        break;
                    case Calendar.WEDNESDAY:
                        dayOfWeek = 2;
                        break;
                    case Calendar.THURSDAY:
                        dayOfWeek = 3;
                        break;
                    case Calendar.FRIDAY:
                        dayOfWeek = 4;
                        break;
                    case Calendar.SATURDAY:
                        dayOfWeek = 5;
                        break;
                    case Calendar.SUNDAY:
                        dayOfWeek = 6;
                        break;
                }
                if (timeCondition.getDaysActive()[dayOfWeek]) {
                    timeCondition.setTrue(true);
                    profile.notifyConditionChanged(timeCondition);
                }
                break;
            case Condition.Type.WIFI:
                WifiItem wifiItem = null;
                WifiManager wifiManager = (WifiManager) App.getInstance().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                if (wifiManager != null) {
                    WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                    if (wifiInfo != null) {
                        String ssid = wifiInfo.getSSID();
                        wifiItem = new WifiItem(ssid.substring(1, ssid.length() - 1));
                    }
                }
                ((WifiCondition) condition).onWifiStateChanged(wifiItem);
                profile.notifyConditionChanged(condition);
                break;
        }
    }

    private void onUnregisterCondition(Profile profile, Condition condition) {
        switch (condition.getType()) {
            case Condition.Type.PLACE:
                App.getGeofenceApiHelper().removeGeofence(Long.toString(profile.getId()));
                break;
            case Condition.Type.TIME:
                break;
            case Condition.Type.WIFI:
                break;
        }
    }

    private void onUpdateCondition(Profile profile, Condition oldCondition, Condition newCondition) {
        if (oldCondition.equals(newCondition)) {
            newCondition.setTrue(oldCondition.isTrue());
            return;
        }
        switch (oldCondition.getType()) {
            case Condition.Type.PLACE:
                onUnregisterCondition(profile, oldCondition);
                onRegisterCondition(profile, newCondition);
                break;
            case Condition.Type.TIME:
                break;
            case Condition.Type.WIFI:
                break;
        }
    }
}
