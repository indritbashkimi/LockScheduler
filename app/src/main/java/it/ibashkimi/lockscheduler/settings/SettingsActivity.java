package it.ibashkimi.lockscheduler.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import it.ibashkimi.lockscheduler.App;
import it.ibashkimi.lockscheduler.R;
import it.ibashkimi.support.design.color.Themes;
import it.ibashkimi.support.design.utils.ThemeUtils;


public class SettingsActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    private SharedPreferences settings;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        settings = getSharedPreferences("prefs", Context.MODE_PRIVATE);
        @Themes.Theme int themeId = settings.getInt("theme", Themes.Theme.APP_THEME_DAYNIGHT_INDIGO);
        setTheme(Themes.resolveTheme(themeId));

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Set up toolbar as ActionBar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(R.id.container, new PrefsFragment())
                .commit();

    }

    @Override
    protected void onStart() {
        super.onStart();
        settings.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onStop() {
        settings.unregisterOnSharedPreferenceChangeListener(this);
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        NavUtils.navigateUpFromSameTask(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        switch (s) {
            case "theme":
                @Themes.Theme int themeId = sharedPreferences.getInt("theme_id", Themes.Theme.APP_THEME_DAYNIGHT_INDIGO);
                ThemeUtils.applyTheme(this, themeId);
                recreate();
                break;
            case "theme_mode":
                ThemeUtils.applyDayNightMode(this, sharedPreferences.getString("theme_mode", "light"));
                recreate();
                break;
            case "loitering_delay":
                App.getGeofenceApiHelper().initGeofences();
                break;
        }
    }


    public static class PrefsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

        private SharedPreferences settings;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            // Define the settings file to use by this settings fragment
            getPreferenceManager().setSharedPreferencesName("prefs");
            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.pref_general);
            settings = getActivity().getSharedPreferences("prefs", Context.MODE_PRIVATE);
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

