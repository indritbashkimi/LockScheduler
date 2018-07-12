package com.ibashkimi.lockscheduler.settings

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.text.InputType
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.annotation.XmlRes
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceFragmentCompat
import com.afollestad.materialdialogs.MaterialDialog
import com.ibashkimi.lockscheduler.Constants
import com.ibashkimi.lockscheduler.R
import com.ibashkimi.lockscheduler.model.action.LockAction
import com.ibashkimi.lockscheduler.model.prefs.AppPreferencesHelper
import com.ibashkimi.lockscheduler.ui.BaseActivity
import com.ibashkimi.lockscheduler.util.*
import com.ibashkimi.theme.activity.ThemePreferences
import com.ibashkimi.theme.preference.ThemeAdapter
import com.ibashkimi.theme.theme.NavBarColor
import com.ibashkimi.theme.theme.NightMode
import com.ibashkimi.theme.theme.Theme


class SettingsFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {

    private val sharedPreferences: SharedPreferences by lazy {
        context!!.getSharedPreferences(Constants.PREFERNCES_NAME, Context.MODE_PRIVATE)
    }

    private var lockTypeIfGranted: Int
        get() = sharedPreferences.getInt("lock_if_granted", LockAction.LockType.UNCHANGED)
        set(value) = sharedPreferences.edit().putInt("lock_if_granted", value).apply()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.settings, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val navController = Navigation.findNavController(requireActivity(), R.id.main_nav_host_fragment)
        return NavigationUI.onNavDestinationSelected(item, navController)
                || super.onOptionsItemSelected(item)
    }

    override fun onStart() {
        super.onStart()
        AppPreferencesHelper.preferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onStop() {
        super.onStop()
        AppPreferencesHelper.preferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        val themeActivity = requireActivity() as BaseActivity
        when (key) {
            ThemePreferences.KEY_NIGHT_MODE -> themeActivity.applyNightMode(themeActivity.themePreferences
                    .getNightMode(NightMode.DAYNIGHT))
            ThemePreferences.KEY_NAV_BAR_COLOR -> themeActivity.applyNavBarColor(themeActivity.themePreferences
                    .getNavBarColor(NavBarColor.SYSTEM))
            "loitering_delay" -> Toast.makeText(requireContext(), "Not implemented yet", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreatePreferences(bundle: Bundle?, s: String?) {
        preferenceManager.sharedPreferencesName = AppPreferencesHelper.PREFERENCES_NAME
        addPreferencesFromResource(R.xml.preferences)
    }

    override fun addPreferencesFromResource(@XmlRes preferencesResId: Int) {
        super.addPreferencesFromResource(preferencesResId)
        // Colored navigation bar
        if (!Utils.hasNavBar(requireContext())) {
            val category = findPreference("appearance") as PreferenceCategory
            category.removePreference(findPreference("colored_navigation_bar"))
        }

        findPreference("min_pin_length").summary = "" + getIntPreference("min_pin_length", 4)
        findPreference("min_password_length").summary = "" + getIntPreference("min_password_length", 4)
        updateSummary(getIntPreference("lock_at_boot", LockAction.LockType.UNCHANGED))
    }

    override fun onDisplayPreferenceDialog(preference: Preference) {
        var dialogFragment: androidx.fragment.app.DialogFragment? = null
        if (preference is ThemePreference) {
            dialogFragment = ThemePreferenceDialogFragmentCompat.newInstance(preference.getKey())
        }

        // If it was one of our custom Preferences, show its dialog
        if (dialogFragment != null) {
            dialogFragment.setTargetFragment(this, 0)
            dialogFragment.show(fragmentManager,
                    "androidx.preference" + ".PreferenceFragment.DIALOG")
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
                val themePrefs = ThemePreferences(preferenceManager.sharedPreferences)
                val alertDialog = ThemeDialogFragment.newInstance(themePrefs.getTheme(Theme.INDIGO_PINK))
                alertDialog.listener = ThemeAdapter.ThemeSelectedListener { theme ->
                    preferenceManager.sharedPreferences
                            .edit()
                            .putString("theme", theme.name)
                            .apply()

                }
                alertDialog.show(activity!!.supportFragmentManager, "theme_dialog")
                return true
            }
            "min_password_length" -> {
                MaterialDialog.Builder(context!!)
                        .title(R.string.pref_min_password_length_dialog_title)
                        .inputType(InputType.TYPE_CLASS_NUMBER)
                        .input("", "" + getIntPreference("min_password_length", 4)) { _, input -> setPreference("min_password_length", Integer.parseInt(input.toString())) }.show()
                return true
            }
            "min_pin_length" -> {
                MaterialDialog.Builder(context!!)
                        .title(R.string.pref_min_pin_length_dialog_title)
                        .inputType(InputType.TYPE_CLASS_NUMBER)
                        .input("", "" + getIntPreference("min_pin_length", 4)) { _, input -> setPreference("min_pin_length", Integer.parseInt(input.toString())) }.show()
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
        set(ringtone) = setPreference("notifications_ringtone", ringtone!!, false)


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
        private const val REQUEST_CODE_ALERT_RINGTONE = 0
        private const val REQUEST_CODE_PIN = 1
        private const val REQUEST_CODE_PASSWORD = 2
        private const val REQUEST_CODE_ADMIN_PERMISSION = 3
    }
}
