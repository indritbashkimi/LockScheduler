package com.ibashkimi.lockscheduler.service;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.location.GeofencingEvent;
import com.ibashkimi.lockscheduler.R;
import com.ibashkimi.lockscheduler.model.ProfileManager;
import com.ibashkimi.lockscheduler.model.prefs.AppPreferencesHelper;
import com.ibashkimi.lockscheduler.profiles.ProfilesActivity;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 */
public class TransitionsIntentService extends IntentService {
    private static final String TAG = "TransitionsIntent";

    public TransitionsIntentService() {
        super("TransitionsIntentService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "onHandleIntent() called with: intent = [" + intent + "]");
        String action = intent.getAction();
        if (action != null && action.equals("profile_state_changed")) {
            if (AppPreferencesHelper.INSTANCE.getShowNotifications()) {
                String profileId = intent.getStringExtra("profile_id");
                String profileName = intent.getStringExtra("profile_name");
                boolean isActive = intent.getBooleanExtra("profile_active", false);
                String notificationTitle = getString(isActive ? R.string.notif_profile_activated : R.string.notif_profile_deactivated);
                sendNotification(notificationTitle, profileName, (int) Long.parseLong(profileId));
            }
        } else {
            ProfileManager.Companion.getInstance().getPlaceHandler()
                    .onGeofenceEvent(GeofencingEvent.fromIntent(intent));
        }
    }

    private void sendNotification(String title, String content, int notificationId) {
        boolean vibrate = AppPreferencesHelper.INSTANCE.isVibrateActive();
        String ringtone = AppPreferencesHelper.INSTANCE.getNotificationsRingtone();
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
}
