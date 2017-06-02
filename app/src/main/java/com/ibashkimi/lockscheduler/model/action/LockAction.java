package com.ibashkimi.lockscheduler.model.action;


import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Locale;

import static com.ibashkimi.lockscheduler.model.action.LockAction.LockType.PASSWORD;
import static com.ibashkimi.lockscheduler.model.action.LockAction.LockType.PIN;
import static com.ibashkimi.lockscheduler.model.action.LockAction.LockType.SWIPE;
import static com.ibashkimi.lockscheduler.model.action.LockAction.LockType.UNCHANGED;

public class LockAction extends Action {

    @IntDef({PIN, PASSWORD, SWIPE, UNCHANGED})
    @Retention(RetentionPolicy.SOURCE)
    public @interface LockType {
        int PIN = 0;
        int PASSWORD = 1;
        int SWIPE = 2;
        int UNCHANGED = 3;
    }

    @LockType
    private int lockType;
    private String input;

    public LockAction() {
        this(LockType.UNCHANGED);
    }

    public LockAction(@LockType int lockType) {
        this(lockType, "");
    }

    public LockAction(@LockType int lockType, String input) {
        super(Type.LOCK);
        this.lockType = lockType;
        this.input = input;
    }

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    @LockType
    public int getLockType() {
        return lockType;
    }

    public void setLockType(@LockType int lockType) {
        this.lockType = lockType;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof LockAction))
            return false;
        LockAction lockAction = (LockAction) obj;
        if (lockType != lockAction.lockType)
            return false;
        switch (lockType) {
            case LockType.PASSWORD:
                if (!input.equals(lockAction.getInput())) return false;
                break;
            case LockType.PIN:
                if (!input.equals(lockAction.getInput())) return false;
                break;
            case LockType.SWIPE:
                break;
            case LockType.UNCHANGED:
                break;
        }
        return true;
    }

    @Override
    public String toString() {
        return String.format(Locale.ENGLISH, "LockMode[%s]", lockTypeToString(lockType));
    }

    public static String lockTypeToString(@LockAction.LockType int lockType) {
        switch (lockType) {
            case LockAction.LockType.UNCHANGED:
                return "Unchanged";
            case LockAction.LockType.PASSWORD:
                return "Password";
            case LockAction.LockType.PIN:
                return "PIN";
            case LockAction.LockType.SWIPE:
                return "Swipe";
            default:
                return "Unknown";
        }
    }
}
