package com.ibashkimi.lockscheduler.settings

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.support.annotation.XmlRes
import android.support.v4.app.DialogFragment
import android.support.v7.preference.Preference
import android.support.v7.preference.PreferenceCategory
import android.support.v7.preference.PreferenceFragmentCompat
import android.support.v7.widget.GridLayoutManager
import android.text.InputType
import android.widget.Toast
import com.afollestad.materialdialogs.MaterialDialog
import com.ibashkimi.lockscheduler.R
import com.ibashkimi.lockscheduler.model.action.LockAction
import com.ibashkimi.lockscheduler.model.prefs.AppPreferencesHelper
import com.ibashkimi.lockscheduler.util.*
import com.ibashkimi.support.preference.ThemeAdapter
import com.ibashkimi.support.preference.ThemePreference
import com.ibashkimi.support.preference.ThemePreferenceDialogFragmentCompat
import com.ibashkimi.support.preference.Themes


class SettingsFragment : PreferenceFragmentCompat() {

    private var themeDialog: MaterialDialog? = null

    private val sharedPreferences: SharedPreferences by lazy {
        context.getSharedPreferences("settings", Context.MODE_PRIVATE)
    }

    private var lockTypeIfGranted: Int
        get() = sharedPreferences.getInt("lock_if_granted", LockAction.LockType.UNCHANGED)
        set(value) = sharedPreferences.edit().putInt("lock_if_granted", value).apply()

    override fun onCreatePreferences(bundle: Bundle?, s: String?) {
        preferenceManager.sharedPreferencesName = AppPreferencesHelper.PREFERENCES_NAME
        addPreferencesFromResource(R.xml.preferences)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun addPreferencesFromResource(@XmlRes preferencesResId: Int) {
        super.addPreferencesFromResource(preferencesResId)
        // Colored navigation bar
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP && !Utils.hasNavBar(context)) {
            val category = findPreference("appearance") as PreferenceCategory
            category.removePreference(findPreference("colored_navigation_bar"))
        }

        findPreference("min_pin_length").summary = "" + getIntPreference("min_pin_length", 4)
        findPreference("min_password_length").summary = "" + getIntPreference("min_password_length", 4)
        updateSummary(getIntPreference("lock_at_boot", LockAction.LockType.UNCHANGED))
    }

    override fun onDisplayPreferenceDialog(preference: Preference) {
        var dialogFragment: DialogFragment? = null
        if (preference is ThemePreference) {
            dialogFragment = ThemePreferenceDialogFragmentCompat.newInstance(preference.getKey())
        }

        // If it was one of our custom Preferences, show its dialog
        if (dialogFragment != null) {
            dialogFragment.setTargetFragment(this, 0)
            dialogFragment.show(fragmentManager,
                    "android.support.v7.preference" + ".PreferenceFragment.DIALOG")
        } else {
            super.onDisplayPreferenceDialog(preference)
        }
    }

