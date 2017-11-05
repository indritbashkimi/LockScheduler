package com.ibashkimi.lockscheduler.settings;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.ibashkimi.lockscheduler.R;
import com.ibashkimi.lockscheduler.about.AboutActivity;
import com.ibashkimi.lockscheduler.help.HelpActivity;
import com.ibashkimi.lockscheduler.model.prefs.AppPreferencesHelper;
import com.ibashkimi.lockscheduler.ui.BaseActivity;
import com.ibashkimi.support.activity.ThemePreferences;
import com.ibashkimi.support.utils.ThemeUtils;


public class SettingsActivity extends BaseActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, new SettingsFragment())
                    .commit();
        }
    }

    @Override
    public void onBackPressed() {
        onSupportNavigateUp();
    }

    @Override
    protected void onStart() {
        super.onStart();
        AppPreferencesHelper.INSTANCE.getPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        AppPreferencesHelper.INSTANCE.getPreferences().unregisterOnSharedPreferenceChangeListener(this);
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
            case R.id.action_help:
                startActivity(new Intent(this, HelpActivity.class));
                return true;
            case R.id.action_about:
                startActivityForResult(new Intent(this, AboutActivity.class), 0);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        switch (s) {
            case ThemePreferences.KEY_THEME:
                applyTheme(getThemePreferences().getTheme());
                recreate();
                break;
            case ThemePreferences.KEY_NIGHT_MODE:
                applyNightMode(getThemePreferences().getNightMode());
                recreate();
                break;
            case "loitering_delay":
                Toast.makeText(this, "Not implemented yet", Toast.LENGTH_SHORT).show();
                //App.getGeofenceApiHelper().initGeofences();
                break;
            case ThemePreferences.KEY_COLORED_NAV_BAR:
                applyNavBarColor(getThemePreferences().getColoredNavBar());
                break;
        }
    }
}

