package com.ibashkimi.lockscheduler.model.condition

data class PowerCondition(val powerConnected: Boolean): Condition(com.ibashkimi.lockscheduler.model.Condition.Type.POWER)