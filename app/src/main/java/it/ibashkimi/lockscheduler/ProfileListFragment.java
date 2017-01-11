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

    public ProfileListFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSettings = getContext().getSharedPreferences("prefs", Context.MODE_PRIVATE);
        mMapStyle = Utils.resolveMapStyle(mSettings.getInt("map_style", 0));
        int itemLayout = mSettings.getInt("item_layout", 0);
        mItemLayout = resolveLayout(itemLayout);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_profile_list, container, false);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        final ArrayList<Profile> profiles = getProfiles();
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
                App.getProfileApiHelper().swap(viewHolder.getAdapterPosition(), targetPosition);
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

    public ProfileAdapter getAdapter() {
        return mAdapter;
    }

    public void notifyDataHasChanged() {
        mAdapter = new ProfileAdapter(getContext(), getProfiles(), mItemLayout, mMapStyle, this);
        mRecyclerView.setAdapter(mAdapter);
    }

    private ArrayList<Profile> getProfiles() {
        return App.getProfileApiHelper().getProfiles();
    }

    private static int resolveLayout(int itemLayout) {
        switch (itemLayout) {
            case 0:
                return R.layout.item_profile_0;
            case 1:
                return R.layout.item_profile_1;
            case 2:
                return R.layout.item_profile_2;
            case 3:
                return R.layout.item_profile_3;
            default:
                return R.layout.item_profile_0;
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        if (s.equals("item_layout")) {
            mItemLayout = resolveLayout(Integer.parseInt(sharedPreferences.getString("item_layout", "0")));
            mAdapter = new ProfileAdapter(getContext(), getProfiles(), mItemLayout, mMapStyle, this);
            mRecyclerView.setAdapter(mAdapter);
        } else if (s.equals("map_style")) {
            mMapStyle = Utils.resolveMapStyle(sharedPreferences.getInt("map_style", 0));
            mAdapter = new ProfileAdapter(getContext(), getProfiles(), mItemLayout, mMapStyle, this);
            mRecyclerView.setAdapter(mAdapter);
        }
    }


    @Override
    public void onProfileRemoved(Profile profile, int position) {
        App.getProfileApiHelper().removeProfile(profile);
        mAdapter.notifyItemRemoved(position);
    }

    @Override
    public void onProfileClicked(Profile profile) {
        Intent intent = new Intent(getActivity(), ProfileActivity.class);
        intent.setAction(ProfileActivity.ACTION_VIEW);
        intent.putExtra("profile", profile.toJson());
        getActivity().startActivityForResult(intent, MainActivity.RESULT_PROFILE);
    }
}
