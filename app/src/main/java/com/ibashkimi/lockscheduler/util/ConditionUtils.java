package com.ibashkimi.lockscheduler.util;

import android.content.Context;
import android.support.annotation.StringRes;

import com.ibashkimi.lockscheduler.R;
import com.ibashkimi.lockscheduler.model.Time;
import com.ibashkimi.lockscheduler.model.TimeCondition;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * @author Indrit Bashkimi (mailto: indrit.bashkimi@gmail.com)
 */

public class ConditionUtils {

    public static String daysToString(Context context, TimeCondition timeCondition) {
        ArrayList<Integer> activeDays = new ArrayList<>(7);
        for (int i = 0; i < 7; i++) {
            if (timeCondition.getDaysActive()[i])
                activeDays.add(i);
        }

        if (activeDays.size() == 0)
            return context.getString(R.string.time_condition_days_selection_none);
        if (activeDays.size() == 7)
            return context.getString(R.string.time_condition_days_selection_all);
        StringBuilder res = new StringBuilder();
        res.append(getDayName(context, activeDays.get(0)));
        if (activeDays.size() > 1) {
            res.append(", ");
            for (int i = 1; i < activeDays.size() - 1; i++) {
                res.append(getDayName(context, activeDays.get(i))).append(", ");
            }
            res.append(getDayName(context, activeDays.get(activeDays.size() - 1)));
        }
        return res.toString();
    }

    public static String internvalToString(Time startTime, Time endTime) {
        return String.format(Locale.ENGLISH, "%02d:%02d - %02d:%02d", startTime.hour, startTime.minute, endTime.hour, endTime.minute);
    }

    public static String getDayName(Context context, int dayIndex) {
        @StringRes int stringRes;
        switch (dayIndex) {
            case 0:
                stringRes = R.string.monday_short;
                break;
            case 1:
                stringRes = R.string.tuesday_short;
                break;
            case 2:
                stringRes = R.string.wednesday_short;
                break;
            case 3:
                stringRes = R.string.thursday_short;
                break;
            case 4:
                stringRes = R.string.friday_short;
                break;
            case 5:
                stringRes = R.string.saturday_short;
                break;
            case 6:
                stringRes = R.string.sunday_short;
                break;
            default:
                throw new RuntimeException("Invalid day index: " + dayIndex + ".");
        }
        return context.getString(stringRes);
    }

    public static String concatenate(List<String> stringList, String separator) {
        return concatenate((CharSequence[]) stringList.toArray(), separator);
    }

    public static String concatenate(CharSequence[] stringArray, String separator) {
        if (stringArray.length == 0)
            return "";
        StringBuilder res = new StringBuilder();
        res.append(stringArray[0]);
        if (stringArray.length > 1) {
            res.append(separator);
            for (int i = 1; i < stringArray.length - 1; i++)
                res.append(stringArray[i]).append(separator);
            res.append(stringArray[stringArray.length - 1]);
        }
        return res.toString();
    }
}
