package it.ibashkimi.lockscheduler.settings;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import it.ibashkimi.lockscheduler.App;
import it.ibashkimi.lockscheduler.R;
import it.ibashkimi.lockscheduler.about.AboutActivity;
import it.ibashkimi.lockscheduler.model.source.ProfilesRepository;
import it.ibashkimi.lockscheduler.ui.BaseActivity;
import com.ibashkimi.support.preference.Themes;
import com.ibashkimi.support.utils.ThemeUtils;


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
            case R.id.action_help:
                Intent helpIntent = new Intent(this, AboutActivity.class);
                helpIntent.setAction(AboutActivity.ACTION_HELP);
                startActivity(helpIntent);
                return true;
            case R.id.action_about:
                Intent aboutIntent = new Intent(this, AboutActivity.class);
                aboutIntent.setAction(AboutActivity.ACTION_ABOUT);
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
                App.getGeofenceApiHelper().initGeofences(ProfilesRepository.getInstance().getProfiles());
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
}

