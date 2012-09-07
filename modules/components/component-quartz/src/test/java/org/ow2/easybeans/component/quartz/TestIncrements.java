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

import org.testng.Assert;
import org.testng.annotations.Test;


/**
 * Test increments
 * @author Florent Benoit
 */
public class TestIncrements {


    @Test
    public void testBasicIncrementsSeconds() {

        List<Integer> allValues = Arrays.asList(new Integer[] {0,5,10,15,20,25,30,35,40,45,50,55});
        ScheduleValueIncrements scheduleValueIncrements = new ScheduleValueIncrements("*", 5, Calendar.SECOND);

        boolean minuteHasChanged = false;

        Calendar now = Calendar.getInstance();
        int i = 0;
        while (i < 100) {
            ValueResult res = scheduleValueIncrements.getTimeAfter(now);
            now.set(Calendar.SECOND, res.getResult());
            if (res.needsIncrement()) {
                now.add(Calendar.MINUTE, 1);
                minuteHasChanged = true;
            }
            Assert.assertTrue(allValues.contains(now.get(Calendar.SECOND)));
            now.add(Calendar.SECOND, 1);
            i++;
        }
        Assert.assertTrue(minuteHasChanged);
    }



    @Test
    public void testOtherIncrementsSeconds() {

        List<Integer> allValues = Arrays.asList(new Integer[] {45,48,51,54,57});
        ScheduleValueIncrements scheduleValueIncrements = new ScheduleValueIncrements("45", 3, Calendar.SECOND);

        boolean minuteHasChanged = false;

        Calendar now = Calendar.getInstance();
        int i = 0;
        while (i < 30) {
            ValueResult res = scheduleValueIncrements.getTimeAfter(now);
            now.set(Calendar.SECOND, res.getResult());
            if (res.needsIncrement()) {
                now.add(Calendar.MINUTE, 1);
                minuteHasChanged = true;
            }
            Assert.assertTrue(allValues.contains(now.get(Calendar.SECOND)));
            now.add(Calendar.SECOND, 1);
            i++;
        }
        Assert.assertTrue(minuteHasChanged);
    }


    @Test
    public void testOtherIncrementsHours() {

        List<Integer> allValues = Arrays.asList(new Integer[] {19,22});
        ScheduleValueIncrements scheduleValueIncrements = new ScheduleValueIncrements("19", 3, Calendar.HOUR_OF_DAY);

        boolean hourHasChanged = false;

        Calendar now = Calendar.getInstance();
        int i = 0;
        while (i < 30) {
            ValueResult res = scheduleValueIncrements.getTimeAfter(now);
            now.set(Calendar.HOUR_OF_DAY, res.getResult());
            if (res.needsIncrement()) {
                now.add(Calendar.DAY_OF_MONTH, 1);
                now.set(Calendar.MINUTE, 0);
                now.set(Calendar.SECOND, 0);
                hourHasChanged = true;
            }
            Assert.assertTrue(allValues.contains(now.get(Calendar.HOUR_OF_DAY)));
            now.add(Calendar.MINUTE, 28);
            i++;
        }
        Assert.assertTrue(hourHasChanged);
    }

}
