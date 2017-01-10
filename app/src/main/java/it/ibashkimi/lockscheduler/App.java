package it.ibashkimi.lockscheduler;

import android.app.Application;
import android.support.v7.app.AppCompatDelegate;
import android.util.Log;

import it.ibashkimi.lockscheduler.api.GeofenceApiHelper;
import it.ibashkimi.lockscheduler.api.GoogleApiHelper;
import it.ibashkimi.lockscheduler.api.ProfileApiHelper;


public class App extends Application {

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    private static final String TAG = "App";

    private GoogleApiHelper googleApiHelper;
    private GeofenceApiHelper geofenceApiHelper;
    private ProfileApiHelper profileApiHelper;
    private static App mInstance;
    private LockManager lockManager;

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate: ");
        super.onCreate();
        //Toast.makeText(this, "App onCreate", Toast.LENGTH_LONG).show();
        mInstance = this;
        googleApiHelper = new GoogleApiHelper(this);
        geofenceApiHelper = new GeofenceApiHelper(this, googleApiHelper);
        geofenceApiHelper.initGeofences();
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

    public LockManager getLockManagerInstance() {
        if (lockManager == null) {
            lockManager = new LockManager(this);
        }
        return lockManager;
    }

    public ProfileApiHelper getProfileApiHelperInstance() {
        if (profileApiHelper == null)
            profileApiHelper = new ProfileApiHelper(this);
        return profileApiHelper;
    }

    public static LockManager getLockManager() {
        return getInstance().getLockManagerInstance();
    }

    public static ProfileApiHelper getProfileApiHelper() {
        return getInstance().getProfileApiHelperInstance();
    }

    public static GoogleApiHelper getGoogleApiHelper() {
        return getInstance().getGoogleApiHelperInstance();
    }

    public static GeofenceApiHelper getGeofenceApiHelper() {
        return getInstance().getGeofenceApiHelperInstance();
    }
}
