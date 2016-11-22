package it.ibashkimi.lockscheduler;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;

import java.util.ArrayList;

import it.ibashkimi.lockscheduler.domain.Profile;

/**
 * @author Indrit Bashkimi (mailto: indrit.bashkimi@studio.unibo.it)
 */

public class ProfileModifierActivity extends AppCompatActivity {
    int PLACE_PICKER_REQUEST = 1;
    private Profile profile;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_modifier);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        Spinner lockSpinner = (Spinner) findViewById(R.id.lock_spinner);
        ArrayList<String> spinnerArray = new ArrayList<>();
        spinnerArray.add("Swipe");
        spinnerArray.add("PIN");
        spinnerArray.add("Password");
        spinnerArray.add("Sequence");
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, spinnerArray);
        lockSpinner.setAdapter(spinnerArrayAdapter);

        Spinner otherwiseSpinner = (Spinner) findViewById(R.id.otherwise_spinner);
        ArrayList<String> spinnerArray2 = new ArrayList<>();
        spinnerArray2.add("Swipe");
        spinnerArray2.add("PIN");
        spinnerArray2.add("Password");
        spinnerArray2.add("Sequence");
        ArrayAdapter<String> spinnerArrayAdapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, spinnerArray2);
        otherwiseSpinner.setAdapter(spinnerArrayAdapter2);

        /*RecyclerView conditionRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        conditionRecyclerView.setLayoutManager(new GridLayoutManager(this, 1));
        conditionRecyclerView.setAdapter(new ConditionAdapter(profile.getConditions()));*/

        CardView addMapCondition = (CardView) findViewById(R.id.add_place_condition);
        addMapCondition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                try {
                    startActivityForResult(builder.build(ProfileModifierActivity.this), PLACE_PICKER_REQUEST);
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
            }
        });

        CardView addTimeCondition = (CardView) findViewById(R.id.add_time_condition);
        addTimeCondition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ProfileModifierActivity.this, TimeActivity.class);
                startActivityForResult(intent, 1);
            }
        });
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(data, this);
                String toastMsg = String.format("Place: %s", place.getName());
                Toast.makeText(this, toastMsg, Toast.LENGTH_LONG).show();
                setRadius();
            }
        }
    }

    String radius;

    private void setRadius(){
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        final EditText input = new EditText(this);
        alert.setView(input);

        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            //@Override
            public void onClick(DialogInterface dialog, int which) {
                Editable value = input.getText();
                radius = value.toString();
            }
        });
        alert.setTitle("Set radius");
        alert.show();
    }
}
