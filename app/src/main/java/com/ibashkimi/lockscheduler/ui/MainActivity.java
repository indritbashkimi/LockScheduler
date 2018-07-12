package com.ibashkimi.lockscheduler.ui;

import android.os.Bundle;

import com.ibashkimi.lockscheduler.R;

import androidx.appcompat.widget.Toolbar;
import androidx.navigation.Navigation;


public class MainActivity extends BaseActivity {

    /*private static final String TAG = MainActivity.class.getSimpleName();
    private static final String FRAGMENT_TAG_PROFILES = "main_fragment";
    private static final String FRAGMENT_TAG_PERMISSION_DENIED = "permission_denied_fragment";
    private static final int RESULT_ADMIN_ENABLE = 1;
    private static final int RESULT_LOCATION_PERMISSION = 2;*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*SharedPreferences prefs = getPreferences();
        if (prefs.getBoolean("first_run", true)) {
            startActivity(new Intent(this, IntroActivity.class));
            prefs.edit().putBoolean("first_run", false).apply();
        }*/
        // TODO: 7/12/18

        setContentView(R.layout.activity_profiles);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    public boolean onSupportNavigateUp() {
        return Navigation.findNavController(this, R.id.main_nav_host_fragment).navigateUp();
    }

    /*@Override
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
        snackbar.setAction(R.string.action_ask_again, view -> ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                RESULT_LOCATION_PERMISSION));
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
            snackbar.setAction(R.string.permission_action_settings, v -> {
                final Intent i = new Intent();
                i.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                i.addCategory(Intent.CATEGORY_DEFAULT);
                i.setData(Uri.parse("package:" + getPackageName()));
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                startActivity(i);
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
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_permission_denied, container, false);
        }
    }*/
}
