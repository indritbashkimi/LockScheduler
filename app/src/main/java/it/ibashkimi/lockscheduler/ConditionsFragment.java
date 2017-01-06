package it.ibashkimi.lockscheduler;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.transition.TransitionManager;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;

import it.ibashkimi.lockscheduler.adapters.ConditionsAdapter;
import it.ibashkimi.lockscheduler.domain.Condition;
import it.ibashkimi.lockscheduler.domain.PlaceCondition;
import it.ibashkimi.lockscheduler.domain.TimeCondition;

import static android.app.Activity.RESULT_OK;


public class ConditionsFragment extends Fragment implements ConditionsAdapter.Callbacks {

    private static final String TAG = "ConditionsFragment";
    private static final int PLACE_PICKER_REQUEST = 1;
    private SparseArray<Condition> conditions;
    private ConditionsAdapter adapter;
    private RecyclerView recyclerView;
    private ProfileFragment parent;
    private View addPlace;
    private View addTime;
    private View addWifi;

    @SuppressWarnings("unused")
    public static ConditionsFragment newInstance() {
        ConditionsFragment fragment = new ConditionsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        parent = (ProfileFragment) getParentFragment();
        conditions = parent.getConditions();
        adapter = new ConditionsAdapter(conditions, this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_conditions, container, false);
        recyclerView = (RecyclerView) rootView.findViewById(R.id.conditionsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setAdapter(adapter);

        addPlace = rootView.findViewById(R.id.add_place);
        addPlace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: place pressed");
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
        });
        if (conditions.get(Condition.Type.PLACE) != null) {
            addPlace.setVisibility(View.GONE);
        }
        addTime = rootView.findViewById(R.id.add_time);
        addTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: time pressed");
                adapter.getConditions().put(Condition.Type.TIME, new TimeCondition("Time"));
                TransitionManager.beginDelayedTransition(recyclerView);
                adapter.notifyDataSetChanged();
                addTime.setVisibility(View.GONE);
            }
        });
        if (conditions.get(Condition.Type.TIME) != null) {
            addTime.setVisibility(View.GONE);
        }
        addWifi = rootView.findViewById(R.id.add_wifi);
        addWifi.setVisibility(View.GONE);
        return rootView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(getActivity(), data);
                PlaceCondition placeCondition = null;
                Condition condition = conditions.get(Condition.Type.PLACE);
                if (condition != null) {
                    placeCondition = (PlaceCondition) condition;
                }
                if (placeCondition != null) {
                    placeCondition.setPlace(place.getLatLng());
                    parent.onConditionChanged(placeCondition);
                } else {
                    placeCondition = new PlaceCondition("Place", place.getLatLng(), 300);
                    conditions.put(Condition.Type.PLACE, placeCondition);
                    parent.onConditionAdded(placeCondition);
                    addPlace.setVisibility(View.GONE);
                }
                parent.onPlacePicked(place);
                TransitionManager.beginDelayedTransition(recyclerView);
                adapter.notifyDataSetChanged();
            }
        }
    }

    public SparseArray<Condition> getConditions() {
        return conditions;
    }

    @Override
    public void onConditionClicked(Condition condition) {
        switch (condition.getType()) {
            case Condition.Type.PLACE:
                PlaceCondition placeCondition = (PlaceCondition) condition;
                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                if (placeCondition.getPlace() != null)
                    builder.setLatLngBounds(Utils.calculateBounds(placeCondition.getPlace(), placeCondition.getRadius()));
                try {
                    startActivityForResult(builder.build(getActivity()), PLACE_PICKER_REQUEST);
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesNotAvailableException e) {
                    Log.d(TAG, "onClick: play service not available");
                    e.printStackTrace();
                }
                break;
        }
    }

    @Override
    public void onConditionRemoved(Condition condition) {
        switch (condition.getType()) {
            case Condition.Type.PLACE:
                addPlace.setVisibility(View.VISIBLE);
                break;
            case Condition.Type.TIME:
                addTime.setVisibility(View.VISIBLE);
                break;
            case Condition.Type.WIFI:
                addWifi.setVisibility(View.VISIBLE);
                break;
        }
    }

    public boolean saveData() {
        return true;
    }
}
