package com.ibashkimi.lockscheduler.manager.scheduler

import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import com.ibashkimi.lockscheduler.App
import com.ibashkimi.lockscheduler.data.ProfilesDataSource
import com.ibashkimi.lockscheduler.model.Profile
import com.ibashkimi.lockscheduler.model.condition.Condition
import com.ibashkimi.lockscheduler.model.condition.PowerCondition
import com.ibashkimi.lockscheduler.model.findCondition


class PowerConditionScheduler(
    repository: ProfilesDataSource,
    private val listener: ConditionChangeListener
) : ConditionScheduler(Condition.Type.POWER, repository) {

    override suspend fun init() {

    }

    override suspend fun register(profile: Profile): Boolean {
        android.util.Log.d("PowerConditionScheduler", "register called with profile ${profile.id}")
        super.register(profile)
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val batteryStatus = App.getInstance().registerReceiver(null, filter)
        val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING
        val condition = profile.findCondition<PowerCondition>()!!
        condition.isTriggered = if (condition.powerConnected) isCharging else !isCharging
        return condition.isTriggered
    }

    @Synchronized
    fun onPowerStateEvent(isPowerConnected: Boolean) {
        for (profile in registeredProfiles) {
            val wasActive = profile.isActive()
            profile.findCondition<PowerCondition>()?.let { condition ->
                val wasTrue = condition.isTriggered
                condition.isTriggered =
                    if (condition.powerConnected) isPowerConnected else !isPowerConnected
                if (condition.isTriggered != wasTrue)
                    listener.notifyConditionChanged(profile, condition, wasActive)
            }
        }
    }
}