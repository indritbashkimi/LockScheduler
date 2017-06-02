package com.ibashkimi.lockscheduler.model.condition;

import com.ibashkimi.support.utils.CompareResult;

import java.util.Calendar;
import java.util.Locale;

import static com.ibashkimi.support.utils.CompareResult.EQUAL;
import static com.ibashkimi.support.utils.CompareResult.HIGHER;
import static com.ibashkimi.support.utils.CompareResult.LOWER;

public class Time {
    public final int hour;
    public final int minute;

    public Time(int hour, int minute) {
        this.hour = hour;
        this.minute = minute;
    }

    public boolean isMidnight() {
        return hour == 0 && minute == 0;
    }

    /**
     * -1 this < time
     * 0  this equals time
     * 1  this > time
     */
    public CompareResult compareTo(Time time) {
        if (this.equals(time))
            return new CompareResult(EQUAL);
        if (hour == time.hour) {
            int result = minute < time.minute ? LOWER : HIGHER;
            return new CompareResult(result);
        }
        if (hour < time.hour) {
            return new CompareResult(LOWER);
        }
        return new CompareResult(HIGHER);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Time))
            return false;
        Time time = (Time) obj;
        return time.hour == hour && time.minute == minute;
    }

    @Override
    public String toString() {
        return String.format(Locale.ENGLISH, "Time[%02d:%02d]", hour, minute);
    }

    public static Time now() {
        return fromTimeStamp(System.currentTimeMillis());
    }

    public static Time fromTimeStamp(long timeStamp) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timeStamp);
        return new Time(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE));
    }
}
