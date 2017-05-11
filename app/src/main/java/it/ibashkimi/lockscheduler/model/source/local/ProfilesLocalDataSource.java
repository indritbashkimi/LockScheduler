package it.ibashkimi.lockscheduler.model.source.local;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import it.ibashkimi.lockscheduler.model.Profile;
import it.ibashkimi.lockscheduler.model.source.ProfilesDataSource;
import it.ibashkimi.lockscheduler.model.source.serializer.ProfileSerializer;

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
        List<String> ids = restoreProfileIds();
        ArrayList<Profile> profiles;
        if (ids != null) {
            profiles = new ArrayList<>(ids.size());
            for (String profileId : ids) {
                try {
                    Profile profile = ProfileSerializer.parseJson(sharedPreferences.getString(profileId, null));
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
                profile = ProfileSerializer.parseJson(json);
                Log.d(TAG, "Profile parsed correctly.");
            } catch (JSONException e) {
                Log.e(TAG, "cannot parse json.");
                e.printStackTrace();
            }
        }
        Log.d(TAG, "get() returned: " + profile);
        return profile;
    }

    @Override
    public void save(@NonNull Profile profile) {
        Log.d(TAG, "save() called with: profile = [" + profile + "]");
        List<String> ids = restoreProfileIds();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (!ids.contains(profile.getId())) {
            Log.d(TAG, "ids don't contain profileId. OK");
            ids.add(profile.getId());
            editor.putString("ids", idsToJsonArray(ids));
            Log.d(TAG, "Profile saved.");
        } else {
            Log.e(TAG, "Profile not saved.");
        }
        editor.putString(profile.getId(), ProfileSerializer.toJson(profile)).commit();
    }

    @NonNull
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
        sharedPreferences.edit().clear().commit();
    }

    @Override
    public void delete(@NonNull String profileId) {
        Log.d(TAG, "delete() called with: profileId = [" + profileId + "]");
        List<String> ids = restoreProfileIds();

        Log.d(TAG, "ids.size = " + ids.size());
        for (String id : ids) {
            Log.d(TAG, "ids id = " + id);
        }
        ids.remove(profileId);
        Log.d(TAG, "After remove");
        Log.d(TAG, "ids.size = " + ids.size());
        for (String id : ids) {
            Log.d(TAG, "ids id = " + id);
        }
        sharedPreferences.edit()
                .putString("ids", idsToJsonArray(ids))
                .remove(profileId)
                .commit();
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
        sharedPreferences.edit().putString("ids", idsToJsonArray(ids)).commit();
    }
}
