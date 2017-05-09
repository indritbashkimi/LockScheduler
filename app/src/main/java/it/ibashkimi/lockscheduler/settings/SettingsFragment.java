package it.ibashkimi.lockscheduler.settings;

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
import android.support.v7.widget.GridLayoutManager;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.List;

import it.ibashkimi.lockscheduler.R;
import it.ibashkimi.lockscheduler.util.Utils;
import it.ibashkimi.support.preference.ThemeAdapter;
import it.ibashkimi.support.preference.ThemeItem;
import it.ibashkimi.support.preference.ThemePreference;
import it.ibashkimi.support.preference.ThemePreferenceDialogFragmentCompat;
import it.ibashkimi.support.preference.Themes;


public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final int REQUEST_CODE_ALERT_RINGTONE = 0;

    private MaterialDialog themeDialog;

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        getPreferenceManager().setSharedPreferencesName("settings");
        addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void addPreferencesFromResource(@XmlRes int preferencesResId) {
        super.addPreferencesFromResource(preferencesResId);
        // Colored navigation bar
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP && !Utils.hasNavBar(getContext())) {
            PreferenceCategory category = (PreferenceCategory) findPreference("appearance");
            CheckBoxPreference preference = (CheckBoxPreference) findPreference("colored_navigation_bar");
            category.removePreference(preference);
        }
    }

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
        } else if (preference.getKey().equals("theme")) {
            final List<ThemeItem> themes = Themes.getThemeItems();
            int savedThemeId = getPreferenceManager().getSharedPreferences().getInt("theme", Themes.Theme.APP_THEME_DAYNIGHT_INDIGO);
            int themeIndex = -1;
            for (int i = 0; i < themes.size(); i++) {
                if (themes.get(i).id == savedThemeId) {
                    themeIndex = i;
                    break;
                }
            }
            ThemeAdapter themeAdapter = new ThemeAdapter(getContext(), Themes.getThemeItems(), themeIndex, new ThemeAdapter.ThemeSelectedListener() {
                @Override
                public void onThemeSelected(ThemeItem item) {
                    getPreferenceManager().getSharedPreferences().edit().putInt("theme", item.id).apply();
                    if (themeDialog != null) themeDialog.dismiss();
                    themeDialog = null;
                }
            });
            themeDialog = new MaterialDialog.Builder(getContext())
                    .title(R.string.pref_title_theme)
                    // second parameter is an optional layout manager. Must be a LinearLayoutManager or GridLayoutManager.
                    .adapter(themeAdapter, new GridLayoutManager(getContext(), 2))
                    .negativeText(R.string.dialog_action_cancel)
                    .itemsCallbackSingleChoice(themeIndex, new MaterialDialog.ListCallbackSingleChoice() {
                        @Override
                        public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                            setPreference("theme", themes.get(which).id);
                            return true;
                        }
                    })
                    .show();
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
        return getPreferenceManager().getSharedPreferences().getString("notifications_ringtone", null);
    }

    private void setRingtonePreferenceValue(String ringtone) {
        setPreference("notifications_ringtone", ringtone);
    }

    private void setPreference(String key, String value) {
        getPreferenceManager().getSharedPreferences().edit().putString(key, value).apply();
    }

    private void setPreference(String key, int value) {
        getPreferenceManager().getSharedPreferences().edit().putInt(key, value).apply();
    }

    @Override
    public void onStart() {
        super.onStart();
        SharedPreferences settings = getPreferenceManager().getSharedPreferences();
        settings.registerOnSharedPreferenceChangeListener(this);

        EditTextPreference minPasswordLength = (EditTextPreference) findPreference("min_password_length");
        minPasswordLength.setSummary(settings.getString("min_password_length", "4"));
        EditTextPreference minPinLength = (EditTextPreference) findPreference("min_pin_length");
        minPinLength.setSummary(settings.getString("min_pin_length", "4"));
    }

    @Override
    public void onStop() {
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
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
