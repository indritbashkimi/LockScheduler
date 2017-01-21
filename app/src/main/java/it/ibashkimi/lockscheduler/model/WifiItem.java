package it.ibashkimi.lockscheduler.model;


import org.json.JSONException;
import org.json.JSONObject;

public class WifiItem {
    public final String SSID;

    public WifiItem(String SSID) {
        this.SSID = SSID;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof WifiItem))
            return false;
        WifiItem wifiItem = (WifiItem) obj;
        return wifiItem.SSID.equals(SSID);
    }

    @Override
    public String toString() {
        return "WiFi[" + SSID + "]";
    }

    public String toJson() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("ssid", SSID);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
    }

    public static WifiItem parseJson(String jsonRep) {
        try {
            JSONObject json = new JSONObject(jsonRep);
            return new WifiItem(json.getString("ssid"));
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }
}
