package com.ibashkimi.lockscheduler.addeditprofile.conditions;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.transition.TransitionManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ibashkimi.lockscheduler.R;
import com.ibashkimi.lockscheduler.addeditprofile.conditions.picker.PlacePickerActivity;
import com.ibashkimi.lockscheduler.addeditprofile.conditions.picker.WifiPickerActivity;
import com.ibashkimi.lockscheduler.model.Condition;
import com.ibashkimi.lockscheduler.model.PlaceCondition;
import com.ibashkimi.lockscheduler.model.PowerCondition;
import com.ibashkimi.lockscheduler.model.TimeCondition;
import com.ibashkimi.lockscheduler.model.WifiCondition;
import com.ibashkimi.lockscheduler.model.WifiItem;
import com.ibashkimi.lockscheduler.model.prefs.AppPreferencesHelper;
import com.ibashkimi.lockscheduler.util.ConditionUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.app.Activity.RESULT_OK;
import static com.ibashkimi.lockscheduler.addeditprofile.conditions.PlaceConditionFragment.PLACE_PICKER_REQUEST;
import static com.ibashkimi.lockscheduler.model.Condition.Type.PLACE;
import static com.ibashkimi.lockscheduler.model.Condition.Type.POWER;
import static com.ibashkimi.lockscheduler.model.Condition.Type.TIME;
import static com.ibashkimi.lockscheduler.model.Condition.Type.WIFI;

public class ConditionsFragment extends Fragment {

    public static final int REQUEST_WIFI_PICKER = 5;

    @BindView(R.id.place_layout)
    ViewGroup placeLayout;

    @BindView(R.id.place_delete)
    View placeDelete;

    @BindView(R.id.time_layout)
    ViewGroup timeLayout;

    @BindView(R.id.time_delete)
    View timeDelete;

    @BindView(R.id.wifi_layout)
    ViewGroup wifiLayout;

    @BindView(R.id.wifi_body)
    View wifiBody;

    @BindView(R.id.wifi_delete)
    View wifiDelete;

    @BindView(R.id.networks_summary)
    TextView wifiSummary;

    @BindView(R.id.power_layout)
    View powerLayout;

    @BindView(R.id.power_summary)
    TextView powerSummary;

    @BindView(R.id.power_delete)
    View powerDelete;

    private FragmentManager fragmentManager;
    private List<Condition> conditions;
    private List<WifiItem> wifiItems;
    private boolean powerWhenConnected;

    private boolean placeConditionAdded = false;
    private boolean timeConditionAdded = false;
    private boolean wifiConditionAdded = false;
    private boolean powerConditionAdded = false;

    public static ConditionsFragment newInstance() {
        return new ConditionsFragment();
    }

    public void setData(List<Condition> conditions) {
        this.conditions = conditions;
    }

    public List<Condition> assembleConditions() {
        List<Condition> conditions = new ArrayList<>(3);
        if (placeConditionAdded) {
            PlaceCondition placeCondition = getPlaceConditionFragment().assembleCondition();
            if (placeCondition != null)
                conditions.add(placeCondition);
        }
        if (timeConditionAdded) {
            TimeCondition timeCondition = getTimeConditionFragment().assembleCondition();
            if (timeCondition != null)
                conditions.add(timeCondition);
        }
        if (wifiConditionAdded) {
            if (wifiItems != null && wifiItems.size() > 0)
                conditions.add(new WifiCondition(wifiItems));
        }
        if (powerConditionAdded) {
            conditions.add(new PowerCondition(powerWhenConnected));
        }
        return conditions;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fragmentManager = getChildFragmentManager();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_conditions, container, false);
        ButterKnife.bind(this, root);

