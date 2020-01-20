package com.ibashkimi.lockscheduler.scheduler

import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.ibashkimi.lockscheduler.api.GeofenceClient
import com.ibashkimi.lockscheduler.data.ProfilesDataSource
import com.ibashkimi.lockscheduler.data.prefs.AppPreferencesHelper
import com.ibashkimi.lockscheduler.model.Profile
import com.ibashkimi.lockscheduler.model.condition.Condition
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.util.*

class PlaceConditionScheduler(
    private val geofenceApiHelper: GeofenceClient,
    repository: ProfilesDataSource,
    val listener: ConditionChangeListener
) : ConditionScheduler(Condition.Type.PLACE, repository) {

    private val TAG = "PlaceCondition"

    private val scope = CoroutineScope(Dispatchers.IO)

    override suspend fun init() {
        geofenceApiHelper.addGeofences(registeredProfiles.toGeofenceList())
    }

    override suspend fun register(profile: Profile): Boolean {
        Log.d(TAG, "register() called with profile=$profile")
        super.register(profile)
        geofenceApiHelper.addGeofences(registeredProfiles.toGeofenceList())
        return true
    }

    override suspend fun unregister(profileId: String) {
        Log.d(TAG, "unregister() called with profile=$profileId")
        super.unregister(profileId)
        geofenceApiHelper.removeGeofence(profileId)
    }

    suspend fun onGeofenceEvent(geofencingEvent: GeofencingEvent) {
        if (geofencingEvent.hasError()) {
            throw RuntimeException("Geofencing error is not handled. This is the error btw: " + geofencingEvent.errorCode)
        }
        val geofenceTransition = geofencingEvent.geofenceTransition
        val geofenceList = geofencingEvent.triggeringGeofences
        for (geofence in geofenceList) {
            val profile = getProfile(geofence.requestId)
            if (profile == null) {
                unregister(geofence.requestId)
            } else {
                val wasActive = profile.isActive()
                val condition = profile.conditions.placeCondition!! // todo
                condition.isTriggered =
                    geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER || geofenceTransition == Geofence.GEOFENCE_TRANSITION_DWELL
                listener.notifyConditionChanged(profile, condition, wasActive)
            }
        }
    }

    private fun List<Profile>.toGeofenceList(): List<Geofence> {
        val delayStr = AppPreferencesHelper.loiteringDelay
        val loiteringDelay = delayStr.toInt()
        Log.d("GeofenceClient", "getGeofenceList: loitering $loiteringDelay")
        Log.d("GeofenceClient", "size: ${this.size}")
        val geofences = ArrayList<Geofence>()
        for ((id, _, conditions) in this) {
            Log.d("GeofenceClient", "profile ${id}, looking for placeCondition")
            conditions.placeCondition?.let { placeCondition ->
                Log.d("GeofenceClient", "adding $placeCondition")
                val builder = Geofence.Builder()
                    .setRequestId(id)
                    .setCircularRegion(
                        placeCondition.latitude,
                        placeCondition.longitude,
                        placeCondition.radius.toFloat()
                    )
                    .setExpirationDuration(Geofence.NEVER_EXPIRE)
                if (loiteringDelay == 0) {
                    builder.setTransitionTypes(
                        Geofence.GEOFENCE_TRANSITION_ENTER or
                                Geofence.GEOFENCE_TRANSITION_EXIT
                    )
                } else {
                    builder.setTransitionTypes(
                        Geofence.GEOFENCE_TRANSITION_DWELL or Geofence.GEOFENCE_TRANSITION_EXIT
                    ).setLoiteringDelay(loiteringDelay)
                }
                geofences.add(builder.build())
            }
        }
        return geofences
    }

}