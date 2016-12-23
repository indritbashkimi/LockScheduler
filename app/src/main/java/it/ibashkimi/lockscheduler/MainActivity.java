package it.ibashkimi.lockscheduler;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.PendingIntent;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import it.ibashkimi.lockscheduler.domain.Profile;
import it.ibashkimi.lockscheduler.services.GeofenceTransitionsIntentService;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = MainActivity.class.getCanonicalName();

    private static final int RESULT_ENABLE = 1;
    public static final int RESULT_PROFILE = 0;

    private DevicePolicyManager deviceManger;
    private ActivityManager activityManager;
    private ComponentName compName;
    private GoogleApiClient mGoogleApiClient;
    private PendingIntent mGeofencePendingIntent;
    private ArrayList<Profile> mProfiles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        deviceManger = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        compName = new ComponentName(this, LockSchedulerAdmin.class);

        if (!deviceManger.isAdminActive(compName)) {
            addAdmin();
        }

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                intent.setAction(ProfileActivity.ACTION_NEW);
                startActivityForResult(intent, 0);
            }
        });

        restoreProfiles();

        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    public void initGeofences(GoogleApiClient googleApiClient) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.GeofencingApi.addGeofences(
                googleApiClient,
                getGeofencingRequest(),
                getGeofencePendingIntent()
        ).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {
                Log.d(TAG, "Geofence: " + (status.isSuccess() ? "successful." : "failed."));
            }
        });
    }

    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(this, GeofenceTransitionsIntentService.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
        // calling addGeofences() and removeGeofences().
        mGeofencePendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return mGeofencePendingIntent;
    }

    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER | GeofencingRequest.INITIAL_TRIGGER_EXIT);
        builder.addGeofences(getGeofenceList());
        return builder.build();
    }

    private List<Geofence> getGeofenceList() {
        ArrayList<Geofence> geofences = new ArrayList<>();
        getProfiles();
        for (Profile profile : getProfiles()) {
            geofences.add(new Geofence.Builder()
                    // Set the request ID of the geofence. This is a string to identify this
                    // geofence.
                    .setRequestId(Long.toString(profile.getId()))
                    .setCircularRegion(
                            profile.getPlace().latitude,
                            profile.getPlace().longitude,
                            profile.getRadius()
                    )
                    .setExpirationDuration(Geofence.NEVER_EXPIRE)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                            Geofence.GEOFENCE_TRANSITION_EXIT)
                    .setLoiteringDelay(60000) // 1 min
                    .build());
        }
        return geofences;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void addAdmin() {
        Log.d(TAG, "addAdmin() called");
        Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, compName);
        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                "Additional text explaining why this needs to be added.");
        startActivityForResult(intent, RESULT_ENABLE);
    }

    public ArrayList<Profile> getProfiles() {
        if (mProfiles == null)
            restoreProfiles();
        return mProfiles;
    }

    private void restoreProfiles() {
        Log.d(TAG, "restoreProfiles() called");
        SharedPreferences sharedPreferences = getSharedPreferences("prefs", Context.MODE_PRIVATE);
        String ids = sharedPreferences.getString("ids", "");
        String[] idStrings = ids.equals("") ? new String[0] : ids.split(",");
        for(String s : idStrings)
            Log.d(TAG, "restoreProfiles: s = " + s);
        Log.d(TAG, "restoreProfiles: ids = " + ids);
        Log.d(TAG, "restoreProfiles: idStrings.length=" + idStrings.length);
        //Log.d(TAG, "restoreProfiles: idstings[0] = " + idStrings[0]);
        mProfiles = new ArrayList<>(idStrings.length);
        for (String idString : idStrings) {
            try {
                mProfiles.add(Profile.fromJsonString(sharedPreferences.getString("profile_" + idString, null)));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void saveProfiles() {
        if (mProfiles.size() > 0) {
            SharedPreferences.Editor editor = getSharedPreferences("prefs", Context.MODE_PRIVATE).edit();
            String ids = Long.toString(mProfiles.get(0).getId());
            for (int i = 1; i < mProfiles.size(); i++)
                ids += "," + Long.toString(mProfiles.get(i).getId());
            editor.putString("ids", ids);
            for (Profile profile : mProfiles) {
                editor.putString("profile_" + profile.getId(), profile.toJson().toString());
            }
            editor.apply();
        }
    }

    @Override
    protected void onStart() {
        Log.d(TAG, "onStart() called");
        super.onStart();
        mGoogleApiClient.connect();
        if (mProfiles == null)
            restoreProfiles();
        // Bind to GeofenceManagerService
        //Intent intent = new Intent(this, GeofenceManagerService.class);
        //bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop() called");
        mGoogleApiClient.disconnect();
        saveProfiles();
        super.onStop();
        // Unbind from the service
        /*if (bound) {
            unbindService(mConnection);
            bound = false;
        }*/
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult() called with: requestCode = [" + requestCode + "], resultCode = [" + resultCode + "], data = [" + data + "]");
        switch (requestCode) {
            case RESULT_ENABLE:
                if (resultCode == Activity.RESULT_OK) {
                    Log.i(TAG, "Admin enabled!");
                } else {
                    Log.i(TAG, "Admin enable FAILED!");
                }
                return;
            case RESULT_PROFILE:
                if (resultCode == Activity.RESULT_OK) {
                    SharedPreferences prefs = getSharedPreferences("prefs", Context.MODE_PRIVATE);
                    Profile profile = data.getParcelableExtra("profile");
                    Log.d(TAG, "onActivityResult: profile = " + profile);
                    if (mProfiles == null)
                        restoreProfiles();
                    switch (data.getAction()) {
                        case "delete":
                            for (Profile p : mProfiles) {
                                if (p.getId() == profile.getId()) {
                                    mProfiles.remove(p);
                                    break;
                                }
                            }
                            if (profile.isEnabled()) {
                                ArrayList<String> removeList = new ArrayList<>(1);
                                removeList.add(Long.toString(profile.getId()));
                                LocationServices.GeofencingApi.removeGeofences(mGoogleApiClient, removeList);
                            }
                            prefs.edit().remove("profile_" + profile.getId()).apply();
                            saveProfiles();
                            break;
                        case "new":
                            mProfiles.add(profile);
                            saveProfiles();
                            //initGeofences(mGoogleApiClient);
                            break;
                        case "update":
                            for (Profile p : mProfiles) {
                                if (p.getId() == profile.getId()) {
                                    Log.d(TAG, "onActivityResult: updating profile");
                                    p.setName(profile.getName());
                                    p.setRadius(profile.getRadius());
                                    if (!p.getPlace().equals(profile.getPlace())) {
                                        p.setPlace(profile.getPlace());
                                        //initGeofences(mGoogleApiClient);
                                    }
                                    break;
                                }
                            }
                            saveProfiles();
                            break;
                        default:
                            break;
                    }

                    MainFragment fragment = (MainFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);
                    fragment.notifyDataHasChanged();
                    Log.d(TAG, "onActivityResult: profiles.size = " + mProfiles.size());
                }
                return;
            default:
                Log.d(TAG, "onActivityResult: unknown request code");
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void removeAdmin() {
        deviceManger.removeActiveAdmin(compName);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        //Toast.makeText(this, "Google api client Connected", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "onConnected: initGeofences ");
        initGeofences(mGoogleApiClient);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Toast.makeText(this, "Connection suspended", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, "Connection failed", Toast.LENGTH_SHORT).show();
    }
}
