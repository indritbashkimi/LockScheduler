package it.ibashkimi.lockscheduler.model

import android.util.ArraySet
import it.ibashkimi.lockscheduler.model.scheduler.ProfileScheduler
import it.ibashkimi.lockscheduler.model.source.ProfilesRepository

/**
 * @author Indrit Bashkimi (mailto: indrit.bashkimi@studio.unibo.it)
 */
class ProfileManager(val repository: ProfilesRepository, val scheduler: ProfileScheduler) {

    fun add(profile: Profile) {
        repository.save(profile)
        scheduler.register(profile)
    }

    fun get(profileId: String): Profile? = repository.get(profileId)

    fun getAll(): List<Profile> = repository.profiles

    fun remove(profileId: String) {
        repository.delete(profileId)
        //scheduler.unregister(profileId)
    }

    fun removeAll(profileIds: ArraySet<String>? = null) {

    }

    fun save(profile: Profile) = repository.save(profile)

    fun update(profile: Profile) {

    }
}