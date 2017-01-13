package it.ibashkimi.lockscheduler.settings;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.annotation.XmlRes;
import android.support.v4.app.DialogFragment;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.util.Log;

import it.ibashkimi.lockscheduler.R;
import it.ibashkimi.lockscheduler.Utils;
import it.ibashkimi.support.design.preference.ThemePreference;
import it.ibashkimi.support.design.preference.ThemePreferenceDialogFragmentCompat;


public class SettingsFragmentCompat extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final int REQUEST_CODE_ALERT_RINGTONE = 0;
    private SharedPreferences settings;

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        getPreferenceManager().setSharedPreferencesName("prefs");
        addPreferencesFromResource(R.xml.pref_general);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        settings = getActivity().getSharedPreferences("prefs", Context.MODE_PRIVATE);
    }

    @Override
    public void addPreferencesFromResource(@XmlRes int preferencesResId) {
        super.addPreferencesFromResource(preferencesResId);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP && !Utils.hasNavBar(getContext())) {
            Log.d(TAG, "onDisplayPreferenceDialog: removing preference");
            PreferenceCategory category = (PreferenceCategory) findPreference("appearance");
            CheckBoxPreference preference = (CheckBoxPreference) findPreference("colored_navigation_bar");
            category.removePreference(preference);
        }
    }

    private static final String TAG = "SettingsFragmentCompat";

    @Override
    public void onDisplayPreferenceDialog(Preference preference) {
        DialogFragment dialogFragment = null;
        if (preference instanceof ThemePreference) {
            dialogFragment = ThemePreferenceDialogFragmentCompat.newInstance(preference.getKey());
        }

        // If it was one of our custom Preferences, show its dialog
        if (dialogFragment != null) {
            dialogFragment.setTargetFragment(this, 0);
            dialogFragment.show(getFragmentManager(),
                    "android.support.v7.preference" + ".PreferenceFragment.DIALOG");
        } else {
            super.onDisplayPreferenceDialog(preference);
        }
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference.getKey().equals("notifications_ringtone")) {
            Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI, Settings.System.DEFAULT_NOTIFICATION_URI);
            String existingValue = getRingtonePreferenceValue();
            if (existingValue != null) {
                if (existingValue.length() == 0) {
                    // Select "Silent"
                    intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, (Uri) null);
                } else {
                    intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, Uri.parse(existingValue));
                }
            } else {
                // No ringtone has been selected, set to the default
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, Settings.System.DEFAULT_NOTIFICATION_URI);
            }
            startActivityForResult(intent, REQUEST_CODE_ALERT_RINGTONE);
            return true;
        } else {
            return super.onPreferenceTreeClick(preference);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_ALERT_RINGTONE && data != null) {
            Uri ringtone = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
            if (ringtone != null) {
                setRingtonePreferenceValue(ringtone.toString());
            } else {
                // "Silent" was selected
                setRingtonePreferenceValue("");
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Nullable
    private String getRingtonePreferenceValue() {
        return settings.getString("notifications_ringtone", null);
    }

    private void setRingtonePreferenceValue(String ringtone) {
        settings.edit().putString("notifications_ringtone", ringtone).apply();
    }

    @Override
    public void onStart() {
        super.onStart();
        settings.registerOnSharedPreferenceChangeListener(this);

        EditTextPreference minPasswordLength = (EditTextPreference) findPreference("min_password_length");
        minPasswordLength.setSummary(settings.getString("min_password_length", "4"));
        EditTextPreference minPinLength = (EditTextPreference) findPreference("min_pin_length");
        minPinLength.setSummary(settings.getString("min_pin_length", "4"));
    }

    @Override
    public void onStop() {
        settings.unregisterOnSharedPreferenceChangeListener(this);
        super.onStop();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Preference pref = findPreference(key);
        if (pref instanceof EditTextPreference) {
            EditTextPreference listPref = (EditTextPreference) pref;
            pref.setSummary(listPref.getText());
        }
    }
}
