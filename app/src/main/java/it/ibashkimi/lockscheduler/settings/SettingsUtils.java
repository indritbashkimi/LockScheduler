package it.ibashkimi.lockscheduler.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Utilities and constants related to app settings_prefs.
 */
public class SettingsUtils {

    public static final String PREF_NIGHT_MODE = "pref_night_mode";

    public static final String PREF_MAP_STYLE = "pref_map_style";

    public static final String PREF_THEME = "pref_theme";

    public static final String PREF_COLORED_NAV_BAR = "pref_colored_nav_bar";

    public static final String PREF_PROFILE_LAYOUT = "pref_profile_layout";

    public static final String PREF_LOITERING_DELAY = "pref_loitering_delay";

    public static final String PREF_MIN_PASSWORD_LEN = "pref_min_password_len";

    public static final String PREF_MIN_PIN_LEN = "pref_min_pin_len";

    public static final String PREF_SHOW_NOTIFICATIONS = "pref_show_notifications";

    public static final String PREF_RINGTONE = "pref_ringtone";

    public static final String PREF_VIBRATE = "pref_vibrate";

    /**
     * Boolean indicating whether the app has performed the (one-time) welcome flow.
     */
    public static final String PREF_WELCOME_DONE = "pref_welcome_done";

    /**
     * Return true if the first-app-run-activities have already been executed.
     *
     * @param context Context to be used to lookup the {@link android.content.SharedPreferences}.
     */
    public static boolean isFirstRunProcessComplete(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(PREF_WELCOME_DONE, false);
    }

    /**
     * Mark {@code newValue whether} this is the first time the first-app-run-processes have run.
     *
     * @param context  Context to be used to edit the {@link android.content.SharedPreferences}.
     * @param newValue New value that will be set.
     */
    public static void markFirstRunProcessesDone(final Context context, boolean newValue) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putBoolean(PREF_WELCOME_DONE, newValue).apply();
    }

    public static boolean isColoredNavBarEnabled(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(PREF_COLORED_NAV_BAR, false);
    }

    /**
     * Helper method to register a settings_prefs listener. This method does not automatically handle
     * {@code unregisterOnSharedPreferenceChangeListener() un-registering} the listener at the end
     * of the {@code context} lifecycle.
     *
     * @param context  Context to be used to lookup the {@link android.content.SharedPreferences}.
     * @param listener Listener to register.
     */
    public static void registerOnSharedPreferenceChangeListener(final Context context,
                                                                SharedPreferences.OnSharedPreferenceChangeListener listener) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.registerOnSharedPreferenceChangeListener(listener);
    }

    /**
     * Helper method to un-register a settings_prefs listener typically registered with
     * {@code registerOnSharedPreferenceChangeListener()}
     *
     * @param context  Context to be used to lookup the {@link android.content.SharedPreferences}.
     * @param listener Listener to un-register.
     */
    public static void unregisterOnSharedPreferenceChangeListener(final Context context,
                                                                  SharedPreferences.OnSharedPreferenceChangeListener listener) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.unregisterOnSharedPreferenceChangeListener(listener);
    }
}
