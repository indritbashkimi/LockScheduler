package com.ibashkimi.lockscheduler.model.scheduler

import android.content.Context
import android.net.wifi.WifiManager
import android.util.Log
import com.ibashkimi.lockscheduler.App
import com.ibashkimi.lockscheduler.model.Profile
import com.ibashkimi.lockscheduler.model.condition.Condition
import com.ibashkimi.lockscheduler.model.condition.WifiItem
import com.ibashkimi.lockscheduler.model.source.ProfilesDataSource


class WifiConditionScheduler(
    repository: ProfilesDataSource,
    private val listener: ConditionChangeListener
) : ConditionScheduler(Condition.Type.WIFI, repository) {

    override fun init() {
        for (profile in registeredProfiles)
            register(profile)
    }

    override fun register(profile: Profile): Boolean {
        Log.d(TAG, "register() called with profile=$profile")
        super.register(profile)
        var wifiItem: WifiItem? = null
        val wifiManager =
            App.getInstance().applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager?
        if (wifiManager != null) {
            val wifiInfo = wifiManager.connectionInfo
            if (wifiInfo != null) {
                val ssid = wifiInfo.ssid
                wifiItem = WifiItem(ssid.substring(1, ssid.length - 1))
            }
        }
        val condition = profile.conditions.wifiCondition!!
        condition.isTriggered = wifiItem != null && condition.wifiList.contains(wifiItem)
        return condition.isTriggered

    }

    override fun unregister(profileId: String) {
        Log.d(TAG, "register() called with profile=$profileId")
        super.unregister(profileId)
    }

    @Synchronized
    fun onWifiChanged(wifiItem: WifiItem?) {
        Log.d(TAG, "onWifiChanged() called with: wifiItem = [$wifiItem]")
        for (profile in registeredProfiles) {
            Log.d(TAG, "checking profile = $profile")
            val wasActive = profile.isActive()
            val condition = profile.conditions.wifiCondition!!
            val isTrue = condition.isTriggered
            if (wifiItem == null) {
                condition.isTriggered = false
            } else {
                condition.isTriggered = condition.wifiList.contains(wifiItem)
            }
            if (condition.isTriggered != isTrue)
                listener.notifyConditionChanged(profile, condition, wasActive)
        }
    }

    companion object {
        const val TAG = "WifiConditionScheduler"
    }
}