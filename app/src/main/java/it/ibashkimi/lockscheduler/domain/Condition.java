package it.ibashkimi.lockscheduler.domain;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static it.ibashkimi.lockscheduler.domain.Condition.Type.PLACE;
import static it.ibashkimi.lockscheduler.domain.Condition.Type.TIME;
import static it.ibashkimi.lockscheduler.domain.Condition.Type.WIFI;


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
    private String name;
    private boolean isTrue;

    public Condition(int type, String name) {
        this.type = type;
        this.name = name;
    }

    @Type
    public int getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isTrue() {
        return isTrue;
    }

    public void setTrue(boolean isTrue) {
        this.isTrue = isTrue;
    }

    public abstract String toJson();
}
