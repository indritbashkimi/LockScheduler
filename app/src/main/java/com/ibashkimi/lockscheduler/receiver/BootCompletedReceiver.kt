package com.ibashkimi.lockscheduler.receiver

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast

import com.ibashkimi.lockscheduler.model.ProfileManager
import com.ibashkimi.lockscheduler.model.action.LockAction
import com.ibashkimi.lockscheduler.model.api.LockManager
import com.ibashkimi.lockscheduler.model.prefs.AppPreferencesHelper


class BootCompletedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "onReceive")
        // TODO: API 24 ACTION_LOCKED_BOOT_COMPLETED https://developer.android.com/reference/android/content/Intent.html#ACTION_BOOT_COMPLETED
        Log.d(TAG, "action = " + intent.action!!)
        if (Intent.ACTION_BOOT_COMPLETED == intent.action) {
            when (AppPreferencesHelper.lockAtBoot) {
                LockAction.LockType.SWIPE -> LockManager.resetPassword(context)
                LockAction.LockType.PASSWORD -> LockManager.setPassword(context, AppPreferencesHelper.lockAtBootInput)
                LockAction.LockType.PIN -> LockManager.setPin(context, AppPreferencesHelper.lockAtBootInput)
                LockAction.LockType.UNCHANGED -> { } // do nothing
            }

            val delay = java.lang.Long.parseLong(AppPreferencesHelper.bootDelay)
            if (delay < 0)
                throw IllegalArgumentException("Delay cannot be negative. Delay = $delay.")
            if (delay == 0L)
                ProfileManager.init()
            else {
                Toast.makeText(context, "Setting alarm. Delay = $delay", Toast.LENGTH_SHORT).show()
                LockManager.resetPassword(context)
                val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                val alarmIntent = Intent(context, AlarmReceiver::class.java)
                alarmIntent.putExtra("boot", "boot")
                val pi = PendingIntent.getBroadcast(context, 1, alarmIntent, PendingIntent.FLAG_CANCEL_CURRENT)
                val now = System.currentTimeMillis()
                val nextAlarm = now + delay
                am.set(AlarmManager.RTC_WAKEUP, nextAlarm, pi)
            }
        } else {
            Log.w(TAG, "Unhandled action: " + intent.action!!)
        }
    }

    companion object {

        private val TAG = "BootCompletedReceiver"
    }
}
