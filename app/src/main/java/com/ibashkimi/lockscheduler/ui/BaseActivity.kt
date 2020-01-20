package com.ibashkimi.lockscheduler.ui

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.ibashkimi.lockscheduler.data.prefs.AppPreferencesHelper


abstract class BaseActivity : AppCompatActivity() {

    private val preferencesName = AppPreferencesHelper.PREFERENCES_NAME

    val preferences: SharedPreferences by lazy {
        getSharedPreferences(preferencesName, Context.MODE_PRIVATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            val nightMode = preferences.getString("night_mode", null) ?: "system_default"
            applyGlobalNightMode(nightMode)
        }

        super.onCreate(savedInstanceState)
    }
}

fun applyGlobalNightMode(nightMode: String) {
    AppCompatDelegate.setDefaultNightMode(
        when (nightMode) {
            "on" -> AppCompatDelegate.MODE_NIGHT_YES
            "system_default" -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            "battery_saver" -> AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY
            "off" -> AppCompatDelegate.MODE_NIGHT_NO
            else -> throw IllegalArgumentException("Invalid night mode $nightMode.")
        }
    )
}

fun Activity.applyNightMode(nightMode: String) {
    applyGlobalNightMode(nightMode)
    recreate()
}
