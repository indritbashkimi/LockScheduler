package it.ibashkimi.lockscheduler.settings;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.annotation.XmlRes;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import it.ibashkimi.lockscheduler.App;
import it.ibashkimi.lockscheduler.BaseActivity;
import it.ibashkimi.lockscheduler.R;
import it.ibashkimi.lockscheduler.Utils;
import it.ibashkimi.support.design.preference.ThemePreference;
import it.ibashkimi.support.design.preference.ThemePreferenceDialogFragmentCompat;
import it.ibashkimi.support.design.preference.Themes;
import it.ibashkimi.support.design.utils.ThemeUtils;


public class SettingsActivity extends BaseActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, new MaterialDialogsSettingsFragment())
                    .commit();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onStop() {
        getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.settings, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_feedback:
                Utils.sendFeedback(this);
                return true;
            case R.id.action_about:
                Intent aboutIntent = new Intent(this, AboutActivity.class);
                startActivityForResult(aboutIntent, 0);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        switch (s) {
            case "theme":
                @Themes.Theme int themeId = sharedPreferences.getInt("theme_id", Themes.Theme.APP_THEME_DAYNIGHT_INDIGO);
                ThemeUtils.applyTheme(this, themeId);
                recreate();
                break;
            case "night_mode":
                ThemeUtils.applyDayNightMode(this, sharedPreferences.getString("night_mode", "auto"));
                recreate();
                break;
            case "loitering_delay":
                App.getGeofenceApiHelper().initGeofences();
                break;
            case "colored_navigation_bar":
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    boolean coloredNavBar = sharedPreferences.getBoolean("colored_navigation_bar", false);
                    int navBarColor = ThemeUtils.getColorFromAttribute(this, coloredNavBar ? R.attr.colorPrimaryDark : android.R.attr.navigationBarColor);
                    getWindow().setNavigationBarColor(navBarColor);
                }
                break;
        }
    }


    public static class SettingsFragmentCompat extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {
        private static final int REQUEST_CODE_ALERT_RINGTONE = 0;
        private SharedPreferences settings;

        @Override
        public void onCreatePreferences(Bundle bundle, String s) {
            getPreferenceManager().setSharedPreferencesName("prefs");
            addPreferencesFromResource(R.xml.pref_general);
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            settings = getActivity().getSharedPreferences("prefs", Context.MODE_PRIVATE);
        }

        @Override
        public void addPreferencesFromResource(@XmlRes int preferencesResId) {
            super.addPreferencesFromResource(preferencesResId);
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP && !Utils.hasNavBar(getContext())) {
                Log.d(TAG, "onDisplayPreferenceDialog: removing preference");
                PreferenceCategory category = (PreferenceCategory) findPreference("appearance");
                CheckBoxPreference preference = (CheckBoxPreference) findPreference("colored_navigation_bar");
                category.removePreference(preference);
            }
        }

        private static final String TAG = "SettingsFragmentCompat";
        @Override
        public void onDisplayPreferenceDialog(Preference preference) {
            DialogFragment dialogFragment = null;
            if (preference instanceof ThemePreference) {
                dialogFragment = ThemePreferenceDialogFragmentCompat.newInstance(preference.getKey());
            }

            // If it was one of our custom Preferences, show its dialog
            if (dialogFragment != null) {
                dialogFragment.setTargetFragment(this, 0);
                dialogFragment.show(getFragmentManager(),
                        "android.support.v7.preference" + ".PreferenceFragment.DIALOG");
            } else {
                super.onDisplayPreferenceDialog(preference);
            }
        }

        @Override
        public boolean onPreferenceTreeClick(Preference preference) {
            if (preference.getKey().equals("notifications_ringtone")) {
                Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI, Settings.System.DEFAULT_NOTIFICATION_URI);
                String existingValue = getRingtonePreferenceValue();
                if (existingValue != null) {
                    if (existingValue.length() == 0) {
                        // Select "Silent"
                        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, (Uri) null);
                    } else {
                        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, Uri.parse(existingValue));
                    }
                } else {
                    // No ringtone has been selected, set to the default
                    intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, Settings.System.DEFAULT_NOTIFICATION_URI);
                }
                startActivityForResult(intent, REQUEST_CODE_ALERT_RINGTONE);
                return true;
            } else {
                return super.onPreferenceTreeClick(preference);
            }
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            if (requestCode == REQUEST_CODE_ALERT_RINGTONE && data != null) {
                Uri ringtone = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
                if (ringtone != null) {
                    setRingtonePreferenceValue(ringtone.toString());
                } else {
                    // "Silent" was selected
                    setRingtonePreferenceValue("");
                }
            } else {
                super.onActivityResult(requestCode, resultCode, data);
            }
        }

        @Nullable
        private String getRingtonePreferenceValue() {
            return settings.getString("notifications_ringtone", null);
        }

        private void setRingtonePreferenceValue(String ringtone) {
            settings.edit().putString("notifications_ringtone", ringtone).apply();
        }

        @Override
        public void onStart() {
            super.onStart();
            settings.registerOnSharedPreferenceChangeListener(this);

            EditTextPreference minPasswordLength = (EditTextPreference) findPreference("min_password_length");
            minPasswordLength.setSummary(settings.getString("min_password_length", "4"));
            EditTextPreference minPinLength = (EditTextPreference) findPreference("min_pin_length");
            minPinLength.setSummary(settings.getString("min_pin_length", "4"));
        }

        @Override
        public void onStop() {
            settings.unregisterOnSharedPreferenceChangeListener(this);
            super.onStop();
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            Preference pref = findPreference(key);
            if (pref instanceof EditTextPreference) {
                EditTextPreference listPref = (EditTextPreference) pref;
                pref.setSummary(listPref.getText());
            }
        }
    }
}

