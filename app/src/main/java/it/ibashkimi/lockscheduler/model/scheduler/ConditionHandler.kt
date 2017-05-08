package it.ibashkimi.lockscheduler.model.scheduler

import android.content.SharedPreferences
import android.util.Log
import it.ibashkimi.lockscheduler.model.Profile
import it.ibashkimi.lockscheduler.model.source.ProfilesRepository

/**
 * @author Indrit Bashkimi (mailto: indrit.bashkimi@studio.unibo.it)
 */

abstract class ConditionHandler(val repository: ProfilesRepository, val listener: ConditionChangeListener) {

    private val TAG = "ConditionHandler"

    abstract val sharedPreferences: SharedPreferences

    abstract fun init()

    abstract fun register(profile: Profile)

    abstract fun unregister(profileId: String)

    protected fun addProfileId(profileId: String) {
        val profileIds = sharedPreferences.getStringSet("registered_profiles", mutableSetOf())
        if (profileIds.add(profileId))
            sharedPreferences.edit().putStringSet("registered_profiles", profileIds).commit()
    }

    protected fun removeProfileId(profileId: String) {
        val profileIds = sharedPreferences.getStringSet("registered_profiles", mutableSetOf())
        profileIds.remove(profileId)
        sharedPreferences.edit().putStringSet("registered_profiles", profileIds).commit()
    }

    protected fun getRegisteredProfiles(): MutableSet<String> {
        Log.d(TAG, "getRegisteredProfiles called()")
        val res = sharedPreferences.getStringSet("registered_profiles", mutableSetOf())
        Log.d(TAG, "result: size = ${res.size}")
        for (item in res)
            Log.d(TAG, "result: item $item")
        return res
    }

    protected fun setRegisteredProfiles(profileIds: Set<String>) {
        Log.d(TAG, "setRegisteredProfiles() called.")
        Log.d(TAG, "size = ${profileIds.size}")
        for (item in profileIds)
            Log.d(TAG, "item $item")
        sharedPreferences.edit().putStringSet("registered_profiles", profileIds).commit()
    }

    protected fun getProfiles(profileIds: Set<String>): List<Profile> {
        Log.d(TAG, "getProfiles() called.")
        Log.d(TAG, "size = ${profileIds.size}")
        for (item in profileIds)
            Log.d(TAG, "item $item")
        val profiles = mutableListOf<Profile>()
        if (profileIds.isNotEmpty()) {
            for (id in profileIds) {
                Log.d(TAG, "get profile with id = $id")
                val profile = repository.get(id)
                Log.d(TAG, "profile = $profile")
                profiles.add(profile!!)
            }
        }
        return profiles
    }
}
