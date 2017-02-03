package it.ibashkimi.lockscheduler.addeditprofile.conditions;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;

import java.util.ArrayList;
import java.util.List;

import it.ibashkimi.lockscheduler.R;
import it.ibashkimi.lockscheduler.addeditprofile.conditions.picker.TimePickerFragment;
import it.ibashkimi.lockscheduler.addeditprofile.conditions.picker.WifiPickerActivity;
import it.ibashkimi.lockscheduler.addeditprofile.conditions.picker.WifiPickerFragment;
import it.ibashkimi.lockscheduler.model.Condition;
import it.ibashkimi.lockscheduler.model.PlaceCondition;
import it.ibashkimi.lockscheduler.model.TimeCondition;
import it.ibashkimi.lockscheduler.model.WifiCondition;
import it.ibashkimi.lockscheduler.model.WifiItem;

import static android.app.Activity.RESULT_OK;


public class ConditionsFragment extends Fragment implements ConditionsContract.View {
    private static final String TAG = "ConditionsFragment";

    public static final int PLACE_PICKER_REQUEST = 1;

    private PlaceConditionView placeConditionView;

    private TimeConditionView timeConditionView;

    private WifiConditionView wifiConditionView;

    private ConditionsContract.Presenter presenter;

    private View appBar;

    public static ConditionsFragment newInstance() {
        return new ConditionsFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreate() called with: savedInstanceState = [" + savedInstanceState + "]");
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_conditions_test2, container, false);
        final ViewGroup placeLayout = (ViewGroup) root.findViewById(R.id.place_layout);
        placeConditionView = new PlaceConditionView(this, placeLayout, savedInstanceState);

        final ViewGroup timeLayout = (ViewGroup) root.findViewById(R.id.time_layout);
        timeConditionView = new TimeConditionView(this, null, timeLayout, savedInstanceState);

        final ViewGroup wifiLayout = (ViewGroup) root.findViewById(R.id.wifi_layout);
        wifiConditionView = new WifiConditionView(this, null, wifiLayout, savedInstanceState);

        return root;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString("pene", "figa");
        placeConditionView.onSaveInstanceState(outState);
        timeConditionView.onSaveInstanceState(outState);
        wifiConditionView.onSaveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(getActivity(), data);
                PlaceCondition placeCondition = new PlaceCondition("Place", place.getLatLng(), 300);
                showPlaceCondition(placeCondition);
            }
        }

        /*Log.d(TAG, "onActivityResult: ");
        if (requestCode == PLACE_PICKER_REQUEST) {
            //create place condition
            //showPlaceCondition(mPlaceCondition);
            //placeConditionView.onActivityResult(resultCode, data);
        }*/
    }

    @Override
    public void setPresenter(ConditionsContract.Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: ");
        //presenter.start();
    }

    @Override
    public void showEmptyConditions() {

    }

    @Override
    public boolean isActive() {
        return isAdded();
    }

    public void showTimePicker() {
        Log.d(TAG, "showWifiPicker() called");
        getActivity().getSupportFragmentManager().beginTransaction()
                //.hide(getActivity().getSupportFragmentManager().findFragmentById(android.R.id.content))
                .add(android.R.id.content, new TimePickerFragment())
                .addToBackStack(null)
                .commit();
    }

    public void showWifiPicker(List<WifiItem> items) {
        Intent intent = new Intent(getContext(), WifiPickerActivity.class);

        int[] networkIds = new int[items.size()];
        String[] ssids = new String[items.size()];
        for (int i = 0; i < items.size(); i++) {
            networkIds[i] = items.get(i).networkId;
            ssids[i] = items.get(i).SSID;
        }
        intent.putExtra("network_ids", networkIds);
        intent.putExtra("ssids", ssids);

        startActivityForResult(intent, 2);
    }

    public void showPlacePicker() {
        //startActivityForResult(new Intent(getContext(), PlacePickerActivity.class), 1);
        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
        try {
            startActivityForResult(builder.build(getActivity()), PLACE_PICKER_REQUEST);
        } catch (GooglePlayServicesRepairableException e) {
            e.printStackTrace();
        } catch (GooglePlayServicesNotAvailableException e) {
            Log.d(TAG, "onClick: play service not available");
            e.printStackTrace();
        }
    }

    @Override
    public void showPlaceCondition(PlaceCondition placeCondition) {
        placeConditionView.setPlaceCondition(placeCondition);
    }

    @Override
    public void showTimeCondition(TimeCondition timeCondition) {
        timeConditionView.setTimeCondition(timeCondition);
    }

    @Override
    public void showWifiCondition(WifiCondition wifiCondition) {
        wifiConditionView.setWifiCondition(wifiCondition);
    }

    @Override
    public void hidePlaceCondition() {
        placeConditionView.showPlaceEmpty();
    }

    @Override
    public void hideTimeCondition() {
        timeConditionView.showTimeEmpty();
    }

    @Override
    public void hideWifiCondition() {
        wifiConditionView.showWifiEmpty();
    }

    public void requestSave() {
        List<Condition> conditions = new ArrayList<>(3);
        if (placeConditionView.getPlaceCondition() != null)
            conditions.add(placeConditionView.getPlaceCondition());
        if (timeConditionView.getTimeCondition() != null)
            conditions.add(timeConditionView.getTimeCondition());
        if (wifiConditionView.getWifiCondition() != null)
            conditions.add(wifiConditionView.getWifiCondition());
        presenter.saveConditions(conditions);
    }
}
