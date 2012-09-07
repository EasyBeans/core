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

import java.util.Calendar;

/**
 * Defines a specific value for the given calendar field.
 * @author Florent Benoit
 */
public class ScheduleValueAttribute extends ScheduleValue {

    /**
     * Value to use.
     */
    private int value;

    /**
     * Build a new value attribute for the specific value and the given calendar
     * field.
     * @param value the given value
     * @param calendarField the calendar field
     */
    public ScheduleValueAttribute(final int value, final int calendarField) {
        super(calendarField);
        this.value = value;

        if (Calendar.DAY_OF_WEEK == calendarField) {
            this.value++;

            // 8 is sunday
            if (this.value > SUNDAY_7) {
                this.value = Calendar.SUNDAY;
            }
        }

    }

    /**
     * @param afterTimeCalendar the calendar to use in order to compute the next
     *        available value
     * @return the next value result for the given calendar for the calendard
     *         field associated.
     */
    @Override
    public ValueResult getTimeAfter(final Calendar afterTimeCalendar) {
        int currentVal = afterTimeCalendar.get(getCalendarField());

        ValueResult valueResult = new ValueResult();
        valueResult.setResult(this.value);
        if (this.value < currentVal) {
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
        sb.append("[value=");
        sb.append(this.value);
        sb.append("]");
        return sb.toString();
    }

}
