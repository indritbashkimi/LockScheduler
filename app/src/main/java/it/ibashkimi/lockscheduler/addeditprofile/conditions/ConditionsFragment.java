package it.ibashkimi.lockscheduler.addeditprofile.conditions;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.transition.TransitionManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import it.ibashkimi.lockscheduler.R;
import it.ibashkimi.lockscheduler.addeditprofile.conditions.picker.PlacePickerActivity;
import it.ibashkimi.lockscheduler.addeditprofile.conditions.picker.WifiPickerActivity;
import it.ibashkimi.lockscheduler.model.Condition;
import it.ibashkimi.lockscheduler.model.PlaceCondition;
import it.ibashkimi.lockscheduler.model.TimeCondition;
import it.ibashkimi.lockscheduler.model.WifiCondition;
import it.ibashkimi.lockscheduler.model.WifiItem;
import it.ibashkimi.lockscheduler.util.ConditionUtils;

import static android.app.Activity.RESULT_OK;
import static it.ibashkimi.lockscheduler.addeditprofile.conditions.PlaceConditionFragment.PLACE_PICKER_REQUEST;
import static it.ibashkimi.lockscheduler.addeditprofile.conditions.WifiConditionFragment.REQUEST_WIFI_PICKER;
import static it.ibashkimi.lockscheduler.model.Condition.Type.PLACE;
import static it.ibashkimi.lockscheduler.model.Condition.Type.TIME;
import static it.ibashkimi.lockscheduler.model.Condition.Type.WIFI;


public class ConditionsFragment extends Fragment {

    private static final String TAG = "ConditionsFragment";

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

    private FragmentManager fragmentManager;
    private List<Condition> conditions;
    private List<WifiItem> wifiItems;

    private boolean placeConditionAdded = false;
    private boolean timeConditionAdded = false;
    private boolean wifiConditionAdded = false;

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
            if (wifiItems != null && wifiItems.size() > 0) {
                WifiCondition wifiCondition = new WifiCondition();
                wifiCondition.setNetworks(wifiItems);
                conditions.add(wifiCondition);
            }
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
                    }
                }
            }
            fragmentTransaction.commit();
        } else {
            placeConditionAdded = savedInstanceState.getBoolean("place_added");
            timeConditionAdded = savedInstanceState.getBoolean("time_added");
            wifiConditionAdded = savedInstanceState.getBoolean("wifi_added");
            if (savedInstanceState.containsKey("wifi_items_size")) {
                int size = savedInstanceState.getInt("wifi_items_size");
                wifiItems = new ArrayList<>(size);
                for (int i = 0; i < size; i++)
                    wifiItems.add(WifiItem.parseJson(savedInstanceState.getString("wifi_item_" + i)));
                showWifiCondition(wifiItems);
            }
        }

        return root;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("place_added", placeConditionAdded);
        outState.putBoolean("time_added", timeConditionAdded);
        outState.putBoolean("wifi_added", wifiConditionAdded);
        if (wifiItems != null) {
            for (int i = 0; i < wifiItems.size(); i++) {
                outState.putString("wifi_item_" + i, wifiItems.get(i).toJson());
            }
            outState.putInt("wifi_items_size", wifiItems.size());
        }
    }

    public void showPlacePicker() {
        Intent intent = new Intent(getContext(), PlacePickerActivity.class);
        intent.putExtra("radius", 300);
        startActivityForResult(intent, PLACE_PICKER_REQUEST);
    }

    public void showPlacePicker(@NonNull PlaceCondition placeCondition) {
        Intent intent = new Intent(getContext(), PlacePickerActivity.class);
        intent.putExtra("latitude", placeCondition.getPlace().latitude);
        intent.putExtra("longitude", placeCondition.getPlace().longitude);
        intent.putExtra("radius", placeCondition.getRadius());
        intent.putExtra("map_type", getContext().getSharedPreferences("settings", Context.MODE_PRIVATE)
                .getString("map_style", "normal"));
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

                PlaceCondition placeCondition = new PlaceCondition(new LatLng(latitude, longitude), (int) radius);
                placeCondition.setAddress(address);

                FragmentTransaction transaction = fragmentManager.beginTransaction();
                showPlaceCondition(transaction, placeCondition);
                //http://stackoverflow.com/questions/7469082/getting-exception-illegalstateexception-can-not-perform-this-action-after-onsa
                transaction.commitAllowingStateLoss();

                placeDelete.setVisibility(View.VISIBLE);
            }
        } else if (requestCode == REQUEST_WIFI_PICKER) {
            if (resultCode == RESULT_OK) {
                String[] ssids = data.getStringArrayExtra("ssids");
                wifiItems = new ArrayList<>(ssids.length);
                for (String ssid : ssids) wifiItems.add(new WifiItem(ssid));
                showWifiCondition(wifiItems);
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
    public void onWifiDeleteClicked() {
        TransitionManager.beginDelayedTransition(placeLayout);
        wifiConditionAdded = false;
        //fragmentManager.beginTransaction().remove(getWifiConditionFragment()).commit();
        wifiDelete.setVisibility(View.GONE);
        wifiBody.setVisibility(View.GONE);
        wifiItems = null;
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
        wifiItems = condition.getNetworks();
        showWifiCondition(wifiItems);
    }

    private void showWifiCondition(FragmentTransaction transaction, WifiCondition condition) {
        WifiConditionFragment fragment = getWifiConditionFragment();
        fragment.setData(condition);
        //transaction.replace(R.id.wifi_condition_container, fragment, "wifi_condition");
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

    private WifiConditionFragment getWifiConditionFragment() {
        WifiConditionFragment fragment = (WifiConditionFragment) fragmentManager.findFragmentByTag("wifi_condition");
        if (fragment == null) {
            fragment = new WifiConditionFragment();
        }
        return fragment;
    }
}
