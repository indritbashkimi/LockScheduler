package com.ibashkimi.lockscheduler.manager.scheduler

import android.content.Context
import android.net.wifi.WifiManager
import android.util.Log
import com.ibashkimi.lockscheduler.App
import com.ibashkimi.lockscheduler.data.ProfilesDataSource
import com.ibashkimi.lockscheduler.model.Profile
import com.ibashkimi.lockscheduler.model.condition.Condition
import com.ibashkimi.lockscheduler.model.condition.WifiCondition
import com.ibashkimi.lockscheduler.model.condition.WifiItem
import com.ibashkimi.lockscheduler.model.findCondition


class WifiConditionScheduler(
    repository: ProfilesDataSource,
    private val listener: ConditionChangeListener
) : ConditionScheduler(Condition.Type.WIFI, repository) {

    override suspend fun init() {
        for (profile in registeredProfiles)
            register(profile)
    }

    override suspend fun register(profile: Profile): Boolean {
        Log.d(TAG, "register() called with profile=$profile")
        super.register(profile)
        var wifiItem: WifiItem? = null
        val wifiManager =
            App.getInstance().applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager?
        if (wifiManager != null) {
            val wifiInfo = wifiManager.connectionInfo
            if (wifiInfo != null) {
                val ssid = wifiInfo.ssid
                val bssid = wifiInfo.bssid
                wifiItem = WifiItem(ssid.substring(1, ssid.length - 1), bssid)
            }
        }
        val condition = profile.findCondition<WifiCondition>()!!
        condition.isTriggered = wifiItem != null && condition.wifiList.contains(wifiItem)
        return condition.isTriggered
    }

    override suspend fun unregister(profileId: String) {
        Log.d(TAG, "register() called with profile=$profileId")
        super.unregister(profileId)
    }

    @Synchronized
    fun onWifiChanged(wifiItem: WifiItem?) {
        Log.d(TAG, "onWifiChanged() called with: wifiItem = [$wifiItem]")
        for (profile in registeredProfiles) {
            Log.d(TAG, "checking profile = $profile")
            val wasActive = profile.isActive()
            val condition = profile.findCondition<WifiCondition>()!!
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