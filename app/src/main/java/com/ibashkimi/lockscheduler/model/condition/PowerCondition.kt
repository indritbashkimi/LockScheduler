package com.ibashkimi.lockscheduler.model.condition

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class PowerCondition(val powerConnected: Boolean) : Condition(Condition.Type.POWER), Parcelable