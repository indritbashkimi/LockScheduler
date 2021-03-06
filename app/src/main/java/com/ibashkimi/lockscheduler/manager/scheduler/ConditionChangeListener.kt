package com.ibashkimi.lockscheduler.manager.scheduler

import com.ibashkimi.lockscheduler.model.Profile
import com.ibashkimi.lockscheduler.model.condition.Condition


interface ConditionChangeListener {

    fun notifyConditionChanged(profile: Profile, condition: Condition, wasActive: Boolean)
}