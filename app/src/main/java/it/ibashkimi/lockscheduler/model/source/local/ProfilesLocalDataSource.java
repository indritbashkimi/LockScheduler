package it.ibashkimi.lockscheduler.model.source.local;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

import it.ibashkimi.lockscheduler.model.Condition;
import it.ibashkimi.lockscheduler.model.Profile;
import it.ibashkimi.lockscheduler.model.source.ProfilesDataSource;


/**
 * Concrete implementation of a data source as a db.
 */
public class ProfilesLocalDataSource implements ProfilesDataSource {

    private static final String TAG = "ProfilesLocalDataSource";
    private static ProfilesLocalDataSource INSTANCE;

    private SharedPreferences sharedPreferences;
    @Nullable
    private ArrayList<Profile> profiles;

    private ProfilesLocalDataSource(@NonNull Context context) {
        sharedPreferences = context.getSharedPreferences("profiles", Context.MODE_PRIVATE);
    }

    public static ProfilesLocalDataSource getInstance(@NonNull Context context) {
        if (INSTANCE == null) {
            INSTANCE = new ProfilesLocalDataSource(context);
        }
        return INSTANCE;
    }

    /**
     * Note: {@link ProfilesDataSource.LoadProfilesCallback#onDataNotAvailable()} is fired if the database doesn't exist
     * or the table is empty.
     */
    @Override
    public void getProfiles(@NonNull LoadProfilesCallback callback) {
        callback.onProfilesLoaded(getProfiles());
    }

    @Override
    public void getProfile(long profileId, @NonNull GetProfileCallback callback) {
        Profile profile = getProfileWithId(profileId);
        if (profile != null)
            callback.onProfileLoaded(profile);
        else
            callback.onDataNotAvailable();
    }

    @Override
    public void saveProfile(@NonNull Profile profile) {
        getProfiles().add(profile);
        saveProfiles();
    }

    @Override
    public void refreshProfiles() {
        restoreProfiles();
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
    public void swapProfiles(int pos1, int pos2) {
        throw new RuntimeException("Not implemented yet");
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
}
