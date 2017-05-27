package it.ibashkimi.lockscheduler.model.scheduler

import android.support.annotation.CallSuper
import it.ibashkimi.lockscheduler.model.Profile
import it.ibashkimi.lockscheduler.model.source.ProfilesDataSource

abstract class ConditionScheduler(val conditionType: Int, private val repository: ProfilesDataSource) {

    abstract fun init()

    protected val registeredProfiles: List<Profile>
        get() = repository.getConditionProfiles(conditionType)

    @CallSuper
    open fun register(profile: Profile): Boolean {
        repository.saveCondition(profile.id, conditionType)
        return true
    }

    @CallSuper
    open fun unregister(profileId: String) {
        repository.deleteCondition(profileId, conditionType)
    }

    protected fun getProfile(id: String): Profile {
        val profile = repository.getProfile(id) ?: throw IllegalStateException("Data is corrupted.")
        return profile
    }
}