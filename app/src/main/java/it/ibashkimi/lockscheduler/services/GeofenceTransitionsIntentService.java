package it.ibashkimi.lockscheduler.services;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.Context;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import it.ibashkimi.lockscheduler.LockManager;
import it.ibashkimi.lockscheduler.Profiles;
import it.ibashkimi.lockscheduler.R;
import it.ibashkimi.lockscheduler.domain.LockMode;
import it.ibashkimi.lockscheduler.domain.Profile;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 */
public class GeofenceTransitionsIntentService extends IntentService {
    private static final String TAG = "GeofenceTransitionsInte";

    public GeofenceTransitionsIntentService() {
        super("GeofenceTransitionsIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "onHandleIntent() called with: intent = [" + intent + "]");
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            /*String errorMessage = GeofenceErrorMessages.getErrorString(this,
                    geofencingEvent.getErrorCode());*/
            Log.e(TAG, "geofencing has error");
            return;
        }

        int geofenceTransition = geofencingEvent.getGeofenceTransition();
        List<Geofence> geofenceList = geofencingEvent.getTriggeringGeofences();
        ArrayList<Profile> profiles = Profiles.restoreProfiles(this);
        for (Geofence geofence : geofenceList) {
            Profile profile = findProfile(profiles, Long.parseLong(geofence.getRequestId()));
            if (profile == null) {
                Log.wtf(TAG, "onHandleIntent: no profile found with id " + geofence.getRequestId());
                return;
            }
            if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_DWELL || geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
                if (!profile.isEntered()) {
                    Log.d(TAG, "onHandleIntent: profile " + profile.getName() + " enter event accepted.");
                    profile.setEntered(true);
                    sendNotification(getTransitionName(geofenceTransition).toUpperCase() + ": " + profile.getName());
                    doJob(profile.getEnterLockMode());
                } else {
                    Log.d(TAG, String.format("onHandleIntent: profile %s already entered. ignoring enter event", profile.getName()));
                }
            } else {
                if (profile.isEntered()) {
                    Log.d(TAG, "onHandleIntent: profile " + profile.getName() + " exit event accepted.");
                    profile.setEntered(false);
                    sendNotification(getTransitionName(geofenceTransition).toUpperCase() + ": " + profile.getName());
                    doJob(profile.getExitLockMode());
                } else {
                    Log.d(TAG, String.format("onHandleIntent: profile %s not entered yet. ignoring exit event.", profile.getName()));
                }
            }
            Profiles.saveProfiles(this, profiles);
        }
        Log.d(TAG, "onHandleIntent() returned: notification created");
    }

    private void doJob(LockMode lockMode) {
        LockManager lockManager = new LockManager(this);
        switch (lockMode.getLockType()) {
            case LockMode.LockType.PASSWORD:
                lockManager.setLockPin(lockMode.getPassword());
                break;
            case LockMode.LockType.PIN:
                lockManager.setLockPin(lockMode.getPin());
                break;
            case LockMode.LockType.SEQUENCE:
                break;
            case LockMode.LockType.SWIPE:
                lockManager.removeLockPin();
                break;
            case LockMode.LockType.UNCHANGED:
                break;
        }
    }

    private Profile findProfile(ArrayList<Profile> profiles, long id) {
        for (Profile profile : profiles)
            if (profile.getId() == id)
                return profile;
        return null;
    }

    private void sendNotification(String transitionName) {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_notif)
                        .setContentTitle("Geofence")
                        .setContentText("Transition " + transitionName);

        // Sets an ID for the notification
        int mNotificationId = 1;
        // Gets an instance of the NotificationManager service
        NotificationManager mNotifyMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        // Builds the notification and issues it.
        mNotifyMgr.notify(mNotificationId, mBuilder.build());
    }

    private String getTransitionName(int transitionType) {
        switch (transitionType) {
            case Geofence.GEOFENCE_TRANSITION_ENTER:
                return "enter";
            case Geofence.GEOFENCE_TRANSITION_DWELL:
                return "dwell";
            case Geofence.GEOFENCE_TRANSITION_EXIT:
                return "exit";
            default:
                return "unknown";
        }
    }
}
