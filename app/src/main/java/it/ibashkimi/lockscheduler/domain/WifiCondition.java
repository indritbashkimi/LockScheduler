package it.ibashkimi.lockscheduler.domain;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class WifiCondition extends Condition {

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

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof WifiCondition))
            return false;
        WifiCondition condition = (WifiCondition) obj;
        if (wifiItemList.size() != condition.getNetworks().size())
            return false;
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
        int wifiItemSize = jsonObject.getInt("wifi_items_len");
        ArrayList<WifiItem> items = new ArrayList<>(wifiItemSize);
        for (int i = 0; i < wifiItemSize; i++) {
            String wifiItemJson = jsonObject.getString("wifi_item_" + i);
            WifiItem item = WifiItem.parseJson(wifiItemJson);
            items.add(item);
        }
        return new WifiCondition(name, items);
    }
}
