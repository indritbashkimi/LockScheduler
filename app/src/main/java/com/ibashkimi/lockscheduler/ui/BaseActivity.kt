package com.ibashkimi.lockscheduler.ui

import android.content.Context
import android.content.SharedPreferences
import android.support.v7.app.AppCompatDelegate
import com.ibashkimi.lockscheduler.Constants

import com.ibashkimi.support.activity.DayNightActivity
import com.ibashkimi.support.activity.ThemePreferences
import com.ibashkimi.support.activity.ThemeSupportPreferences


abstract class BaseActivity : DayNightActivity() {

    protected val preferencesName = Constants.PREFERNCES_NAME

    protected val preferences: SharedPreferences by lazy {
        getSharedPreferences(preferencesName, Context.MODE_PRIVATE)
    }

    override val themePreferences: ThemeSupportPreferences
        get() = ThemePreferences(preferences)

    companion object {

        init {
            AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        }
    }
}
