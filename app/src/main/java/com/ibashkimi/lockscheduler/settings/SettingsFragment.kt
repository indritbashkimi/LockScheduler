package com.ibashkimi.lockscheduler.settings

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.InputType
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.annotation.XmlRes
import androidx.navigation.Navigation.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceFragmentCompat
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.input.input
import com.ibashkimi.lockscheduler.R
import com.ibashkimi.lockscheduler.model.action.LockAction
import com.ibashkimi.lockscheduler.data.prefs.AppPreferencesHelper
import com.ibashkimi.lockscheduler.ui.BaseActivity
import com.ibashkimi.lockscheduler.util.*
import com.ibashkimi.theme.activity.ThemePreferences
import com.ibashkimi.theme.theme.NavBarColor
import com.ibashkimi.theme.theme.NightMode


class SettingsFragment : PreferenceFragmentCompat(),
    SharedPreferences.OnSharedPreferenceChangeListener {

    private val sharedPreferences: SharedPreferences by lazy {
        context!!.getSharedPreferences(AppPreferencesHelper.PREFERENCES_NAME, Context.MODE_PRIVATE)
    }

    private var lockTypeIfGranted: LockAction.LockType
        get() {
            val rep =
                sharedPreferences.getString("lock_if_granted", LockAction.LockType.UNCHANGED.name)!!
            return LockAction.LockType.values().first { it.value == rep }
        }
        set(value) = sharedPreferences.edit().putString("lock_if_granted", value.name).apply()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.settings, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val navController = findNavController(requireActivity(), R.id.main_nav_host_fragment)
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
            ThemePreferences.KEY_NIGHT_MODE -> themeActivity.applyNightMode(
                themeActivity.themePreferences
                    .getNightMode(NightMode.DAYNIGHT)
            )
            ThemePreferences.KEY_NAV_BAR_COLOR -> themeActivity.applyNavBarColor(
                themeActivity.themePreferences
                    .getNavBarColor(NavBarColor.SYSTEM)
            )
            "loitering_delay" -> Toast.makeText(
                requireContext(),
                "Not implemented yet",
                Toast.LENGTH_SHORT
            ).show()
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
            findPreference<PreferenceCategory>("appearance")
                ?.removePreference(findPreference("colored_navigation_bar"))
        }

        findPreference<Preference>("min_pin_length")?.summary =
            "" + getIntPreference("min_pin_length", 4)
        findPreference<Preference>("min_password_length")?.summary =
            "" + getIntPreference("min_password_length", 4)
        val rep = getStringPreference("lock_at_boot", LockAction.LockType.UNCHANGED.name)
        updateSummary(LockAction.LockType.valueOf(rep))
    }

    override fun onPreferenceTreeClick(preference: Preference): Boolean {
        when (preference.key) {
            "notifications" -> {
                val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS).apply {
                        putExtra(Settings.EXTRA_APP_PACKAGE, requireContext().packageName)
                        putExtra(
                            Settings.EXTRA_CHANNEL_ID,
                            PROFILE_ACTIVATED_NOTIFICATION_CHANNEL_ID
                        )
                    }
                } else {
                    val intent = Intent()
                    intent.action = "android.settings.APP_NOTIFICATION_SETTINGS"

                    //for Android 5-7
                    intent.putExtra("app_package", requireContext().packageName)
                    intent.putExtra("app_uid", requireContext().applicationInfo.uid)

                    // for Android 8 and above
                    //intent.putExtra("android.provider.extra.APP_PACKAGE", getPackageName())
                }
                startActivity(intent)
                return true
            }
            "theme" -> {
                findNavController(requireActivity(), R.id.main_nav_host_fragment)
                    .navigate(R.id.action_settings_to_themeFragment)
                return true
            }
            "min_password_length" -> {
                MaterialDialog(requireContext()).show {
                    title(R.string.pref_min_password_length_dialog_title)
                    input(
                        inputType = InputType.TYPE_CLASS_NUMBER,
                        prefill = getIntPreference("min_password_length", 4).toString()
                    ) { _, text ->
                        setPreference("min_password_length", Integer.parseInt(text.toString()))
                    }
                }
                return true
            }
            "min_pin_length" -> {
                MaterialDialog(requireContext()).show {
                    title(R.string.pref_min_pin_length_dialog_title)
                    input(
                        inputType = InputType.TYPE_CLASS_NUMBER,
                        prefill = getIntPreference("min_pin_length", 4).toString()
                    ) { _, charSequence ->
                        setPreference("min_pin_length", Integer.parseInt(charSequence.toString()))
                    }
                }
                return true
            }
            "lock_at_boot" -> {
                showPasswordDialog(
                    LockAction.LockType.valueOf(
                        getStringPreference(
                            "lock_at_boot",
                            LockAction.LockType.UNCHANGED.name
                        )
                    )
                ) { which ->
                    onLockTypeSelected(positionToLockType(which))
                }
            }
        }
        return super.onPreferenceTreeClick(preference)
    }

    private fun onLockTypeSelected(lockType: LockAction.LockType) {
        if (lockType == LockAction.LockType.UNCHANGED) {
            setPreference("lock_at_boot", LockAction.LockType.UNCHANGED.name)
            updateSummary(LockAction.LockType.UNCHANGED)
        } else {
            checkAdminPermission(
                onGranted = {
                    when (lockType) {
                        LockAction.LockType.PIN -> showPinChooser(REQUEST_CODE_PIN)
                        LockAction.LockType.PASSWORD -> showPasswordChooser(REQUEST_CODE_PASSWORD)
                        LockAction.LockType.SWIPE -> {
                            setPreference("lock_at_boot", LockAction.LockType.SWIPE.name)
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

    private fun updateSummary(lockType: LockAction.LockType) {
        findPreference<Preference>("lock_at_boot")?.setSummary(lockTypeToTextRes(lockType))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_CODE_PIN -> if (resultCode == Activity.RESULT_OK) {
                AppPreferencesHelper.lockAtBoot = LockAction.LockType.PIN
                AppPreferencesHelper.lockAtBootInput = data!!.getStringExtra("input")!!
                updateSummary(LockAction.LockType.PIN)
            }
            REQUEST_CODE_PASSWORD -> if (resultCode == Activity.RESULT_OK) {
                AppPreferencesHelper.lockAtBoot = LockAction.LockType.PASSWORD
                AppPreferencesHelper.lockAtBootInput = data!!.getStringExtra("input")!!
                updateSummary(LockAction.LockType.PASSWORD)
            }
            REQUEST_CODE_ADMIN_PERMISSION -> handleAdminPermissionResult(
                resultCode = resultCode,
                onGranted = {
                    Toast.makeText(
                        context,
                        R.string.admin_permission_granted_msg,
                        Toast.LENGTH_SHORT
                    ).show()
                    when (lockTypeIfGranted) {
                        LockAction.LockType.PIN -> showPinChooser(REQUEST_CODE_PASSWORD)
                        LockAction.LockType.PASSWORD -> showPasswordChooser(REQUEST_CODE_PIN)
                        else -> {
                        } // do nothing
                    }
                },
                onDenied = { onAdminPermissionDenied() })
        }
    }

    private fun setPreference(key: String, value: String, updateSummary: Boolean = false) {
        preferenceManager.sharedPreferences.edit().putString(key, value).apply()
        if (updateSummary)
            findPreference<Preference>(key)?.summary = value
    }

    private fun setPreference(key: String, value: Int, updateSummary: Boolean = false) {
        preferenceManager.sharedPreferences.edit().putInt(key, value).apply()
        if (updateSummary)
            findPreference<Preference>(key)?.summary = Integer.toString(value)
    }

    private fun getStringPreference(key: String, defaultValue: String?): String {
        return preferenceManager.sharedPreferences.getString(key, defaultValue)!!
    }

    private fun getIntPreference(key: String, defaultValue: Int): Int {
        return preferenceManager.sharedPreferences.getInt(key, defaultValue)
    }

    private fun onAdminPermissionDenied() {
        Toast.makeText(context, "Admin permission denied", Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val REQUEST_CODE_PIN = 1
        private const val REQUEST_CODE_PASSWORD = 2
        private const val REQUEST_CODE_ADMIN_PERMISSION = 3
    }
}
