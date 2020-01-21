package com.ibashkimi.lockscheduler.model.condition

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
data class Time(val hour: Int, val minute: Int) : Parcelable {

    val isMidnight: Boolean
        get() = hour == 0 && minute == 0

    fun isHigherThan(time: Time): Boolean {
        return hour > time.hour || (hour == time.hour && minute > time.minute)
    }

    fun isLowerThan(time: Time): Boolean {
        return hour < time.hour || (hour == time.hour && minute < time.minute)
    }

    companion object {

        fun fromTimeStamp(timeStamp: Long): Time {
            val cal = Calendar.getInstance()
            cal.timeInMillis = timeStamp
            return Time(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE))
        }

        fun fromCalendar(calendar: Calendar): Time {
            return Time(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE))
        }
    }
}
