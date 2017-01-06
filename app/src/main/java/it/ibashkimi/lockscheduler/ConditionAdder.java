package it.ibashkimi.lockscheduler;


import it.ibashkimi.lockscheduler.domain.Condition;

public interface ConditionAdder {
    void add(@Condition.Type int conditionType);
    void remove(@Condition.Type int conditionType);
}
