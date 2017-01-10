package it.ibashkimi.lockscheduler;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.transition.TransitionManager;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import it.ibashkimi.lockscheduler.adapters.ChipAdapter;
import it.ibashkimi.lockscheduler.adapters.ConditionsAdapter;
import it.ibashkimi.lockscheduler.domain.Condition;
import it.ibashkimi.lockscheduler.domain.PlaceCondition;
import it.ibashkimi.lockscheduler.domain.TimeCondition;
import it.ibashkimi.lockscheduler.domain.WifiCondition;

import static android.app.Activity.RESULT_OK;


public class ConditionsFragment extends Fragment implements ConditionsAdapter.Callbacks {

    private static final String TAG = "ConditionsFragment";
    private static final int PLACE_PICKER_REQUEST = 1;
    private List<Condition> conditions;
    private ConditionsAdapter adapter;
    private ChipAdapter chipAdapter;
    private RecyclerView recyclerView;
    private RecyclerView chipsRecyclerView;
    private TextView addCondition;
    private ProfileFragment parent;
    private ArrayList<ChipAdapter.ChipItem> chipItems;

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
        adapter = new ConditionsAdapter(getContext(), conditions, this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_conditions, container, false);
        recyclerView = (RecyclerView) rootView.findViewById(R.id.conditionsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setAdapter(adapter);

        addCondition = (TextView) rootView.findViewById(R.id.add_condition);

        chipsRecyclerView = (RecyclerView) rootView.findViewById(R.id.chipsRecyclerView);
        chipsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        chipItems = new ArrayList<>();

        if (getCondition(Condition.Type.PLACE) == null) {
            addChip(Condition.Type.PLACE, false);
        }
        if (getCondition(Condition.Type.TIME) == null) {
            addChip(Condition.Type.TIME, false);
        }
        if (getCondition(Condition.Type.WIFI) == null) {
            addChip(Condition.Type.WIFI, false);
        }
        if (chipItems.size() > 0) {
            addCondition.setVisibility(View.VISIBLE);
            chipsRecyclerView.setVisibility(View.VISIBLE);
        }
        chipAdapter = new ChipAdapter(chipItems, new ChipAdapter.Callbacks() {
            @Override
            public void onChipClicked(ChipAdapter.ChipItem chipItem) {
                switch (chipItem.id) {
                    case Condition.Type.PLACE:
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
                        break;
                    case Condition.Type.TIME:
                        Log.d(TAG, "onClick: time pressed");
                        adapter.getConditions().add(new TimeCondition("Time"));
                        TransitionManager.beginDelayedTransition(recyclerView);
                        adapter.notifyItemInserted(adapter.getConditions().size() - 1);
                        removeChip(Condition.Type.TIME);
                        break;
                    case Condition.Type.WIFI:
                        adapter.getConditions().add(new WifiCondition("Wifi"));
                        TransitionManager.beginDelayedTransition(recyclerView);
                        adapter.notifyItemInserted(adapter.getConditions().size() - 1);
                        removeChip(Condition.Type.WIFI);
                        break;
                }
            }
        });
        chipsRecyclerView.setAdapter(chipAdapter);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.Callback() {
            @Override
            public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                return makeFlag(ItemTouchHelper.ACTION_STATE_DRAG,
                        ItemTouchHelper.DOWN | ItemTouchHelper.UP);
            }

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                Log.d(TAG, "onMove: " + viewHolder.getAdapterPosition() + ", " + target.getAdapterPosition());
                int targetPosition = target.getAdapterPosition();
                if (targetPosition == conditions.size()) {
                    targetPosition--;
                }
                Collections.swap(conditions, viewHolder.getAdapterPosition(), targetPosition);
                // and notify the adapter that its data set has changed
                adapter.notifyItemMoved(viewHolder.getAdapterPosition(), targetPosition);
                return true;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {

            }
        });
        itemTouchHelper.attachToRecyclerView(recyclerView);

        return rootView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(getActivity(), data);
                PlaceCondition placeCondition = null;
                Condition condition = getCondition(Condition.Type.PLACE);
                if (condition != null) {
                    placeCondition = (PlaceCondition) condition;
                }
                if (placeCondition != null) {
                    placeCondition.setPlace(place.getLatLng());
                    adapter.notifyItemChanged(getConditions().indexOf(placeCondition));
                    parent.onConditionChanged(placeCondition);
                } else {
                    placeCondition = new PlaceCondition("Place", place.getLatLng(), 300);
                    TransitionManager.beginDelayedTransition(recyclerView);
                    conditions.add(placeCondition);
                    parent.onConditionAdded(placeCondition);
                    adapter.notifyItemInserted(adapter.getConditions().size() - 1);
                    removeChip(Condition.Type.PLACE);
                }
                parent.onPlacePicked(place);
            }
        }
    }

    public Condition getCondition(@Condition.Type int type) {
        for (Condition condition : conditions) {
            if (condition.getType() == type)
                return condition;
        }
        return null;
    }

    public List<Condition> getConditions() {
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
        addChip(condition.getType(), true);
    }

    public boolean saveData() {
        return true;
    }

    private void addChip(@Condition.Type int chipId, boolean notifyAdapter) {
        ChipAdapter.ChipItem chip = null;
        switch (chipId) {
            case Condition.Type.PLACE:
                chip = new ChipAdapter.ChipItem(Condition.Type.PLACE, R.drawable.ic_place, "Place");
                break;
            case Condition.Type.TIME:
                chip = new ChipAdapter.ChipItem(Condition.Type.TIME, R.drawable.ic_time, "Time");
                break;
            case Condition.Type.WIFI:
                chip = new ChipAdapter.ChipItem(Condition.Type.WIFI, R.drawable.ic_wifi, "Wifi");
                break;
        }
        chipItems.add(chip);
        if (notifyAdapter) {
            addCondition.setVisibility(View.VISIBLE);
            chipsRecyclerView.setVisibility(View.VISIBLE);
            chipAdapter.notifyDataSetChanged();
        }
    }

    private void removeChip(@Condition.Type int chipId) {
        for (ChipAdapter.ChipItem chipItem : chipItems) {
            if (chipItem.id == chipId) {
                chipItems.remove(chipItem);
                chipAdapter.notifyDataSetChanged();
                break;
            }
        }
        if (chipItems.size() == 0) {
            addCondition.setVisibility(View.GONE);
            chipsRecyclerView.setVisibility(View.GONE);
        }
    }
}
