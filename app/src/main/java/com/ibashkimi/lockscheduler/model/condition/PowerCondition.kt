package com.ibashkimi.lockscheduler.model.condition

data class PowerCondition(val powerConnected: Boolean): Condition(Condition.Type.POWER)