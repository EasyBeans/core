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
import static org.ow2.easybeans.component.quartz.SchedulePatterns.WILDCARD_CHARACTER;

import java.util.Calendar;
import java.util.NavigableSet;
import java.util.TreeSet;


/**
 * Defines an ranege like '20-25' or '3-last'.
 * @author Florent Benoit
 */
public class ScheduleValueRange extends ScheduleValue {

    /**
     * Left range.
     */
    private String left;

    /**
     * Right range.
     */
    private String right;


    /**
     * Build a new value for the given left range and the right range for the given calendar field.
     * @param left the left value of the range
     * @param right the right value of the range
     * @param calendarField the calendar field
     */
    public ScheduleValueRange(final String left, final String right, final int calendarField) {
        super(calendarField);
        this.left = left;
        this.right = right;
    }

    /**
     * Computes all available values for the given calendar.
     * @param afterTimeCalendar the calendar to use
     * @return the values
     */
    public NavigableSet<Integer> getValues(final Calendar afterTimeCalendar) {

        int start = 0;
        int end = 0;

        // Check if parts of range are nthdays or not
        String[] leftNDays = getNDays(this.left);
        String[] rightNDays = getNDays(this.right);

        // Special case for wildcard
        if (WILDCARD_CHARACTER.equals(this.left)) {
            start = afterTimeCalendar.getActualMaximum(getCalendarField());
        } else if (LAST.equals(this.left)) {
            start = afterTimeCalendar.getActualMaximum(getCalendarField());
        } else if (leftNDays != null) {
            // Needs to compute the start
            Integer dayStart = computeNDays(leftNDays[0], leftNDays[1], afterTimeCalendar);
            if (dayStart == null) {
                return null;
            }
            start = dayStart.intValue();
        } else {
            start = Integer.parseInt(this.left);
        }
        if (WILDCARD_CHARACTER.equals(this.right)) {
            end = afterTimeCalendar.getActualMaximum(getCalendarField());
        } else if (LAST.equals(this.right)) {
            end = afterTimeCalendar.getActualMaximum(getCalendarField());
        } else if (rightNDays != null) {
            // Needs to compute the start
            Integer dayEnd = computeNDays(rightNDays[0], rightNDays[1], afterTimeCalendar);
            if (dayEnd == null) {
                return null;
            }
            end = dayEnd.intValue();
        } else {
            end = Integer.parseInt(this.right);
        }


        if (Calendar.DAY_OF_WEEK == getCalendarField()) {
            start++;
            // 8 is sunday
            if (start > SUNDAY_7) {
                start = Calendar.SUNDAY;
            }

            end++;
            // 8 is sunday
            if (end > SUNDAY_7) {
                end = Calendar.SUNDAY;
            }

        }


        NavigableSet<Integer> allValues = new TreeSet<Integer>();

        // Special case where start > end like 27-3 means "27-->last and 1-3"
        if (start > end) {
            int leftStart = start;
            int leftEnd = afterTimeCalendar.getActualMaximum(getCalendarField());
            for (int i = leftStart; i <= leftEnd; i++) {
                allValues.add(Integer.valueOf(i));
            }
            int rightStart = afterTimeCalendar.getActualMinimum(getCalendarField());
            int rightEnd = end;
            for (int i = rightStart; i <= rightEnd; i++) {
                allValues.add(Integer.valueOf(i));
            }

        } else {
            // classic range
            for (int i = start; i <= end; i++) {
                allValues.add(Integer.valueOf(i));
            }
        }

        return allValues;
    }


    /**
     * @param afterTimeCalendar the calendar to use in order to compute the next
     *        available value
     * @return the next value result for the given calendar for the calendard
     *         field associated.
     */
    @Override
    @SuppressWarnings("boxing")
    public ValueResult getTimeAfter(final Calendar afterTimeCalendar) {

        int currentFieldValue = afterTimeCalendar.get(getCalendarField());
        NavigableSet<Integer> allValues = getValues(afterTimeCalendar);

        // return current value but by sayinf that we needs to increments
        ValueResult valueResult = new ValueResult();
        if (allValues == null) {
            valueResult.setResult(afterTimeCalendar.get(getCalendarField()));
            valueResult.setNeedsIncrement(true);
        }


        Integer foundValue = allValues.ceiling(currentFieldValue);
        if (foundValue != null) {
            valueResult.setResult(foundValue.intValue());
        } else {
            // use the next first available value
            valueResult.setResult(allValues.first());
            valueResult.setNeedsIncrement(true);
        }

        return valueResult;

    }


    /**
     * @return string representation
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.getClass().getSimpleName());
        sb.append("[range : ");
        sb.append(this.left);
        sb.append(" - ");
        sb.append(this.right);
        sb.append("]");
        return sb.toString();
    }
}
