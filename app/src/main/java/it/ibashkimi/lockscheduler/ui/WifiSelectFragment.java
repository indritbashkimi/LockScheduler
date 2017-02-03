package it.ibashkimi.lockscheduler.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import it.ibashkimi.lockscheduler.R;
import it.ibashkimi.lockscheduler.addeditprofile.WifiAdapter;
import it.ibashkimi.lockscheduler.model.Profile;
import it.ibashkimi.lockscheduler.model.WifiCondition;
import it.ibashkimi.lockscheduler.model.WifiItem;
import it.ibashkimi.lockscheduler.model.api.WifiListProvider;


public class WifiSelectFragment extends Fragment {
    WifiCondition wifiCondition;
    WifiAdapter wifiAdapter;
    ArrayList<WifiItem> wifiItems;
    RecyclerView recyclerView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Profile profile = null;// ((ProfileActivity) getActivity()).getProfile();
        wifiCondition = new WifiCondition("Wifi");
        profile.getConditions().add(wifiCondition);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_wifi_selector, container, false);
        Toolbar toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        recyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                LinearLayoutManager.VERTICAL);
        recyclerView.addItemDecoration(dividerItemDecoration);

        refreshList();
        wifiAdapter = new WifiAdapter(wifiItems, new WifiAdapter.Callbacks() {
            @Override
            public void onWifiItemSelectChange(WifiItem item, boolean selected) {
                //item.selected = selected;
                if (selected)
                    wifiCondition.addWifi(item);
                else
                    wifiCondition.removeWifi(item);
            }
        });
        recyclerView.setAdapter(wifiAdapter);
        return rootView;
    }

    public WifiCondition getWifiCondition() {
        return wifiCondition;
    }

    public void setWifiCondition(WifiCondition wifiCondition) {
        this.wifiCondition = wifiCondition;
        //refreshList();
    }

    private void refreshList() {
        final List<WifiItem> availableWifiItems = WifiListProvider.getNetworks(getContext());
        final List<WifiItem> conditionWifiItems = wifiCondition.getNetworks();

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
        wifiItems.addAll(availableWifiItems);
    }
}
