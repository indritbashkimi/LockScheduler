package it.ibashkimi.lockscheduler.settings;


import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.text.InputType;
import android.util.Log;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.List;

import it.ibashkimi.lockscheduler.R;
import it.ibashkimi.support.design.preference.Theme2Adapter;
import it.ibashkimi.support.design.preference.ThemeAdapter;
import it.ibashkimi.support.design.preference.ThemeItem;
import it.ibashkimi.support.design.preference.Themes;

public class MaterialDialogsSettingsFragment extends SettingsFragment {

    private static final String TAG = "MaterialDialogsSettings";

    private MaterialDialog themeDialog;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.night_mode:
                final String[] nightModeValues = getResources().getStringArray(R.array.pref_night_mode_phone_value);
                int nightModeIndex = -1;
                String nightModeSavedVal = mSharedPreferences.getString("night_mode", nightModeValues[0]);
                for (int i = 0; i < nightModeValues.length; i++)
                    if (nightModeValues[i].equals(nightModeSavedVal)) {
                        nightModeIndex = i;
                        break;
                    }
                new MaterialDialog.Builder(getContext())
                        .title(R.string.pref_title_night_mode)
                        .items(R.array.pref_night_mode_human_value)
                        .itemsIds(R.array.pref_night_mode_phone_value)
                        .negativeText(R.string.dialog_action_cancel)
                        .itemsCallbackSingleChoice(nightModeIndex, new MaterialDialog.ListCallbackSingleChoice() {
                            @Override
                            public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                                String value = nightModeValues[which];
                                mSharedPreferences.edit().putString("night_mode", value).apply();
                                nightModeSummary.setText(text);
                                return true;
                            }
                        })
                        .show();
                break;
            case R.id.theme:
                final List<ThemeItem> themes = Themes.getThemeItems();
                int savedThemeId = mSharedPreferences.getInt("theme", Themes.Theme.APP_THEME_DAYNIGHT_INDIGO);
                int themeIndex = -1;
                for (int i = 0; i < themes.size(); i++) {
                    if (themes.get(i).id == savedThemeId) {
                        themeIndex = i;
                        break;
                    }
                }
                Theme2Adapter themeAdapter = new Theme2Adapter(getContext(), Themes.getThemeItems(), themeIndex, new ThemeAdapter.ColorChooserListener() {
                    @Override
                    public void onColorClicked(ThemeItem item) {
                        mSharedPreferences.edit().putInt("theme", item.id).apply();
                        if (themeDialog != null) themeDialog.dismiss();
                        themeDialog = null;
                    }
                });
                Log.d(TAG, "onClick: themeIndex = " + themeIndex);
                themeDialog = new MaterialDialog.Builder(getContext())
                        .title(R.string.pref_title_theme)
                        // second parameter is an optional layout manager. Must be a LinearLayoutManager or GridLayoutManager.
                        .adapter(themeAdapter, new GridLayoutManager(getContext(), 2))
                        .negativeText(R.string.dialog_action_cancel)
                        .itemsCallbackSingleChoice(themeIndex, new MaterialDialog.ListCallbackSingleChoice() {
                            @Override
                            public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                                Log.d(TAG, "onSelection: which=" + which);
                                mSharedPreferences.edit().putInt("theme", themes.get(which).id).apply();
                                return true;
                            }
                        })
                        .show();
                break;
            case R.id.loitering_delay:
                String[] values = getResources().getStringArray(R.array.pref_loitering_delay_values);
                String savedVal = mSharedPreferences.getString("loitering_delay", values[0]);
                int loiteringIndex = -1;
                for (int i = 0; i < values.length; i++)
                    if (values[i].equals(savedVal)) {
                        loiteringIndex = i;
                        break;
                    }
                new MaterialDialog.Builder(getContext())
                        .title(R.string.pref_title_loitering_delay)
                        .items(R.array.pref_loitering_delay_titles)
                        .negativeText(R.string.dialog_action_cancel)
                        .itemsCallbackSingleChoice(loiteringIndex, new MaterialDialog.ListCallbackSingleChoice() {
                            @Override
                            public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                                String value = getResources().getStringArray(R.array.pref_loitering_delay_values)[which];
                                mSharedPreferences.edit().putString("loitering_delay", value).apply();
                                loiteringDelaySummary.setText(text);
                                return true;
                            }
                        })
                        .show();
                break;
            case R.id.map_style:
                new MaterialDialog.Builder(getContext())
                        .title(R.string.pref_title_map_style)
                        .items(R.array.pref_map_style_human)
                        .itemsIds(R.array.pref_map_style_value)
                        .negativeText(R.string.dialog_action_cancel)
                        .itemsCallbackSingleChoice(mSharedPreferences.getInt("map_style", 0), new MaterialDialog.ListCallbackSingleChoice() {
                            @Override
                            public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                                mSharedPreferences.edit().putInt("map_style", which).apply();
                                mapStyleSummary.setText(text);
                                return true;
                            }
                        })
                        .show();
                break;
            case R.id.profile_item_layout:
                new MaterialDialog.Builder(getContext())
                        .title(R.string.pref_appearance_profile_item_layout)
                        .items(R.array.pref_profile_item_layout_human)
                        .itemsIds(R.array.pref_profile_item_layout_value)
                        .negativeText(R.string.dialog_action_cancel)
                        .itemsCallbackSingleChoice(mSharedPreferences.getInt("item_layout", 0), new MaterialDialog.ListCallbackSingleChoice() {
                            @Override
                            public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                                mSharedPreferences.edit().putInt("item_layout", which).apply();
                                profileItemSummary.setText(text);
                                return true;
                            }
                        })
                        .show();
                break;
            case R.id.password_expiration:
                final String[] passwordValues = getResources().getStringArray(R.array.pref_password_expiration_timeout_values);
                String savedPassVal = mSharedPreferences.getString("password_expiration", passwordValues[0]);
                int passwordExpIndex = -1;
                for (int i = 0; i < passwordValues.length; i++)
                    if (passwordValues[i].equals(savedPassVal)) {
                        passwordExpIndex = i;
                        break;
                    }
                new MaterialDialog.Builder(getContext())
                        .title(R.string.pref_title_password_expiration)
                        .items(R.array.pref_password_expiration_timeout_titles)
                        .negativeText(R.string.dialog_action_cancel)
                        .itemsCallbackSingleChoice(passwordExpIndex, new MaterialDialog.ListCallbackSingleChoice() {
                            @Override
                            public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                                String value = passwordValues[which];
                                mSharedPreferences.edit().putString("password_expiration", value).apply();
                                passwordExpirationSummary.setText(text);
                                return true;
                            }
                        })
                        .show();
                break;
            case R.id.min_pass_len:
                new MaterialDialog.Builder(getContext())
                        .title(R.string.pref_min_password_length_dialog_title)
                        .inputType(InputType.TYPE_CLASS_NUMBER)
                        .input("", mSharedPreferences.getString("min_password_length", "4"), new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                                mSharedPreferences.edit().putString("min_password_length", input.toString()).apply();
                                minPassLenSummary.setText(input);
                            }
                        }).show();
                break;
            case R.id.min_pin_len:
                new MaterialDialog.Builder(getContext())
                        .title(R.string.pref_min_pin_length_dialog_title)
                        .inputType(InputType.TYPE_CLASS_NUMBER)
                        .input("", mSharedPreferences.getString("min_pin_length", "4"), new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                                mSharedPreferences.edit().putString("min_pin_length", input.toString()).apply();
                                minPinLenSummary.setText(input);
                            }
                        }).show();
                break;
            default:
                super.onClick(v);
        }
    }
}
