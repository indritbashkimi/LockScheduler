package com.ibashkimi.lockscheduler.model.condition

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class WifiItem(val ssid: String, val bssid: String) : Parcelable
