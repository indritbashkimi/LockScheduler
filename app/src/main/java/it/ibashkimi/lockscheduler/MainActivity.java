package it.ibashkimi.lockscheduler;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
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
    private ComponentName compName;
    private GoogleApiClient mGoogleApiClient;
    private PendingIntent mGeofencePendingIntent;
    private ArrayList<Profile> mProfiles;
    private ArrayList<Runnable> mJobs;

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

        getJobs().add(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "run: firstJob");
                if (getProfiles().size() > 0)
                    initGeofences(mGoogleApiClient);
            }
        });

        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    public ArrayList<Runnable> getJobs() {
        if (mJobs == null) {
            mJobs = new ArrayList<>();
        }
        return mJobs;
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
        String jsonArrayRep = getSharedPreferences("prefs", Context.MODE_PRIVATE).getString("profiles", "");
        try {
            JSONArray jsonArray = new JSONArray(jsonArrayRep);
            mProfiles = new ArrayList<>(jsonArray.length());
            for (int i = 0; i < jsonArray.length(); i++) {
                mProfiles.add(Profile.parseJson(jsonArray.get(i).toString()));
            }
        } catch (JSONException e) {
            if (jsonArrayRep.equals(""))
                Log.d(TAG, "restoreProfiles: no stored profiles found");
            else {
                Log.e(TAG, "restoreProfiles: error during restore");
                e.printStackTrace();
            }
            mProfiles = new ArrayList<>();
        }
    }

    private void saveProfiles() {
        JSONArray jsonArray = new JSONArray();
        for (Profile profile : mProfiles) {
            jsonArray.put(profile.toJson());
        }
        getSharedPreferences("prefs", Context.MODE_PRIVATE)
                .edit()
                .putString("profiles", jsonArray.toString())
                .apply();
    }

    @Override
    protected void onStart() {
        Log.d(TAG, "onStart() called");
        super.onStart();
        mGoogleApiClient.connect();
        if (mProfiles == null)
            restoreProfiles();
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop() called");
        mGoogleApiClient.disconnect();
        saveProfiles();
        super.onStop();
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
                    final Profile profile = data.getParcelableExtra("profile");
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
                                final String profileId = Long.toString(profile.getId());
                                getJobs().add(new Runnable() {
                                    @Override
                                    public void run() {
                                        ArrayList<String> removeList = new ArrayList<>(1);
                                        removeList.add(profileId);
                                        LocationServices.GeofencingApi.removeGeofences(mGoogleApiClient, removeList);
                                    }
                                });
                            }
                            saveProfiles();
                            break;
                        case "new":
                            mProfiles.add(profile);
                            saveProfiles();
                            getJobs().add(new Runnable() {
                                @Override
                                public void run() {
                                    initGeofences(mGoogleApiClient);
                                }
                            });
                            break;
                        case "update":
                            for (final Profile p : mProfiles) {
                                if (p.getId() == profile.getId()) {
                                    p.setName(profile.getName());
                                    p.setRadius(profile.getRadius());
                                    if (!p.getPlace().equals(profile.getPlace())) {
                                        p.setPlace(profile.getPlace());
                                        getJobs().add(new Runnable() {
                                            @Override
                                            public void run() {
                                                ArrayList<String> removeList = new ArrayList<>(1);
                                                removeList.add(Long.toString(p.getId()));
                                                LocationServices.GeofencingApi.removeGeofences(mGoogleApiClient, removeList);
                                                initGeofences(mGoogleApiClient);
                                            }
                                        });
                                    }
                                    saveProfiles();
                                    break;
                                }
                            }
                            break;
                        default:
                            break;
                    }
                    MainFragment fragment = (MainFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);
                    fragment.notifyDataHasChanged();
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
        //initGeofences(mGoogleApiClient);
        Handler handler = new Handler();
        for (Runnable job : getJobs()) {
            handler.post(job);
        }
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
