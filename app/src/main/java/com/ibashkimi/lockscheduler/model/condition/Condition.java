package com.ibashkimi.lockscheduler.model.condition;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static com.ibashkimi.lockscheduler.model.condition.Condition.Type.PLACE;
import static com.ibashkimi.lockscheduler.model.condition.Condition.Type.POWER;
import static com.ibashkimi.lockscheduler.model.condition.Condition.Type.TIME;
import static com.ibashkimi.lockscheduler.model.condition.Condition.Type.WIFI;


public abstract class Condition {
    @IntDef({PLACE,
            TIME,
            WIFI,
            POWER
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface Type {
        int PLACE = 0;
        int TIME = 1;
        int WIFI = 2;
        int POWER = 3;
    }

    @Condition.Type
    private int type;
    private boolean isTrue;

    public Condition(int type) {
        this.type = type;
    }

    @Type
    public int getType() {
        return type;
    }

    public boolean isTrue() {
        return isTrue;
    }

    public void setTrue(boolean isTrue) {
        this.isTrue = isTrue;
    }
}
