package com.ibashkimi.lockscheduler.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.ibashkimi.lockscheduler.App;
import com.ibashkimi.lockscheduler.model.ProfileManager;
import com.ibashkimi.lockscheduler.model.condition.WifiItem;


public class WifiReceiver extends BroadcastReceiver {

    private static final String TAG = "WifiReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if ("android.net.wifi.STATE_CHANGE".equals(intent.getAction())) {
            WifiItem wifiItem = null;
            NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            if (info != null && info.isConnected()) {
                WifiManager wifiManager = (WifiManager) App.getInstance().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                if (wifiManager != null) {
                    WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                    if (wifiInfo != null) {
                        String ssid = wifiInfo.getSSID();
                        wifiItem = new WifiItem(ssid.substring(1, ssid.length() - 1));
                    }

                }
            }
            ProfileManager.INSTANCE.getWifiHandler().onWifiChanged(wifiItem);
        } else {
            Log.w(TAG, "Unhandled action: " + intent.getAction());
        }
    }
}
