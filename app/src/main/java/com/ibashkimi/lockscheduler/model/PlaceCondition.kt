package com.ibashkimi.lockscheduler.model

data class PlaceCondition(val latitude: Double, val longitude: Double, val radius: Int, val address: String) : Condition(Type.PLACE)
