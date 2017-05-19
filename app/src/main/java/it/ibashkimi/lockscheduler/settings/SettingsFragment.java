package it.ibashkimi.lockscheduler.settings;

import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.XmlRes;
import android.support.v4.app.DialogFragment;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.widget.GridLayoutManager;
import android.text.InputType;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.List;

import it.ibashkimi.lockscheduler.R;
import it.ibashkimi.lockscheduler.util.Utils;
import com.ibashkimi.support.preference.ThemeAdapter;
import com.ibashkimi.support.preference.ThemeItem;
import com.ibashkimi.support.preference.ThemePreference;
import com.ibashkimi.support.preference.ThemePreferenceDialogFragmentCompat;
import com.ibashkimi.support.preference.Themes;


public class SettingsFragment extends PreferenceFragmentCompat {
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
            category.removePreference(findPreference("colored_navigation_bar"));
        }

        findPreference("min_pin_length").setSummary("" + getIntPreference("min_pin_length", 4));
        findPreference("min_password_length").setSummary("" + getIntPreference("min_password_length", 4));
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
        switch (preference.getKey()) {
            case "notifications_ringtone":
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
            case "theme":
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
            case "min_password_length":
                new MaterialDialog.Builder(getContext())
                        .title(R.string.pref_min_password_length_dialog_title)
                        .inputType(InputType.TYPE_CLASS_NUMBER)
                        .input("", "" + getIntPreference("min_password_length", 4), new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                                setPreference("min_password_length", Integer.parseInt(input.toString()));
                            }
                        }).show();
                return true;
            case "min_pin_length":
                new MaterialDialog.Builder(getContext())
                        .title(R.string.pref_min_pin_length_dialog_title)
                        .inputType(InputType.TYPE_CLASS_NUMBER)
                        .input("", "" + getIntPreference("min_pin_length", 4), new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                                setPreference("min_pin_length", Integer.parseInt(input.toString()));
                            }
                        }).show();
                return true;
        }
        return super.onPreferenceTreeClick(preference);
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
        return getStringPreference("notifications_ringtone", null);
    }

    private void setRingtonePreferenceValue(String ringtone) {
        setPreference("notifications_ringtone", ringtone, false);
    }

    /*private void setPreference(String key, String value) {
        setPreference(key, value, true);
    }*/

    private void setPreference(String key, int value) {
        setPreference(key, value, true);
    }

    private void setPreference(String key, String value, boolean updateSummary) {
        getPreferenceManager().getSharedPreferences().edit().putString(key, value).apply();
        if (updateSummary)
            findPreference(key).setSummary(value);
    }

    private void setPreference(String key, int value, boolean updateSummary) {
        getPreferenceManager().getSharedPreferences().edit().putInt(key, value).apply();
        if (updateSummary)
            findPreference(key).setSummary(Integer.toString(value));
    }

    private String getStringPreference(String key, String defaultValue) {
        return getPreferenceManager().getSharedPreferences().getString(key, defaultValue);
    }

    private int getIntPreference(String key, int defaultValue) {
        return getPreferenceManager().getSharedPreferences().getInt(key, defaultValue);
    }
}
