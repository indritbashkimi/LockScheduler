package it.ibashkimi.lockscheduler.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.google.android.gms.location.places.Place;
import com.google.android.gms.maps.MapsInitializer;

import org.json.JSONException;

import java.util.List;

import it.ibashkimi.lockscheduler.R;
import it.ibashkimi.lockscheduler.model.Condition;
import it.ibashkimi.lockscheduler.model.Profile;


public class ProfileFragment extends Fragment {
    private static final String TAG = "ProfileFragment";
    private Profile mProfile;
    private EditText mName;
    private boolean showDeleteOption;
    @StringRes
    private int title;

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    public static ProfileFragment newInstance(String profile, boolean showDeleteOption) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putString("profile", profile);
        args.putBoolean("show_delete_option", showDeleteOption);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            mProfile = Profile.parseJson(getArguments().getString("profile"));
        } catch (JSONException e) {
            Log.e(TAG, "onCreate: cannot retrieve profile.");
            e.printStackTrace();
        }
        showDeleteOption = getArguments().getBoolean("show_delete_option");
        title = showDeleteOption ? R.string.profile_title : R.string.new_profile_title;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_profile_old, container, false);
        Toolbar toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        toolbar.setTitle(title);

        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(R.drawable.ic_cancel_toolbar);
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        mName = (EditText) rootView.findViewById(R.id.profile_name);
        mName.setText(mProfile.getName());
        mName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                mProfile.setName(s.toString());
            }
        });

        MapsInitializer.initialize(getActivity());
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_profile, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.action_delete).setVisible(showDeleteOption);
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getActivity().onBackPressed();
                return true;
            /*case R.id.action_save:
                save();
                return true;*/
            case R.id.action_delete:
                delete();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public List<Condition> getConditions() {
        return mProfile.getConditions();
    }

    public Profile getProfile() {
        return mProfile;
    }

    public void onPlacePicked(Place place) {
        if (mProfile.getName().equals("")) {
            CharSequence address = place.getAddress();
            if (address == null) {
                address = place.getLatLng().toString();
            }
            setName(address.toString());
        }
    }

    public void onConditionChanged(Condition condition) {

    }

    public void onConditionAdded(Condition condition) {

    }

    public void onConditionRemoved(Condition condition) {

    }

    public void setName(String name) {
        mName.setText(name);
        mProfile.setName(name);
    }

    private ActionsFragment getActionsFragment() {
        return (ActionsFragment) getChildFragmentManager().findFragmentById(R.id.actions);
    }

    private ConditionsFragment getConditionsFragment() {
        return (ConditionsFragment) getChildFragmentManager().findFragmentById(R.id.conditions);
    }

    private void cancel() {
        ((ProfileActivity) getActivity()).cancel();
    }

    private void delete() {
        ((ProfileActivity) getActivity()).delete(mProfile);
    }

    private void save() {
        if (getConditionsFragment().saveData() && getActionsFragment().saveData()) {
            ((ProfileActivity) getActivity()).save(mProfile);
        }
    }
}
