package com.ibashkimi.lockscheduler.model.scheduler

import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.ibashkimi.lockscheduler.model.Condition
import com.ibashkimi.lockscheduler.model.PlaceCondition
import com.ibashkimi.lockscheduler.model.Profile
import com.ibashkimi.lockscheduler.model.api.GeofenceApiHelper
import com.ibashkimi.lockscheduler.model.source.ProfilesDataSource

class PlaceConditionScheduler(val geofenceApiHelper: GeofenceApiHelper, repository: ProfilesDataSource, val listener: ConditionChangeListener)
    : ConditionScheduler(Condition.Type.PLACE, repository) {

    private val TAG = "PlaceCondition"

    override fun init() {
        geofenceApiHelper.initGeofences(registeredProfiles)
    }

    override fun register(profile: Profile): Boolean {
        Log.d(TAG, "register() called with profile=$profile")
        super.register(profile)
        geofenceApiHelper.initGeofences(registeredProfiles)
        return false
    }

    override fun unregister(profileId: String) {
        Log.d(TAG, "unregister() called with profile=$profileId")
        super.unregister(profileId)
        geofenceApiHelper.removeGeofence(profileId)
    }

    fun onGeofenceEvent(geofencingEvent: GeofencingEvent) {
        if (geofencingEvent.hasError()) {
            throw RuntimeException("Geofencing error is not handled. This is the error btw: " + geofencingEvent.errorCode)
        }
        val geofenceTransition = geofencingEvent.geofenceTransition
        val geofenceList = geofencingEvent.triggeringGeofences
        for (geofence in geofenceList) {
            val profile = getProfile(geofence.requestId)
            val wasActive = profile.isActive()
            val condition = profile.getCondition(Condition.Type.PLACE) as PlaceCondition
            condition.isTrue = geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER || geofenceTransition == Geofence.GEOFENCE_TRANSITION_DWELL
            listener.notifyConditionChanged(profile, condition, wasActive)
        }
    }

}