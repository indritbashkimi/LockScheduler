package com.ibashkimi.lockscheduler.model.scheduler

import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import com.ibashkimi.lockscheduler.App
import com.ibashkimi.lockscheduler.model.Profile
import com.ibashkimi.lockscheduler.model.condition.Condition
import com.ibashkimi.lockscheduler.model.source.ProfilesDataSource


class PowerConditionScheduler(
    repository: ProfilesDataSource,
    private val listener: ConditionChangeListener
) : ConditionScheduler(Condition.Type.POWER, repository) {

    override fun init() {

    }

    override fun register(profile: Profile): Boolean {
        super.register(profile)
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val batteryStatus = App.getInstance().registerReceiver(null, filter)
        val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING
        val condition = profile.conditions.powerCondition!!
        condition.isTriggered = if (condition.powerConnected) isCharging else !isCharging
        return condition.isTriggered
    }

    @Synchronized
    fun onPowerStateEvent(isPowerConnected: Boolean) {
        for (profile in registeredProfiles) {
            val wasActive = profile.isActive()
            val condition = profile.conditions.powerCondition!!
            val wasTrue = condition.isTriggered
            condition.isTriggered =
                if (condition.powerConnected) isPowerConnected else !isPowerConnected
            if (condition.isTriggered != wasTrue)
                listener.notifyConditionChanged(profile, condition, wasActive)
        }
    }
}