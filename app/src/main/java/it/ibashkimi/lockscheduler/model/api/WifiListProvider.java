package it.ibashkimi.lockscheduler.model.api;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;

import java.util.ArrayList;
import java.util.List;

import it.ibashkimi.lockscheduler.model.WifiItem;


public class WifiListProvider {

    public static List<WifiItem> getNetworks(Context context) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        ArrayList<WifiItem> wifiList = new ArrayList<>();
        if (wifiManager.isWifiEnabled()) {
            for (WifiConfiguration wifiConfiguration : wifiManager.getConfiguredNetworks()) {
                String ssid = wifiConfiguration.SSID;
                ssid = ssid.substring(1, ssid.length() - 1); // Remove " at the start and end.
                wifiList.add(new WifiItem(ssid));
            }
        }
        return wifiList;
    }
}
