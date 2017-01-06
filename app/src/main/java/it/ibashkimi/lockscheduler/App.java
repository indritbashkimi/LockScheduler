package it.ibashkimi.lockscheduler;

import android.app.Application;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatDelegate;
import android.util.Log;

import java.util.ArrayList;

import it.ibashkimi.lockscheduler.api.GeofenceApiHelper;
import it.ibashkimi.lockscheduler.api.GoogleApiHelper;
import it.ibashkimi.lockscheduler.domain.Profile;


public class App extends Application implements SharedPreferences.OnSharedPreferenceChangeListener {

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    private static final String TAG = "App";

    private GoogleApiHelper googleApiHelper;
    private GeofenceApiHelper geofenceApiHelper;
    private static App mInstance;
    private ArrayList<Profile> mProfiles;

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate: ");
        super.onCreate();
        //Toast.makeText(this, "App onCreate", Toast.LENGTH_LONG).show();
        mInstance = this;
        googleApiHelper = new GoogleApiHelper(this);
        geofenceApiHelper = new GeofenceApiHelper(this, googleApiHelper);
        geofenceApiHelper.initGeofences();
        //getSharedPreferences("prefs", MODE_PRIVATE).registerOnSharedPreferenceChangeListener(this);
    }

    public static synchronized App getInstance() {
        return mInstance;
    }

    public GoogleApiHelper getGoogleApiHelperInstance() {
        return this.googleApiHelper;
    }

    public GeofenceApiHelper getGeofenceApiHelperInstance() {
        return this.geofenceApiHelper;
    }

    public static GoogleApiHelper getGoogleApiHelper() {
        return getInstance().getGoogleApiHelperInstance();
    }

    public static GeofenceApiHelper getGeofenceApiHelper() {
        return getInstance().getGeofenceApiHelperInstance();
    }

    public ArrayList<Profile> getProfiles() {
        if (mProfiles == null) {
            mProfiles = Profiles.restoreProfiles(this);
        }
        return mProfiles;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals("profiles")) {
            mProfiles = Profiles.restoreProfiles(this);
        }
    }
}
