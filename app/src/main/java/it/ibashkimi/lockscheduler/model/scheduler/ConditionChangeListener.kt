package it.ibashkimi.lockscheduler.model.scheduler

import it.ibashkimi.lockscheduler.model.Condition
import it.ibashkimi.lockscheduler.model.Profile


interface ConditionChangeListener {

    fun notifyConditionChanged(profile: Profile, condition: Condition)
}