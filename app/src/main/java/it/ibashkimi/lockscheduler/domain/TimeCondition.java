package it.ibashkimi.lockscheduler.domain;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;


public class TimeCondition extends Condition {

    private boolean[] daysActive;

    public TimeCondition(String name) {
        this(name, new boolean[]{true, true, true, true, true, true, true});
    }

    public TimeCondition(String name, boolean[] daysActive) {
        super(Type.TIME, name);
        this.daysActive = daysActive;
        for (int i = 0; i < 7; i++) {
            Log.d("TimeCondition", "day_" + i + ": " + daysActive[i]);
        }
    }

    public boolean[] getDaysActive() {
        return daysActive;
    }

    public void setDaysActive(boolean[] daysActive) {
        this.daysActive = daysActive;
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
        boolean[] daysActive = new boolean[7];
        for (int i = 0; i < 7; i++) {
            daysActive[i] = jsonObject.getBoolean("day_" + i);
        }
        return new TimeCondition(name, daysActive);
    }
}
