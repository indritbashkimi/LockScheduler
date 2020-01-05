package com.ibashkimi.lockscheduler.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.ibashkimi.lockscheduler.data.ProfileManager


class PowerStateReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (val action: String? = intent.action) {
            Intent.ACTION_POWER_CONNECTED -> {
                ProfileManager.powerHandler.onPowerStateEvent(true)
            }
            Intent.ACTION_POWER_DISCONNECTED -> {
                ProfileManager.powerHandler.onPowerStateEvent(false)
            }
            else -> Log.w("PowerStateReceiver", "Unhandled action: $action.")
        }
    }
}