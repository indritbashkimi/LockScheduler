package com.ibashkimi.lockscheduler.model.condition

data class WifiCondition(val wifiList: List<WifiItem>): Condition(Condition.Type.WIFI)
