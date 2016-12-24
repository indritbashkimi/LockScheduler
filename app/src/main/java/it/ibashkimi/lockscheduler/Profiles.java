package it.ibashkimi.lockscheduler;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

import it.ibashkimi.lockscheduler.domain.Profile;


public class Profiles {

    public static ArrayList<Profile> restoreProfiles(Context context) {
        ArrayList<Profile> profiles;
        String jsonArrayRep = context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
                .getString("profiles", "");
        try {
            JSONArray jsonArray = new JSONArray(jsonArrayRep);
            profiles = new ArrayList<>(jsonArray.length());
            for (int i = 0; i < jsonArray.length(); i++) {
                profiles.add(Profile.parseJson(jsonArray.get(i).toString()));
            }
        } catch (JSONException e) {
            e.printStackTrace();
            profiles = new ArrayList<>();
        }
        return profiles;
    }

    public static void saveProfiles(Context context, ArrayList<Profile> profiles) {
        JSONArray jsonArray = new JSONArray();
        for (Profile profile : profiles) {
            jsonArray.put(profile.toJson());
        }
        context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
                .edit()
                .putString("profiles", jsonArray.toString())
                .apply();
    }
}
