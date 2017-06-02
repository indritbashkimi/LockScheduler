package com.ibashkimi.lockscheduler.model

data class PowerCondition(val powerConnected: Boolean): Condition(Condition.Type.POWER)