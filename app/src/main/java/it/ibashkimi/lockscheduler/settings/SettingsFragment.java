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
import android.support.v4.app.Fragment;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import it.ibashkimi.lockscheduler.R;
import it.ibashkimi.lockscheduler.Utils;


public class SettingsFragment extends Fragment implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    private static final String TAG = "SettingsFragment";
    private static final int REQUEST_CODE_ALERT_RINGTONE = 0;

    protected TextView nightModeSummary;
    protected TextView mapStyleSummary;
    protected TextView profileItemSummary;
    protected TextView loiteringDelaySummary;
    protected TextView minPassLenSummary;
    protected TextView minPinLenSummary;
    protected TextView passwordExpirationSummary;
    protected CompoundButton coloredNavBarCheckBox;
    protected SwitchCompat notificationSwitch;
    protected SwitchCompat vibrateSwitch;
    protected View ringtone;
    protected View vibrate;
    protected SharedPreferences mSharedPreferences;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSharedPreferences = getContext().getSharedPreferences("prefs", Context.MODE_PRIVATE);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_settings, container, false);

        // Appearance
        View nightMode = rootView.findViewById(R.id.night_mode);
        TextView nightModeTitle = (TextView) nightMode.findViewById(android.R.id.title);
        nightModeTitle.setText(getString(R.string.pref_title_night_mode));
        nightModeSummary = (TextView) nightMode.findViewById(android.R.id.summary);
        int nightModeIndex = 0;
        String nightModeSaved = mSharedPreferences.getString("night_mode", "auto");
        String[] nightModeValues = getResources().getStringArray(R.array.pref_night_mode_phone_value);
        for (int i = 0; i < nightModeValues.length; i++)
            if (nightModeValues[i].equals(nightModeSaved)) {
                nightModeIndex = i;
                break;
            }
        nightModeSummary.setText(getResources().getStringArray(R.array.pref_night_mode_human_value)[nightModeIndex]);
        nightMode.setOnClickListener(this);

        View theme = rootView.findViewById(R.id.theme);
        theme.setOnClickListener(this);

        View mapStyle = rootView.findViewById(R.id.map_style);
        TextView mapStyleTitle = (TextView) mapStyle.findViewById(android.R.id.title);
        mapStyleTitle.setText(getString(R.string.pref_title_map_style));
        mapStyleSummary = (TextView) mapStyle.findViewById(android.R.id.summary);
        mapStyleSummary.setText(getResources().getStringArray(R.array.pref_map_style_human)[mSharedPreferences.getInt("map_style", 0)]);
        mapStyle.setOnClickListener(this);

        View profileItem = rootView.findViewById(R.id.profile_item_layout);
        TextView profileItemTitle = (TextView) profileItem.findViewById(android.R.id.title);
        profileItemTitle.setText(getString(R.string.pref_appearance_profile_item_layout));
        profileItemSummary = (TextView) profileItem.findViewById(android.R.id.summary);
        profileItemSummary.setText(getResources().getStringArray(R.array.pref_profile_item_layout_human)[mSharedPreferences.getInt("item_layout", 0)]);
        profileItem.setOnClickListener(this);

        View coloredNavBar = rootView.findViewById(R.id.colored_nav_bar);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP && !Utils.hasNavBar(getContext())) {
            coloredNavBar.setVisibility(View.GONE);
        } else {
            coloredNavBar.setOnClickListener(this);
            this.coloredNavBarCheckBox = (CheckBox) coloredNavBar.findViewById(R.id.coloredCheckbox);
            this.coloredNavBarCheckBox.setOnCheckedChangeListener(this);
            this.coloredNavBarCheckBox.setChecked(mSharedPreferences.getBoolean("colored_navigation_bar", false));
        }

        // General
        View loiteringDelay = rootView.findViewById(R.id.loitering_delay);
        loiteringDelay.setOnClickListener(this);
        loiteringDelaySummary = (TextView) loiteringDelay.findViewById(android.R.id.summary);
        String[] loiteringDelayArray = getResources().getStringArray(R.array.pref_loitering_delay_values);
        int loiteringIndex = 0;
        String loiteringSaved = mSharedPreferences.getString("loitering_delay", "auto");
        for (int i = 0; i < loiteringDelayArray.length; i++)
            if (loiteringDelayArray[i].equals(loiteringSaved)) {
                loiteringIndex = i;
                break;
            }
        loiteringDelaySummary.setText(getResources().getStringArray(R.array.pref_loitering_delay_titles)[loiteringIndex]);
        loiteringDelay.setOnClickListener(this);

        View minPassLen = rootView.findViewById(R.id.min_pass_len);
        minPassLenSummary = (TextView) minPassLen.findViewById(android.R.id.summary);
        minPassLenSummary.setText(mSharedPreferences.getString("min_password_length", "4"));
        minPassLen.setOnClickListener(this);

        View minPinLen = rootView.findViewById(R.id.min_pin_len);
        minPinLenSummary = (TextView) minPinLen.findViewById(android.R.id.summary);
        minPinLenSummary.setText(mSharedPreferences.getString("min_pin_length", "4"));
        minPinLen.setOnClickListener(this);

        View passwordExpiration = rootView.findViewById(R.id.password_expiration);
        passwordExpiration.setOnClickListener(this);
        passwordExpirationSummary = (TextView) passwordExpiration.findViewById(android.R.id.summary);
        String[] passwordExpirationArray = getResources().getStringArray(R.array.pref_password_expiration_timeout_values);
        int passwordExpirationIndex = 0;
        String passwordExpirationSaved = mSharedPreferences.getString("password_expiration", "auto");
        for (int i = 0; i < passwordExpirationArray.length; i++)
            if (passwordExpirationArray[i].equals(passwordExpirationSaved)) {
                passwordExpirationIndex = i;
                break;
            }
        passwordExpirationSummary.setText(getResources().getStringArray(R.array.pref_password_expiration_timeout_titles)[passwordExpirationIndex]);
        passwordExpiration.setOnClickListener(this);

        // Notifications
        View showNotifications = rootView.findViewById(R.id.show_notifications);
        this.notificationSwitch = (SwitchCompat) showNotifications.findViewById(R.id.notificationSwitchView);
        this.notificationSwitch.setOnCheckedChangeListener(this);
        showNotifications.setOnClickListener(this);

        this.ringtone = rootView.findViewById(R.id.ringtone);
        this.ringtone.setOnClickListener(this);

        this.vibrate = rootView.findViewById(R.id.vibrate);
        this.vibrateSwitch = (SwitchCompat) vibrate.findViewById(R.id.vibrateSwitchView);
        this.vibrateSwitch.setOnCheckedChangeListener(this);
        this.vibrate.setOnClickListener(this);

        this.vibrateSwitch.setChecked(mSharedPreferences.getBoolean("notifications_vibrate", true));
        this.notificationSwitch.setChecked(mSharedPreferences.getBoolean("notifications_show", true));
        if (!notificationSwitch.isChecked()) {
            ringtone.setEnabled(false);
            ringtone.setClickable(false);
            ringtone.findViewById(android.R.id.title).setEnabled(false);
            vibrate.setEnabled(false);
            vibrate.setClickable(false);
            vibrate.findViewById(android.R.id.title).setEnabled(false);
            vibrateSwitch.setEnabled(false);
        }
        return rootView;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.colored_nav_bar:
                coloredNavBarCheckBox.performClick();
                break;
            case R.id.show_notifications:
                notificationSwitch.performClick();
                break;
            case R.id.ringtone:
                showRingtoneDialog();
                break;
            case R.id.vibrate:
                vibrateSwitch.performClick();
                break;
            default:
                Toast.makeText(getContext(), "Not implemented yet", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.coloredCheckbox:
                mSharedPreferences.edit().putBoolean("colored_navigation_bar", isChecked).apply();
                break;
            case R.id.notificationSwitchView:
                mSharedPreferences.edit().putBoolean("notifications_show", isChecked).apply();
                ringtone.setEnabled(isChecked);
                ringtone.setClickable(isChecked);
                ringtone.findViewById(android.R.id.title).setEnabled(isChecked);
                vibrate.setEnabled(isChecked);
                vibrate.setClickable(isChecked);
                vibrate.findViewById(android.R.id.title).setEnabled(isChecked);
                vibrateSwitch.setEnabled(isChecked);
                break;
            case R.id.vibrateSwitchView:
                mSharedPreferences.edit().putBoolean("notifications_vibrate", isChecked).apply();
                break;
            default:
                Toast.makeText(getContext(), "Not implemented yet.", Toast.LENGTH_SHORT).show();
        }
    }

    private void showRingtoneDialog() {
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
        return mSharedPreferences.getString("notifications_ringtone", null);
    }

    private void setRingtonePreferenceValue(String ringtone) {
        mSharedPreferences.edit().putString("notifications_ringtone", ringtone).apply();
    }
}
