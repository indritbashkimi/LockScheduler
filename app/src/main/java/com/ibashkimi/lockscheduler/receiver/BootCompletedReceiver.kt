package com.ibashkimi.lockscheduler.receiver

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.ibashkimi.lockscheduler.data.prefs.AppPreferencesHelper
import com.ibashkimi.lockscheduler.manager.ProfileManager
import com.ibashkimi.lockscheduler.manager.action.LockManager
import com.ibashkimi.lockscheduler.model.action.LockAction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class BootCompletedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (Intent.ACTION_BOOT_COMPLETED == intent.action) {
            when (AppPreferencesHelper.lockAtBoot) {
                LockAction.LockType.SWIPE -> LockManager.resetPassword(context)
                LockAction.LockType.PASSWORD -> LockManager.setPassword(
                    context,
                    AppPreferencesHelper.lockAtBootInput
                )
                LockAction.LockType.PIN -> LockManager.setPin(
                    context,
                    AppPreferencesHelper.lockAtBootInput
                )
                LockAction.LockType.UNCHANGED -> {
                } // do nothing
            }

            val delay = java.lang.Long.parseLong(AppPreferencesHelper.bootDelay)
            if (delay < 0)
                throw IllegalArgumentException("Delay cannot be negative. Delay = $delay.")
            if (delay == 0L) {
                CoroutineScope(Dispatchers.IO).launch { ProfileManager.init() } // todo
            } else {
                LockManager.resetPassword(context)
                val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                val alarmIntent = Intent(context, AlarmReceiver::class.java)
                alarmIntent.putExtra("boot", "boot")
                val pi = PendingIntent.getBroadcast(
                    context,
                    1,
                    alarmIntent,
                    PendingIntent.FLAG_CANCEL_CURRENT
                )
                val now = System.currentTimeMillis()
                val nextAlarm = now + delay
                am.set(AlarmManager.RTC_WAKEUP, nextAlarm, pi)
            }
        }
    }
}
