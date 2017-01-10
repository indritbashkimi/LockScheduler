package it.ibashkimi.lockscheduler.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;


public class BootCompleteReceiver extends BroadcastReceiver {

    private static final String TAG = "BootCompleteReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive");
        /*ArrayList<Profile> profiles = ProfileApiHelper.restoreProfilesDeprecated(context);
        if (profiles.size() > 0) {
            Log.d(TAG, "onReceive: initGeofencing");
            for (Profile profile: profiles)
                profile.setActive(false);
            ProfileApiHelper.saveProfilesDepre(context, profiles);
            GoogleApiHelper googleApiHelper = new GoogleApiHelper(context);
            GeofenceApiHelper geofenceApiHelper = new GeofenceApiHelper(context, googleApiHelper);
            geofenceApiHelper.initGeofences();
        }*/
    }
}
