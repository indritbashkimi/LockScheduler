package it.ibashkimi.lockscheduler.api;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import it.ibashkimi.lockscheduler.domain.Profile;


public class ProfileApiHelper {

    private static WeakReference<ProfileApiHelper> instance;

    public static ProfileApiHelper getInstance(Context context) {
        if (instance.get() == null) {
            instance = new WeakReference<>(new ProfileApiHelper(context));
        }
        return instance.get();
    }

    private Context mContext;
    private ArrayList<Profile> mProfiles;
    private SharedPreferences mSharedPrefs;

    private ProfileApiHelper(Context context) {
        mContext = context;
        mSharedPrefs = mContext.getSharedPreferences("prefs", Context.MODE_PRIVATE);
        restore();
    }

    public void add(Profile profile) {
        mProfiles.add(profile);
    }

    public void remove(Profile profile) {
        mProfiles.remove(profile);
    }

    public void update(Profile profile) {

    }

    public List<Profile> get() {
        return mProfiles;
    }

    public Profile get(int position) {
        return mProfiles.get(position);
    }

    public void save() {
        JSONArray jsonArray = new JSONArray();
        for (Profile profile : mProfiles) {
            jsonArray.put(profile.toJson());
        }
        mSharedPrefs
                .edit()
                .putString("profiles", jsonArray.toString())
                .apply();
    }

    public List<Profile> restore() {
        String jsonArrayRep = mSharedPrefs.getString("profiles", "");
        try {
            JSONArray jsonArray = new JSONArray(jsonArrayRep);
            mProfiles = new ArrayList<>(jsonArray.length());
            for (int i = 0; i < jsonArray.length(); i++) {
                mProfiles.add(Profile.parseJson(jsonArray.get(i).toString()));
            }
        } catch (JSONException e) {
            e.printStackTrace();
            mProfiles = new ArrayList<>();
        }
        return mProfiles;
    }
}
