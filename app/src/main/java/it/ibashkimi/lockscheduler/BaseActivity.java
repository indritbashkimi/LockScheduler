package it.ibashkimi.lockscheduler;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import it.ibashkimi.support.design.preference.Themes;
import it.ibashkimi.support.design.utils.ThemeUtils;


public class BaseActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "prefs";
    private static final int DEFAULT_THEME = Themes.Theme.APP_THEME_DAYNIGHT_INDIGO;

    private SharedPreferences mSharedPrefs;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        mSharedPrefs = getSharedPreferences("prefs", Context.MODE_PRIVATE);
        ThemeUtils.applyDayNightMode(this, mSharedPrefs.getString("night_mode", "auto"));
        @Themes.Theme int themeId = mSharedPrefs.getInt("theme", DEFAULT_THEME);
        ThemeUtils.applyTheme(this, themeId);

        super.onCreate(savedInstanceState);
    }

    protected SharedPreferences getSharedPreferences() {
        if (mSharedPrefs == null) {
            mSharedPrefs = getSharedPreferences("prefs", Context.MODE_PRIVATE);
        }
        return mSharedPrefs;
    }
}
