package it.ibashkimi.lockscheduler.model.source.local;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import it.ibashkimi.lockscheduler.model.Profile;
import it.ibashkimi.lockscheduler.model.source.ProfilesDataSource;

/**
 * Concrete implementation of a data source as a db.
 */
public class ProfilesLocalDataSource implements ProfilesDataSource {

    private static final String TAG = "ProfilesLocalDataSource";

    private static ProfilesLocalDataSource INSTANCE;

    private SharedPreferences sharedPreferences;

    private ProfilesLocalDataSource(@NonNull Context context) {
        sharedPreferences = context.getSharedPreferences("profiles", Context.MODE_PRIVATE);
    }

    public static ProfilesLocalDataSource getInstance(@NonNull Context context) {
        if (INSTANCE == null) {
            INSTANCE = new ProfilesLocalDataSource(context);
        }
        return INSTANCE;
    }

    @Override
    public List<Profile> getProfiles() {
        return restoreProfiles();
    }

    @Override
    public Profile getProfile(long profileId) {
        for (Profile profile : getProfiles()) {
            if (profile.getId() == profileId)
                return profile;
        }
        return null;
    }

    @Override
    public void saveProfile(@NonNull Profile profile) {
        getProfiles().add(profile);
        saveProfiles();
    }

    @Override
    public void deleteAllProfiles() {
        sharedPreferences.edit().clear().apply();
    }

    @Override
    public void deleteProfile(long profileId) {
        for (Profile p : getProfiles()) {
            if (p.getId() == profileId) {
                getProfiles().remove(p);
                break;
            }
        }
        saveProfiles();
    }

    @Override
    public void updateProfile(Profile profile) {
        List<Profile> profiles = getProfiles();
        Profile targetProfile = null;
        for (Profile p : profiles) {
            if (p.getId() == profile.getId()) {
                targetProfile = p;
                break;
            }
        }
        targetProfile.update(profile);
        saveProfiles();
    }

    @Override
    public void swapProfiles(int pos1, int pos2) {
        Collections.swap(getProfiles(), pos1, pos2);
        saveProfiles();
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

    private synchronized void saveProfiles() {
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
}
