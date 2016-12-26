package it.ibashkimi.lockscheduler;

import android.content.Context;
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
 * A placeholder fragment containing a simple view.
 */
public class MainFragment extends Fragment {
    private static final String TAG = "MainFragment";

    private RecyclerView.Adapter adapter;
    private RecyclerView recyclerView;
    private int mapType;

    public MainFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mapType = Utils.resolveMapStyle(getContext()
                .getSharedPreferences("prefs", Context.MODE_PRIVATE)
                .getString("map_style", "hybrid"));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        recyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ProfileAdapter(getActivity(), getProfiles(), mapType);
        recyclerView.setAdapter(adapter);
        return rootView;
    }

    public void notifyDataHasChanged() {
        adapter = new ProfileAdapter(getActivity(), getProfiles());
        recyclerView.setAdapter(adapter);
    }

    private ArrayList<Profile> getProfiles() {
        return Profiles.restoreProfiles(getContext());
    }
}
