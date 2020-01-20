package com.ibashkimi.lockscheduler.addeditprofile.conditions.wifi

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class SelectableWifiItem(val SSID: String, val BSSID: String, var isSelected: Boolean = false): Parcelable