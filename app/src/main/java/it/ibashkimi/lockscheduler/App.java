package it.ibashkimi.lockscheduler;

import android.app.Application;

import it.ibashkimi.lockscheduler.model.api.GeofenceApiHelper;
import it.ibashkimi.lockscheduler.model.api.GoogleApiHelper;

public class App extends Application {

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    private static final String TAG = "App";

    private GoogleApiHelper googleApiHelper;
    private GeofenceApiHelper geofenceApiHelper;

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
        googleApiHelper = new GoogleApiHelper(this);
        geofenceApiHelper = new GeofenceApiHelper(this, googleApiHelper);
    }

    public static synchronized App getInstance() {
        return sInstance;
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
