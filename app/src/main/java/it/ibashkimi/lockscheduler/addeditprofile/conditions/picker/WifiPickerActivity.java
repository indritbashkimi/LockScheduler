package it.ibashkimi.lockscheduler.addeditprofile.conditions.picker;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;

import java.util.ArrayList;
import java.util.List;

import it.ibashkimi.lockscheduler.R;
import it.ibashkimi.lockscheduler.model.WifiItem;
import it.ibashkimi.lockscheduler.ui.BaseActivity;


public class WifiPickerActivity extends BaseActivity implements WifiInfoProvider {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_picker);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_cancel_toolbar);
        }

        int[] ids = getIntent().getIntArrayExtra("network_ids");
        String[] ssids = getIntent().getStringArrayExtra("ssids");
        List<WifiItem> selectedItems = new ArrayList<>(ids.length);
        for (int i = 0; i < ids.length; i++)
            selectedItems.add(new WifiItem(ids[i], ssids[i]));

        WifiPickerFragment fragment = (WifiPickerFragment)
                getSupportFragmentManager().findFragmentByTag(WifiPickerFragment.class.getName());
        if (fragment == null) {
            fragment = WifiPickerFragment.newInstance();
        }
        WifiPickerPresenter presenter = new WifiPickerPresenter(this, fragment, selectedItems);
        fragment.setPresenter(presenter);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.wifi_picker_container, fragment, fragment.getClass().getName())
                .commit();
    }

    @Override
    public void getWifiList(Callback callback) {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiManager.isWifiEnabled()) {
            ArrayList<WifiItem> wifiList = new ArrayList<>();
            for (WifiConfiguration wifiConfiguration : wifiManager.getConfiguredNetworks()) {
                String title = wifiConfiguration.SSID;
                title = title.substring(1, title.length() - 1); // Remove " at the start and end.
                wifiList.add(new WifiItem(wifiConfiguration.networkId, title));
                callback.onDataLoaded(wifiList);
            }
        } else {
            callback.onDataNotAvailable();
        }
    }
}
