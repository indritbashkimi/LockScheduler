package it.ibashkimi.lockscheduler;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
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
import com.google.android.gms.maps.model.LatLngBounds;

import java.util.ArrayList;

import it.ibashkimi.lockscheduler.domain.LockMode;
import it.ibashkimi.lockscheduler.domain.Profile;

/**
 * @author Indrit Bashkimi (mailto: indrit.bashkimi@studio.unibo.it)
 */

public class ProfileActivity extends AppCompatActivity {
    public static final String ACTION_NEW = "it.ibashkimi.lockscheduler.profile.new";
    public static final String ACTION_VIEW = "it.ibashkimi.lockscheduler.profile.view";

    private static final int DEFAULT_RADIUS = 300;

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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
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
            mProfile.setRadius(DEFAULT_RADIUS);
            //mProfile.setName(DEFAULT_NAME);
        }
        Log.d(TAG, "onCreate: Profile: " + mProfile.toString());

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

        mEnterInput = (EditText) findViewById(R.id.enter_input);
        mEnterInput.setVisibility(View.GONE);
        mExitInput = (EditText) findViewById(R.id.exit_input);
        mExitInput.setVisibility(View.GONE);

        Spinner lockSpinner = (Spinner) findViewById(R.id.lock_spinner);
        ArrayList<String> spinnerArray = new ArrayList<>();
        spinnerArray.add("Do nothing");
        spinnerArray.add("Password");
        spinnerArray.add("PIN");
        spinnerArray.add("Swipe");
        spinnerArray.add("Sequence");
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, spinnerArray);
        lockSpinner.setAdapter(spinnerArrayAdapter);
        lockSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mProfile.getEnterLockMode().setLockType(getLockTypeFromSpinnerPosition(position));
                if (position == 1 || position == 2) {
                    mEnterInput.setVisibility(View.VISIBLE);
                } else {
                    mEnterInput.setVisibility(View.GONE);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        lockSpinner.setSelection(getSpinnerPositionFromLockType(mProfile.getEnterLockMode().getLockType()));
        Spinner otherwiseSpinner = (Spinner) findViewById(R.id.otherwise_spinner);
        ArrayList<String> spinnerArray2 = new ArrayList<>();
        spinnerArray2.add("Do nothing");
        spinnerArray2.add("Password");
        spinnerArray2.add("PIN");
        spinnerArray2.add("Sequence");
        spinnerArray2.add("Swipe");
        ArrayAdapter<String> spinnerArrayAdapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, spinnerArray2);
        otherwiseSpinner.setAdapter(spinnerArrayAdapter2);
        otherwiseSpinner.setSelection(getSpinnerPositionFromLockType(mProfile.getExitLockMode().getLockType()));
        otherwiseSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mProfile.getExitLockMode().setLockType(getLockTypeFromSpinnerPosition(position));
                if (position == 1 || position == 2) {
                    mExitInput.setVisibility(View.VISIBLE);
                } else {
                    mExitInput.setVisibility(View.GONE);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // Gets the MapView from the XML layout and creates it
        mMapView = (MapView) findViewById(R.id.mapview);
        mMapView.onCreate(savedInstanceState);

        // Gets to GoogleMap from the MapView and does initialization stuff
        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mGoogleMap = googleMap;
                mGoogleMap.getUiSettings().setMyLocationButtonEnabled(false);
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
        Log.d(TAG, "onCreate: action = " + action);
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
            Log.d(TAG, "finish: returning profile: " + mProfile.toString());
            Intent resultIntent = new Intent();
            resultIntent.putExtra("profile", mProfile);
            String action = mDelete ? "delete" : (getIntent().getAction().equals(ACTION_NEW) ? "new" : "update");
            Log.d(TAG, "finish: action=" + action);
            resultIntent.setAction(action);
            setResult(Activity.RESULT_OK, resultIntent);
        }
        super.finish();
    }

    private int getSpinnerPositionFromLockType(@LockMode.LockType int lockType) {
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

    private @LockMode.LockType int getLockTypeFromSpinnerPosition(int position) {
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
        int padding = (int) Utils.dpToPx(this, 8);
        if (mCircle == null) {
            if (mGoogleMap != null && mProfile.getPlace() != null) {
                mCircle = mGoogleMap.addCircle(new CircleOptions()
                        .center(mProfile.getPlace())
                        .radius(mProfile.getRadius())
                        .strokeColor(Color.RED));
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(Utils.calculateBounds(mProfile.getPlace(), mProfile.getRadius()), padding);
                mGoogleMap.animateCamera(cameraUpdate);
            }
        } else {
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(Utils.calculateBounds(mProfile.getPlace(), mProfile.getRadius()), padding);
            mGoogleMap.animateCamera(cameraUpdate);
            mCircle.setCenter(mProfile.getPlace());
            mCircle.setRadius(mProfile.getRadius());
        }
    }
}
