package com.ibashkimi.lockscheduler.model.condition

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
data class TimeCondition(var daysActive: BooleanArray = booleanArrayOf(true, true, true, true, true, true, true),
                    var startTime: Time = Time(0, 0),
                    var endTime: Time = Time(0, 0))
    : Condition(Condition.Type.TIME), Parcelable {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TimeCondition

        if (!Arrays.equals(daysActive, other.daysActive)) return false
        if (startTime != other.startTime) return false
        if (endTime != other.endTime) return false

        return true
    }

    override fun hashCode(): Int {
        var result = Arrays.hashCode(daysActive)
        result = 31 * result + startTime.hashCode()
        result = 31 * result + endTime.hashCode()
        return result
    }
}
