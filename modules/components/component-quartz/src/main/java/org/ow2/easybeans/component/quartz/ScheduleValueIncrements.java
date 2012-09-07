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

import static org.ow2.easybeans.component.quartz.SchedulePatterns.WILDCARD_CHARACTER;

import java.util.Calendar;
import java.util.NavigableSet;
import java.util.TreeSet;

/**
 * Defines an increments like '* / 5' or 10/5.
 * @author Florent Benoit
 */
public class ScheduleValueIncrements extends ScheduleValue {

    /**
     * Starting point. Wildcard is transformed into 0.
     */
    private int startingPoint;

    /**
     * Interval of the increments.
     */
    private int interval;

    /**
     * All possible values.
     */
    private NavigableSet<Integer> allValues = null;

    /**
     * Build a new value for the given start and the given interval for the given calendar field.
     * @param startingPoint start value
     * @param interval interval period
     * @param calendarField the calendar field
     */
    public ScheduleValueIncrements(final String startingPoint, final int interval, final int calendarField) {
        super(calendarField);
        if (WILDCARD_CHARACTER.equals(startingPoint)) {
            this.startingPoint = Calendar.getInstance().getMinimum(calendarField);
        } else {
            this.startingPoint = Integer.parseInt(startingPoint);
        }
        this.interval = interval;
        this.allValues = new TreeSet<Integer>();

        // initialize all possible values for the given increments
        init();

    }

    /**
     * Initialize all possible values for the given increments.
     */
    @SuppressWarnings("boxing")
    private void init() {
        int current = this.startingPoint;

        this.allValues.add(current);
        current += this.interval;
        while (current < getCalendar().getMaximum(getCalendarField())) {
            this.allValues.add(current);
            current += this.interval;
        }
    }

    /**
     * @param afterTimeCalendar the calendar to use in order to compute the next available value
     * @return the next value result for the given calendar for the calendard field associated.
     */
    @Override
    @SuppressWarnings("boxing")
    public ValueResult getTimeAfter(final Calendar afterTimeCalendar) {

        ValueResult valueResult = new ValueResult();
        int currentFieldValue = afterTimeCalendar.get(getCalendarField());

        Integer foundValue = this.allValues.ceiling(currentFieldValue);
        if (foundValue != null) {
            valueResult.setResult(foundValue.intValue());
        } else {
            // use the next first available value
            valueResult.setResult(this.allValues.first());
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
        sb.append("[startingPoint=");
        sb.append(this.startingPoint);
        sb.append(", interval=");
        sb.append(this.interval);
        sb.append(", calendarField=");
        sb.append(getCalendarField());
        sb.append(", allValues=");
        sb.append(this.allValues);
        sb.append("]");
        return sb.toString();
    }
}
