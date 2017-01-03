package it.ibashkimi.lockscheduler;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collections;

import it.ibashkimi.lockscheduler.adapters.ProfileAdapter;
import it.ibashkimi.lockscheduler.domain.Profile;

/**
 * Fragment used to display profile list.
 */
public class ProfileListFragment extends Fragment implements SharedPreferences.OnSharedPreferenceChangeListener, ProfileAdapter.Callback {
    private static final String TAG = "ProfileListFragment";

    private ProfileAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private int mItemLayout;
    private int mMapStyle;
    private SharedPreferences mSettings;
    private ItemTouchHelper mItemTouchHelper;
    private ArrayList<Profile> mProfiles;

    public ProfileListFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSettings = getContext().getSharedPreferences("prefs", Context.MODE_PRIVATE);
        mMapStyle = Utils.resolveMapStyle(mSettings.getString("map_style", "hybrid"));
        String itemLayout = mSettings.getString("item_layout", "0");
        mItemLayout = resolveLayout(Integer.parseInt(itemLayout));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_profile_list, container, false);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        final ArrayList<Profile> profiles = getProfiles(true);
        mAdapter = new ProfileAdapter(getContext(), profiles, mItemLayout, mMapStyle, this);
        mRecyclerView.setAdapter(mAdapter);
        mItemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.Callback() {
            @Override
            public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                return makeFlag(ItemTouchHelper.ACTION_STATE_DRAG,
                        ItemTouchHelper.DOWN | ItemTouchHelper.UP);
            }

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                Log.d(TAG, "onMove: " + viewHolder.getAdapterPosition() + ", " + target.getAdapterPosition());
                int targetPosition = target.getAdapterPosition();
                if (targetPosition == profiles.size()) {
                    targetPosition--;
                }
                Collections.swap(profiles, viewHolder.getAdapterPosition(), targetPosition);
                // and notify the adapter that its data set has changed
                mAdapter.notifyItemMoved(viewHolder.getAdapterPosition(), targetPosition);
                return true;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {

            }
        });
        mItemTouchHelper.attachToRecyclerView(mRecyclerView);
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        mSettings.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onStop() {
        mSettings.unregisterOnSharedPreferenceChangeListener(this);
        super.onStop();
    }

    public void notifyDataHasChanged() {
        mAdapter = new ProfileAdapter(getContext(), getProfiles(true), mItemLayout, mMapStyle, this);
        mRecyclerView.setAdapter(mAdapter);
    }

    private ArrayList<Profile> getProfiles() {
        return Profiles.restoreProfiles(getContext());
    }

    private ArrayList<Profile> getProfiles(boolean forceLoad) {
        if (forceLoad || mProfiles == null) {
            mProfiles = Profiles.restoreProfiles(getContext());
        }
        return mProfiles;
    }

    private static int resolveLayout(int itemLayout) {
        switch (itemLayout) {
            case 0:
                return R.layout.item_profile;
            case 1:
                return R.layout.item_profile2;
            case 2:
                return R.layout.item_profile_3;
            default:
                return R.layout.item_profile;
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        if (s.equals("item_layout")) {
            mItemLayout = resolveLayout(Integer.parseInt(sharedPreferences.getString("item_layout", "0")));
            mAdapter = new ProfileAdapter(getContext(), getProfiles(true), mItemLayout, mMapStyle, this);
            mRecyclerView.setAdapter(mAdapter);
        } else if (s.equals("map_style")) {
            mMapStyle = Utils.resolveMapStyle(sharedPreferences.getString("map_style", "hybrid"));
            mAdapter = new ProfileAdapter(getContext(), getProfiles(true), mItemLayout, mMapStyle, this);
            mRecyclerView.setAdapter(mAdapter);
        }
    }


    @Override
    public void onProfileRemoved(Profile profile, int position) {
        mProfiles.remove(position);
        Profiles.saveProfiles(getContext(), mProfiles);
        if (profile.isEnabled()) {
            App.getGeofenceApiHelper().removeGeofence(Long.toString(profile.getId()));
        }
        mAdapter.notifyItemRemoved(position);
    }

    @Override
    public void onProfileClicked(Profile profile) {
        Intent intent = new Intent(getActivity(), ProfileActivity.class);
        intent.setAction(ProfileActivity.ACTION_VIEW);
        intent.putExtra("profile", profile);
        getActivity().startActivityForResult(intent, MainActivity.RESULT_PROFILE);
    }

    @Override
    public void onProfileEnabled(Profile profile, boolean enabled) {
        profile.setEnabled(enabled);
        // TODO: 31/12/16
    }
}
