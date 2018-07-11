package com.ibashkimi.lockscheduler.addeditprofile.conditions.time;

public class CompareResult {
    public static final int LOWER = -1;
    public static final int EQUAL = 0;
    public static final int HIGHER = 1;

    private final int result;

    public CompareResult(int result) {
        this.result = result;
    }

    public boolean isLower() {
        return result == LOWER;
    }

    public boolean isNotLower() {
        return result != LOWER;
    }

    public boolean isEqual() {
        return result == EQUAL;
    }

    public boolean isNotEqual() {
        return result != EQUAL;
    }

    public boolean isHigher() {
        return result == HIGHER;
    }

    public boolean isNotHigher() {
        return result != HIGHER;
    }

    public int getResult() {
        return result;
    }
}
