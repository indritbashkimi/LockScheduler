package it.ibashkimi.lockscheduler;

import android.app.Application;
import android.util.Log;

import it.ibashkimi.lockscheduler.api.GeofenceApiHelper;
import it.ibashkimi.lockscheduler.api.GoogleApiHelper;


public class App extends Application {

    private static final String TAG = "App";

    private GoogleApiHelper googleApiHelper;
    private GeofenceApiHelper geofenceApiHelper;
    private static App mInstance;

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate: ");
        super.onCreate();
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

    public static GoogleApiHelper getGoogleApiHelper() {
        return getInstance().getGoogleApiHelperInstance();
    }

    public static GeofenceApiHelper getGeofenceApiHelper() {
        return getInstance().getGeofenceApiHelperInstance();
    }
}
