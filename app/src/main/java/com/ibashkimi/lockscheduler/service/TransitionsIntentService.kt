package com.ibashkimi.lockscheduler.service

import android.app.IntentService
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.GeofencingEvent
import com.ibashkimi.lockscheduler.R
import com.ibashkimi.lockscheduler.manager.ProfileManager.placeHandler
import com.ibashkimi.lockscheduler.data.prefs.AppPreferencesHelper.showNotifications
import com.ibashkimi.lockscheduler.ui.MainActivity
import com.ibashkimi.lockscheduler.util.PROFILE_ACTIVATED_NOTIFICATION_CHANNEL_ID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * An [IntentService] subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 *
 */
class TransitionsIntentService : IntentService("TransitionsIntentService") {
    override fun onHandleIntent(intent: Intent?) {
        Log.d(TAG, "onHandleIntent() called with: intent = [$intent]")
        val action = intent!!.action
        if (action != null && action == "profile_state_changed") {
            if (showNotifications) {
                val profileId = intent.getStringExtra("profile_id")
                val profileName = intent.getStringExtra("profile_name")
                val isActive = intent.getBooleanExtra("profile_active", false)
                val notificationTitle =
                    getString(if (isActive) R.string.notif_profile_activated else R.string.notif_profile_deactivated)
                sendNotification(notificationTitle, profileName, profileId.toLong().toInt())
            }
        } else {
            CoroutineScope(Dispatchers.IO).launch {
                placeHandler.onGeofenceEvent(GeofencingEvent.fromIntent(intent))
            }
        }
    }

    private fun sendNotification(title: String, content: String, notificationId: Int) {
        val builder =
            NotificationCompat.Builder(this,
                PROFILE_ACTIVATED_NOTIFICATION_CHANNEL_ID
            )
                .setSmallIcon(R.drawable.ic_notif)
                .setContentTitle(title)
                .setContentText(content)
        // Builds the notification and issues it.
        val notificationIntent = Intent(this, MainActivity::class.java)
        notificationIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        val intent = PendingIntent.getActivity(this, 0, notificationIntent, 0)
        builder.setContentIntent(intent)
        val notification = builder.build()
        notification.flags = notification.flags or Notification.FLAG_AUTO_CANCEL
        // Gets an instance of the NotificationManager service
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, notification)
    }

    companion object {
        private const val TAG = "TransitionsIntent"
    }
}