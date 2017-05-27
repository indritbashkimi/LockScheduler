package com.ibashkimi.lockscheduler.model.prefs

import android.content.Context
import com.ibashkimi.support.preference.Themes
import com.ibashkimi.lockscheduler.App

object AppPreferencesHelper : PreferencesHelper {
    const val PREFERENCES_NAME = "settings"
    const val THEME_KEY = "theme_id"
    const val NIGHT_MODE_KEY = "night_mode"
    const val MAP_STYLE_KEY = "map_style"
    const val COLORED_NAVIGATION_BAR_KEY = "colored_navigation_bar"
    const val MIN_PASSWORD_LENGTH_KEY = "min_password_length"
    const val MIN_PIN_LENGTH_KEY = "min_pin_length"
    const val NOTIFICATIONS_SHOW_KEY = "notifications_show"
    const val NOTIFICATIONS_RINGTONE_KEY = "notifications_ringtone"
    const val NOTIFICATIONS_VIBRATE_KEY = "notifications_vibrate"
    const val BOOT_DELAY_KEY = "boot_delay"
    const val LOITERING_DELAY_KEY = "loitering_delay"
    const val PASSWORD_EXPIRATION_KEY = "password_expiration"

    val preferences = App.getInstance().getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)!!

    override var mapStyle: String
        get() = getString(MAP_STYLE_KEY, "normal")
        set(value) = putString(MAP_STYLE_KEY, value)

    override var nightMode: String
        get() = getString(NIGHT_MODE_KEY, "auto")
        set(value) = putString(MAP_STYLE_KEY, value)

    override var theme: Int
        get() = getInt(THEME_KEY, Themes.Theme.APP_THEME_DAYNIGHT_INDIGO)
        set(value) = putInt(THEME_KEY, value)

    override var isColoredNavigationBarActive: Boolean
        get() = getBoolean(COLORED_NAVIGATION_BAR_KEY, false)
        set(value) = putBoolean(COLORED_NAVIGATION_BAR_KEY, value)

    override var minPasswordLength: Int
        get() = getInt(MIN_PASSWORD_LENGTH_KEY, 4)
        set(value) = putInt(MIN_PASSWORD_LENGTH_KEY, value)

    override var minPinLength: Int
        get() = getInt(MIN_PIN_LENGTH_KEY, 4)
        set(value) = putInt(MIN_PIN_LENGTH_KEY, value)

    override var showNotifications: Boolean
        get() = getBoolean(NOTIFICATIONS_SHOW_KEY, true)
        set(value) = putBoolean(NOTIFICATIONS_SHOW_KEY, value)

    override var notificationsRingtone: String
        get() = getString(NOTIFICATIONS_RINGTONE_KEY, "DEFAULT_SOUND")
        set(value) = putString(NOTIFICATIONS_RINGTONE_KEY, value)

    override var isVibrateActive: Boolean
        get() = getBoolean(NOTIFICATIONS_VIBRATE_KEY, true)
        set(value) = putBoolean(NOTIFICATIONS_VIBRATE_KEY, value)

    override var bootDelay: String
        get() = getString(BOOT_DELAY_KEY, "0")
        set(value) = putString(BOOT_DELAY_KEY, value)

    override var loiteringDelay: String
        get() = getString(LOITERING_DELAY_KEY, "0")
        set(value) = putString(LOITERING_DELAY_KEY, value)

    override var passwordExpiration: String
        get() = getString(PASSWORD_EXPIRATION_KEY, "0")
        set(value) = putString(PASSWORD_EXPIRATION_KEY, value)

    private fun getString(key: String, defaultValue: String) = preferences.getString(key, defaultValue)

    private fun putString(key: String, value: String) = preferences.edit().putString(key, value).apply()

    private fun getInt(key: String, defaultValue: Int) = preferences.getInt(key, defaultValue)

    private fun putInt(key: String, value: Int) = preferences.edit().putInt(key, value).apply()

    private fun getBoolean(key: String, defaultValue: Boolean) = preferences.getBoolean(key, defaultValue)

    private fun putBoolean(key: String, value: Boolean) = preferences.edit().putBoolean(key, value).apply()
}