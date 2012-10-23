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

import java.text.ParseException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.ejb.ScheduleExpression;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests the trigger.
 * @author Florent Benoit
 */
public class TestScheduleTrigger {

    @Test
    public void testScheduleEvery5Seconds() throws ParseException {
        // EasyBeans value
        ScheduleExpression scheduleExpression = new ScheduleExpression().second("*/5").minute("*").hour("*");
        EasyBeansScheduleTrigger scheduleTrigger = new EasyBeansScheduleTrigger(scheduleExpression);

        Calendar now = Calendar.getInstance();
        boolean minuteHasChanged = false;
        int minute = now.get(Calendar.MINUTE);

        List<Integer> allValues = Arrays.asList(new Integer[] {0, 5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55});

        int i = 0;
        while (i < 100) {
            Date newEasybeansDate = scheduleTrigger.getTimeAfter(now.getTime());
            Assert.assertTrue(allValues.contains(newEasybeansDate.getSeconds()));

            Calendar newCalendar = new GregorianCalendar();
            newCalendar.setTime(newEasybeansDate);
            newCalendar.add(Calendar.SECOND, 1);
            if (newCalendar.get(Calendar.MINUTE) != minute) {
                minuteHasChanged = true;
            }

            now = newCalendar;
            i++;
        }

        Assert.assertTrue(minuteHasChanged);
    }


    /**
     * Test when we've duplicates in a field.
     */
    @Test
    public void testDuplicateList() throws ParseException {

        // EasyBeans value
        ScheduleExpression scheduleExpression = new ScheduleExpression().second("*/5").minute("*").hour("*").dayOfMonth(
                "LAST,lAsT").month("3").year("2079");
        Calendar easybeansCalendar = getAfterTime(scheduleExpression);

        // We should get Fri Mar 31 00:00:00 CEST 2079
        Assert.assertEquals(easybeansCalendar.get(Calendar.SECOND), 0);
        Assert.assertEquals(easybeansCalendar.get(Calendar.MINUTE), 0);
        Assert.assertEquals(easybeansCalendar.get(Calendar.HOUR_OF_DAY), 0);
        Assert.assertEquals(easybeansCalendar.get(Calendar.MONTH), Calendar.MARCH);
        Assert.assertEquals(easybeansCalendar.get(Calendar.DAY_OF_MONTH), 31);
        Assert.assertEquals(easybeansCalendar.get(Calendar.YEAR), 2079);

    }

    /**
     * Test of specifying both dayOfWeek and dayOfMonth.
     */
    @Test
    public void testBothDays() throws ParseException {

        // EasyBeans value
        ScheduleExpression scheduleExpression = new ScheduleExpression().second("0").minute("*").hour("*").dayOfMonth("Last")
                .dayOfWeek("Fri-Thu");
        Calendar easybeansCalendar = getAfterTime(scheduleExpression);

        Calendar now = Calendar.getInstance();

        // We should get today as dayofweek is matching all days
        Assert.assertEquals(easybeansCalendar.get(Calendar.SECOND), 0);
        Assert.assertEquals(easybeansCalendar.get(Calendar.MONTH), now.get(Calendar.MONTH));
        Assert.assertEquals(easybeansCalendar.get(Calendar.DAY_OF_MONTH), now.get(Calendar.DAY_OF_MONTH));
        Assert.assertEquals(easybeansCalendar.get(Calendar.DAY_OF_WEEK), now.get(Calendar.DAY_OF_WEEK));

    }

    @Test
    public void testSpecificDayInFuture() {
        ScheduleExpression scheduleExpression = new ScheduleExpression().second("10").minute("20").hour("15").dayOfMonth(
                "last fri").month("7").year("2050");
        Calendar easybeansCalendar = getAfterTime(scheduleExpression);

        Assert.assertEquals(easybeansCalendar.get(Calendar.SECOND), 10);
        Assert.assertEquals(easybeansCalendar.get(Calendar.MINUTE), 20);
        Assert.assertEquals(easybeansCalendar.get(Calendar.HOUR_OF_DAY), 15);
        Assert.assertEquals(easybeansCalendar.get(Calendar.DAY_OF_MONTH), 29);
        Assert.assertEquals(easybeansCalendar.get(Calendar.DAY_OF_WEEK), Calendar.FRIDAY);
        Assert.assertEquals(easybeansCalendar.get(Calendar.MONTH), Calendar.JULY);
        Assert.assertEquals(easybeansCalendar.get(Calendar.YEAR), 2050);

    }

