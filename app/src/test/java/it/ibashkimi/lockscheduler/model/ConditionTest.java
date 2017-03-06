package it.ibashkimi.lockscheduler.model;

import org.junit.Test;

import static org.junit.Assert.*;


public class ConditionTest {

    @Test
    public void getType() throws Exception {
        Condition condition = new ConditionImpl(Condition.Type.PLACE, "Place");
        assertTrue(condition.getType() == Condition.Type.PLACE);
        condition = new ConditionImpl(Condition.Type.TIME, "Time");
        assertTrue(condition.getType() != Condition.Type.PLACE);
        assertTrue(condition.getType() == Condition.Type.TIME);
        condition = new ConditionImpl(Condition.Type.WIFI, "Wifi");
        assertTrue(condition.getType() != Condition.Type.PLACE);
        assertTrue(condition.getType() != Condition.Type.TIME);
        assertTrue(condition.getType() == Condition.Type.WIFI);
    }

    @Test
    public void getName() throws Exception {
        Condition condition = new ConditionImpl(Condition.Type.PLACE, "Place");
        assertTrue(condition.getName().equals("Place"));
        condition = new ConditionImpl(Condition.Type.PLACE, "Time");
        assertTrue(condition.getName().equals("Time"));
    }

    @Test
    public void setName() throws Exception {
        Condition condition = new ConditionImpl(Condition.Type.PLACE, "Place");
        assertTrue(condition.getName().equals("Place"));
        condition.setName("Time");
        assertTrue(condition.getName().equals("Time"));
    }

    @Test
    public void isTrue() throws Exception {
        Condition condition = new ConditionImpl(Condition.Type.PLACE, "Place");
        assertTrue(!condition.isTrue());
        condition.setTrue(true);
        assertTrue(condition.isTrue());
    }

    @Test
    public void setTrue() throws Exception {
        Condition condition = new ConditionImpl(Condition.Type.PLACE, "Place");
        condition.setTrue(false);
        assertTrue(!condition.isTrue());
        condition.setTrue(true);
        assertTrue(condition.isTrue());
    }


    private class ConditionImpl extends Condition {

        ConditionImpl(int type, String name) {
            super(type, name);
        }

        @Override
        public String toJson() {
            return null;
        }
    }

}