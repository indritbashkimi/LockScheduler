package it.ibashkimi.lockscheduler.model;

import java.util.ArrayList;
import java.util.List;


public class WifiCondition extends Condition {

    private static final String TAG = "WifiCondition";

    private List<WifiItem> wifiItemList;

    public WifiCondition() {
        this(new ArrayList<WifiItem>());
    }

    public WifiCondition(ArrayList<WifiItem> items) {
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
}
