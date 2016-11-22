package it.ibashkimi.lockscheduler;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import it.ibashkimi.lockscheduler.adapters.ProfileAdapter;
import it.ibashkimi.lockscheduler.domain.Profile;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainFragment extends Fragment {

    public MainFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        RecyclerView.Adapter adapter = new ProfileAdapter(getActivity(), getProfiles());
        recyclerView.setAdapter(adapter);
        return rootView;
    }

    private List<Profile> getProfiles() {
        // TODO
        ArrayList<Profile> profiles = new ArrayList<>();
        profiles.add(new Profile("Profile 1", true));
        profiles.add(new Profile("Profile 2"));
        return profiles;
    }
}
