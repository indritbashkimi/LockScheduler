package com.ibashkimi.lockscheduler.extention

import android.content.Context
import androidx.annotation.StringRes
import com.ibashkimi.lockscheduler.R
import com.ibashkimi.lockscheduler.model.condition.DaysOfWeek


fun DaysOfWeek.toDaysString(context: Context): String? {
    val activeDays = activeDaysIndexes()
    if (activeDays.isEmpty()) return context.getString(R.string.time_condition_days_selection_none)
    if (activeDays.size == 7) return context.getString(R.string.time_condition_days_selection_all)
    val res = StringBuilder()
    res.append(getDayName(context, activeDays[0]))
    if (activeDays.size > 1) {
        res.append(", ")
        for (i in 1 until activeDays.size - 1) {
            res.append(getDayName(context, activeDays[i])).append(", ")
        }
        res.append(getDayName(context, activeDays[activeDays.size - 1]))
    }
    return res.toString()
}

fun DaysOfWeek.getDayName(context: Context, dayIndex: Int): String {
    @StringRes val stringRes: Int = when (dayIndex) {
        0 -> R.string.monday_short
        1 -> R.string.tuesday_short
        2 -> R.string.wednesday_short
        3 -> R.string.thursday_short
        4 -> R.string.friday_short
        5 -> R.string.saturday_short
        6 -> R.string.sunday_short
        else -> throw RuntimeException("Invalid day index: $dayIndex.")
    }
    return context.getString(stringRes)
}