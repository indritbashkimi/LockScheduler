package com.ibashkimi.lockscheduler.model;

import java.util.ArrayList;
import java.util.List;

public class WifiCondition extends Condition {

    private List<WifiItem> wifiList;

    public WifiCondition() {
        this(new ArrayList<WifiItem>());
    }

    public WifiCondition(ArrayList<WifiItem> wifiList) {
        super(Type.WIFI);
        this.wifiList = wifiList;
    }

    public void add(WifiItem wifi) {
        if (!wifiList.contains(wifi))
            wifiList.add(wifi);
    }

    public void remove(WifiItem wifi) {
        wifiList.remove(wifi);
    }

    public List<WifiItem> getWifiList() {
        return wifiList;
    }

    public void setWifiList(List<WifiItem> wifiList) {
        this.wifiList = wifiList;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        else if (!(obj instanceof WifiCondition))
            return false;
        else {
            WifiCondition condition = (WifiCondition) obj;
            if (wifiList.size() != condition.getWifiList().size())
                return false;
            for (int i = 0; i < wifiList.size(); i++) {
                if (!wifiList.get(i).equals(condition.getWifiList().get(i)))
                    return false;
            }
            return true;
        }
    }
}