        if (savedInstanceState == null) {
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            if (conditions != null) {
                for (Condition condition : conditions) {
                    switch (condition.getType()) {
                        case PLACE:
                            showPlaceCondition(fragmentTransaction, (PlaceCondition) condition);
                            placeDelete.setVisibility(View.VISIBLE);
                            break;
                        case TIME:
                            showTimeCondition(fragmentTransaction, (TimeCondition) condition);
                            timeDelete.setVisibility(View.VISIBLE);
                            break;
                        case WIFI:
                            showWifiCondition((WifiCondition) condition);
                            wifiDelete.setVisibility(View.VISIBLE);
                            break;
                        case POWER:
                            showPowerCondition((PowerCondition) condition);
                            powerDelete.setVisibility(View.VISIBLE);
                            break;
                    }
                }
            }
            fragmentTransaction.commit();
        } else {
            placeConditionAdded = savedInstanceState.getBoolean("place_added");
            timeConditionAdded = savedInstanceState.getBoolean("time_added");
            wifiConditionAdded = savedInstanceState.getBoolean("wifi_added");
            powerConditionAdded = savedInstanceState.getBoolean("power_added");
            if (savedInstanceState.containsKey("wifi_items_size")) {
                int size = savedInstanceState.getInt("wifi_items_size");
                wifiItems = new ArrayList<>(size);
                for (int i = 0; i < size; i++) {
                    String ssid = savedInstanceState.getString("wifi_item_" + i, null);
                    if (ssid == null)
                        throw new IllegalStateException("Cannot restore wifi item at position " + i + ".");
                    wifiItems.add(new WifiItem(ssid));
                }
                showWifiCondition(wifiItems);
            }
            powerWhenConnected = savedInstanceState.getBoolean("when_power_connected");
            showPowerCondition(new PowerCondition(powerWhenConnected));
        }
        return root;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("place_added", placeConditionAdded);
        outState.putBoolean("time_added", timeConditionAdded);
        outState.putBoolean("wifi_added", wifiConditionAdded);
        outState.putBoolean("power_added", powerConditionAdded);
        if (wifiItems != null) {
            for (int i = 0; i < wifiItems.size(); i++) {
                outState.putString("wifi_item_" + i, wifiItems.get(i).getSsid());
            }
            outState.putInt("wifi_items_size", wifiItems.size());
        }
        outState.putBoolean("when_power_connected", powerWhenConnected);
    }

    public void showPlacePicker() {
        Intent intent = new Intent(getContext(), PlacePickerActivity.class);
        intent.putExtra("radius", 300);
        startActivityForResult(intent, PLACE_PICKER_REQUEST);
    }

    public void showPlacePicker(@NonNull PlaceCondition placeCondition) {
        Intent intent = new Intent(getContext(), PlacePickerActivity.class);
        intent.putExtra("latitude", placeCondition.getLatitude());
        intent.putExtra("longitude", placeCondition.getLongitude());
        intent.putExtra("radius", placeCondition.getRadius());
        intent.putExtra("map_type", AppPreferencesHelper.INSTANCE.getMapStyle());
        startActivityForResult(intent, PLACE_PICKER_REQUEST);
    }

    public void showWifiPicker(List<WifiItem> items) {
        Intent intent = new Intent(getContext(), WifiPickerActivity.class);

        if (items != null && items.size() > 0) {
            String[] itemReps = new String[items.size()];
            for (int i = 0; i < items.size(); i++) {
                itemReps[i] = items.get(i).getSsid();
            }
            intent.putExtra("ssids", itemReps);
        }
        startActivityForResult(intent, REQUEST_WIFI_PICKER);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                double latitude = data.getDoubleExtra("latitude", 0);
                double longitude = data.getDoubleExtra("longitude", 0);
                float radius = data.getFloatExtra("radius", 0);
                String address = data.getStringExtra("address");

                PlaceCondition placeCondition = new PlaceCondition(latitude, longitude, (int) radius, address);

                FragmentTransaction transaction = fragmentManager.beginTransaction();
                showPlaceCondition(transaction, placeCondition);
                //http://stackoverflow.com/questions/7469082/getting-exception-illegalstateexception-can-not-perform-this-action-after-onsa
                transaction.commitAllowingStateLoss();

                placeDelete.setVisibility(View.VISIBLE);
            }
        } else if (requestCode == REQUEST_WIFI_PICKER) {
            if (resultCode == RESULT_OK) {
                String[] ssidArray = data.getStringArrayExtra("ssids");
                wifiItems = new ArrayList<>(ssidArray.length);
                for (String ssid : ssidArray) wifiItems.add(new WifiItem(ssid));
                if (wifiItems.size() > 0)
                    showWifiCondition(wifiItems);
                else
                    removeWifiCondition();
            }
        }
    }

    private void showWifiCondition(List<WifiItem> networks) {
        CharSequence[] wifiList = new CharSequence[networks.size()];
        for (int i = 0; i < wifiList.length; i++) wifiList[i] = networks.get(i).getSsid();
        wifiSummary.setText(ConditionUtils.concatenate(wifiList, ", "));
        wifiBody.setVisibility(View.VISIBLE);
        wifiDelete.setVisibility(View.VISIBLE);
    }

    @OnClick(R.id.place_layout)
    public void onPlaceLayoutClick() {
        placeConditionAdded = true;
        TransitionManager.beginDelayedTransition(placeLayout);
        showPlacePicker();
    }

    @OnClick(R.id.place_delete)
    public void onPlaceDeleteClick() {
        TransitionManager.beginDelayedTransition(placeLayout);
        placeConditionAdded = false;
        fragmentManager.beginTransaction().remove(getPlaceConditionFragment()).commit();
        placeDelete.setVisibility(View.GONE);
    }

    @OnClick(R.id.time_layout)
    public void onTimeLayoutClick() {
        timeConditionAdded = true;
        TransitionManager.beginDelayedTransition(placeLayout);
        fragmentManager
                .beginTransaction()
                .replace(R.id.time_condition_container, getTimeConditionFragment(), "time_condition")
                .commit();
        timeDelete.setVisibility(View.VISIBLE);
    }

    @OnClick(R.id.time_delete)
    public void onTimeDeleteClick() {
        TransitionManager.beginDelayedTransition(placeLayout);
        timeConditionAdded = false;
        fragmentManager.beginTransaction().remove(getTimeConditionFragment()).commit();
        timeDelete.setVisibility(View.GONE);
    }

    @OnClick({R.id.wifi_layout, R.id.wifi_body})
    public void onWifiLayoutClicked() {
        wifiConditionAdded = true;
        TransitionManager.beginDelayedTransition(placeLayout);
        showWifiPicker(wifiItems);
    }

    @OnClick(R.id.wifi_delete)
    public void removeWifiCondition() {
        TransitionManager.beginDelayedTransition(placeLayout);
        wifiConditionAdded = false;
        //fragmentManager.beginTransaction().remove(getWifiConditionFragment()).commit();
        wifiDelete.setVisibility(View.GONE);
        wifiBody.setVisibility(View.GONE);
        wifiItems = null;
    }

    @OnClick(R.id.power_layout)
    public void onPowerLayoutClicked() {
        showPowerConditionDialog();
    }

    @OnClick(R.id.power_delete)
    public void removePowerCondition() {
        powerConditionAdded = false;
        powerDelete.setVisibility(View.GONE);
        powerSummary.setVisibility(View.GONE);
    }

    private void showPlaceCondition(FragmentTransaction transaction, PlaceCondition condition) {
        placeConditionAdded = true;
        PlaceConditionFragment fragment = getPlaceConditionFragment();
        fragment.setData(condition);
        transaction.replace(R.id.place_condition_container, fragment, "place_condition");
    }

    private void showTimeCondition(FragmentTransaction transaction, TimeCondition condition) {
        timeConditionAdded = true;
        TimeConditionFragment fragment = getTimeConditionFragment();
        fragment.setData(condition);
        transaction.replace(R.id.time_condition_container, fragment, "time_condition");
    }

    private void showWifiCondition(WifiCondition condition) {
        wifiConditionAdded = true;
        wifiItems = condition.getWifiList();
        showWifiCondition(wifiItems);
    }

    private void showPowerCondition(PowerCondition condition) {
        powerSummary.setText(condition.getPowerConnected() ? R.string.power_connected : R.string.power_disconnected);
        powerSummary.setVisibility(View.VISIBLE);
        powerDelete.setVisibility(View.VISIBLE);
        powerConditionAdded = true;
    }

    private void showPowerConditionDialog() {
        String[] items = getResources().getStringArray(R.array.power_state);
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.power_condition_title)
                .setSingleChoiceItems(items, powerWhenConnected ? 0 : 1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        powerWhenConnected = which == 0;
                        showPowerCondition(new PowerCondition(powerWhenConnected));
                        dialog.dismiss();
                    }
                })
                .create().show();
    }

    private PlaceConditionFragment getPlaceConditionFragment() {
        PlaceConditionFragment fragment = (PlaceConditionFragment) fragmentManager.findFragmentByTag("place_condition");
        if (fragment == null) {
            fragment = new PlaceConditionFragment();
        }
        return fragment;
    }

    private TimeConditionFragment getTimeConditionFragment() {
        TimeConditionFragment fragment = (TimeConditionFragment) fragmentManager.findFragmentByTag("time_condition");
        if (fragment == null) {
            fragment = new TimeConditionFragment();
        }
        return fragment;
    }
}
