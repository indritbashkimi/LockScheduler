package it.ibashkimi.lockscheduler.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.ArrayList;

import it.ibashkimi.lockscheduler.Profiles;
import it.ibashkimi.lockscheduler.api.GeofenceApiHelper;
import it.ibashkimi.lockscheduler.api.GoogleApiHelper;
import it.ibashkimi.lockscheduler.domain.Profile;


public class BootCompleteReceiver extends BroadcastReceiver {

    private static final String TAG = "BootCompleteReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive");
        ArrayList<Profile> profiles = Profiles.restoreProfiles(context);
        if (profiles.size() > 0) {
            Log.d(TAG, "onReceive: initGeofencing");
            for (Profile profile: profiles)
                profile.setActive(false);
            Profiles.saveProfiles(context, profiles);
            GoogleApiHelper googleApiHelper = new GoogleApiHelper(context);
            GeofenceApiHelper geofenceApiHelper = new GeofenceApiHelper(context, googleApiHelper);
            geofenceApiHelper.initGeofences();
        }
    }
}
