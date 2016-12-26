package it.ibashkimi.lockscheduler;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
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
import it.ibashkimi.support.design.utils.*;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getCanonicalName();

    private static final int RESULT_ADMIN_ENABLE = 1;
    public static final int RESULT_PROFILE = 0;

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
