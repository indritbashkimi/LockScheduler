package com.ibashkimi.lockscheduler.util;

import java.util.Locale;


public class Utils {

    public static String formatTime(int hours, int minutes) {
        return String.format(Locale.getDefault(), "%02d:%02d", hours, minutes);
    }
}
