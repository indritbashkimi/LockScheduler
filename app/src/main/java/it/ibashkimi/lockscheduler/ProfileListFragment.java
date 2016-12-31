package it.ibashkimi.lockscheduler;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import it.ibashkimi.lockscheduler.adapters.ProfileAdapter;
import it.ibashkimi.lockscheduler.domain.Profile;

/**
 * Fragment used to display profile list.
 */
public class ProfileListFragment extends Fragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String TAG = "ProfileListFragment";

    private ProfileAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private int mItemLayout;
    private int mMapStyle;
    private SharedPreferences mSettings;

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
        mAdapter = new ProfileAdapter(getActivity(), getProfiles(), mItemLayout, mMapStyle);
        mRecyclerView.setAdapter(mAdapter);
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
        mAdapter = new ProfileAdapter(getActivity(), getProfiles());
        mRecyclerView.setAdapter(mAdapter);
    }

    private ArrayList<Profile> getProfiles() {
        return Profiles.restoreProfiles(getContext());
    }

    private static int resolveLayout(int itemLayout) {
        switch (itemLayout) {
            case 0:
                return R.layout.item_profile;
            case 1:
                return R.layout.item_profile2;
            default:
                return R.layout.item_profile;
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        if (s.equals("item_layout")) {
            mItemLayout = resolveLayout(Integer.parseInt(sharedPreferences.getString("item_layout", "0")));
            mAdapter = new ProfileAdapter(getActivity(), getProfiles(), mItemLayout, mMapStyle);
            mRecyclerView.setAdapter(mAdapter);
        } else if (s.equals("map_style")) {
            mMapStyle = Utils.resolveMapStyle(sharedPreferences.getString("map_style", "hybrid"));
            mAdapter = new ProfileAdapter(getActivity(), getProfiles(), mItemLayout, mMapStyle);
            mRecyclerView.setAdapter(mAdapter);
        }
    }
}
