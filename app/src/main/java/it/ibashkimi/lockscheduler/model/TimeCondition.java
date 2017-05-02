package it.ibashkimi.lockscheduler.model;

import android.support.annotation.IntDef;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Calendar;

import static it.ibashkimi.lockscheduler.model.TimeCondition.Day.FRIDAY;
import static it.ibashkimi.lockscheduler.model.TimeCondition.Day.MONDAY;
import static it.ibashkimi.lockscheduler.model.TimeCondition.Day.SATURDAY;
import static it.ibashkimi.lockscheduler.model.TimeCondition.Day.SUNDAY;
import static it.ibashkimi.lockscheduler.model.TimeCondition.Day.THURSDAY;
import static it.ibashkimi.lockscheduler.model.TimeCondition.Day.TUESDAY;
import static it.ibashkimi.lockscheduler.model.TimeCondition.Day.WEDNESDAY;


public class TimeCondition extends Condition {

    private static final String TAG = "TimeCondition";

    @IntDef({MONDAY,
            TUESDAY,
            WEDNESDAY,
            THURSDAY,
            FRIDAY,
            SATURDAY,
            SUNDAY
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface Day {
        int MONDAY = 0;
        int TUESDAY = 1;
        int WEDNESDAY = 2;
        int THURSDAY = 3;
        int FRIDAY = 4;
        int SATURDAY = 5;
        int SUNDAY = 6;
    }

    public static final int[] ALL_DAYS = new int[]{MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY};

    private boolean[] daysActive;

    private int[] startTime;

    private int[] endTime;

    public TimeCondition() {
        this(new boolean[]{true, true, true, true, true, true, true});
    }

    public TimeCondition(boolean[] daysActive) {
        this(daysActive, new int[]{0, 0}, new int[]{0, 0});
    }

    public TimeCondition(boolean[] daysActive, int[] startTime, int[] endTime) {
        super(Type.TIME);
        this.daysActive = daysActive;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public boolean[] getDaysActive() {
        return daysActive;
    }

    public int[] getStartTime() {
        return startTime;
    }

    public int[] getEndTime() {
        return endTime;
    }

    public void setStartTime(int[] startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(int[] endTime) {
        this.endTime = endTime;
    }

    public void setDaysActive(boolean[] daysActive) {
        Log.d(TAG, "setDaysActive() called with: daysActive = [" + daysActive + "]");
        for (boolean day : daysActive)
            Log.d(TAG, "setDaysActive: day " + day);
        this.daysActive = daysActive;
    }

    public void checkNow() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.get(Calendar.DAY_OF_WEEK);
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK); // dayOfWeek = (dayOfWeek + 5) % 6;
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
        setTrue(getDaysActive()[dayOfWeek]);
    }

    public long getNextAlarm() {
        boolean allTrue = true;
        for (int i = 0; i < 7; i++)
            if (!daysActive[i]) {
                allTrue = false;
                break;
            }
        if (allTrue) {
            Log.d(TAG, "getNextAlarm: -1");
            return -1;
        }
        boolean allFalse = true;
        for (int i = 0; i < 7; i++)
            if (daysActive[i]) {
                allFalse = false;
                break;
            }
        if (allFalse) {
            Log.d(TAG, "getNextAlarm: -1");
            return -1;
        }
        int dayOfWeek = dayOfWeek();
        boolean dayVal = daysActive[dayOfWeek];
        int specialDay = -1;
        for (int i = dayOfWeek + 1; i < 7; i++) {
            if (dayVal != daysActive[i]) {
                specialDay = i - dayOfWeek;
                break;
            }
        }
        if (specialDay > -1) {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_MONTH, specialDay);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            Log.d(TAG, "getNextAlarm: " + cal);
            return cal.getTimeInMillis();
        }
        for (int i = dayOfWeek - 1; i > -1; i--) {
            if (dayVal != daysActive[i]) {
                specialDay = 7 - dayOfWeek + i;
                break;
            }
        }
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, specialDay);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Log.d(TAG, "getNextAlarm: " + cal);
        return cal.getTimeInMillis();
    }

