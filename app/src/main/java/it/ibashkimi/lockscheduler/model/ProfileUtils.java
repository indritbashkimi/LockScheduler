package it.ibashkimi.lockscheduler.model;

/**
 * @author Indrit Bashkimi (mailto: indrit.bashkimi@studio.unibo.it)
 */

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
}
