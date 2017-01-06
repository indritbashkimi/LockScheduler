package it.ibashkimi.lockscheduler;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

import it.ibashkimi.lockscheduler.domain.Profile;


public class Profiles {
    private static final String TAG = "Profiles";

    public static ArrayList<Profile> restoreProfiles(Context context) {
        Log.d(TAG, "restoreProfiles: ");
        ArrayList<Profile> profiles;
        String jsonArrayRep = context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
                .getString("profiles", "[]");
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

    public static void saveProfiles(Context context, ArrayList<Profile> profiles) {
        Log.d(TAG, "saveProfiles: len = " + profiles.size());
        for (Profile profile : profiles) {
            Log.d(TAG, "saveProfiles: profile: " + profile.toString());
        }
        JSONArray jsonArray = new JSONArray();
        for (Profile profile : profiles) {
            jsonArray.put(profile.toJson());
        }

        context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
                .edit()
                .putString("profiles", jsonArray.toString())
                .apply();
        Log.d(TAG, "saveProfiles: " + jsonArray.toString());
    }
}
