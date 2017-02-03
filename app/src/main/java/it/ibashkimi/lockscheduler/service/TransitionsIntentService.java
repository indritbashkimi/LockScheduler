package it.ibashkimi.lockscheduler.service;

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

import it.ibashkimi.lockscheduler.App;
import it.ibashkimi.lockscheduler.R;
import it.ibashkimi.lockscheduler.model.Condition;
import it.ibashkimi.lockscheduler.model.Profile;
import it.ibashkimi.lockscheduler.profiles.ProfilesActivity;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 */
public class TransitionsIntentService extends IntentService {
    private static final String TAG = "GeofenceTransitionsInte";

    private SharedPreferences mSharedPreferences;

    public TransitionsIntentService() {
        super("TransitionsIntentService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mSharedPreferences = getSharedPreferences("prefs", Context.MODE_PRIVATE);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "onHandleIntent() called with: intent = [" + intent + "]");
        String action = intent.getAction();
        if (action != null && action.equals("condition_state_changed")) {
            boolean showNotification = mSharedPreferences.getBoolean("notifications_show", true);
            if (showNotification) {
                long profileId = intent.getLongExtra("profile_id", -1);
                Profile profile = App.getProfileApiHelper().getProfileWithId(profileId);
                assert profile != null;
                String title;
                String content;
                if (profile.isActive()) {
                    title = "Activated";
                    content = profile.getName();
                } else {
                    title = "Deactivated";
                    content = profile.getName();
                }
                sendNotification(title, content, (int) profile.getId());
            }

        } else {
            GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
            if (geofencingEvent.hasError()) {
            /*String errorMessage = GeofenceErrorMessages.getErrorString(this,
                    geofencingEvent.getErrorCode());*/
                Log.e(TAG, "geofencing has error");
                return;
            }

            int geofenceTransition = geofencingEvent.getGeofenceTransition();
            List<Geofence> geofenceList = geofencingEvent.getTriggeringGeofences();
            for (Geofence geofence : geofenceList) {
                Profile profile = App.getProfileApiHelper().getProfileWithId(Long.parseLong(geofence.getRequestId()));
                if (profile == null) {
                    Log.wtf(TAG, "onHandleIntent: no profile found with id " + geofence.getRequestId());
                    return;
                }
                if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER
                        || geofenceTransition == Geofence.GEOFENCE_TRANSITION_DWELL) {
                    profile.setConditionState(Condition.Type.PLACE, true);
                } else {
                    profile.setConditionState(Condition.Type.PLACE, false);
                }
                App.getProfileApiHelper().saveProfiles();
            }
        }

    }

    private Profile findProfile(ArrayList<Profile> profiles, long id) {
        for (Profile profile : profiles)
            if (profile.getId() == id)
                return profile;
        return null;
    }

    private void sendNotification(String title, String content, int notificationId) {
        boolean vibrate = mSharedPreferences.getBoolean("notifications_vibrate", true);
        String ringtone = mSharedPreferences.getString("notifications_ringtone", "DEFAULT_SOUND");
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_notif)
                        .setContentTitle(title)
                        .setContentText(content)
                        .setSound(alarmSound);

        // Gets an instance of the NotificationManager service
        NotificationManager mNotifyMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        // Builds the notification and issues it.
        Intent notificationIntent = new Intent(this, ProfilesActivity.class);
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
