package it.ibashkimi.lockscheduler.domain;


import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static it.ibashkimi.lockscheduler.domain.Action.Type.LOCK;

public abstract class Action {

    @IntDef({LOCK})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Type {
        int LOCK = 0;
    }

    @Type
    private int type;

    public Action(int type) {
        this.type = type;
    }

    @Type
    public int getType() {
        return type;
    }

    public void setType(@Type int type) {
        this.type = type;
    }

    public abstract void doJob();

    public abstract String toJson();
}


