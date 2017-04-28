package it.ibashkimi.lockscheduler.addeditprofile.conditions.picker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import it.ibashkimi.lockscheduler.R;
import it.ibashkimi.lockscheduler.model.WifiItem;
import it.ibashkimi.lockscheduler.ui.BaseActivity;
import it.ibashkimi.lockscheduler.ui.recyclerview.SelectableAdapter;


public class WifiPickerActivity extends BaseActivity implements WifiInfoProvider {
    private static final String TAG = "WifiPickerActivity";

    @BindView(R.id.turn_on_wifi)
    TextView turnOnWifi;

    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;

    private SparseBooleanArray sparseArray;

    private List<WifiItem> wifiItems;
    private WifiAdapter wifiAdapter;

    private List<WifiItem> userSelected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_picker);
        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_cancel_toolbar);
        }

        sparseArray = new SparseBooleanArray();
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if (extras != null && extras.containsKey("items")) {
                Log.d(TAG, "onCreate: contains items key");
                String[] items = extras.getStringArray("items");
                if (items != null) {
                    userSelected = new ArrayList<>(items.length);
                    Log.d(TAG, "onCreate: items.length = " + items.length);
                    for (String itemRep : items) {
                        userSelected.add(WifiItem.parseJson(itemRep));
                    }
                    Log.d(TAG, "onCreate: userSelected.size = " + userSelected.size());
                } else {
                    Log.d(TAG, "onCreate: items == null");
                }
            } else {
                Log.d(TAG, "onCreate: extras doesn't contain items key");
            }
        }

        wifiItems = new ArrayList<>();
        wifiAdapter = new WifiAdapter(wifiItems, sparseArray);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setAdapter(wifiAdapter);
        /*load();
        int[] selectedNetworks;
        if (savedInstanceState == null) {
            if (getIntent().getExtras() != null) {
                selectedNetworks = getIntent().getIntArrayExtra("network_ids");
                initSelectedNetworkList(selectedNetworks);
            } else {
                sparseArray.clear();
            }
            load();
        } else {
            selectedNetworks = savedInstanceState.getIntArray("network_ids");
            initSelectedNetworkList(selectedNetworks);
        }*/
    }


    private void initSelectedNetworkList(int[] selectedNetworks) {
        sparseArray.clear();
        for (int network : selectedNetworks)
            sparseArray.append(network, true);
    }

    private BroadcastReceiver wifiBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            load();
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        registerReceiver(wifiBroadcastReceiver, intentFilter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(wifiBroadcastReceiver);
    }

    private void showTurnOnWifi() {
        recyclerView.setVisibility(View.GONE);
        turnOnWifi.setVisibility(View.VISIBLE);
    }

    private void showWifiList() {
        turnOnWifi.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);

        wifiAdapter.notifyDataSetChanged();
    }

    private void load() {
        getWifiList(new Callback() {
            @Override
            public void onDataLoaded(List<WifiItem> items) {
                wifiItems.clear();
                wifiItems.addAll(items);
                if (userSelected != null) {
                    for (WifiItem userItem : userSelected) {
                        if (!wifiItems.contains(userItem)) {
                            wifiItems.add(userItem);
                        }
                        sparseArray.put(userItem.networkId, true);
                    }
                    for (int i = 0; i < sparseArray.size(); i++) {
                        wifiAdapter.toggleSelection(sparseArray.keyAt(i));
                    }
                    userSelected = null;
                }
                showWifiList();
            }

            @Override
            public void onDataNotAvailable() {
                showTurnOnWifi();
            }
        });
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
            }
            callback.onDataLoaded(wifiList);
        } else {
            callback.onDataNotAvailable();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onCancel();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @OnClick(R.id.fab)
    public void onSave() {
        String[] itemReps = new String[sparseArray.size()];
        for (int i = 0; i < sparseArray.size(); i++) {
            itemReps[i] = wifiItems.get(sparseArray.keyAt(i)).toJson();
        }

        Log.d(TAG, "onSave: itemReps.size = " + itemReps.length);

        Intent intent = new Intent();
        intent.putExtra("items", itemReps);
        setResult(RESULT_OK, intent);
        finish();
    }

    public void onCancel() {
        setResult(RESULT_CANCELED);
        finish();
    }

    class WifiAdapter extends SelectableAdapter<WifiAdapter.ViewHolder> {
        private List<WifiItem> wifiList;
        private SparseBooleanArray selectedNetworks;

        public WifiAdapter(List<WifiItem> wifiList, SparseBooleanArray selectedNetworks) {
            super();
            this.wifiList = wifiList;
            this.selectedNetworks = selectedNetworks;
        }

        public List<WifiItem> getWifiList() {
            return wifiList;
        }

        public void setWifiList(List<WifiItem> wifiList) {
            this.wifiList = wifiList;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.
                    from(parent.getContext()).
                    inflate(R.layout.item_wifi_connection, parent, false);
            return new ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            final WifiItem wifiItem = wifiList.get(position);
            holder.title.setText(wifiItem.SSID);
            holder.checkBox.setChecked(selectedNetworks.get(wifiList.get(position).networkId));
            holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    selectedNetworks.put(wifiList.get(holder.getAdapterPosition()).networkId, isChecked);
                }
            });
            holder.rootView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    holder.checkBox.performClick();
                }
            });
        }

        @Override
        public int getItemCount() {
            return wifiList.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            View rootView;
            TextView title;
            CheckBox checkBox;

            public ViewHolder(View itemView) {
                super(itemView);
                rootView = itemView;
                title = (TextView) itemView.findViewById(R.id.title);
                checkBox = (CheckBox) itemView.findViewById(R.id.checkbox);
            }
        }
    }
}
