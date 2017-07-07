package com.ibashkimi.lockscheduler.model

import android.content.Context
import com.ibashkimi.lockscheduler.model.condition.Condition
import com.ibashkimi.lockscheduler.model.condition.PlaceCondition

fun isLocationPermissionOk(context: Context): Boolean {
    val shouldCheckPermission = ProfileManager.getAll()
            .map { it.getCondition(Condition.Type.PLACE) }
            .any { it is PlaceCondition }
    TODO()
    return false
}

fun isAdminPermissionOk(): Boolean {
    TODO()
}

