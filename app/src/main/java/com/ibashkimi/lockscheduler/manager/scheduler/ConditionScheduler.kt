package com.ibashkimi.lockscheduler.manager.scheduler

import androidx.annotation.CallSuper
import com.ibashkimi.lockscheduler.model.Profile
import com.ibashkimi.lockscheduler.model.condition.Condition
import com.ibashkimi.lockscheduler.data.ProfilesDataSource

abstract class ConditionScheduler(
    private val conditionType: Condition.Type,
    private val repository: ProfilesDataSource
) {

    abstract suspend fun init()

    protected val registeredProfiles: List<Profile>
        get() = repository.getConditionProfiles(conditionType)

    @CallSuper
    open suspend fun register(profile: Profile): Boolean {
        repository.saveCondition(profile.id, conditionType)
        return true
    }

    @CallSuper
    open suspend fun unregister(profileId: String) {
        repository.deleteCondition(profileId, conditionType)
    }

    protected fun getProfile(id: String): Profile? = repository.getProfile(id)
}
