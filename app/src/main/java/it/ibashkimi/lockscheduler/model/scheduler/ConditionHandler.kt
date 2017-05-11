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

    protected fun add(profileId: String): Boolean {
        Log.d(TAG, "add called with id = $profileId")
        val profileIds: Set<String> = getRegisteredIds()
        val newProfileIds = mutableSetOf<String>()
        newProfileIds.addAll(profileIds)
        if (newProfileIds.add(profileId)) {
            sharedPreferences.edit().putStringSet("registered_profiles", newProfileIds).commit()
            return true
        } else {
            return false
        }
    }

    protected fun remove(profileId: String) {
        Log.d(TAG, "remove called with id = $profileId")
        val profileIds: Set<String> = sharedPreferences.getStringSet("registered_profiles", mutableSetOf())
        val newProfileIds = mutableSetOf<String>()
        newProfileIds.addAll(profileIds)
        newProfileIds.remove(profileId)
        sharedPreferences.edit().putStringSet("registered_profiles", newProfileIds).commit()
    }

    protected fun getRegisteredIds(): Set<String> {
        Log.d(TAG, "getRegisteredIds called()")
        val res: Set<String> = sharedPreferences.getStringSet("registered_profiles", mutableSetOf())
        return res
    }

    protected fun setRegisteredIds(profileIds: Set<String>) {
        Log.d(TAG, "setRegisteredIds() called.")
        sharedPreferences.edit().putStringSet("registered_profiles", profileIds).commit()
    }

    protected fun getRegisteredProfiles(): List<Profile> {
        Log.d(TAG, "getRegisteredProfiles() called.")
        val profileIds = getRegisteredIds()
        printIdsForDebug(profileIds)
        val profiles = mutableListOf<Profile>()
        val idsToRemove = mutableListOf<String>()
        if (profileIds.isNotEmpty()) {
            for (id in profileIds) {
                Log.d(TAG, "get profile with id = $id")
                val profile = repository.get(id)
                Log.d(TAG, "profile = $profile")
                // profiles.add(profile!!)
                if (profile == null) {
                    idsToRemove.add(id)
                    Log.e(TAG, "Profile $id is null. Data corruption. Doing cleanup.")
                } else {
                    profiles.add(profile)
                }
            }
        }
        if (idsToRemove.isNotEmpty()) {
            val finalProfileIds = mutableListOf<String>()
            finalProfileIds.addAll(profileIds)
            for (id in idsToRemove)
                finalProfileIds.remove(id)
            setRegisteredIds(finalProfileIds.toSet())
        }

        return profiles
    }

    private fun printIdsForDebug(ids: Set<String>) {
        Log.d(TAG, "ids.size = ${ids.size}")
        for (id in ids)
            Log.d(TAG, "id: $id")
    }
}
