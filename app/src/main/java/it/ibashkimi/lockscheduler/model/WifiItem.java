package it.ibashkimi.lockscheduler.model;


import org.json.JSONException;
import org.json.JSONObject;

public class WifiItem {

    private final String ssid;

    public WifiItem(String ssid) {
        this.ssid = ssid;
    }

    public String getSsid() {
        return ssid;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof WifiItem))
            return false;
        WifiItem wifiItem = (WifiItem) obj;
        return wifiItem.ssid.equals(ssid);
    }

    @Override
    public String toString() {
        return "WiFi[" + ssid + "]";
    }

    public String toJson() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("ssid", ssid);
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
