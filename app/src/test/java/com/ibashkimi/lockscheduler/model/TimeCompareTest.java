package com.ibashkimi.lockscheduler.model;

import com.ibashkimi.lockscheduler.model.condition.Time;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


public class TimeCompareTest {

    @Test
    public void isEqual() throws Exception {
        Time time1 = new Time(0, 0);
        Time time2 = new Time(0, 0);
        assertTrue(time1.compareTo(time2).isEqual());
        assertTrue(time1.compareTo(time2).isNotLower());
        assertTrue(time1.compareTo(time2).isNotHigher());
        assertTrue(!time1.compareTo(time2).isLower());
        assertTrue(!time1.compareTo(time2).isHigher());
        assertFalse(time1.compareTo(time2).isHigher());

        time1 = new Time(1, 5);
        time2 = new Time(1, 4);
        assertTrue(!time1.compareTo(time2).isEqual());
        assertTrue(time1.compareTo(time2).isNotLower());
        assertTrue(!time1.compareTo(time2).isNotHigher());
        assertTrue(!time1.compareTo(time2).isLower());
        assertTrue(time1.compareTo(time2).isHigher());

        time1 = new Time(2, 3);
        time2 = new Time(4, 7);
        assertTrue(!time1.compareTo(time2).isEqual());
        assertTrue(!time1.compareTo(time2).isNotLower());
        assertTrue(time1.compareTo(time2).isNotHigher());
        assertTrue(time1.compareTo(time2).isLower());
        assertTrue(!time1.compareTo(time2).isHigher());
    }
}
