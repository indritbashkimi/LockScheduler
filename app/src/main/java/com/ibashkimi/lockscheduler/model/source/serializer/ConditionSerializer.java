package com.ibashkimi.lockscheduler.model.source.serializer;

import com.ibashkimi.lockscheduler.model.Condition;
import com.ibashkimi.lockscheduler.model.PlaceCondition;
import com.ibashkimi.lockscheduler.model.PowerCondition;
import com.ibashkimi.lockscheduler.model.Time;
import com.ibashkimi.lockscheduler.model.TimeCondition;
import com.ibashkimi.lockscheduler.model.WifiCondition;
import com.ibashkimi.lockscheduler.model.WifiItem;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ConditionSerializer {

    private static String toJson(TimeCondition condition) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("type", condition.getType());
            jsonObject.put("true", condition.isTrue());
            for (int i = 0; i < 7; i++) {
                jsonObject.put("day_" + i, condition.getDaysActive()[i]);
            }
            jsonObject.put("start_time_hour", condition.getStartTime().hour);
            jsonObject.put("start_time_minute", condition.getStartTime().minute);
            jsonObject.put("end_time_hour", condition.getEndTime().hour);
            jsonObject.put("end_time_minute", condition.getEndTime().minute);
        } catch (JSONException e) {
            e.printStackTrace();
            return "";
        }
        return jsonObject.toString();
    }

    public static String toJson(PlaceCondition condition) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("type", condition.getType());
            jsonObject.put("true", condition.isTrue());
            jsonObject.put("latitude", condition.getLatitude());
            jsonObject.put("longitude", condition.getLongitude());
            jsonObject.put("radius", condition.getRadius());
            jsonObject.put("address", condition.getAddress());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
    }

    private static String toJson(WifiCondition condition) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("type", condition.getType());
            jsonObject.put("true", condition.isTrue());
            jsonObject.put("wifi_items_len", condition.getWifiList().size());
            for (int i = 0; i < condition.getWifiList().size(); i++) {
                jsonObject.put("wifi_item_" + i, condition.getWifiList().get(i).getSsid());
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return "";
        }
        return jsonObject.toString();
    }

    private static String toJson(PowerCondition condition) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("type", condition.getType());
            jsonObject.put("true", condition.isTrue());
            jsonObject.put("power_connected", condition.getPowerConnected());
        } catch (JSONException e) {
            e.printStackTrace();
            return "";
        }
        return jsonObject.toString();
    }

    static String conditionToJson(Condition condition) {
        switch (condition.getType()) {
            case Condition.Type.PLACE:
                return toJson((PlaceCondition) condition);
            case Condition.Type.TIME:
                return toJson((TimeCondition) condition);
            case Condition.Type.WIFI:
                return toJson((WifiCondition) condition);
            case Condition.Type.POWER:
                return toJson((PowerCondition) condition);
        }
        throw new RuntimeException("Unsupported condition type " + condition.getType());
    }

    static Condition parseCondition(String conditionJson) throws JSONException {
        JSONObject conditionJsonObject = new JSONObject(conditionJson);
        @Condition.Type int type = conditionJsonObject.getInt("type");
        switch (type) {
            case Condition.Type.PLACE:
                return parsePlaceConditionJson(conditionJson);
            case Condition.Type.TIME:
                return parseTimeConditionJson(conditionJson);
            case Condition.Type.WIFI:
                return parseWifiCondition(conditionJson);
            case Condition.Type.POWER:
                return parsePowerCondition(conditionJson);
        }
        throw new RuntimeException("Unsupported condition type " + type);
    }

    public static PlaceCondition parsePlaceConditionJson(String json) throws JSONException {
        return parsePlaceConditionJson(new JSONObject(json));
    }

    private static PlaceCondition parsePlaceConditionJson(JSONObject jsonObject) throws JSONException {
        double latitude = jsonObject.getDouble("latitude");
        double longitude = jsonObject.getDouble("longitude");
        int radius = jsonObject.getInt("radius");
        boolean isTrue = jsonObject.getBoolean("true");
        String address = jsonObject.getString("address");
        PlaceCondition placeCondition = new PlaceCondition(latitude, longitude, radius, address);
        placeCondition.setTrue(isTrue);
        return placeCondition;
    }

    private static TimeCondition parseTimeConditionJson(String json) throws JSONException {
        JSONObject jsonObject = new JSONObject(json);
        boolean[] daysActive = new boolean[7];
        for (int i = 0; i < 7; i++) {
            daysActive[i] = jsonObject.getBoolean("day_" + i);
        }
        Time startTime = new Time(jsonObject.getInt("start_time_hour"), jsonObject.getInt("start_time_minute"));
        Time endTime = new Time(jsonObject.getInt("end_time_hour"), jsonObject.getInt("end_time_minute"));
        TimeCondition timeCondition = new TimeCondition(daysActive, startTime, endTime);
        timeCondition.setTrue(jsonObject.getBoolean("true"));
        return timeCondition;
    }

    private static WifiCondition parseWifiCondition(String json) throws JSONException {
        JSONObject jsonObject = new JSONObject(json);
        int type = jsonObject.getInt("type");
        boolean isTrue = jsonObject.getBoolean("true");
        int wifiItemSize = jsonObject.getInt("wifi_items_len");
        ArrayList<WifiItem> items = new ArrayList<>(wifiItemSize);
        for (int i = 0; i < wifiItemSize; i++) {
            String ssid = jsonObject.getString("wifi_item_" + i);
            WifiItem item = new WifiItem(ssid);
            items.add(item);
        }
        WifiCondition wifiCondition = new WifiCondition(items);
        wifiCondition.setTrue(isTrue);
        return wifiCondition;
    }

    private static PowerCondition parsePowerCondition(String json) throws JSONException {
        JSONObject jsonObject = new JSONObject(json);
        int type = jsonObject.getInt("type");
        boolean isTrue = jsonObject.getBoolean("true");
        PowerCondition condition = new PowerCondition(jsonObject.getBoolean("power_connected"));
        condition.setTrue(isTrue);
        return condition;
    }
}
