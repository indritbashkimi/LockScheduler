package com.ibashkimi.lockscheduler.model.condition

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class PlaceCondition(val latitude: Double, val longitude: Double, val radius: Int, val address: String) : Condition(Condition.Type.PLACE), Parcelable
