package com.ibashkimi.lockscheduler.model;

public class TimeCondition extends Condition {

    private boolean[] daysActive;

    private Time startTime;

    private Time endTime;

    public TimeCondition() {
        this(new boolean[]{true, true, true, true, true, true, true});
    }

    public TimeCondition(boolean[] daysActive) {
        this(daysActive, new Time(0, 0), new Time(0, 0));
    }

    public TimeCondition(boolean[] daysActive, Time startTime, Time endTime) {
        super(Type.TIME);
        this.daysActive = daysActive;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public boolean[] getDaysActive() {
        return daysActive;
    }

    public Time getStartTime() {
        return startTime;
    }

    public Time getEndTime() {
        return endTime;
    }

    public void setStartTime(Time startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(Time endTime) {
        this.endTime = endTime;
    }

    public void setDaysActive(boolean[] daysActive) {
        this.daysActive = daysActive;
    }

    @Override
    public String toString() {
        return "TimeCondition[" + daysActive[0] + daysActive[1] + daysActive[2] + daysActive[3] + daysActive[4] + daysActive[5] + daysActive[6] + ", startTime=" + startTime + ", endTime=" + endTime + "]";
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof TimeCondition))
            return false;
        TimeCondition condition = (TimeCondition) obj;
        for (int i = 0; i < 7; i++) {
            if (condition.getDaysActive()[i] != daysActive[i])
                return false;
        }
        return condition.startTime.equals(startTime) && condition.endTime.equals(endTime);
    }
}
