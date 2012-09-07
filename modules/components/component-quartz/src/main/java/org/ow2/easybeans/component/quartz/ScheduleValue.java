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

import static org.ow2.easybeans.component.quartz.SchedulePatterns.LAST;
import static org.ow2.easybeans.component.quartz.SchedulePatterns.PATTERN_NDAYS;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;


/**
 * Defines the root class for all the schedule values that are available (list, range, increments, wildcards, etc.).
 * @author Florent Benoit
 */
public abstract class ScheduleValue {

    /**
     * Sunday is also numbered 7.
     */
    public static final int SUNDAY_7 = 7;

    /**
     * Calendar Field on which this increments is applied.
     * It should be second/minute or hour.
     */
    private int calendarField;


    /**
     * Translate string week day into its integer value.
     */
    private Map<String, Integer> weekDaysToInt = null;

    /**
     * The calendar used to extract values.
     */
    private Calendar calendar = null;

    /**
     * Build a new schedule value for the given calendar field.
     * @param calendarField the given calendar field.
     */
    @SuppressWarnings("boxing")
    public ScheduleValue(final int calendarField) {
        this.calendarField = calendarField;
        this.calendar =  new GregorianCalendar();

        // Init the values
        this.weekDaysToInt = new HashMap<String, Integer>();
        this.weekDaysToInt.put("sun", Calendar.SUNDAY);
        this.weekDaysToInt.put("mon", Calendar.MONDAY);
        this.weekDaysToInt.put("tue", Calendar.TUESDAY);
        this.weekDaysToInt.put("wed", Calendar.WEDNESDAY);
        this.weekDaysToInt.put("thu", Calendar.THURSDAY);
        this.weekDaysToInt.put("fri", Calendar.FRIDAY);
        this.weekDaysToInt.put("sat", Calendar.SATURDAY);
    }

    /**
     * If the given input is a nday, extract the number and the day.
     * @param input the given input to handle
     * @return an array of number/day or null
     */
    protected String[] getNDays(final String input) {
        Matcher matcher = PATTERN_NDAYS.matcher(input);
        if (matcher.matches()) {
            return new String[] {matcher.group(1), matcher.group(2)};
        }
        return null;
    }

    /**
     * Find the number day (for example 1st monday) in the given calendar.
     * @param number the number day
     * @param day the day of the week
     * @param calendar the calendar used to compute value
     * @return the computed day
     */
    @SuppressWarnings("boxing")
    protected Integer computeNDays(final String number, final String day, final Calendar calendar) {

        int dayToGet = this.weekDaysToInt.get(day);
        Calendar newCalendar = (Calendar) calendar.clone();

        if (LAST.equals(number)) {
            newCalendar.set(Calendar.DAY_OF_WEEK, dayToGet);
            newCalendar.set(Calendar.DAY_OF_WEEK_IN_MONTH, -1);
            return newCalendar.get(Calendar.DAY_OF_MONTH);
        }

        int wantedNumber = Integer.parseInt(Character.valueOf(number.charAt(0)).toString());

        newCalendar.set(Calendar.DAY_OF_WEEK, dayToGet);
        newCalendar.set(Calendar.DAY_OF_WEEK_IN_MONTH, wantedNumber);


        // Check if month has been changed
        if (calendar.get(Calendar.MONTH) != newCalendar.get(Calendar.MONTH)) {
            return null;
        }
        return newCalendar.get(Calendar.DAY_OF_MONTH);

    }

    /**
     * @return a calendar
     */
    public Calendar getCalendar() {
        return this.calendar;
    }

    /**
     * @return calendar field of this schedule value
     */
    public int getCalendarField() {
        return this.calendarField;
    }

    /**
     * Computes the value for the given calendar by using the current calendar field.
     * @param afterTimeCalendar the calendar to use in order to compute the next
     *        available value
     * @return the next value result for the given calendar for the calendard
     *         field associated.
     */
    public abstract ValueResult getTimeAfter(final Calendar afterTimeCalendar);
}
