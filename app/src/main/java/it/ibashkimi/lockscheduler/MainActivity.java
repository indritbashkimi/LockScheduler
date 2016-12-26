package it.ibashkimi.lockscheduler;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

import it.ibashkimi.lockscheduler.api.AdminApiHelper;
import it.ibashkimi.lockscheduler.domain.Profile;
import it.ibashkimi.lockscheduler.settings.SettingsActivity;
import it.ibashkimi.support.design.color.Themes;
import it.ibashkimi.support.design.utils.ThemeUtils;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getCanonicalName();

    private static final int RESULT_ADMIN_ENABLE = 1;
    public static final int RESULT_PROFILE = 0;
    private static final int RESULT_PERMISSION = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences settings = getSharedPreferences("prefs", Context.MODE_PRIVATE);
        ThemeUtils.applyDayNightMode(this, settings.getString("theme_mode", "light"));
        @Themes.Theme int themeId = settings.getInt("theme", Themes.Theme.APP_THEME_DAYNIGHT_INDIGO);
        ThemeUtils.applyTheme(this, themeId);

        super.onCreate(savedInstanceState);

        AdminApiHelper adminApiHelper = new AdminApiHelper(this);
        if (!adminApiHelper.isAdminActive()) {
            startActivityForResult(adminApiHelper.buildAddAdminIntent(), RESULT_ADMIN_ENABLE);
        }

        /*if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                // Show an explanation to the user *asynchronously*. After the user
                // sees the explanation, try again to request the permission.
                showWritePermissionRationale();
            } else {
                // No explanation needed, we can request the permission.
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
            }

        }*/

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

        MapsInitializer.initialize(this);
    }

    private Snackbar snackbar;

    /*private void showWritePermissionRationale() {
        View rootView = findViewById(R.id.rootView);
        snackbar = Snackbar.make(rootView, R.string.location_permission_rationale,
                Snackbar.LENGTH_LONG);
        snackbar.setAction(R.string.action_ask_again, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.this.requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        RESULT_PERMISSION);
            }
        });
        snackbar.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE) {
            String permission = permissions[0];
            int grantResult = grantResults[0];
            if (permission.equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                if (grantResult == PackageManager.PERMISSION_GRANTED) {
                    onWritePermissionGranted();
                } else {
                    onWritePermissionDenied();
                }
            }
        }
    }

    private void onWritePermissionDenied() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            showWritePermissionRationale();
        } else {
            Snackbar snackbar = Snackbar.make(this.rootView, R.string.frag_inspector_write_permission_rationale,
                    Snackbar.LENGTH_LONG);
            snackbar.setAction(R.string.frag_inspector_action_settings, v -> {
                final Intent i = new Intent();
                i.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                i.addCategory(Intent.CATEGORY_DEFAULT);
                i.setData(Uri.parse("package:" + getContext().getPackageName()));
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                getContext().startActivity(i);
            });
            snackbar.show();
        }
    }

    private void onWritePermissionGranted() {
        Snackbar.make(this.rootView, R.string.frag_inspector_write_permission_granted,
                Snackbar.LENGTH_SHORT).show();
    }*/

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
            Intent intent = new Intent();
            intent.setClass(this, SettingsActivity.class);
            startActivityForResult(intent, 0);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult() called with: requestCode = [" + requestCode + "], resultCode = [" + resultCode + "], data = [" + data + "]");
        switch (requestCode) {
            case RESULT_ADMIN_ENABLE:
                if (resultCode == Activity.RESULT_OK) {
                    Log.i(TAG, "Admin enabled!");
                } else {
                    Log.i(TAG, "Admin enable FAILED!");
                }
                return;
            case RESULT_PROFILE:
                if (resultCode == Activity.RESULT_OK) {
                    final Profile resultProfile = data.getParcelableExtra("profile");
                    ArrayList<Profile> profiles = Profiles.restoreProfiles(this);
                    Log.d(TAG, "onActivityResult: action" + data.getAction());
                    switch (data.getAction()) {
                        case "delete":
                            for (Profile profile : profiles) {
                                if (profile.getId() == resultProfile.getId()) {
                                    profiles.remove(profile);
                                    break;
                                }
                            }
                            Profiles.saveProfiles(this, profiles);
                            if (resultProfile.isEnabled()) {
                                App.getGeofenceApiHelper().removeGeofence(Long.toString(resultProfile.getId()));
                            }
                            break;
                        case "new":
                            profiles.add(resultProfile);
                            Profiles.saveProfiles(this, profiles);
                            App.getGeofenceApiHelper().initGeofences();
                            break;
                        case "update":
                            for (final Profile profile : profiles) {
                                if (profile.getId() == resultProfile.getId()) {
                                    LatLng previousPlace = profile.getPlace();
                                    int previousRadius = profile.getRadius();
                                    profile.update(resultProfile);
                                    if (!previousPlace.equals(profile.getPlace()) || previousRadius != profile.getRadius()) {
                                        App.getGeofenceApiHelper().removeGeofence(Long.toString(profile.getId()));
                                    }
                                    Profiles.saveProfiles(this, profiles);
                                    App.getGeofenceApiHelper().initGeofences();
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
}
