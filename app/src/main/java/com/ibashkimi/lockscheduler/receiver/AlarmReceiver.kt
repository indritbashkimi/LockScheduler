package com.ibashkimi.lockscheduler.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import android.widget.Toast

import com.ibashkimi.lockscheduler.manager.ProfileManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKE_LOCK_TAG)
        wl.acquire(5000)

        Toast.makeText(context, "AlarmReceiver!", Toast.LENGTH_LONG).show()

        CoroutineScope(Dispatchers.IO).launch {
            intent.getStringExtra("profileId")?.let {
                ProfileManager.timeHandler.onAlarm(it)
            }
            intent.getStringExtra("boot")?.let {
                ProfileManager.init()
            }

            wl.release()
        }
    }

    companion object {
        const val WAKE_LOCK_TAG = "com.ibashkimi.lockscheduler:AlarmReceiver"
    }
}