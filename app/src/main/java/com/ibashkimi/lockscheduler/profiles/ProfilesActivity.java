package com.ibashkimi.lockscheduler.profiles;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.ibashkimi.lockscheduler.R;
import com.ibashkimi.lockscheduler.about.AboutActivity;
import com.ibashkimi.lockscheduler.help.HelpActivity;
import com.ibashkimi.lockscheduler.intro.IntroActivity;
import com.ibashkimi.lockscheduler.model.ProfileManager;
import com.ibashkimi.lockscheduler.settings.SettingsActivity;
import com.ibashkimi.lockscheduler.ui.BaseActivity;
import com.ibashkimi.lockscheduler.util.PlatformUtils;


public class ProfilesActivity extends BaseActivity {

    private static final String TAG = ProfilesActivity.class.getSimpleName();
    private static final String FRAGMENT_TAG_PROFILES = "main_fragment";
    private static final String FRAGMENT_TAG_PERMISSION_DENIED = "permission_denied_fragment";
    private static final int RESULT_ADMIN_ENABLE = 1;
    private static final int RESULT_LOCATION_PERMISSION = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = getSharedPreferences();
        if (prefs.getBoolean("first_run", true)) {
            startActivity(new Intent(this, IntroActivity.class));
            prefs.edit().putBoolean("first_run", false).apply();
        }

        setContentView(R.layout.activity_profiles);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ProfilesFragment profilesFragment =
                (ProfilesFragment) getSupportFragmentManager().findFragmentById(R.id.profiles_container);
        if (profilesFragment == null) {
            profilesFragment = ProfilesFragment.newInstance();
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.profiles_container, profilesFragment)
                    .commit();
        }

        ProfilesPresenter profilesPresenter = new ProfilesPresenter(
                ProfileManager.INSTANCE, profilesFragment);
        profilesFragment.setPresenter(profilesPresenter);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == RESULT_LOCATION_PERMISSION) {
            String permission = permissions[0];
            int grantResult = grantResults[0];
            if (permission.equals(android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                if (grantResult == PackageManager.PERMISSION_GRANTED) {
                    onLocationPermissionGranted();
                } else {
                    onLocationPermissionDenied();
                }
            }
        }
    }

    private void onShowLocationPermissionRationale() {
        attachPermissionDeniedFragment();
        View rootView = findViewById(R.id.rootView);
        Snackbar snackbar = Snackbar.make(rootView, R.string.location_permission_rationale,
                Snackbar.LENGTH_LONG);
        snackbar.setAction(R.string.action_ask_again, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityCompat.requestPermissions(ProfilesActivity.this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        RESULT_LOCATION_PERMISSION);
            }
        });
        snackbar.show();
    }

    private void onLocationPermissionGranted() {
        attachMainFragment();
        Snackbar.make(findViewById(R.id.rootView), R.string.location_permission_granted,
                Snackbar.LENGTH_SHORT).show();
    }

    private void onLocationPermissionDenied() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)) {
            onShowLocationPermissionRationale();
        } else {
            attachPermissionDeniedFragment();
            Snackbar snackbar = Snackbar.make(findViewById(R.id.rootView), R.string.location_permission_denied,
                    Snackbar.LENGTH_LONG);
            snackbar.setAction(R.string.permission_action_settings, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final Intent i = new Intent();
                    i.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    i.addCategory(Intent.CATEGORY_DEFAULT);
                    i.setData(Uri.parse("package:" + getPackageName()));
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                    i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                    startActivity(i);
                }
            });
            snackbar.show();
        }
    }

    private void attachMainFragment() {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG_PROFILES);
        if (fragment == null)
            fragment = new ProfilesFragment();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.profiles_container, fragment, FRAGMENT_TAG_PROFILES)
                .commit();
    }

    private void attachPermissionDeniedFragment() {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG_PERMISSION_DENIED);
        if (fragment == null)
            fragment = new PermissionDeniedFragment();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.profiles_container, fragment, FRAGMENT_TAG_PERMISSION_DENIED)
                .commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_settings:
                Intent intent = new Intent();
                intent.setClass(this, SettingsActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_help:
                Intent helpIntent = new Intent(this, HelpActivity.class);
                startActivity(helpIntent);
                return true;
            case R.id.action_about:
                Intent aboutIntent = new Intent(this, AboutActivity.class);
                startActivity(aboutIntent);
                return true;
            case R.id.action_uninstall:
                PlatformUtils.uninstall(this);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case RESULT_ADMIN_ENABLE:
                if (resultCode == Activity.RESULT_OK) {
                    Log.i(TAG, "Admin enabled!");
                } else {
                    Log.i(TAG, "Admin enable FAILED!");
                }
                return;
            default:
                Log.d(TAG, "onActivityResult: unknown request code");
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void showSnackBar(String msg) {
        View rootView = findViewById(R.id.rootView);
        Snackbar.make(rootView, msg, Snackbar.LENGTH_SHORT).show();
    }

    public static class PermissionDeniedFragment extends Fragment {
        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_permission_denied, container, false);
        }
    }
}
