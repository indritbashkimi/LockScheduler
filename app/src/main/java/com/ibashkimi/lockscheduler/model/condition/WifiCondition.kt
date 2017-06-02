package com.ibashkimi.lockscheduler.model.condition

data class WifiCondition(val wifiList: List<WifiItem>): Condition(com.ibashkimi.lockscheduler.model.Condition.Type.WIFI)
