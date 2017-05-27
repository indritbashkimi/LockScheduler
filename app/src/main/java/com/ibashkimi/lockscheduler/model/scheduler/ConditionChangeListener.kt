package com.ibashkimi.lockscheduler.model.scheduler

import com.ibashkimi.lockscheduler.model.Condition
import com.ibashkimi.lockscheduler.model.Profile


interface ConditionChangeListener {

    fun notifyConditionChanged(profile: Profile, condition: Condition, wasActive: Boolean)
}