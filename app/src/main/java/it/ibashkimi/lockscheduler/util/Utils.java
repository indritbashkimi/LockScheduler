package it.ibashkimi.lockscheduler.util;

import android.content.Context;
import android.util.TypedValue;

import java.util.Locale;


public class Utils {

    public static float dpToPx(Context context, int px) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, px, context.getResources().getDisplayMetrics());
    }

    public static boolean hasNavBar (Context context) {
        //http://stackoverflow.com/questions/28983621/detect-soft-navigation-bar-availability-in-android-device-progmatically
        int id = context.getResources().getIdentifier("config_showNavigationBar", "bool", "android");
        return id > 0 && context.getResources().getBoolean(id);
    }

    public static String formatTime(int hours, int minutes) {
        return String.format(Locale.getDefault(), "%02d:%02d", hours, minutes);
    }
}
