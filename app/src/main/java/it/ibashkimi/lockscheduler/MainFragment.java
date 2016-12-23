package it.ibashkimi.lockscheduler;

import android.app.Activity;
import android.content.Intent;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

import it.ibashkimi.lockscheduler.adapters.ProfileAdapter;
import it.ibashkimi.lockscheduler.domain.Profile;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainFragment extends Fragment {
    private static final String TAG = "MainFragment";

    private RecyclerView.Adapter adapter;
    private RecyclerView recyclerView;

    public MainFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        recyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        MainActivity activity = (MainActivity) getActivity();
        adapter = new ProfileAdapter(getActivity(), activity.getProfiles());
        recyclerView.setAdapter(adapter);
        return rootView;
    }

    public void notifyDataHasChanged() {
        MainActivity activity = (MainActivity) getActivity();
        adapter = new ProfileAdapter(getActivity(), activity.getProfiles());
        recyclerView.setAdapter(adapter);
    }
}
