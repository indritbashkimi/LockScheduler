package it.ibashkimi.lockscheduler.model;


import org.json.JSONException;
import org.json.JSONObject;

public class WifiItem {

    public final int networkId;

    public final String SSID;

    public WifiItem(int networkId, String SSID) {
        this.networkId = networkId;
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
        return "WiFi[" + networkId + ", " + SSID + "]";
    }

    public String toJson() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("networkId", networkId);
            jsonObject.put("ssid", SSID);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
    }

    public static WifiItem parseJson(String jsonRep) {
        try {
            JSONObject json = new JSONObject(jsonRep);
            return new WifiItem(json.getInt("networkId"), json.getString("ssid"));
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }
}
