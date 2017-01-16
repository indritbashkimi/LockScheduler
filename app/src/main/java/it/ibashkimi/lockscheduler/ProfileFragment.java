package it.ibashkimi.lockscheduler;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.location.places.Place;
import com.google.android.gms.maps.MapsInitializer;

import org.json.JSONException;

import java.util.List;

import it.ibashkimi.lockscheduler.domain.Condition;
import it.ibashkimi.lockscheduler.domain.Profile;
import it.ibashkimi.support.design.utils.ThemeUtils;


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

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_profile, container, false);
        Toolbar toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.menu_profile);
        toolbar.getMenu().findItem(R.id.action_delete).setVisible(showDeleteOption);
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
        ImageView cancel = (ImageView) rootView.findViewById(R.id.cancel_view);
        cancel.setColorFilter(ThemeUtils.getColorFromAttribute(toolbar.getContext(), android.R.attr.textColorPrimary));
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancel();
            }
        });
        ((TextView) toolbar.findViewById(R.id.title_text)).setText(title);
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
