package com.ibashkimi.lockscheduler.model;


import com.ibashkimi.lockscheduler.model.action.Action;
import com.ibashkimi.lockscheduler.model.action.LockAction;
import com.ibashkimi.lockscheduler.model.condition.Condition;
import com.ibashkimi.lockscheduler.model.condition.PlaceCondition;
import com.ibashkimi.lockscheduler.model.condition.PowerCondition;
import com.ibashkimi.lockscheduler.model.condition.TimeCondition;
import com.ibashkimi.lockscheduler.model.condition.WifiCondition;

public class ProfileUtils {

    public static PlaceCondition getPlaceCondition(Profile profile) {
        Condition condition = profile.getCondition(Condition.Type.PLACE);
        if (condition != null)
            return (PlaceCondition) condition;
        return null;
    }

    public static TimeCondition getTimeCondition(Profile profile) {
        Condition condition = profile.getCondition(Condition.Type.TIME);
        if (condition != null)
            return (TimeCondition) condition;
        return null;
    }

    public static WifiCondition getWifiCondition(Profile profile) {
        Condition condition = profile.getCondition(Condition.Type.WIFI);
        if (condition != null)
            return (WifiCondition) condition;
        return null;
    }

    public static LockAction getLockAction(Profile profile, boolean fromEnterActions) {
        Action action = profile.getAction(Action.Type.LOCK, fromEnterActions);
        if (action != null)
            return (LockAction) action;
        return null;
    }

    public static PowerCondition getPowerCondition(Profile profile) {
        Condition condition = profile.getCondition(Condition.Type.POWER);
        if (condition != null)
            return (PowerCondition) condition;
        return null;
    }
}
