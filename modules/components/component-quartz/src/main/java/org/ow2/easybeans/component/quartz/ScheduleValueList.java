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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.NavigableSet;
import java.util.TreeSet;

/**
 * Defines a list of value availables for the given calendar field.
 * @author Florent Benoit
 */
public class ScheduleValueList extends ScheduleValue {

    /**
     * List of values in the current list.
     */
    private List<ScheduleValue> scheduleValues = null;

    /**
     * Build a new value for the given calendar field.
     * @param calendarField the calendar field
     */
    public ScheduleValueList(final int calendarField) {
        super(calendarField);
        this.scheduleValues = new ArrayList<ScheduleValue>();
    }

    /**
     * @return the list of the schedule values
     */
    public List<ScheduleValue> getScheduleValues() {
        return this.scheduleValues;
    }

    /**
     * Adds the given schedule value.
     * @param scheduleValue the value
     */
    public void add(final ScheduleValue scheduleValue) {
        this.scheduleValues.add(scheduleValue);
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

        NavigableSet<Integer> allValuesNoIncrement = new TreeSet<Integer>();
        NavigableSet<Integer> allValuesWithIncrement = new TreeSet<Integer>();

        NavigableSet<Integer> allValues = new TreeSet<Integer>();

        for (ScheduleValue scheduleValue : this.scheduleValues) {
            ValueResult val = scheduleValue.getTimeAfter(afterTimeCalendar);
            if (val.needsIncrement()) {
                allValuesWithIncrement.add(val.getResult());
            } else {
                allValuesNoIncrement.add(val.getResult());
            }
            allValues.add(val.getResult());
        }

        ValueResult valueResult = new ValueResult();
        Integer foundValue = allValuesNoIncrement.ceiling(currentFieldValue);
        if (foundValue != null) {
            valueResult.setResult(foundValue.intValue());
        } else {
            foundValue = allValuesWithIncrement.ceiling(currentFieldValue);
            if (foundValue != null) {
                valueResult.setResult(foundValue.intValue());
            }
        }

        if (foundValue == null) {
            valueResult.setResult(allValues.first());
            valueResult.setNeedsIncrement(true);
        }


        return valueResult;
    }

}
