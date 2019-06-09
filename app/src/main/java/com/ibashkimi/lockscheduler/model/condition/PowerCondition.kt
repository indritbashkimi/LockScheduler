package com.ibashkimi.lockscheduler.model.condition

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class PowerCondition(val powerConnected: Boolean, override var isTriggered: Boolean = false)
    : Condition(Type.POWER, isTriggered), Parcelable