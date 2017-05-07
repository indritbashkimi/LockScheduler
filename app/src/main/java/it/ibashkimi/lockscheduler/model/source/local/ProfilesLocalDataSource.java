package it.ibashkimi.lockscheduler.model.source.local;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import it.ibashkimi.lockscheduler.model.Profile;
import it.ibashkimi.lockscheduler.model.source.ProfilesDataSource;

/**
 * Concrete implementation of a data source as a db.
 */
public class ProfilesLocalDataSource implements ProfilesDataSource {

    //private static final String TAG = "ProfilesLocalDataSource";

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
        List<String> ids = restoreProfileIds();
        ArrayList<Profile> profiles;
        if (ids != null) {
            profiles = new ArrayList<>(ids.size());
            for (String profileId : ids) {
                try {
                    Profile profile = Profile.parseJson(sharedPreferences.getString(profileId, null));
                    if (profile == null)
                        throw new RuntimeException("Profile cannot be null. Data may be corrupted.");
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

    @Nullable
    @Override
    public Profile get(@NonNull String profileId) {
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
    public void save(@NonNull Profile profile) {
        List<String> ids = restoreProfileIds();
        if (!ids.contains(profile.getId())) {
            ids.add(profile.getId());
            sharedPreferences.edit()
                    .putString("ids", idsToJsonArray(ids))
                    .putString(profile.getId(), profile.toJson())
                    .apply();
        }
    }

    private List<String> restoreProfileIds() {
        String jsonArrayRep = sharedPreferences.getString("ids", "[]");
        try {
            JSONArray jsonArray = new JSONArray(jsonArrayRep);
            List<String> ids = new ArrayList<>(jsonArray.length());
            for (int i = 0; i < jsonArray.length(); i++) {
                ids.add(jsonArray.get(i).toString());
            }
            return ids;
        } catch (JSONException e) {
            e.printStackTrace();
            return new ArrayList<>(0);
        }
    }

    private String idsToJsonArray(List<String> ids) {
        JSONArray jsonArray = new JSONArray();
        for (String id : ids) {
            jsonArray.put(id);
        }
        return jsonArray.toString();
    }

    @Override
    public void deleteAll() {
        sharedPreferences.edit().clear().apply();
    }

    @Override
    public void delete(@NonNull String profileId) {
        List<String> ids = restoreProfileIds();
        if (ids != null) {
            ids.remove(profileId);
            sharedPreferences.edit()
                    .putString("ids", idsToJsonArray(ids))
                    .remove(profileId)
                    .apply();
        }
    }

    @Override
    public void substitute(@NonNull Profile newProfile, @Nullable Profile oldProfile) {
        save(newProfile);
    }

    @Override
    public void update(@NonNull Profile profile) {
        save(profile);
    }

    @Override
    public void swap(int pos1, int pos2) {
        List<String> ids = restoreProfileIds();
        String id = ids.get(pos1);
        ids.set(pos1, ids.get(pos2));
        ids.set(pos2, id);
        sharedPreferences.edit().putString("ids", idsToJsonArray(ids)).apply();
    }
}
