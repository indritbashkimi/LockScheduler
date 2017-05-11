package it.ibashkimi.lockscheduler.model;

import java.util.Calendar;
import java.util.Locale;

import static it.ibashkimi.lockscheduler.model.TimeCondition.CompareResult.EQUAL;
import static it.ibashkimi.lockscheduler.model.TimeCondition.CompareResult.HIGHER;
import static it.ibashkimi.lockscheduler.model.TimeCondition.CompareResult.LOWER;


public class TimeCondition extends Condition {

    private static final String TAG = "TimeCondition";

    public static class CompareResult {
        public static final int LOWER = -1;
        public static final int EQUAL = 0;
        public static final int HIGHER = 1;

        private final int result;

        public CompareResult(int result) {
            this.result = result;
        }

        public boolean isLower() {
            return result == LOWER;
        }

        public boolean isNotLower() {
            return result != LOWER;
        }

        public boolean isEqual() {
            return result == EQUAL;
        }

        public boolean isNotEqual() {
            return result != EQUAL;
        }

        public boolean isHigher() {
            return result == HIGHER;
        }

        public boolean isNotHigher() {
            return result != HIGHER;
        }

        public int getResult() {
            return result;
        }
    }

    public static class Time {
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

    private boolean[] daysActive;

    private Time startTime;

    private Time endTime;

    public TimeCondition() {
        this(new boolean[]{true, true, true, true, true, true, true});
    }

    public TimeCondition(boolean[] daysActive) {
        this(daysActive, new Time(0, 0), new Time(0, 0));
    }

    public TimeCondition(boolean[] daysActive, Time startTime, Time endTime) {
        super(Type.TIME);
        this.daysActive = daysActive;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public boolean[] getDaysActive() {
        return daysActive;
    }

    public Time getStartTime() {
        return startTime;
    }

    public Time getEndTime() {
        return endTime;
    }

    public void setStartTime(Time startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(Time endTime) {
        this.endTime = endTime;
    }

    public void setDaysActive(boolean[] daysActive) {
        this.daysActive = daysActive;
    }

    private int getArrayIndexOfCalendarDay(int day) {
        switch (day) {
            case Calendar.MONDAY:
                return 0;
            case Calendar.TUESDAY:
                return 1;
            case Calendar.WEDNESDAY:
                return 2;
            case Calendar.THURSDAY:
                return 3;
            case Calendar.FRIDAY:
                return 4;
            case Calendar.SATURDAY:
                return 5;
            case Calendar.SUNDAY:
                return 6;
            default:
                return -1;
        }
    }

    public int dayOfWeekIndex() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.get(Calendar.DAY_OF_WEEK);
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK); // dayOfWeekIndex = (dayOfWeekIndex + 5) % 6;
        switch (dayOfWeek) {
            case Calendar.MONDAY:
                dayOfWeek = 0;
                break;
            case Calendar.TUESDAY:
                dayOfWeek = 1;
                break;
            case Calendar.WEDNESDAY:
                dayOfWeek = 2;
                break;
            case Calendar.THURSDAY:
                dayOfWeek = 3;
                break;
            case Calendar.FRIDAY:
                dayOfWeek = 4;
                break;
            case Calendar.SATURDAY:
                dayOfWeek = 5;
                break;
            case Calendar.SUNDAY:
                dayOfWeek = 6;
                break;
        }
        return dayOfWeek;
    }

    public boolean isCalendarDayActive(int calendarDay) {
        return daysActive[getArrayIndexOfCalendarDay(calendarDay)];
    }

    @Override
    public String toString() {
        return "TimeCondition[" + daysActive[0] + daysActive[1] + daysActive[2] + daysActive[3] + daysActive[4] + daysActive[5] + daysActive[6] + ", startTime=" + startTime + ", endTime=" + endTime + "]";
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof TimeCondition))
            return false;
        TimeCondition condition = (TimeCondition) obj;
        for (int i = 0; i < 7; i++) {
            if (condition.getDaysActive()[i] != daysActive[i])
                return false;
        }
        return condition.startTime.equals(startTime) && condition.endTime.equals(endTime);
    }
}
