package com.ibashkimi.lockscheduler.util

import android.annotation.TargetApi
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.ibashkimi.lockscheduler.R

const val PROFILE_ACTIVATED_NOTIFICATION_CHANNEL_ID = "lock_changed"

@TargetApi(26)
fun Context.createNotificationChannels() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
        return
    }
    val notificationChannels = ArrayList<NotificationChannel>()
    val recordingChannel = NotificationChannel(
            PROFILE_ACTIVATED_NOTIFICATION_CHANNEL_ID,
            getString(R.string.notification_channel_profile_activated),
            NotificationManager.IMPORTANCE_DEFAULT
    )
    //recordingChannel.enableLights(true)
    recordingChannel.setShowBadge(true)
    //recordingNotificationChannel.enableVibration(true)
    recordingChannel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
    notificationChannels.add(recordingChannel)

    (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
            .createNotificationChannels(notificationChannels)
}