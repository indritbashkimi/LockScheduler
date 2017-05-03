package it.ibashkimi.lockscheduler.model.scheduler

import android.content.Context
import android.net.wifi.WifiManager
import android.util.Log
import it.ibashkimi.lockscheduler.App
import it.ibashkimi.lockscheduler.model.Condition
import it.ibashkimi.lockscheduler.model.Profile
import it.ibashkimi.lockscheduler.model.WifiCondition
import it.ibashkimi.lockscheduler.model.WifiItem
import it.ibashkimi.lockscheduler.model.source.ProfilesRepository

/**
 * @author Indrit Bashkimi (mailto: indrit.bashkimi@studio.unibo.it)
 */
class WifiConditionHandler(repository: ProfilesRepository, listener: ConditionChangeListener) : ConditionHandler(repository, listener) {

    override val sharedPreferences = App.getInstance().getSharedPreferences("wifi_condition_handler", Context.MODE_PRIVATE)!!

    override fun init() {
        for (profile in getProfiles(getRegisteredProfiles()))
            register(profile)
    }

    override fun register(profile: Profile) {
        val registeredProfileIds = getRegisteredProfiles()
        if (registeredProfileIds.add(profile.id)) {
            setRegisteredProfiles(registeredProfileIds)
            var wifiItem: WifiItem? = null
            val wifiManager = App.getInstance().applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager?
            if (wifiManager != null) {
                val wifiInfo = wifiManager.connectionInfo
                if (wifiInfo != null) {
                    val ssid = wifiInfo.ssid
                    wifiItem = WifiItem(ssid.substring(1, ssid.length - 1))
                }
            }
            val condition = profile.getCondition(Condition.Type.WIFI) as WifiCondition
            val isTrue = condition.isTrue
            condition.isTrue = wifiItem != null && condition.networks.contains(wifiItem)
            if (condition.isTrue != isTrue)
                listener.notifyConditionChanged(profile, condition)
        }
    }

    override fun unregister(profileId: String) {
        removeProfileId(profileId)
    }

    fun onWifiChanged(wifiItem: WifiItem?) {
        Log.d(TAG, "onWifiChanged() called with: wifiItem = [$wifiItem]")
        for (profile in getProfiles(getRegisteredProfiles())) {
            val condition = profile.getCondition(Condition.Type.WIFI) as WifiCondition
            val isTrue = condition.isTrue
            if (wifiItem == null) {
                condition.isTrue = false
            } else {
                condition.isTrue = condition.networks.contains(wifiItem)
            }
            if (condition.isTrue != isTrue)
                listener.notifyConditionChanged(profile, condition)
        }
    }

    companion object {
        val TAG = "WifiConditionHandler"
    }
}