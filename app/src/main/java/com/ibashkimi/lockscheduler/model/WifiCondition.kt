package com.ibashkimi.lockscheduler.model

data class WifiCondition(val wifiList: List<WifiItem>): Condition(Type.WIFI)
