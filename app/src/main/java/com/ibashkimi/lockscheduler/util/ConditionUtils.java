package com.ibashkimi.lockscheduler.util;

import com.ibashkimi.lockscheduler.model.condition.Time;

import java.util.List;
import java.util.Locale;

/**
 * @author Indrit Bashkimi (mailto: indrit.bashkimi@gmail.com)
 */

public class ConditionUtils {

    public static String internvalToString(Time startTime, Time endTime) {
        return String.format(Locale.ENGLISH, "%02d:%02d - %02d:%02d", startTime.getHour(), startTime.getMinute(), endTime.getHour(), endTime.getMinute());
    }
}
