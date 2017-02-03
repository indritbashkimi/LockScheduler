package it.ibashkimi.lockscheduler.addeditprofile.conditions.picker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.List;

import it.ibashkimi.lockscheduler.R;
import it.ibashkimi.lockscheduler.addeditprofile.WifiAdapter;
import it.ibashkimi.lockscheduler.model.WifiItem;
import it.ibashkimi.lockscheduler.model.api.WifiListProvider;


public class WifiPickerFragment extends Fragment implements WifiPickerContract.View {

    private static final String TAG = "WifiPickerFragment";

    WifiAdapter wifiAdapter;

    List<WifiItem> wifiItems;

    RecyclerView recyclerView;

    private BroadcastReceiver wifiBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Toast.makeText(getContext(), "onReceive", Toast.LENGTH_LONG).show();
            if (isWifiEnabled()) {
                showWifiList();
            } else {
                showTurnOnWifi();
            }
        }
    };
    private WifiPickerContract.Presenter presenter;

    public void setWifiItems(List<WifiItem> items) {
        this.wifiItems = items;
    }

    public List<WifiItem> getItems() {
        return wifiItems;
    }

    public List<WifiItem> getSelectedItems() {
        return null;
    }

    public static WifiPickerFragment newInstance() {

        Bundle args = new Bundle();

        WifiPickerFragment fragment = new WifiPickerFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_wifi_picker, container, false);

        recyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView);
        if (isWifiEnabled()) {
            recyclerView.setVisibility(View.VISIBLE);
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                    LinearLayoutManager.VERTICAL);
            recyclerView.addItemDecoration(dividerItemDecoration);

            //
        } else {
            recyclerView.setVisibility(View.GONE);
            rootView.findViewById(R.id.turn_on_wifi).setVisibility(View.VISIBLE);
        }

        return rootView;
    }

    @Override
    public void onPause() {
        getContext().unregisterReceiver(wifiBroadcastReceiver);
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        getContext().registerReceiver(wifiBroadcastReceiver, intentFilter);
    }

    private void showTurnOnWifi() {
        recyclerView.setVisibility(View.GONE);
        getView().findViewById(R.id.turn_on_wifi).setVisibility(View.VISIBLE);
    }

    private void showWifiList() {
        getView().findViewById(R.id.turn_on_wifi).setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);

        refreshList();
        wifiAdapter = new WifiAdapter(wifiItems, new WifiAdapter.Callbacks() {
            @Override
            public void onWifiItemSelectChange(WifiItem item, boolean selected) {
                presenter.setSelected(item, selected);
            }
        });
        recyclerView.setAdapter(wifiAdapter);
    }

    private boolean isWifiEnabled() {
        WifiManager wifi = (WifiManager) getContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        return wifi.isWifiEnabled();
    }

    private void refreshList() {
        final List<WifiItem> availableWifiItems = WifiListProvider.getNetworks(getContext());
        /*final List<WifiItem> conditionWifiItems = wifiItems;

        for (WifiItem item : conditionWifiItems) {
            for (int i = 0; i < availableWifiItems.size(); i++) {
                if (availableWifiItems.get(i).equals(item)) {
                    availableWifiItems.remove(i);
                    break;
                }
            }
        }
        if (wifiItems == null)
            wifiItems = new ArrayList<>(conditionWifiItems.size() + availableWifiItems.size());
        else
            wifiItems.clear();
        wifiItems.addAll(conditionWifiItems);
        wifiItems.addAll(availableWifiItems);*/
        wifiItems.clear();
        wifiItems.addAll(availableWifiItems);
    }

    @Override
    public void setPresenter(WifiPickerContract.Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void showWifiList(List<WifiItem> wifiItems, int selectedItems) {

    }

    @Override
    public void setSelected(int item, boolean selected) {

    }

    @Override
    public void showWiFiDisabled() {

    }
}