    private int dayOfWeek() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.get(Calendar.DAY_OF_WEEK);
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK); // dayOfWeek = (dayOfWeek + 5) % 6;
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

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof TimeCondition)) {
            return false;
        }
        TimeCondition condition = (TimeCondition) obj;
        for (int i = 0; i < 7; i++) {
            if (condition.getDaysActive()[i] != daysActive[i])
                return false;
        }
        int[] startTime = condition.getStartTime();
        int[] endTime = condition.getEndTime();
        return this.startTime[0] == startTime[0] &&
                this.startTime[1] == startTime[1] &&
                this.endTime[0] == endTime[0] &&
                this.endTime[1] == endTime[1];
    }

    @Override
    public String toJson() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("type", getType());
            jsonObject.put("true", isTrue());
            for (int i = 0; i < 7; i++) {
                jsonObject.put("day_" + i, daysActive[i]);
            }
            jsonObject.put("start_time_hour", startTime[0]);
            jsonObject.put("start_time_minute", startTime[1]);
            jsonObject.put("end_time_hour", endTime[0]);
            jsonObject.put("end_time_minute", endTime[1]);
        } catch (JSONException e) {
            e.printStackTrace();
            return "";
        }
        return jsonObject.toString();
    }

    public static TimeCondition parseJson(String json) throws JSONException {
        JSONObject jsonObject = new JSONObject(json);
        int type = jsonObject.getInt("type");
        boolean isTrue = jsonObject.getBoolean("true");
        boolean[] daysActive = new boolean[7];
        for (int i = 0; i < 7; i++) {
            daysActive[i] = jsonObject.getBoolean("day_" + i);
        }
        int hours = jsonObject.getInt("start_time_hour");
        int minutes = jsonObject.getInt("start_time_minute");
        int[] startTime = new int[]{hours, minutes};
        hours = jsonObject.getInt("end_time_hour");
        minutes = jsonObject.getInt("end_time_minute");
        int[] endTime = new int[]{hours, minutes};
        TimeCondition timeCondition = new TimeCondition(daysActive, startTime, endTime);
        timeCondition.setTrue(isTrue);
        return timeCondition;
    }

    /*public int[] getActiveDays() {
        int size = 0;
        for (int i = 0; i < activeDays.size(); i++)
            if (activeDays.valueAt(i))
                size++;
        int[] result = new int[size];
        int j = 0;
        for (int i = 0; i < activeDays.size(); i++)
            if (activeDays.valueAt(i)) {
                result[j] = activeDays.keyAt(i);
                j++;
            }
        return result;
    }

    public boolean[] getWeek() {
        boolean[] week = new boolean[7];
        for (int i = 0; i < 7; i++) {
            @Day int day = MONDAY;
            switch ()
        }
    }

    public int[] getStartTime() {
        return startTime;
    }

    public int[] getEndTime() {
        return endTime;
    }

    public void setStartTime(int[] startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(int[] endTime) {
        this.endTime = endTime;
    }

    public void setActiveDays(int[] activeDays) {
        for (int day : activeDays)
            this.activeDays.put(day, true);
    }

    public boolean isDayActive(@Day int day) {
        return activeDays.get(day);
    }

    public void setDayActive(@Day int day, boolean isActive) {
        activeDays.put(day, isActive);
    }

    private int getIndex(int day) {
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

    public void checkNow() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        switch (dayOfWeek) {
            case Calendar.MONDAY:
                dayOfWeek = MONDAY;
                break;
            case Calendar.TUESDAY:
                dayOfWeek = TUESDAY;
                break;
            case Calendar.WEDNESDAY:
                dayOfWeek = WEDNESDAY;
                break;
            case Calendar.THURSDAY:
                dayOfWeek = THURSDAY;
                break;
            case Calendar.FRIDAY:
                dayOfWeek = FRIDAY;
                break;
            case Calendar.SATURDAY:
                dayOfWeek = SATURDAY;
                break;
            case Calendar.SUNDAY:
                dayOfWeek = SUNDAY;
                break;
        }
        setTrue(activeDays.get(dayOfWeek));
    }

    public long getNextAlarm() {
        boolean allTrue = true;
        boolean allFalse = true;
        for (int i = 0; i < activeDays.size(); i++) {
            if (!activeDays.valueAt(i))
                allTrue = false;
            if (activeDays.valueAt(i))
                allFalse = false;
        }
        if (allTrue || allFalse) {
            return -1;
        }

        @Day int today = dayOfWeek();
        @Day int nextDay = nextDay(today);
        while (today == nextDay) {
            today = nextDay;
            nextDay = nextDay(today);
        }

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, calendarDay(nextDay));
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Log.d(TAG, "getNextAlarm: " + cal);
        return cal.getTimeInMillis();
    }

    @Day
    private int dayOfWeek() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        @Day int dayOfWeek = -1;
        switch (calendar.get(Calendar.DAY_OF_WEEK)) {
            case Calendar.MONDAY:
                dayOfWeek = MONDAY;
                break;
            case Calendar.TUESDAY:
                dayOfWeek = TUESDAY;
                break;
            case Calendar.WEDNESDAY:
                dayOfWeek = WEDNESDAY;
                break;
            case Calendar.THURSDAY:
                dayOfWeek = THURSDAY;
                break;
            case Calendar.FRIDAY:
                dayOfWeek = FRIDAY;
                break;
            case Calendar.SATURDAY:
                dayOfWeek = SATURDAY;
                break;
            case Calendar.SUNDAY:
                dayOfWeek = SUNDAY;
                break;
        }
        return dayOfWeek;
    }

    @Day
    private int nextDay(@Day int day) {
        switch (day) {
            case MONDAY:
                return TUESDAY;
            case Day.TUESDAY:
                return WEDNESDAY;
            case Day.WEDNESDAY:
                return THURSDAY;
            case Day.THURSDAY:
                return FRIDAY;
            case Day.FRIDAY:
                return SATURDAY;
            case Day.SATURDAY:
                return SUNDAY;
            case Day.SUNDAY:
                return MONDAY;
        }
        throw new RuntimeException("Invalid day number " + day + ".");
    }

    @Day
    private int previousDay(@Day int day) {
        switch (day) {
            case MONDAY:
                return SUNDAY;
            case Day.TUESDAY:
                return MONDAY;
            case Day.WEDNESDAY:
                return TUESDAY;
            case Day.THURSDAY:
                return TUESDAY;
            case Day.FRIDAY:
                return THURSDAY;
            case Day.SATURDAY:
                return FRIDAY;
            case Day.SUNDAY:
                return SATURDAY;
        }
        throw new RuntimeException("Invalid day number " + day + ".");
    }

    private int calendarDay(@Day int day) {
        switch (day) {
            case MONDAY:
                return Calendar.MONDAY;
            case Day.FRIDAY:
                return Calendar.FRIDAY;
            case Day.SATURDAY:
                return Calendar.SATURDAY;
            case Day.SUNDAY:
                return Calendar.SUNDAY;
            case Day.THURSDAY:
                return Calendar.THURSDAY;
            case Day.TUESDAY:
                return Calendar.TUESDAY;
            case Day.WEDNESDAY:
                return Calendar.WEDNESDAY;
        }
        throw new RuntimeException("Invalid day number " + day + ".");
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof TimeCondition)) {
            return false;
        }
        TimeCondition condition = (TimeCondition) obj;
        for (int i = 0; i < 7; i++) {
            if (condition.activeDays.get(i) != activeDays.get(i))
                return false;
        }
        int[] startTime = condition.getStartTime();
        int[] endTime = condition.getEndTime();
        return this.startTime[0] == startTime[0] &&
                this.startTime[1] == startTime[1] &&
                this.endTime[0] == endTime[0] &&
                this.endTime[1] == endTime[1];
    }*/
}
