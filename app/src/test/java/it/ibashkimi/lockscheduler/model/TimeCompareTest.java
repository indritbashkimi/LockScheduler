package it.ibashkimi.lockscheduler.model;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * @author Indrit Bashkimi (mailto: indrit.bashkimi@studio.unibo.it)
 */

public class TimeCompareTest {

    @Test
    public void isEqual() throws Exception {
        TimeCondition.Time time1 = new TimeCondition.Time(0, 0);
        TimeCondition.Time time2 = new TimeCondition.Time(0, 0);
        assertTrue(time1.compareTo(time2).isEqual());
        assertTrue(time1.compareTo(time2).isNotLower());
        assertTrue(time1.compareTo(time2).isNotHigher());
        assertTrue(!time1.compareTo(time2).isLower());
        assertTrue(!time1.compareTo(time2).isHigher());
        assertFalse(time1.compareTo(time2).isHigher());

        time1 = new TimeCondition.Time(1, 5);
        time2 = new TimeCondition.Time(1, 4);
        assertTrue(!time1.compareTo(time2).isEqual());
        assertTrue(time1.compareTo(time2).isNotLower());
        assertTrue(!time1.compareTo(time2).isNotHigher());
        assertTrue(!time1.compareTo(time2).isLower());
        assertTrue(time1.compareTo(time2).isHigher());

        time1 = new TimeCondition.Time(2, 3);
        time2 = new TimeCondition.Time(4, 7);
        assertTrue(!time1.compareTo(time2).isEqual());
        assertTrue(!time1.compareTo(time2).isNotLower());
        assertTrue(time1.compareTo(time2).isNotHigher());
        assertTrue(time1.compareTo(time2).isLower());
        assertTrue(!time1.compareTo(time2).isHigher());
    }
}
