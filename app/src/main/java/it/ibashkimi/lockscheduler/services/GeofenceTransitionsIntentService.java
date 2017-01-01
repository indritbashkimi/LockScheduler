package it.ibashkimi.lockscheduler.services;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.ArrayList;
import java.util.List;

import it.ibashkimi.lockscheduler.LockManager;
import it.ibashkimi.lockscheduler.MainActivity;
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

    private SharedPreferences mSharedPreferences;

    public GeofenceTransitionsIntentService() {
        super("GeofenceTransitionsIntentService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mSharedPreferences = getSharedPreferences("prefs", Context.MODE_PRIVATE);
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

        boolean showNotification = mSharedPreferences.getBoolean("notifications_show", true);

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
                    if (showNotification)
                        sendNotification(getTransitionName(geofenceTransition).toUpperCase() + ": " + profile.getName(), (int)profile.getId());
                    doJob(profile.getEnterLockMode());
                } else {
                    Log.d(TAG, String.format("onHandleIntent: profile %s already entered. ignoring enter event", profile.getName()));
                }
            } else {
                if (profile.isEntered()) {
                    Log.d(TAG, "onHandleIntent: profile " + profile.getName() + " exit event accepted.");
                    profile.setEntered(false);
                    if (showNotification)
                        sendNotification(getTransitionName(geofenceTransition).toUpperCase() + ": " + profile.getName(), (int)profile.getId());
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
        Log.d(TAG, "doJob() called with: lockMode = [" + LockMode.lockTypeToString(lockMode.getLockType()) + "]");
        LockManager lockManager = new LockManager(this);
        switch (lockMode.getLockType()) {
            case LockMode.LockType.PASSWORD:
                lockManager.setPassword(lockMode.getPassword());
                break;
            case LockMode.LockType.PIN:
                lockManager.setPin(lockMode.getPin());
                break;
            case LockMode.LockType.SEQUENCE:
                break;
            case LockMode.LockType.SWIPE:
                lockManager.resetPassword();
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

    private void sendNotification(String transitionName, int notificationId) {
        boolean vibrate = mSharedPreferences.getBoolean("notifications_vibrate", true);
        String ringtone = mSharedPreferences.getString("notifications_ringtone", "DEFAULT_SOUND");
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_notif)
                        .setContentTitle("Geofence")
                        .setContentText("Transition " + transitionName)
                        .setSound(alarmSound);

        // Gets an instance of the NotificationManager service
        NotificationManager mNotifyMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        // Builds the notification and issues it.
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent intent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        mBuilder.setContentIntent(intent);
        Notification notification = mBuilder.build();
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notification.defaults |= Notification.DEFAULT_LIGHTS;
        if (vibrate)
            notification.defaults |= Notification.DEFAULT_VIBRATE;
        notification.sound = Uri.parse(ringtone);



        mNotifyMgr.notify(notificationId, notification);
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
