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

/**
 * Defines an EasyBeans Schedule expression.
 * @author Florent Benoit
 */
public class EasyBeansScheduleExpression {

    /**
     * Schedule value for the seconds.
     */
    private ScheduleValue second;

    /**
     * Schedule value for the minute.
     */
    private ScheduleValue minute;

    /**
     * Schedule value for the hour.
     */
    private ScheduleValue hour;

    /**
     * Schedule value for the dayOfMonth.
     */
    private ScheduleValue dayOfMonth;

    /**
     * Schedule value for the month.
     */
    private ScheduleValue month;

    /**
     * Schedule value for the dayOfWeek.
     */
    private ScheduleValue dayOfWeek;

    /**
     * Schedule value for the year.
     */
    private ScheduleValue year;

    /**
     * @return seconds
     */
    public ScheduleValue getSecond() {
        return this.second;
    }

    /**
     * Sets the seconds.
     * @param second the given seconds
     */
    public void setSecond(final ScheduleValue second) {
        this.second = second;
    }

    /**
     * @return minute
     */
    public ScheduleValue getMinute() {
        return this.minute;
    }

    /**
     * Sets the minute.
     * @param minute the given minute
     */
    public void setMinute(final ScheduleValue minute) {
        this.minute = minute;
    }

    /**
     * @return hour
     */
    public ScheduleValue getHour() {
        return this.hour;
    }

    /**
     * Sets the hour.
     * @param hour the given hour
     */
    public void setHour(final ScheduleValue hour) {
        this.hour = hour;
    }

    /**
     * @return dayOfMonth
     */
    public ScheduleValue getDayOfMonth() {
        return this.dayOfMonth;
    }

    /**
     * Sets the dayOfMonth.
     * @param dayOfMonth the given dayOfMonth
     */
    public void setDayOfMonth(final ScheduleValue dayOfMonth) {
        this.dayOfMonth = dayOfMonth;
    }

    /**
     * @return month
     */
    public ScheduleValue getMonth() {
        return this.month;
    }

    /**
     * Sets the month.
     * @param month the given month
     */
    public void setMonth(final ScheduleValue month) {
        this.month = month;
    }

    /**
     * @return dayOfWeek
     */
    public ScheduleValue getDayOfWeek() {
        return this.dayOfWeek;
    }

    /**
     * Sets the dayOfWeek.
     * @param dayOfWeek the given dayOfWeek
     */
    public void setDayOfWeek(final ScheduleValue dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    /**
     * @return year
     */
    public ScheduleValue getYear() {
        return this.year;
    }

    /**
     * Sets the year.
     * @param year the given year
     */
    public void setYear(final ScheduleValue year) {
        this.year = year;
    }

    /**
     * @return Stringified value.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.getClass().getSimpleName());
        sb.append("[second=");
        sb.append(this.second);
        sb.append(", minute=");
        sb.append(this.minute);
        sb.append(", hour=");
        sb.append(this.hour);
        sb.append(", dayOfMonth=");
        sb.append(this.dayOfMonth);
        sb.append(", month=");
        sb.append(this.month);
        sb.append(", dayOfWeek=");
        sb.append(this.dayOfWeek);
        sb.append(", year=");
        sb.append(this.year);
        sb.append("]");
        return sb.toString();
    }

}
