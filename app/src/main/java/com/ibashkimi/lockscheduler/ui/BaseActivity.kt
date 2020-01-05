package com.ibashkimi.lockscheduler.ui

import android.content.Context
import android.content.SharedPreferences
import com.ibashkimi.lockscheduler.data.prefs.AppPreferencesHelper
import com.ibashkimi.theme.activity.DayNightActivity
import com.ibashkimi.theme.activity.ThemePreferences
import com.ibashkimi.theme.activity.ThemeSupportPreferences


abstract class BaseActivity : DayNightActivity() {

    private val preferencesName = AppPreferencesHelper.PREFERENCES_NAME

    val preferences: SharedPreferences by lazy {
        getSharedPreferences(preferencesName, Context.MODE_PRIVATE)
    }

    override val themePreferences: ThemeSupportPreferences
        get() = ThemePreferences(preferences)
}
