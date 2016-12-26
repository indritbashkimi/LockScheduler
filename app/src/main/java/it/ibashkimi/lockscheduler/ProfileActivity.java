package it.ibashkimi.lockscheduler;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputLayout;
import android.support.transition.TransitionManager;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;

import it.ibashkimi.lockscheduler.domain.LockMode;
import it.ibashkimi.lockscheduler.domain.Profile;
import it.ibashkimi.support.design.color.Themes;
import it.ibashkimi.support.design.utils.ThemeUtils;

/**
 * @author Indrit Bashkimi (mailto: indrit.bashkimi@studio.unibo.it)
 */

public class ProfileActivity extends AppCompatActivity {
    public static final String ACTION_NEW = "it.ibashkimi.lockscheduler.profile.new";
    public static final String ACTION_VIEW = "it.ibashkimi.lockscheduler.profile.view";

    private static final String TAG = "ProfileActivity";
    private static final int PLACE_PICKER_REQUEST = 1;

    private Profile mProfile;
    private EditText mName;
    private EditText mRadius;
    private MapView mMapView;
    private GoogleMap mGoogleMap;
    private Circle mCircle;
    private boolean mSaved;
    private boolean mDelete;
    private EditText mEnterInput;
    private EditText mExitInput;
    private TextInputLayout mEnterInputLayout;
    private TextInputLayout mExitInputLayout;
    private int mMapType;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        SharedPreferences settings = getSharedPreferences("prefs", Context.MODE_PRIVATE);
        @Themes.Theme int themeId = settings.getInt("theme", Themes.Theme.APP_THEME_DAYNIGHT_INDIGO);
        setTheme(Themes.resolveTheme(themeId));

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.menu_profile);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.action_save) {
                    Log.d(TAG, "onMenuItemClick: action_save");
                    mSaved = true;
                    finish();
                    return true;
                } else if (item.getItemId() == R.id.action_delete) {
                    mDelete = true;
                    finish();
                    return true;
                }
                return false;
            }
        });
        View cancelView = toolbar.findViewById(R.id.cancel_view);
        cancelView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSaved = false;
                finish();
            }
        });

        mProfile = getIntent().getParcelableExtra("profile");
        if (mProfile == null) {
            mProfile = new Profile();
            mProfile.setId(System.currentTimeMillis());
            mProfile.setRadius(Constants.GEOFENCE_RADIUS_IN_METERS);
        }

        mMapType = Utils.resolveMapStyle(getSharedPreferences("prefs", Context.MODE_PRIVATE)
                .getString("map_style", "hybrid"));

        mName = (EditText) findViewById(R.id.input_name);
        mName.setText(mProfile.getName());

        mRadius = (EditText) findViewById(R.id.radiusInput);
        mRadius.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mProfile.setRadius(s.length() == 0 ? 0 : Integer.parseInt(s.toString()));
                updateMap();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        mRadius.setText(String.format(getResources().getString(R.string.profile_radius), mProfile.getRadius()));

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSaved = true;
                finish();
            }
        });

        mEnterInputLayout = (TextInputLayout) findViewById(R.id.enter_password_layout);
        mEnterInputLayout.setVisibility(View.GONE);
        mExitInputLayout = (TextInputLayout) findViewById(R.id.exit_password_layout);
        mExitInputLayout.setVisibility(View.GONE);

        mEnterInput = (EditText) findViewById(R.id.enter_input);
        mExitInput = (EditText) findViewById(R.id.exit_input);

        EditText enterInputConfirm = (EditText) findViewById(R.id.enter_input_confirm);
        EditText exitInputConfirm = (EditText) findViewById(R.id.exit_input_confirm);
        TextInputLayout enterInputConfirmLayout = (TextInputLayout) findViewById(R.id.enter_password_layout_confirm);
        TextInputLayout exitInputConfirmLayout = (TextInputLayout) findViewById(R.id.exit_password_layout_confirm);

        NestedScrollView nestedScrollView = (NestedScrollView) findViewById(R.id.nestedScroolView);

        Spinner enterSpinner = (Spinner) findViewById(R.id.lock_spinner);
        ArrayAdapter<CharSequence> enterSpinnerAdapter = ArrayAdapter.createFromResource(
                this, R.array.lock_modes_array, android.R.layout.simple_spinner_item);
        enterSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        enterSpinner.setAdapter(enterSpinnerAdapter);
        enterSpinner.setOnItemSelectedListener(new SpinnerListener(mProfile.getEnterLockMode(), mEnterInputLayout, enterInputConfirmLayout, mEnterInput, enterInputConfirm, nestedScrollView));
        enterSpinner.setSelection(getSpinnerPositionFromLockType(mProfile.getEnterLockMode().getLockType()));

        Spinner exitSpinner = (Spinner) findViewById(R.id.otherwise_spinner);
        ArrayAdapter<CharSequence> exitSpinnerAdapter = ArrayAdapter.createFromResource(
                this, R.array.lock_modes_array, android.R.layout.simple_spinner_item);
        exitSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        exitSpinner.setAdapter(exitSpinnerAdapter);
        exitSpinner.setOnItemSelectedListener(new SpinnerListener(mProfile.getExitLockMode(), mExitInputLayout, exitInputConfirmLayout, mExitInput, exitInputConfirm, nestedScrollView));
        exitSpinner.setSelection(getSpinnerPositionFromLockType(mProfile.getExitLockMode().getLockType()));

        // Gets the MapView from the XML layout and creates it
        mMapView = (MapView) findViewById(R.id.mapview);
        mMapView.onCreate(savedInstanceState);

        // Gets to GoogleMap from the MapView and does initialization stuff
        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mGoogleMap = googleMap;
                mGoogleMap.getUiSettings().setMyLocationButtonEnabled(false);
                mGoogleMap.setMapType(mMapType);
                //mGoogleMap.getUiSettings().setMapToolbarEnabled(true);
                mGoogleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(LatLng latLng) {
                        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                        if (mProfile.getPlace() != null)
                            builder.setLatLngBounds(Utils.calculateBounds(mProfile.getPlace(), mProfile.getRadius()));
                        try {
                            startActivityForResult(builder.build(ProfileActivity.this), PLACE_PICKER_REQUEST);
                        } catch (GooglePlayServicesRepairableException e) {
                            e.printStackTrace();
                        } catch (GooglePlayServicesNotAvailableException e) {
                            Log.d(TAG, "onClick: play service not available");
                            e.printStackTrace();
                        }
                    }
                });
                // Needs to call MapsInitializer before doing any CameraUpdateFactory calls
                MapsInitializer.initialize(ProfileActivity.this);
                updateMap();
            }
        });

        String action = getIntent().getAction();
        switch (action) {
            case ACTION_NEW:
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                        try {
                            startActivityForResult(builder.build(ProfileActivity.this), PLACE_PICKER_REQUEST);
                        } catch (GooglePlayServicesRepairableException e) {
                            e.printStackTrace();
                        } catch (GooglePlayServicesNotAvailableException e) {
                            Log.d(TAG, "onClick: play service not available");
                            e.printStackTrace();
                        }
                    }
                });
                break;
            case ACTION_VIEW:
                break;
            default:
                break;
        }
    }

    @Override
    public void finish() {
        if (mSaved || mDelete) {
            mProfile.setName(mName.getText().toString());
            mProfile.setRadius(Integer.parseInt(mRadius.getText().toString()));
            @LockMode.LockType int lockType = mProfile.getEnterLockMode().getLockType();
            if (lockType == LockMode.LockType.PASSWORD) {
                mProfile.getEnterLockMode().setPassword(mEnterInput.getText().toString());
            } else if (lockType == LockMode.LockType.PIN) {
                mProfile.getEnterLockMode().setPin(mEnterInput.getText().toString());
            }
            lockType = mProfile.getEnterLockMode().getLockType();
            if (lockType == LockMode.LockType.PASSWORD) {
                mProfile.getExitLockMode().setPassword(mEnterInput.getText().toString());
            } else if (lockType == LockMode.LockType.PIN) {
                mProfile.getExitLockMode().setPin(mEnterInput.getText().toString());
            }
            //Log.d(TAG, "finish: returning profile: " + mProfile.toString());
            Intent resultIntent = new Intent();
            resultIntent.putExtra("profile", mProfile);
            String action = mDelete ? "delete" : (getIntent().getAction().equals(ACTION_NEW) ? "new" : "update");
            resultIntent.setAction(action);
            setResult(Activity.RESULT_OK, resultIntent);
        }
        super.finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mMapView != null)
            mMapView.onDestroy();
    }

    @Override
    protected void onPause() {
        mMapView.onPause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mMapView.onStart();
    }

    @Override
    protected void onStop() {
        mMapView.onStop();
        super.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mMapView.onSaveInstanceState(outState);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult: action = " + getIntent().getAction());
        String action = getIntent().getAction();
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(this, data);
                if (mProfile.getName() == null || "".equals(mProfile.getName())) {
                    CharSequence address = place.getAddress();
                    if (address == null) {
                        address = place.getLatLng().toString();
                    }
                    mProfile.setName(address.toString());
                    mName.setText(address);
                }
                mProfile.setPlace(place.getLatLng());
                updateMap();
            } else {
                if (action.equals(ACTION_NEW)) {
                    finish();
                }
            }
        }
    }

    private void updateMap() {
        int padding = (int) ThemeUtils.dpToPx(this, 8);
        if (mCircle == null) {
            if (mGoogleMap != null && mProfile.getPlace() != null) {
                mCircle = mGoogleMap.addCircle(new CircleOptions()
                        .center(mProfile.getPlace())
                        .radius(mProfile.getRadius())
                        .strokeColor(Color.RED));
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(Utils.calculateBounds(mProfile.getPlace(), mProfile.getRadius()), padding);
                mGoogleMap.moveCamera(cameraUpdate);
            }
        } else {
            if (mProfile.getRadius() > 0) {
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(Utils.calculateBounds(mProfile.getPlace(), mProfile.getRadius()), padding);
                mGoogleMap.animateCamera(cameraUpdate);
            }
            mCircle.setCenter(mProfile.getPlace());
            mCircle.setRadius(mProfile.getRadius());
        }
    }


    private static int getSpinnerPositionFromLockType(@LockMode.LockType int lockType) {
        switch (lockType) {
            case LockMode.LockType.UNCHANGED:
                return 0;
            case LockMode.LockType.PASSWORD:
                return 1;
            case LockMode.LockType.PIN:
                return 2;
            case LockMode.LockType.SEQUENCE:
                return 3;
            case LockMode.LockType.SWIPE:
                return 4;
            default:
                return 0;
        }
    }

    private static
    @LockMode.LockType
    int getLockTypeFromSpinnerPosition(int position) {
        switch (position) {
            case 0:
                return LockMode.LockType.UNCHANGED;
            case 1:
                return LockMode.LockType.PASSWORD;
            case 2:
                return LockMode.LockType.PIN;
            case 3:
                return LockMode.LockType.SEQUENCE;
            case 4:
                return LockMode.LockType.SWIPE;
            default:
                return LockMode.LockType.UNCHANGED;
        }
    }

    private static class SpinnerListener implements AdapterView.OnItemSelectedListener {
        private LockMode lockMode;
        private TextInputLayout inputLayout;
        private TextInputLayout inputLayoutConfirm;
        private EditText input;
        private EditText inputConfirm;
        private ViewGroup rootView;

        SpinnerListener(LockMode lockMode, TextInputLayout inputLayout, TextInputLayout inputLayoutConfirm, EditText input, EditText inputConfirm, ViewGroup rootView) {
            this.lockMode = lockMode;
            this.inputLayout = inputLayout;
            this.input = input;
            this.inputLayoutConfirm = inputLayoutConfirm;
            this.inputConfirm = inputConfirm;
            this.rootView = rootView;
        }

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            @LockMode.LockType int lockType = getLockTypeFromSpinnerPosition(position);
            lockMode.setLockType(lockType);
            TransitionManager.beginDelayedTransition(rootView);
            if (lockType == LockMode.LockType.PASSWORD) {
                input.setText(lockMode.getPassword());
                inputLayout.setVisibility(View.VISIBLE);
                inputLayout.setPasswordVisibilityToggleEnabled(true);
                input.setInputType(InputType.TYPE_CLASS_TEXT |
                        InputType.TYPE_TEXT_VARIATION_PASSWORD);
                input.setSelection(input.getText().length());
                input.setHint("Enter password");
                inputConfirm.setText(lockMode.getPassword());
                inputLayoutConfirm.setVisibility(View.VISIBLE);
                inputLayoutConfirm.setPasswordVisibilityToggleEnabled(true);
                inputConfirm.setInputType(InputType.TYPE_CLASS_TEXT |
                        InputType.TYPE_TEXT_VARIATION_PASSWORD);
                inputConfirm.setSelection(inputConfirm.getText().length());
                inputConfirm.setHint("Confirm password");
            } else if (lockType == LockMode.LockType.PIN) {
                input.setText(lockMode.getPin());
                inputLayout.setVisibility(View.VISIBLE);
                inputLayout.setPasswordVisibilityToggleEnabled(true);
                input.setInputType(InputType.TYPE_CLASS_NUMBER |
                        InputType.TYPE_NUMBER_VARIATION_PASSWORD);
                input.setSelection(input.getText().length());
                input.setHint("Enter PIN");
                inputConfirm.setText(lockMode.getPin());
                inputLayoutConfirm.setVisibility(View.VISIBLE);
                inputLayoutConfirm.setPasswordVisibilityToggleEnabled(true);
                inputConfirm.setInputType(InputType.TYPE_CLASS_NUMBER |
                        InputType.TYPE_NUMBER_VARIATION_PASSWORD);
                inputConfirm.setSelection(inputConfirm.getText().length());
                inputConfirm.setHint("Enter PIN");
            } else {
                inputLayout.setVisibility(View.GONE);
                inputLayoutConfirm.setVisibility(View.GONE);
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    }
}
