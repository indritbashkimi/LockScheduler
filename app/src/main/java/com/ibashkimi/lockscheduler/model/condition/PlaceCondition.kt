package com.ibashkimi.lockscheduler.model.condition

data class PlaceCondition(val latitude: Double, val longitude: Double, val radius: Int, val address: String) : Condition(com.ibashkimi.lockscheduler.model.Condition.Type.PLACE)
