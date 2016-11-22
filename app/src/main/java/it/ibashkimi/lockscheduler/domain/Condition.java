package it.ibashkimi.lockscheduler.domain;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author Indrit Bashkimi (mailto: indrit.bashkimi@studio.unibo.it)
 */

public class Condition {
    public static final int TYPE_LOCATION = 0;
    public static final int TYPE_TIME = 1;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({TYPE_LOCATION, TYPE_TIME})
    public @interface ConditionType {
    }

    private @Condition.ConditionType int conditionType;

    public Condition(int conditionType) {
        this.conditionType = conditionType;
    }
}
