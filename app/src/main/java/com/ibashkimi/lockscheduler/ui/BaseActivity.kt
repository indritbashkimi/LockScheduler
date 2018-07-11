package com.ibashkimi.lockscheduler.ui

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import com.ibashkimi.lockscheduler.Constants
import com.ibashkimi.theme.activity.DayNightActivity
import com.ibashkimi.theme.activity.ThemePreferences
import com.ibashkimi.theme.activity.ThemeSupportPreferences


abstract class BaseActivity : DayNightActivity() {

    protected val preferencesName = Constants.PREFERNCES_NAME

    protected val preferences: SharedPreferences by lazy {
        getSharedPreferences(preferencesName, Context.MODE_PRIVATE)
    }

    override val themePreferences: ThemeSupportPreferences
        get() = ThemePreferences(preferences)
}
