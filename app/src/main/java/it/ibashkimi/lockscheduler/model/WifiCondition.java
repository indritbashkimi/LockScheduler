package it.ibashkimi.lockscheduler.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class WifiCondition extends Condition {

    private static final String TAG = "WifiCondition";

    private List<WifiItem> wifiItemList;

    public WifiCondition() {
        this(new ArrayList<WifiItem>());
    }

    private WifiCondition(ArrayList<WifiItem> items) {
        super(Type.WIFI);
        this.wifiItemList = items;
    }

    public void add(WifiItem wifi) {
        this.wifiItemList.add(wifi);
    }

    public void remove(WifiItem wifi) {
        this.wifiItemList.remove(wifi);
    }

    public List<WifiItem> getNetworks() {
        return wifiItemList;
    }

    public void setNetworks(List<WifiItem> networks) {
        this.wifiItemList = networks;
    }

    public boolean isPresent(WifiItem wifiItem) {
        for (WifiItem item : wifiItemList)
            if (item.equals(wifiItem))
                return true;
        return false;
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
        boolean isTrue = jsonObject.getBoolean("true");
        int wifiItemSize = jsonObject.getInt("wifi_items_len");
        ArrayList<WifiItem> items = new ArrayList<>(wifiItemSize);
        for (int i = 0; i < wifiItemSize; i++) {
            String wifiItemJson = jsonObject.getString("wifi_item_" + i);
            WifiItem item = WifiItem.parseJson(wifiItemJson);
            items.add(item);
        }
        WifiCondition wifiCondition = new WifiCondition(items);
        wifiCondition.setTrue(isTrue);
        return wifiCondition;
    }
}
