package it.ibashkimi.lockscheduler.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import it.ibashkimi.lockscheduler.App;
import it.ibashkimi.lockscheduler.model.api.GeofenceApiHelper;
import it.ibashkimi.lockscheduler.model.api.ProfileApiHelper;
import it.ibashkimi.lockscheduler.model.source.ProfilesRepository;


public class BootCompletedReceiver extends BroadcastReceiver {

    private static final String TAG = "BootCompletedReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive");
        ProfileApiHelper.getInstance(App.getGeofenceApiHelper())
                .initProfiles(ProfilesRepository.getInstance().getProfiles());
    }
}
