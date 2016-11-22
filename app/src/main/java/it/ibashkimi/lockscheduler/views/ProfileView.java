package it.ibashkimi.lockscheduler.views;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.TextView;

import it.ibashkimi.lockscheduler.domain.Profile;
import it.ibashkimi.lockscheduler.R;

/**
 * @author Indrit Bashkimi (mailto: indrit.bashkimi@studio.unibo.it)
 */

public class ProfileView extends CardView {
    private static final String TAG = "ProfileView";

    private TextView name;
    private RecyclerView conditions;
    private Spinner lockSpinner;
    private Spinner otherwiseSpinner;
    private CompoundButton switchView;
    private Profile profile;

    public ProfileView(Context context) {
        this(context, null);
    }

    public ProfileView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ProfileView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        View.inflate(context, R.layout.profile, this);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        name = (TextView) findViewById(R.id.name);
        conditions = (RecyclerView) findViewById(R.id.conditions);

        /*lockSpinner = (Spinner) findViewById(R.id.lock_spinner);
        ArrayList<String> spinnerArray = new ArrayList<>();
        spinnerArray.add("Swipe");
        spinnerArray.add("PIN");
        spinnerArray.add("Password");
        spinnerArray.add("Sequence");
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, spinnerArray);
        lockSpinner.setAdapter(spinnerArrayAdapter);

        otherwiseSpinner = (Spinner) findViewById(R.id.otherwise_spinner);
        ArrayList<String> spinnerArray2 = new ArrayList<>();
        spinnerArray2.add("Swipe");
        spinnerArray2.add("PIN");
        spinnerArray2.add("Password");
        spinnerArray2.add("Sequence");
        ArrayAdapter<String> spinnerArrayAdapter2 = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, spinnerArray2);
        otherwiseSpinner.setAdapter(spinnerArrayAdapter2);*/

        switchView = (CompoundButton) findViewById(R.id.switchView);
        switchView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                enable(b);
            }
        });
    }

    public Profile getProfile() {
        return this.profile;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
        name.setText(profile.getName());
        /*if (profile.isEnabled())
            switchView.performClick();
        enable(profile.isEnabled());*/
    }

    private void enable(boolean b) {
        /*name.setEnabled(b);
        conditions.setEnabled(b);
        lockSpinner.setEnabled(b);
        otherwiseSpinner.setEnabled(b);*/
    }
}
