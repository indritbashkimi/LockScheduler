package it.ibashkimi.lockscheduler.api;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;

import it.ibashkimi.lockscheduler.App;
import it.ibashkimi.lockscheduler.Constants;
import it.ibashkimi.lockscheduler.domain.PlaceCondition;
import it.ibashkimi.lockscheduler.domain.Profile;
import it.ibashkimi.lockscheduler.services.TransitionsIntentService;


public class GeofenceApiHelper {

    private static final String TAG = "GeofenceApiHelper";
    private Context mContext;
    private PendingIntent mGeofencePendingIntent;
    private GoogleApiHelper mGoogleApiHandler;

    public GeofenceApiHelper(Context context, GoogleApiHelper googleApiHelper) {
        this.mContext = context;
        this.mGoogleApiHandler = googleApiHelper;
    }

    public void initGeofences() {
        Log.d(TAG, "initGeofences: adding job");
        mGoogleApiHandler.doJob(new Runnable() {
            @Override
            public void run() {
                initGeofences(mGoogleApiHandler.getGoogleApiClient());
            }
        });
    }

    private void initGeofences(GoogleApiClient googleApiClient) {
        Log.d(TAG, "initGeofences");
        List<Profile> profiles = getProfiles();
        if (profiles.size() == 0) {
            Log.e(TAG, "initGeofences: no profiles found.");
            return;
        }
        boolean hasPlaceConditions = false;
        for (Profile profile : profiles) {
            if (profile.getPlaceCondition() != null) {
                hasPlaceConditions = true;
                break;
            }
        }
        if (!hasPlaceConditions)
            return;

        if (ActivityCompat.checkSelfPermission(this.mContext, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "initGeofences: location permission is needed");
            return;
        }
        LocationServices.GeofencingApi.addGeofences(
                googleApiClient,
                getGeofencingRequest(),
                getGeofencePendingIntent()
        ).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {
                Log.d(TAG, "Geofence: " + (status.isSuccess() ? "successful." : "failed."));
            }
        });
    }

    public void removeGeofence(final String id) {
        mGoogleApiHandler.doJob(new Runnable() {
            @Override
            public void run() {
                ArrayList<String> removeList = new ArrayList<>(1);
                removeList.add(id);
                LocationServices.GeofencingApi.removeGeofences(mGoogleApiHandler.getGoogleApiClient(), removeList);
            }
        });
    }

    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(mContext, TransitionsIntentService.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
        // calling addGeofences() and removeGeofences().
        mGeofencePendingIntent = PendingIntent.getService(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return mGeofencePendingIntent;
    }

    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER | GeofencingRequest.INITIAL_TRIGGER_EXIT);
        builder.addGeofences(getGeofenceList());
        return builder.build();
    }

    private List<Geofence> getGeofenceList() {
        String delayStr = mContext.getSharedPreferences(Constants.MAIN_PREFS, Context.MODE_PRIVATE)
                .getString("loitering_delay", "0");
        int loiteringDelay = Integer.parseInt(delayStr);
        Log.d(TAG, "getGeofenceList: loitering " + loiteringDelay);
        ArrayList<Geofence> geofences = new ArrayList<>();
        PlaceCondition placeCondition;
        for (Profile profile : getProfiles()) {
            placeCondition = profile.getPlaceCondition();
            if (placeCondition != null) {
                Geofence.Builder builder = new Geofence.Builder()
                        .setRequestId(Long.toString(profile.getId()))
                        .setCircularRegion(
                                placeCondition.getPlace().latitude,
                                placeCondition.getPlace().longitude,
                                placeCondition.getRadius())
                        .setExpirationDuration(Geofence.NEVER_EXPIRE);
                if (loiteringDelay == 0) {
                    builder.setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                            Geofence.GEOFENCE_TRANSITION_EXIT);
                } else {
                    builder.setTransitionTypes(Geofence.GEOFENCE_TRANSITION_DWELL |
                            Geofence.GEOFENCE_TRANSITION_EXIT)
                            .setLoiteringDelay(loiteringDelay);
                }
                geofences.add(builder.build());
            }
        }
        return geofences;
    }

    private ArrayList<Profile> getProfiles() {
        return App.getProfileApiHelper().getProfiles();
    }

}
