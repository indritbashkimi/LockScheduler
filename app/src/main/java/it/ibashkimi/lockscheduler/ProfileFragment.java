package it.ibashkimi.lockscheduler;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.google.android.gms.location.places.Place;
import com.google.android.gms.maps.MapsInitializer;

import org.json.JSONException;

import it.ibashkimi.lockscheduler.domain.Condition;
import it.ibashkimi.lockscheduler.domain.Profile;


public class ProfileFragment extends Fragment {
    private static final String TAG = "ProfileFragment";
    private Profile mProfile;
    private EditText mName;

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    public static ProfileFragment newInstance(String profile) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putString("profile", profile);
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
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_profile_main, container, false);
        Toolbar toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.menu_profile);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.action_save) {
                    save();
                    return true;
                } else if (item.getItemId() == R.id.action_delete) {
                    delete();
                    return true;
                }
                return false;
            }
        });
        View cancelView = toolbar.findViewById(R.id.cancel_view);
        cancelView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancel();
            }
        });

        mName = (EditText) rootView.findViewById(R.id.input_name);
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

    public SparseArray<Condition> getConditions() {
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
