package it.ibashkimi.lockscheduler.services;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import org.json.JSONException;

import java.util.List;

import it.ibashkimi.lockscheduler.LockManager;
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

        SharedPreferences sharedPreferences = getSharedPreferences("prefs", Context.MODE_PRIVATE);

        // Get the transition type.
        int geofenceTransition = geofencingEvent.getGeofenceTransition();
        List<Geofence> geofenceList = geofencingEvent.getTriggeringGeofences();
        for (Geofence geofence : geofenceList) {
            String id = geofence.getRequestId();
            try {
                Profile profile = Profile.fromJsonString(sharedPreferences.getString("profile_" + id, null));
                LockMode lockMode;
                if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_DWELL || geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
                    lockMode = profile.getEnterLockMode();
                } else {
                    lockMode = profile.getExitLockMode();
                }
                doJob(lockMode);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        sendNotification(getTransitionName(geofenceTransition));

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
