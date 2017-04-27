package it.ibashkimi.lockscheduler.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.util.List;

import it.ibashkimi.lockscheduler.App;
import it.ibashkimi.lockscheduler.model.Profile;
import it.ibashkimi.lockscheduler.model.WifiCondition;
import it.ibashkimi.lockscheduler.model.WifiItem;
import it.ibashkimi.lockscheduler.model.source.ProfilesRepository;


public class WifiReceiver extends BroadcastReceiver {

    private static final String TAG = "WifiReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive() called with: context = [" + context + "], intent = [" + intent + "]");
        WifiItem wifiItem = null;
        NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
        if (info != null && info.isConnected()) {
            WifiManager wifiManager = (WifiManager) App.getInstance().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            if (wifiManager != null) {
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                if (wifiInfo != null) {
                    String ssid = wifiInfo.getSSID();
                    wifiItem = new WifiItem(wifiInfo.getNetworkId(), ssid.substring(1, ssid.length() - 1));
                }

            }
        }
        List<Profile> profiles = ProfilesRepository.getInstance().getProfiles();
        for (Profile profile : profiles) {
            WifiCondition condition = profile.getWifiCondition();
            if (condition != null) {
                condition.onWifiStateChanged(wifiItem);
                profile.notifyConditionChanged(condition);
                ProfilesRepository.getInstance().updateProfile(profile);
            }
        }
    }
}
