package it.ibashkimi.lockscheduler.model.scheduler

import android.content.SharedPreferences
import android.util.Log
import it.ibashkimi.lockscheduler.model.Profile
import it.ibashkimi.lockscheduler.model.source.ProfilesRepository

/**
 * @author Indrit Bashkimi (mailto: indrit.bashkimi@studio.unibo.it)
 */

abstract class ConditionHandler(val repository: ProfilesRepository, val listener: ConditionChangeListener) {

    abstract val sharedPreferences: SharedPreferences

    abstract fun init()

    abstract fun register(profile: Profile)

    abstract fun unregister(profileId: String)

    protected fun addProfileId(profileId: String) {
        val profileIds = sharedPreferences.getStringSet("registered_profiles", mutableSetOf())
        if (profileIds.add(profileId))
            sharedPreferences.edit().putStringSet("registered_profiles", profileIds).apply()
    }

    protected fun removeProfileId(profileId: String) {
        val profileIds = sharedPreferences.getStringSet("registered_profiles", mutableSetOf())
        profileIds.remove(profileId)
        sharedPreferences.edit().putStringSet("registered_profiles", profileIds).apply()
    }

    protected fun getRegisteredProfiles(): MutableSet<String> {
        return sharedPreferences.getStringSet("registered_profiles", mutableSetOf())
    }

    protected fun setRegisteredProfiles(profileIds: Set<String>) {
        sharedPreferences.edit().putStringSet("registered_profiles", profileIds).apply()
    }

    protected fun getProfiles(profileIds: Set<String>): List<Profile> {
        val profiles = mutableListOf<Profile>()
        if (profileIds.isNotEmpty()) {
            for (id in profileIds) {
                Log.d("ConditionHandler", "id = $id")
                profiles.add(repository.getProfile(id)!!)
            }
        }
        return profiles
    }
}
