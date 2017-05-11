package it.ibashkimi.lockscheduler.model;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static it.ibashkimi.lockscheduler.model.Condition.Type.PLACE;
import static it.ibashkimi.lockscheduler.model.Condition.Type.TIME;
import static it.ibashkimi.lockscheduler.model.Condition.Type.WIFI;


public abstract class Condition {
    @IntDef({PLACE,
            TIME,
            WIFI
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface Type {
        int PLACE = 0;
        int TIME = 1;
        int WIFI = 2;
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
