package com.ibashkimi.lockscheduler.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.ibashkimi.lockscheduler.model.ProfileManager


class PowerStateReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val action: String? = intent?.action
        if (action != null) {
            val handler = ProfileManager.powerHandler
            when (action) {
                Intent.ACTION_POWER_CONNECTED -> {
                    handler.onPowerStateEvent(true)
                }
                Intent.ACTION_POWER_DISCONNECTED -> {
                    handler.onPowerStateEvent(false)
                }
            }
        }
    }
}