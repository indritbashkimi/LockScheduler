package it.ibashkimi.lockscheduler.model.source.local;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.util.Log;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
        Set<String> ids = sharedPreferences.getStringSet("ids", null);
        ArrayList<Profile> profiles;
        if (ids != null) {
            profiles = new ArrayList<>(ids.size());
            for (String profileId : ids) {
                try {
                    Profile profile = Profile.parseJson(sharedPreferences.getString(profileId, null));
                    profiles.add(profile);
                } catch (JSONException e) {
                    e.printStackTrace();
                    throw new RuntimeException("Cannot parse profile.");
                }
            }
        } else {
            profiles = new ArrayList<>(0);
        }
        return profiles;
    }

    @Override
    public Profile getProfile(String profileId) {
        Profile profile = null;
        String json = sharedPreferences.getString(profileId, null);
        if (json != null) {
            try {
                profile = Profile.parseJson(json);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return profile;
    }

    @Override
    public void saveProfile(@NonNull Profile profile) {
        Set<String> ids = sharedPreferences.getStringSet("ids", null);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (ids == null)
            ids = new android.support.v4.util.ArraySet<>();
        if (ids.add(profile.getId())) {
            editor.putStringSet("ids", ids);
        }
        editor.putString(profile.getId(), profile.toJson()).apply();
    }

    @Override
    public void deleteAllProfiles() {
        sharedPreferences.edit().clear().apply();
    }

    @Override
    public void deleteProfile(String profileId) {
        Log.d(TAG, "deleteProfile() called with: profileId = [" + profileId + "]");
        Set<String> ids = sharedPreferences.getStringSet("ids", null);
        if (ids != null) {
            ids.remove(profileId);
            sharedPreferences.edit()
                    .putStringSet("ids", ids)
                    .remove(profileId)
                    .apply();
        }
    }

    @Override
    public void updateProfile(Profile profile) {
        saveProfile(profile);
    }

    @Override
    public void swapProfiles(int pos1, int pos2) {
        // TODO: 27/04/17
        /*Set<String> ids = sharedPreferences.getStringSet("ids", null);
        if (ids != null) {
            android.support.v4.util.ArraySet<String> idsSet = new android.support.v4.util.ArraySet();
            idsSet.addAll(ids);
            idsSet.
            Collections.swap(idsSet, pos1, pos2);
        }

        saveProfiles();*/
    }
}
