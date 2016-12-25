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

import it.ibashkimi.lockscheduler.Profiles;
import it.ibashkimi.lockscheduler.domain.Profile;
import it.ibashkimi.lockscheduler.services.GeofenceTransitionsIntentService;


public class GeofenceApiHelper {

    private static final String TAG = "GeofenceApiHelper";
    private Context mContext;
    private ArrayList<Profile> mProfiles;
    private PendingIntent mGeofencePendingIntent;
    private GoogleApiHelper mGoogleApiHandler;

    public GeofenceApiHelper(Context context, GoogleApiHelper googleApiHelper) {
        this.mContext = context;
        this.mGoogleApiHandler = googleApiHelper;
    }

    public void initGeofences() {
        Log.d(TAG, "initGeofences: 1");
        mGoogleApiHandler.doJob(new Runnable() {
            @Override
            public void run() {
                initGeofences(mGoogleApiHandler.getGoogleApiClient());
            }
        });
    }

    private void initGeofences(GoogleApiClient googleApiClient) {
        Log.d(TAG, "initGeofences");
        if (getProfiles().size() == 0)
            return;
        if (ActivityCompat.checkSelfPermission(this.mContext, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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

    public void updateGeofence(final String id) {
        removeGeofence(id);
        initGeofences();
    }

    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(mContext, GeofenceTransitionsIntentService.class);
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
        ArrayList<Geofence> geofences = new ArrayList<>();
        for (Profile profile : getProfiles()) {
            if (profile.isEnabled()) {
                geofences.add(new Geofence.Builder()
                        // Set the request ID of the geofence. This is a string to identify this
                        // geofence.
                        .setRequestId(Long.toString(profile.getId()))
                        .setCircularRegion(
                                profile.getPlace().latitude,
                                profile.getPlace().longitude,
                                profile.getRadius()
                        )
                        .setExpirationDuration(Geofence.NEVER_EXPIRE)
                        .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                                Geofence.GEOFENCE_TRANSITION_EXIT)
                        .setLoiteringDelay(60000) // 1 min
                        .build());
            }

        }
        return geofences;
    }

    private ArrayList<Profile> getProfiles() {
        return Profiles.restoreProfiles(mContext);
    }

}
