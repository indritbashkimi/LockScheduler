package it.ibashkimi.lockscheduler.model;

import org.junit.Test;

import static org.junit.Assert.*;


public class ConditionTest {

    @Test
    public void getType() throws Exception {
        Condition condition = new ConditionImpl(Condition.Type.PLACE);
        assertTrue(condition.getType() == Condition.Type.PLACE);
        condition = new ConditionImpl(Condition.Type.TIME);
        assertTrue(condition.getType() != Condition.Type.PLACE);
        assertTrue(condition.getType() == Condition.Type.TIME);
        condition = new ConditionImpl(Condition.Type.WIFI);
        assertTrue(condition.getType() != Condition.Type.PLACE);
        assertTrue(condition.getType() != Condition.Type.TIME);
        assertTrue(condition.getType() == Condition.Type.WIFI);
    }

    @Test
    public void isTrue() throws Exception {
        Condition condition = new ConditionImpl(Condition.Type.PLACE);
        assertTrue(!condition.isTrue());
        condition.setTrue(true);
        assertTrue(condition.isTrue());
    }

    @Test
    public void setTrue() throws Exception {
        Condition condition = new ConditionImpl(Condition.Type.PLACE);
        condition.setTrue(false);
        assertTrue(!condition.isTrue());
        condition.setTrue(true);
        assertTrue(condition.isTrue());
    }


    private class ConditionImpl extends Condition {

        ConditionImpl(int type) {
            super(type);
        }
    }

}