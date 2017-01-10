package it.ibashkimi.lockscheduler.domain;

import android.content.Context;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import it.ibashkimi.lockscheduler.App;


public class WifiCondition extends Condition {

    private static final String TAG = "WifiCondition";

    private List<WifiItem> wifiItemList;

    public WifiCondition(String name) {
        this(name, new ArrayList<WifiItem>());
    }

    private WifiCondition(String name, ArrayList<WifiItem> items) {
        super(Type.WIFI, name);
        this.wifiItemList = items;
    }

    public void addWifi(WifiItem wifi) {
        this.wifiItemList.add(wifi);
    }

    public void removeWifi(WifiItem wifi) {
        this.wifiItemList.remove(wifi);
    }

    public List<WifiItem> getNetworks() {
        return wifiItemList;
    }

    public void update(Context context) {

    }

    public boolean isPresent(WifiItem wifiItem) {
        for (WifiItem item : wifiItemList)
            if (item.equals(wifiItem))
                return true;
        return false;
    }

    public void onWifiStateChanged(WifiItem wifiItem) {
        Log.d(TAG, "onWifiStateChanged() called with: wifiItem = " + wifiItem);
        setTrue(wifiItem != null && isPresent(wifiItem));
    }

    public static void onNetworkStateChanged(Context context, NetworkInfo info) {
        boolean interesting = true;
        WifiInfo wifiInfo = null;
        if (info == null || !info.isConnected()) {
            interesting = false;
        } else {
            WifiManager wifiManager = (WifiManager) App.getInstance().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            if (wifiManager == null) {
                interesting = false;
            } else {
                wifiInfo = wifiManager.getConnectionInfo();
                if (wifiInfo == null) {
                    interesting = false;
                }
            }
        }
        List<Profile> profiles = App.getProfileApiHelper().getProfiles();
        if (interesting) {
            String ssid = wifiInfo.getSSID();
            WifiItem wifiItem = new WifiItem(ssid);
            for (Profile profile : profiles) {
                WifiCondition wifiCondition = profile.getWifiCondition();
                if (wifiCondition != null && wifiCondition.isPresent(wifiItem)) {
                    //boolean previouslyActive = profile.isActive();
                    profile.setConditionState(Condition.Type.WIFI, true);
                }
            }
        } else {
            for (Profile profile : profiles) {
                WifiCondition wifiCondition = profile.getWifiCondition();
                if (wifiCondition != null) {
                    profile.setConditionState(Condition.Type.WIFI, false);
                }
            }
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof WifiCondition))
            return false;
        WifiCondition condition = (WifiCondition) obj;
        if (wifiItemList.size() != condition.getNetworks().size())
            return false;
        // TODO: 09/01/17 this has to be and ordered list!
        for (int i = 0; i < wifiItemList.size(); i++) {
            if (!wifiItemList.get(i).equals(condition.getNetworks().get(i)))
                return false;
        }
        return super.equals(obj);
    }

    @Override
    public String toJson() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("type", getType());
            jsonObject.put("name", getName());
            jsonObject.put("true", isTrue());
            jsonObject.put("wifi_items_len", wifiItemList.size());
            for (int i = 0; i < wifiItemList.size(); i++) {
                jsonObject.put("wifi_item_" + i, wifiItemList.get(i).toJson());
            }
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
        boolean isTrue = jsonObject.getBoolean("true");
        int wifiItemSize = jsonObject.getInt("wifi_items_len");
        ArrayList<WifiItem> items = new ArrayList<>(wifiItemSize);
        for (int i = 0; i < wifiItemSize; i++) {
            String wifiItemJson = jsonObject.getString("wifi_item_" + i);
            WifiItem item = WifiItem.parseJson(wifiItemJson);
            items.add(item);
        }
        WifiCondition wifiCondition = new WifiCondition(name, items);
        wifiCondition.setTrue(isTrue);
        return wifiCondition;
    }
}
