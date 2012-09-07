/**
 * EasyBeans
 * Copyright (C) 2012 Bull S.A.S.
 * Contact: easybeans@ow2.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * --------------------------------------------------------------------------
 * $Id$
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.component.quartz;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.NavigableSet;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


/**
 * Test increments
 * @author Florent Benoit
 */
public class TestRange {

    private ScheduleExpressionParser scheduleExpressionParser = null;

    @BeforeClass
    public void init() {
        this.scheduleExpressionParser = new ScheduleExpressionParser();
    }


    @Test
    public void testMaxMinRange() {

        List<Integer> expected = Arrays.asList(new Integer[] {1, 2, 3, 4, 5, 6, 7});


        ScheduleValue scheduleValue = this.scheduleExpressionParser.getScheduleValue("FRI-THU", Calendar.DAY_OF_WEEK);
        Assert.assertTrue(scheduleValue instanceof ScheduleValueRange);
        ScheduleValueRange scheduleValueRange = (ScheduleValueRange) scheduleValue;


        Calendar now = Calendar.getInstance();

        NavigableSet<Integer> values = scheduleValueRange.getValues(now);
        Assert.assertEquals(values, expected);

    }

    @Test
    public void testMaxMinRange2() {

        List<Integer> expected = Arrays.asList(new Integer[] {1, 2, 3, 4, 7});


        ScheduleValue scheduleValue = this.scheduleExpressionParser.getScheduleValue("SAT-WED", Calendar.DAY_OF_WEEK);
        Assert.assertTrue(scheduleValue instanceof ScheduleValueRange);
        ScheduleValueRange scheduleValueRange = (ScheduleValueRange) scheduleValue;


        Calendar now = Calendar.getInstance();

        NavigableSet<Integer> values = scheduleValueRange.getValues(now);
        Assert.assertEquals(values, expected);

    }

    @Test
    public void testMaxMinRangeNumericsWeekDays() {

        List<Integer> expected = Arrays.asList(new Integer[] {1, 2, 3, 4, 7});


        // 6-3 is for sat-wed and we will get 7-4 as JDK is using 1 for Sunday (and not 0)
        ScheduleValue scheduleValue = this.scheduleExpressionParser.getScheduleValue("6-3", Calendar.DAY_OF_WEEK);
        Assert.assertTrue(scheduleValue instanceof ScheduleValueRange);
        ScheduleValueRange scheduleValueRange = (ScheduleValueRange) scheduleValue;


        Calendar now = Calendar.getInstance();

        NavigableSet<Integer> values = scheduleValueRange.getValues(now);
        Assert.assertEquals(values, expected);

    }


}
