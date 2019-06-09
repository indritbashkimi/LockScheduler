package com.ibashkimi.lockscheduler.model.prefs

import android.content.Context
import android.content.SharedPreferences
import com.ibashkimi.lockscheduler.App
import com.ibashkimi.lockscheduler.model.action.LockAction

object AppPreferencesHelper : PreferencesHelper {
    const val PREFERENCES_NAME = "settings"
    const val MAP_STYLE_KEY = "map_style"
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
        get() = get(MAP_STYLE_KEY, "normal")!!
        set(value) = put(MAP_STYLE_KEY, value)

    override var minPasswordLength: Int
        get() = get(MIN_PASSWORD_LENGTH_KEY, 4)
        set(value) = put(MIN_PASSWORD_LENGTH_KEY, value)

    override var minPinLength: Int
        get() = get(MIN_PIN_LENGTH_KEY, 4)
        set(value) = put(MIN_PIN_LENGTH_KEY, value)

    override var showNotifications: Boolean
        get() = get(NOTIFICATIONS_SHOW_KEY, true)
        set(value) = put(NOTIFICATIONS_SHOW_KEY, value)

    override var notificationsRingtone: String
        get() = get(NOTIFICATIONS_RINGTONE_KEY, "DEFAULT_SOUND")!!
        set(value) = put(NOTIFICATIONS_RINGTONE_KEY, value)

    override var isVibrateActive: Boolean
        get() = get(NOTIFICATIONS_VIBRATE_KEY, true)
        set(value) = put(NOTIFICATIONS_VIBRATE_KEY, value)

    override var bootDelay: String
        get() = get(BOOT_DELAY_KEY, "0")!!
        set(value) = put(BOOT_DELAY_KEY, value)

    override var loiteringDelay: String
        get() = get(LOITERING_DELAY_KEY, "0")!!
        set(value) = put(LOITERING_DELAY_KEY, value)

    override var passwordExpiration: String
        get() = get(PASSWORD_EXPIRATION_KEY, "0")!!
        set(value) = put(PASSWORD_EXPIRATION_KEY, value)

    override var lockAtBoot: LockAction.LockType
        get() = preferences.getString("lock_at_boot", null)?.let {
            LockAction.LockType.valueOf(it)
        } ?: LockAction.LockType.UNCHANGED
        set(value) = put("lock_at_boot", value.name)

    override var lockAtBootInput: String
        get() = get("lock_at_boot_input", "")!!
        set(value) = put("lock_at_boot_input", value)

    override var isAdminRationaleNeeded: Boolean
        get() = get("show_admin_permission_rationale", false)
        set(value) = put("show_admin_permission_rationale", value)

    private fun get(key: String, defaultValue: String) = preferences.getString(key, defaultValue)

    private fun get(key: String, defaultValue: Int) = preferences.getInt(key, defaultValue)

    private fun get(key: String, defaultValue: Boolean) = preferences.getBoolean(key, defaultValue)

    private fun put(key: String, value: String) = preferences.edit { putString(key, value) }

    private fun put(key: String, value: Int) = preferences.edit { putInt(key, value) }

    private fun put(key: String, value: Boolean) = preferences.edit { putBoolean(key, value) }

    private inline fun SharedPreferences.edit(func: SharedPreferences.Editor.() -> Unit) {
        val editor = edit()
        editor.func()
        editor.apply()
    }
}