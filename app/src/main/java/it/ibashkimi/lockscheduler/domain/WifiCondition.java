package it.ibashkimi.lockscheduler.domain;

import android.net.wifi.WifiConfiguration;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;


public class WifiCondition extends Condition {

    private List<WifiConfiguration> networks;

    public WifiCondition(String name, List<WifiConfiguration> networks) {
        super(Type.WIFI, name);
        this.networks = networks;
    }

    public List<WifiConfiguration> getNetworks() {
        return networks;
    }

    public void setNetworks(List<WifiConfiguration> networks) {
        this.networks = networks;
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

    public static WifiCondition parseJson(String json) throws JSONException {
        JSONObject jsonObject = new JSONObject(json);
        int type = jsonObject.getInt("type");
        String name = jsonObject.getString("name");
        return new WifiCondition(name, null);
    }
}
