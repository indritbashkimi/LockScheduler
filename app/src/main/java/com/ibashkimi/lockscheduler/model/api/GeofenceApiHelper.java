package com.ibashkimi.lockscheduler.model.api;

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

import com.ibashkimi.lockscheduler.App;
import com.ibashkimi.lockscheduler.model.PlaceCondition;
import com.ibashkimi.lockscheduler.model.Profile;
import com.ibashkimi.lockscheduler.model.ProfileUtils;
import com.ibashkimi.lockscheduler.model.prefs.AppPreferencesHelper;
import com.ibashkimi.lockscheduler.service.TransitionsIntentService;


public class GeofenceApiHelper {
    private static GeofenceApiHelper sInstance;
    private static final String TAG = "GeofenceApiHelper";

    private PendingIntent mGeofencePendingIntent;
    private GoogleApiHelper mGoogleApiHandler;

    private GeofenceApiHelper(GoogleApiHelper googleApiHelper) {
        this.mGoogleApiHandler = googleApiHelper;
    }

    public static synchronized GeofenceApiHelper getInstance() {
        if (sInstance == null) {
            GoogleApiHelper googleApiHelper = new GoogleApiHelper(App.getInstance());
            sInstance = new GeofenceApiHelper(googleApiHelper);
        }
        return sInstance;
    }

    @SuppressWarnings("unused")
    public static synchronized void destroyInstance() {
        sInstance = null;
    }
    public void initGeofences(final List<Profile> profiles) {
        Log.d(TAG, "initGeofences: adding job");
        mGoogleApiHandler.doJob(new Runnable() {
            @Override
            public void run() {
                initGeofences(mGoogleApiHandler.getGoogleApiClient(), profiles);
            }
        });
    }

    private void initGeofences(GoogleApiClient googleApiClient, List<Profile> profiles) {
        Log.d(TAG, "initGeofences");
        if (profiles.size() == 0) {
            Log.e(TAG, "initGeofences: no profiles found.");
            return;
        }
        boolean hasPlaceConditions = false;
        for (Profile profile : profiles) {
            if (ProfileUtils.getPlaceCondition(profile) != null) {
                hasPlaceConditions = true;
                break;
            }
        }
        if (!hasPlaceConditions)
            return;

        if (ActivityCompat.checkSelfPermission(App.getInstance(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "initGeofences: location permission is needed");
            return;
        }
        LocationServices.GeofencingApi.addGeofences(
                googleApiClient,
                getGeofencingRequest(profiles),
                getGeofencePendingIntent()
        ).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {
                Log.d(TAG, "Geofence: " + (status.isSuccess() ? "successful." : "failed."));
            }
        });
    }

    public void removeGeofence(final String id) {
        Log.d(TAG, "removeGeofence() called with: id = [" + id + "]");
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
        Intent intent = new Intent(getContext(), TransitionsIntentService.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
        // calling addGeofences() and removeGeofences().
        mGeofencePendingIntent = PendingIntent.getService(getContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return mGeofencePendingIntent;
    }

    private GeofencingRequest getGeofencingRequest(List<Profile> profiles) {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER | GeofencingRequest.INITIAL_TRIGGER_EXIT);
        builder.addGeofences(getGeofenceList(profiles));
        return builder.build();
    }

    private List<Geofence> getGeofenceList(List<Profile> profiles) {
        String delayStr = AppPreferencesHelper.INSTANCE.getLoiteringDelay();
        int loiteringDelay = Integer.parseInt(delayStr);
        Log.d(TAG, "getGeofenceList: loitering " + loiteringDelay);
        ArrayList<Geofence> geofences = new ArrayList<>();
        PlaceCondition placeCondition;
        for (Profile profile : profiles) {
            placeCondition = ProfileUtils.getPlaceCondition(profile);
            if (placeCondition != null) {
                Geofence.Builder builder = new Geofence.Builder()
                        .setRequestId(profile.getId())
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

    private Context getContext() {
        return App.getInstance();
    }
}
