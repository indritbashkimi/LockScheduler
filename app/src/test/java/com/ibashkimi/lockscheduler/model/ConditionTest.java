package com.ibashkimi.lockscheduler.model;

import com.ibashkimi.lockscheduler.model.condition.Condition;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;


public class ConditionTest {

    @Test
    public void getType() {
        Condition condition = new ConditionImpl(Condition.Type.PLACE);
        assertSame(condition.getType(), Condition.Type.PLACE);
        condition = new ConditionImpl(Condition.Type.TIME);
        assertNotSame(condition.getType(), Condition.Type.PLACE);
        assertSame(condition.getType(), Condition.Type.TIME);
        condition = new ConditionImpl(Condition.Type.WIFI);
        assertNotSame(condition.getType(), Condition.Type.PLACE);
        assertNotSame(condition.getType(), Condition.Type.TIME);
        assertSame(condition.getType(), Condition.Type.WIFI);
    }

    @Test
    public void isTrue() {
        Condition condition = new ConditionImpl(Condition.Type.PLACE);
        assertFalse(condition.isTriggered());
        condition.setTriggered(true);
        assertTrue(condition.isTriggered());
    }

    @Test
    public void setTrue() {
        Condition condition = new ConditionImpl(Condition.Type.PLACE);
        condition.setTriggered(false);
        assertFalse(condition.isTriggered());
        condition.setTriggered(true);
        assertTrue(condition.isTriggered());
    }


    private class ConditionImpl extends Condition {

        ConditionImpl(Condition.Type type) {
            super(type, false);
        }
    }

}