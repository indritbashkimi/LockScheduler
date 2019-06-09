package com.ibashkimi.lockscheduler.model.condition

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class TimeCondition(var daysActive: DaysOfWeek = DaysOfWeek(),
                         var startTime: Time = Time(0, 0),
                         var endTime: Time = Time(0, 0),
                         override var isTriggered: Boolean = false)
    : Condition(Type.TIME, isTriggered), Parcelable

@Parcelize
data class DaysOfWeek (
        var monday: Boolean = false,
        var tuesday: Boolean = false,
        var wednesday: Boolean = false,
        var thursday: Boolean = false,
        var friday: Boolean = false,
        var saturday: Boolean = false,
        var sunday: Boolean = false) : Parcelable {

    operator fun get(index: Int): Boolean {
        return when(index) {
            0 -> monday
            1 -> tuesday
            2 -> wednesday
            3 -> thursday
            4 -> friday
            5 -> saturday
            6 -> sunday
            else -> throw IndexOutOfBoundsException("Invalid index $index. Valid range: [0 : 6].")
        }
    }

    operator fun set(index: Int, value: Boolean) {
        return when(index) {
            0 -> monday = value
            1 -> tuesday = value
            2 -> wednesday = value
            3 -> thursday = value
            4 -> friday = value
            5 -> saturday = value
            6 -> sunday = value
            else -> throw IndexOutOfBoundsException("Invalid index $index. Valid range: [0 : 6].")
        }
    }

    fun asBooleanArray(): BooleanArray {
        return booleanArrayOf(monday, tuesday, wednesday, thursday, friday, saturday, sunday)
    }

    fun createCopy(): DaysOfWeek {
        return DaysOfWeek(monday, tuesday, wednesday, thursday, friday, saturday, sunday)
    }
}
