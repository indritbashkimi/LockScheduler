package it.ibashkimi.lockscheduler.model.scheduler

import android.content.Context
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import it.ibashkimi.lockscheduler.App
import it.ibashkimi.lockscheduler.model.Condition
import it.ibashkimi.lockscheduler.model.PlaceCondition
import it.ibashkimi.lockscheduler.model.Profile
import it.ibashkimi.lockscheduler.model.api.GeofenceApiHelper
import it.ibashkimi.lockscheduler.model.source.ProfilesRepository

/**
 * @author Indrit Bashkimi (mailto: indrit.bashkimi@studio.unibo.it)
 */
class PlaceConditionHandler(val geofenceApiHelper: GeofenceApiHelper, repository: ProfilesRepository, listener: ConditionChangeListener) : ConditionHandler(repository, listener) {
    private val TAG = "PlaceConditionHandler"

    override val sharedPreferences = App.getInstance().getSharedPreferences("place_condition_handler", Context.MODE_PRIVATE)!!

    override fun init() {
        geofenceApiHelper.initGeofences(getProfiles(getRegisteredProfiles()))
    }

    override fun register(profile: Profile) {
        Log.d(TAG, "register() called with profile $profile")
        val registeredProfileIds = getRegisteredProfiles()
        printIds(registeredProfileIds)
        if (registeredProfileIds.add(profile.id)) {
            Log.d(TAG, "adding new profile with id = ${profile.id}")
            setRegisteredProfiles(registeredProfileIds)
            Log.d(TAG, "now registered profiles are:")
            printIds(registeredProfileIds)
            Log.d(TAG, "lets try reading again:")
            printIds(getRegisteredProfiles())
            geofenceApiHelper.initGeofences(getProfiles(getRegisteredProfiles()))
        }
    }

    private fun printIds(ids: Set<String>) {
        Log.d(TAG, "ids.size = ${ids.size}")
        for (id in ids)
            Log.d(TAG, "id: $id")
    }
    override fun unregister(profileId: String) {
        Log.d(TAG, "unregister() called with profile $profileId")
        removeProfileId(profileId)
        geofenceApiHelper.removeGeofence(profileId)
    }

    fun onGeofenceEvent(geofencingEvent: GeofencingEvent) {
        if (geofencingEvent.hasError()) {
            throw RuntimeException("Geofencing error is not handled. This is the error btw: " + geofencingEvent.errorCode)
        }
        val geofenceTransition = geofencingEvent.geofenceTransition
        val geofenceList = geofencingEvent.triggeringGeofences
        for (geofence in geofenceList) {
            val profile = repository.get(geofence.requestId)!!
            val condition = profile.getCondition(Condition.Type.PLACE) as PlaceCondition
            condition.isTrue = geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER || geofenceTransition == Geofence.GEOFENCE_TRANSITION_DWELL
            listener.notifyConditionChanged(profile, condition)
        }
    }

}