    /**
     * Test the different values of the dayOfWeek and check that the computed
     * time is the good one.
     */
    @Test
    public void testDayOfWeek() {

        // Now
        Calendar now = Calendar.getInstance();
        // now.add(Calendar.SECOND, 1);
        int seconds = now.get(Calendar.SECOND) - 3;
        int minutes = now.get(Calendar.MINUTE);
        if (seconds < 0) {
            seconds = 0;
        }
        int hour = now.get(Calendar.HOUR_OF_DAY);
        int currentDayOfWeek = now.get(Calendar.DAY_OF_WEEK);

        // dayOfWeek goes from 0 to 7 (0 and 7 are sundays)
        for (int dayOfWeek = 0; dayOfWeek <= 7; dayOfWeek++) {
            ScheduleExpression scheduleExpression = new ScheduleExpression().second(seconds).minute(minutes).hour(hour)
                    .dayOfWeek(dayOfWeek);

            EasyBeansScheduleTrigger scheduleTrigger = new EasyBeansScheduleTrigger(scheduleExpression);
            Date easybeansDate = scheduleTrigger.getTimeAfter(now.getTime());

            Calendar easybeansCalendar = Calendar.getInstance();
            easybeansCalendar.setTime(easybeansDate);

            int nextDayOfWeek = easybeansCalendar.get(Calendar.DAY_OF_WEEK);
            int diff = 0;
            if (nextDayOfWeek < currentDayOfWeek) {
                diff = 7 + nextDayOfWeek - currentDayOfWeek;
            } else if (nextDayOfWeek == currentDayOfWeek) {
                diff = 7;
            } else {
                diff = nextDayOfWeek - currentDayOfWeek;
            }

            // 86400 seconds in a day and * 1000 to get it in milliseconds
            long expectedDiff = diff * 86400 * 1000;

            if (easybeansCalendar.getTimeZone().inDaylightTime(easybeansDate) != now.getTimeZone().inDaylightTime(now.getTime())) {
                // Take care of daylight time changing for some locales
                int daylightOffset = Math.abs(now.getTimeZone().getOffset(now.getTime().getTime()) -
                        easybeansCalendar.getTimeZone().getOffset(easybeansDate.getTime()));
                if (easybeansCalendar.getTimeZone().inDaylightTime(easybeansDate)) {
                    expectedDiff -= daylightOffset;
                } else {
                    expectedDiff += daylightOffset;
                }
            }

            long realDiff = easybeansCalendar.getTimeInMillis() - now.getTimeInMillis();

            long delta = Math.abs(expectedDiff - realDiff);

            // Check that the delta is less than 5 seconds
            Assert.assertTrue(delta < 5000, "delta was '" + delta + "' and calendar = '" + easybeansCalendar.getTime()
                    + "' for expression '" + scheduleExpression + "'");
        }

    }


    /**
     * Test the current dayofWeek.
     */
    @Test
    public void testDayOfWeekCurrent() throws InterruptedException {

        // Now
        Calendar now = Calendar.getInstance();
        now.add(Calendar.SECOND, 4);
        int seconds = now.get(Calendar.SECOND);
        int minutes = now.get(Calendar.MINUTE);
        int hour = now.get(Calendar.HOUR_OF_DAY);
        int month = now.get(Calendar.MONTH) + 1;
        int currentDayOfWeek = now.get(Calendar.DAY_OF_WEEK) - 1;

        ScheduleExpression scheduleExpression = new ScheduleExpression().second(seconds).minute(minutes).hour(hour).dayOfWeek(
                currentDayOfWeek).month(month).year(now.get(Calendar.YEAR));
        Calendar easybeansCalendar = getAfterTime(scheduleExpression);

        Assert.assertNotNull(easybeansCalendar);
        Assert.assertEquals(seconds, easybeansCalendar.get(Calendar.SECOND));
        Assert.assertEquals(minutes, easybeansCalendar.get(Calendar.MINUTE));
        Assert.assertEquals(hour, easybeansCalendar.get(Calendar.HOUR_OF_DAY));
        Assert.assertEquals(now.get(Calendar.DAY_OF_WEEK), easybeansCalendar.get(Calendar.DAY_OF_WEEK));
        Assert.assertEquals(now.get(Calendar.MONTH), easybeansCalendar.get(Calendar.MONTH));
    }