    override fun onPreferenceTreeClick(preference: Preference): Boolean {
        when (preference.key) {
            "notifications_ringtone" -> {
                val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER)
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION)
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true)
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI, Settings.System.DEFAULT_NOTIFICATION_URI)
                val existingValue = ringtonePreferenceValue
                if (existingValue != null) {
                    if (existingValue.isEmpty()) {
                        // Select "Silent"
                        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, null as Uri?)
                    } else {
                        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, Uri.parse(existingValue))
                    }
                } else {
                    // No ringtone has been selected, set to the default
                    intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, Settings.System.DEFAULT_NOTIFICATION_URI)
                }
                startActivityForResult(intent, REQUEST_CODE_ALERT_RINGTONE)
                return true
            }
            "theme" -> {
                val themes = Themes.getThemeItems()
                val savedThemeId = preferenceManager.sharedPreferences.getInt("theme", Themes.Theme.APP_THEME_DAYNIGHT_INDIGO)
                var themeIndex = -1
                for (i in themes.indices) {
                    if (themes[i].id == savedThemeId) {
                        themeIndex = i
                        break
                    }
                }
                val themeAdapter = ThemeAdapter(context, Themes.getThemeItems(), themeIndex, ThemeAdapter.ThemeSelectedListener { item ->
                    preferenceManager.sharedPreferences.edit().putInt("theme", item.id).apply()
                    if (themeDialog != null) themeDialog!!.dismiss()
                    themeDialog = null
                })
                themeDialog = MaterialDialog.Builder(context)
                        .title(R.string.pref_title_theme)
                        // second parameter is an optional layout manager. Must be a LinearLayoutManager or GridLayoutManager.
                        .adapter(themeAdapter, GridLayoutManager(context, 2))
                        .negativeText(R.string.dialog_action_cancel)
                        .itemsCallbackSingleChoice(themeIndex) { dialog, view, which, text ->
                            setPreference("theme", themes[which].id)
                            true
                        }
                        .show()
                return true
            }
            "min_password_length" -> {
                MaterialDialog.Builder(context)
                        .title(R.string.pref_min_password_length_dialog_title)
                        .inputType(InputType.TYPE_CLASS_NUMBER)
                        .input("", "" + getIntPreference("min_password_length", 4)) { dialog, input -> setPreference("min_password_length", Integer.parseInt(input.toString())) }.show()
                return true
            }
            "min_pin_length" -> {
                MaterialDialog.Builder(context)
                        .title(R.string.pref_min_pin_length_dialog_title)
                        .inputType(InputType.TYPE_CLASS_NUMBER)
                        .input("", "" + getIntPreference("min_pin_length", 4)) { dialog, input -> setPreference("min_pin_length", Integer.parseInt(input.toString())) }.show()
                return true
            }
            "lock_at_boot" -> {
                showPasswordDialog(getIntPreference("lock_at_boot", LockAction.LockType.UNCHANGED),
                        { which -> onLockTypeSelected(positionToLockType(which)) })
            }
        }
        return super.onPreferenceTreeClick(preference)
    }

    private fun onLockTypeSelected(lockType: Int) {
        if (lockType == LockAction.LockType.UNCHANGED) {
            setPreference("lock_at_boot", LockAction.LockType.UNCHANGED)
            updateSummary(LockAction.LockType.UNCHANGED)
        } else {
            checkAdminPermission(
                    onGranted = {
                        when (lockType) {
                            LockAction.LockType.PIN -> showPinChooser(REQUEST_CODE_PIN)
                            LockAction.LockType.PASSWORD -> showPasswordChooser(REQUEST_CODE_PASSWORD)
                            LockAction.LockType.SWIPE -> {
                                setPreference("lock_at_boot", LockAction.LockType.SWIPE)
                                updateSummary(LockAction.LockType.SWIPE)
                            }
                            else -> throw IllegalStateException("Unknown lock type $lockType.")
                        }
                    },
                    onRationaleNeeded = {
                        lockTypeIfGranted = lockType
                        showAdminPermissionRationale(
                                onOk = { askAdminPermission(REQUEST_CODE_ADMIN_PERMISSION) },
                                onCancel = { onAdminPermissionDenied() }
                        )
                    },
                    onDenied = {
                        lockTypeIfGranted = lockType
                        askAdminPermission(REQUEST_CODE_ADMIN_PERMISSION)
                    }
            )
        }
    }

    private fun updateSummary(lockType: Int) {
        findPreference("lock_at_boot").setSummary(lockTypeToTextRes(lockType))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_CODE_ALERT_RINGTONE -> {
                if (data != null) {
                    val ringtone = data.getParcelableExtra<Uri>(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
                    if (ringtone != null) {
                        ringtonePreferenceValue = ringtone.toString()
                    } else {
                        // "Silent" was selected
                        ringtonePreferenceValue = ""
                    }
                }
            }
            REQUEST_CODE_PIN -> if (resultCode == Activity.RESULT_OK) {
                AppPreferencesHelper.lockAtBoot = LockAction.LockType.PIN
                AppPreferencesHelper.lockAtBootInput = data!!.getStringExtra("input")
                updateSummary(LockAction.LockType.PIN)
            }
            REQUEST_CODE_PASSWORD -> if (resultCode == Activity.RESULT_OK) {
                AppPreferencesHelper.lockAtBoot = LockAction.LockType.PASSWORD
                AppPreferencesHelper.lockAtBootInput = data!!.getStringExtra("input")
                updateSummary(LockAction.LockType.PASSWORD)
            }
            REQUEST_CODE_ADMIN_PERMISSION -> handleAdminPermissionResult(
                    resultCode = resultCode,
                    onGranted = {
                        Toast.makeText(context, "Admin permission granted", Toast.LENGTH_SHORT).show()
                        when (lockTypeIfGranted) {
                            LockAction.LockType.PIN -> showPinChooser(REQUEST_CODE_PASSWORD)
                            LockAction.LockType.PASSWORD -> showPasswordChooser(REQUEST_CODE_PIN)
                        }
                    },
                    onDenied = { onAdminPermissionDenied() })
        }
    }

    private var ringtonePreferenceValue: String?
        get() = getStringPreference("notifications_ringtone", "")
        set(ringtone) {
            setPreference("notifications_ringtone", ringtone!!, false)
        }

    private fun setPreference(key: String, value: String, updateSummary: Boolean = false) {
        preferenceManager.sharedPreferences.edit().putString(key, value).apply()
        if (updateSummary)
            findPreference(key).summary = value
    }

    private fun setPreference(key: String, value: Int, updateSummary: Boolean = false) {
        preferenceManager.sharedPreferences.edit().putInt(key, value).apply()
        if (updateSummary)
            findPreference(key).summary = Integer.toString(value)
    }

    private fun getStringPreference(key: String, defaultValue: String?): String {
        return preferenceManager.sharedPreferences.getString(key, defaultValue)
    }

    private fun getIntPreference(key: String, defaultValue: Int): Int {
        return preferenceManager.sharedPreferences.getInt(key, defaultValue)
    }

    private fun onAdminPermissionDenied() {
        Toast.makeText(context, "Admin permission denied", Toast.LENGTH_SHORT).show()
    }

    companion object {
        private val REQUEST_CODE_ALERT_RINGTONE = 0
        private val REQUEST_CODE_PIN = 1
        private val REQUEST_CODE_PASSWORD = 2
        private val REQUEST_CODE_ADMIN_PERMISSION = 3
    }
}
