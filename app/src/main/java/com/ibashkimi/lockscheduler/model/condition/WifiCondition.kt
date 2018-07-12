package com.ibashkimi.lockscheduler.model.condition

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue

@Parcelize
data class WifiCondition(val wifiList: @RawValue List<WifiItem>) : Condition(Condition.Type.WIFI), Parcelable
