package it.ibashkimi.lockscheduler;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.MapsInitializer;

import org.json.JSONException;

import it.ibashkimi.lockscheduler.adapters.ProfileAdapter;
import it.ibashkimi.lockscheduler.api.ProfileApiHelper;
import it.ibashkimi.lockscheduler.domain.Profile;
import it.ibashkimi.lockscheduler.intro.IntroActivity;
import it.ibashkimi.lockscheduler.settings.AboutActivity;
import it.ibashkimi.lockscheduler.settings.SettingsActivity;


public class MainActivity extends BaseActivity {

    // http://stackoverflow.com/a/36899459
    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    private static final String TAG = MainActivity.class.getCanonicalName();
    private static final String FRAGMENT_TAG_MAIN = "main_fragment";
    private static final String FRAGMENT_TAG_PERMISSION_DENIED = "permission_denied_fragment";
    private static final int RESULT_ADMIN_ENABLE = 1;
    public static final int RESULT_PROFILE = 0;
    private static final int RESULT_LOCATION_PERMISSION = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = getSharedPreferences();
        if (prefs.getBoolean("first_run", true)) {
            startActivity(new Intent(this, IntroActivity.class));
            prefs.edit().putBoolean("first_run", false).apply();
        }

        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                intent.setAction(ProfileActivity.ACTION_NEW);
                startActivityForResult(intent, 0);
            }
        });

        /*// You do not need ACCESS_COARSE_LOCATION permission when you define ACCESS_FINE_LOCATION permission.
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                onShowLocationPermissionRationale();
            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        RESULT_LOCATION_PERMISSION);
            }
        } else*/
        if (savedInstanceState == null) {
            attachMainFragment();
        }

        MapsInitializer.initialize(this);
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
                ActivityCompat.requestPermissions(MainActivity.this,
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
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG_MAIN);
        if (fragment == null)
            fragment = new ProfileListFragment();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment, FRAGMENT_TAG_MAIN)
                .commit();
    }

    private void attachPermissionDeniedFragment() {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG_PERMISSION_DENIED);
        if (fragment == null)
            fragment = new PermissionDeniedFragment();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment, FRAGMENT_TAG_PERMISSION_DENIED)
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
                Intent helpIntent = new Intent(this, AboutActivity.class);
                helpIntent.setAction(AboutActivity.ACTION_HELP);
                startActivity(helpIntent);
                return true;
            case R.id.action_about:
                Intent aboutIntent = new Intent(this, AboutActivity.class);
                startActivity(aboutIntent);
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
                    Profile profile;
                    try {
                        profile = Profile.parseJson(data.getStringExtra("profile"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.e(TAG, "onActivityResult: wtf");
                        break;
                    }
                    ProfileListFragment fragment = (ProfileListFragment) getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG_MAIN);
                    ProfileAdapter adapter = fragment.getAdapter();
                    Log.d(TAG, "onActivityResult: adapter == null? " + (adapter == null));
                    ProfileApiHelper profileApiHelper = App.getProfileApiHelper();
                    switch (data.getAction()) {
                        case "delete":
                            Profile removedProfile = profileApiHelper.getProfileWithId(profile.getId());
                            profileApiHelper.removeProfile(removedProfile);
                            if (adapter != null)
                                adapter.notifyItemRemoved(profileApiHelper.indexOf(profile.getId()));
                            showSnackBar("Profile deleted");
                            break;
                        case "new":
                            profileApiHelper.addProfile(profile);
                            if (adapter != null)
                                adapter.notifyItemInserted(profileApiHelper.getProfiles().size() - 1);
                            break;
                        case "update":
                            profileApiHelper.update(profile);
                            if (adapter != null)
                                adapter.notifyItemChanged(profileApiHelper.indexOf(profile.getId()));
                            break;
                        default:
                            break;
                    }
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
