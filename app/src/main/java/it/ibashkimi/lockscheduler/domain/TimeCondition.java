package it.ibashkimi.lockscheduler.domain;

import org.json.JSONException;
import org.json.JSONObject;


public class TimeCondition extends Condition {

    public TimeCondition(String name) {
        super(Type.TIME, name);
    }

    @Override
    public String toJson() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("type", getType());
            jsonObject.put("name", getName());
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
        return new TimeCondition(name);
    }
}
