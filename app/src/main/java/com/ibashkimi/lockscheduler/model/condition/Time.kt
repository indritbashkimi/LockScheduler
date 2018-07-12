package com.ibashkimi.lockscheduler.model.condition

import android.os.Parcelable
import com.ibashkimi.lockscheduler.addeditprofile.conditions.time.CompareResult
import com.ibashkimi.lockscheduler.addeditprofile.conditions.time.CompareResult.*
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
data class Time(val hour: Int, val minute: Int) : Parcelable {

    val isMidnight: Boolean
        get() = hour == 0 && minute == 0

    /**
     * -1 this < time
     * 0  this equals time
     * 1  this > time
     */
    fun compareTo(time: Time): CompareResult {
        if (this == time)
            return CompareResult(EQUAL)
        if (hour == time.hour) {
            val result = if (minute < time.minute) LOWER else HIGHER
            return CompareResult(result)
        }
        return if (hour < time.hour) {
            CompareResult(LOWER)
        } else CompareResult(HIGHER)
    }

    companion object {

        fun fromTimeStamp(timeStamp: Long): Time {
            val cal = Calendar.getInstance()
            cal.timeInMillis = timeStamp
            return Time(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE))
        }
    }
}
