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
 * Defines the last value available for the given calendar field.
 * @author Florent Benoit
 */
public class ScheduleValueLast extends ScheduleValue {

    /**
     * Build a new value for the given calendar field.
     * @param calendarField the calendar field
     */
    public ScheduleValueLast(final int calendarField) {
        super(calendarField);
    }

    /**
     * @param afterTimeCalendar the calendar to use in order to compute the next
     *        available value
     * @return the next value result for the given calendar for the calendard
     *         field associated.
     */
    @Override
    public ValueResult getTimeAfter(final Calendar afterTimeCalendar) {
        ValueResult valueResult = new ValueResult();
        valueResult.setResult(afterTimeCalendar.getActualMaximum(getCalendarField()));
        return valueResult;
    }

    /**
     * @return string representation
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.getClass().getSimpleName());
        sb.append("[last]");
        return sb.toString();
    }
}
