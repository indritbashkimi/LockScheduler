package it.ibashkimi.lockscheduler.model;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;


public class TimeCondition extends Condition {

    private static final String TAG = "TimeCondition";

    private boolean[] daysActive;

    public TimeCondition(String name) {
        this(name, new boolean[]{true, true, true, true, true, true, true});
    }

    public TimeCondition(String name, boolean[] daysActive) {
        super(Type.TIME, name);
        this.daysActive = daysActive;
    }

    public boolean[] getDaysActive() {
        return daysActive;
    }

    public void setDaysActive(boolean[] daysActive) {
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
        return true;
    }

    @Override
    public String toJson() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("type", getType());
            jsonObject.put("name", getName());
            jsonObject.put("true", isTrue());
            for (int i = 0; i < 7; i++) {
                jsonObject.put("day_" + i, daysActive[i]);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return "";
        }
        return jsonObject.toString();
    }

    public static TimeCondition parseJson(String json) throws JSONException {
        JSONObject jsonObject = new JSONObject(json);
        int type = jsonObject.getInt("type");
        String name = jsonObject.getString("name");
        boolean isTrue = jsonObject.getBoolean("true");
        boolean[] daysActive = new boolean[7];
        for (int i = 0; i < 7; i++) {
            daysActive[i] = jsonObject.getBoolean("day_" + i);
        }
        TimeCondition timeCondition = new TimeCondition(name, daysActive);
        timeCondition.setTrue(isTrue);
        return timeCondition;
    }
}