    /**
     * Test a value in the future.
     */
    @Test
    public void testSpecificDayInFutureDayInMonthRange() {
        ScheduleExpression scheduleExpression = new ScheduleExpression().second("10").minute("20").hour("15").dayOfMonth(
                "1st Fri-1st Mon").month("7").year("2050");
        Calendar easybeansCalendar = getAfterTime(scheduleExpression);

        Assert.assertEquals(easybeansCalendar.get(Calendar.SECOND), 10);
        Assert.assertEquals(easybeansCalendar.get(Calendar.MINUTE), 20);
        Assert.assertEquals(easybeansCalendar.get(Calendar.HOUR_OF_DAY), 15);
        Assert.assertEquals(easybeansCalendar.get(Calendar.DAY_OF_MONTH), 1);
        Assert.assertEquals(easybeansCalendar.get(Calendar.DAY_OF_WEEK), Calendar.FRIDAY);
        Assert.assertEquals(easybeansCalendar.get(Calendar.MONTH), Calendar.JULY);
        Assert.assertEquals(easybeansCalendar.get(Calendar.YEAR), 2050);
    }

    /**
     * Test another value in the future.
     */
    @Test
    public void testSpecificDayInFutureDayInMonthRangeLastList() {
        ScheduleExpression scheduleExpression = new ScheduleExpression().second("10").minute("20").hour("15").dayOfMonth(
                "last sun,5th sun").month("7").year("2050");
        Calendar easybeansCalendar = getAfterTime(scheduleExpression);

        Assert.assertEquals(easybeansCalendar.get(Calendar.SECOND), 10);
        Assert.assertEquals(easybeansCalendar.get(Calendar.MINUTE), 20);
        Assert.assertEquals(easybeansCalendar.get(Calendar.HOUR_OF_DAY), 15);
        Assert.assertEquals(easybeansCalendar.get(Calendar.DAY_OF_MONTH), 31);
        Assert.assertEquals(easybeansCalendar.get(Calendar.DAY_OF_WEEK), Calendar.SUNDAY);
        Assert.assertEquals(easybeansCalendar.get(Calendar.MONTH), Calendar.JULY);
        Assert.assertEquals(easybeansCalendar.get(Calendar.YEAR), 2050);
    }

    /**
     * Gets the time for a given schedule expression
     */
    protected Calendar getAfterTime(final ScheduleExpression scheduleExpression) {
        EasyBeansScheduleTrigger scheduleTrigger = new EasyBeansScheduleTrigger(scheduleExpression);

        Calendar now = Calendar.getInstance();

        Date easybeansDate = scheduleTrigger.getTimeAfter(now.getTime());
        Calendar easybeansCalendar = Calendar.getInstance();
        if (easybeansDate == null) {
            return null;
        }
        easybeansCalendar.setTime(easybeansDate);
        return easybeansCalendar;
    }

    /**
     * Check for last day of month.
     */
    @Test
    public void testScheduleLastDayOfMonth() throws ParseException {

        // EasyBeans value
        ScheduleExpression scheduleExpression = new ScheduleExpression().second("0").minute("15").hour("23").dayOfMonth("last");
        EasyBeansScheduleTrigger scheduleTrigger = new EasyBeansScheduleTrigger(scheduleExpression);

        Calendar now = Calendar.getInstance();

        Date easybeansDate = scheduleTrigger.getTimeAfter(now.getTime());
        Assert.assertNotNull(easybeansDate);

        Calendar easybeansCalendar = Calendar.getInstance();
        easybeansCalendar.setTime(easybeansDate);

        Assert.assertEquals(0, easybeansCalendar.get(Calendar.SECOND));
        Assert.assertEquals(15, easybeansCalendar.get(Calendar.MINUTE));
        Assert.assertEquals(23, easybeansCalendar.get(Calendar.HOUR_OF_DAY));

        int maxDayOfMonth = now.getActualMaximum(Calendar.DAY_OF_MONTH);

        Assert.assertEquals(maxDayOfMonth, easybeansCalendar.get(Calendar.DAY_OF_MONTH));
        Assert.assertEquals(now.get(Calendar.MONTH), easybeansCalendar.get(Calendar.MONTH));

    }

}
