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
import java.util.Date;
import java.util.GregorianCalendar;

import javax.ejb.ScheduleExpression;

import org.quartz.impl.triggers.CoreTrigger;
import org.quartz.impl.triggers.CronTriggerImpl;

/**
 * Defines a trigger using the new EJB 3.1 Schedule expression.
 * @author Florent Benoit
 */
public class EasyBeansScheduleTrigger extends CronTriggerImpl implements CoreTrigger{

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 5986420827628682444L;

    /**
     * Schedule expression used by this trigger.
     */
    private ScheduleExpression scheduleExpression = null;

    /**
     * Schedule expression parser.
     */
    private ScheduleExpressionParser parser = null;

    /**
     * The schedule expression.
     */
    private EasyBeansScheduleExpression easyBeansScheduleExpression;

    /**
     * max loop before trying to find a timeout date.
     */
    private static final int MAX_LOOP = 200;

    /**
     * Build a new trigger for the given expression.
     * @param scheduleExpression the given EJB schedule expression
     */
    public EasyBeansScheduleTrigger(final ScheduleExpression scheduleExpression) {
        super();
        this.scheduleExpression = scheduleExpression;
        this.parser = new ScheduleExpressionParser();
        init();
    }

    /**
     * Initialize the expression.
     */
    protected void init() {

        // now we will get schedule values for each part of the expression
        this.easyBeansScheduleExpression = this.parser.parse(this.scheduleExpression);

    }


    /**
     * Find the next occurence after the given date.
     * @param afterTime the time after which we've to find a new date
     * @return new date
     */
    @Override
    protected Date getTimeAfter(final Date afterTime) {

        int loop = 0;

        // Computation is based on Gregorian year only.
        Calendar afterTimeCalendar = new GregorianCalendar(getTimeZone());

        Calendar originalDate = null;

        // If start time has been specified, we have to compute the next time from this start and not from the original given time
        if (this.scheduleExpression.getStart() != null) {
            afterTimeCalendar.setTime(getStartTime());
            originalDate = (Calendar) afterTimeCalendar.clone();
        } else {
            afterTimeCalendar.setTime(afterTime);
            originalDate = (Calendar) afterTimeCalendar.clone();
            // move ahead one second, since we're computing the time *after* the
            // given time
            afterTimeCalendar.add(Calendar.SECOND, 1);
        }

        // CronTrigger does not deal with milliseconds
        afterTimeCalendar.set(Calendar.MILLISECOND, 0);
        afterTimeCalendar.setFirstDayOfWeek(Calendar.SUNDAY);

        boolean daysDone = false;
        boolean gotOne = false;

        // loop until we've computed the next time, or we've past the endTime
        while (!gotOne) {
            loop++;

            // Unable to find a value (should be unable to find it !)
            if (loop > MAX_LOOP) {
                return null;
            }

            ScheduleValue dayOfMonthScheduleValue = this.easyBeansScheduleExpression.getDayOfMonth();
            ScheduleValue dayOfWeekScheduleValue = this.easyBeansScheduleExpression.getDayOfWeek();


            /**
             * Seconds
             */
            ValueResult valueResult = this.easyBeansScheduleExpression.getSecond().getTimeAfter(afterTimeCalendar);
            int previousSeconds = afterTimeCalendar.get(Calendar.SECOND);
            if (valueResult.needsIncrement()) {
                // increment minute
                afterTimeCalendar.add(Calendar.MINUTE, 1);
            }
            // Value has been changed
            if (previousSeconds != valueResult.getResult() || valueResult.needsIncrement()) {
                // Set the second field to the found value
                afterTimeCalendar.set(Calendar.SECOND, valueResult.getResult());
                //continue;
            }


            /**
             * Minutes
             */
            valueResult = this.easyBeansScheduleExpression.getMinute().getTimeAfter(afterTimeCalendar);
            int previousMinutes = afterTimeCalendar.get(Calendar.MINUTE);
            if (valueResult.needsIncrement()) {
                // increment hour
                afterTimeCalendar.add(Calendar.HOUR, 1);
            }
            // Value has been changed
            if (previousMinutes != valueResult.getResult() || valueResult.needsIncrement()) {
                // Set the minute field to the found value
                afterTimeCalendar.set(Calendar.MINUTE, valueResult.getResult());

                // Change to 0
                afterTimeCalendar.set(Calendar.SECOND, 0);
                continue;
            }

            /**
             * Hour
             */
            valueResult = this.easyBeansScheduleExpression.getHour().getTimeAfter(afterTimeCalendar);
            int previousHours = afterTimeCalendar.get(Calendar.HOUR_OF_DAY);
            if (valueResult.needsIncrement() && dayOfWeekScheduleValue instanceof ScheduleValueWildCard && dayOfMonthScheduleValue instanceof ScheduleValueWildCard) {
                // increment day
                afterTimeCalendar.add(Calendar.DAY_OF_MONTH, 1);
            }
            // Value has been changed
            if (previousHours != valueResult.getResult() || valueResult.needsIncrement()) {
                // Set the hour field to the found value
                afterTimeCalendar.set(Calendar.HOUR_OF_DAY, valueResult.getResult());

                // Change to 0
                afterTimeCalendar.set(Calendar.SECOND, 0);
                afterTimeCalendar.set(Calendar.MINUTE, 0);
                continue;
            }


            /**
             * Month
             */
            valueResult = this.easyBeansScheduleExpression.getMonth().getTimeAfter(afterTimeCalendar);
            int previousMonth = afterTimeCalendar.get(Calendar.MONTH);
            Date startTime = this.scheduleExpression.getStart();


            if (valueResult.needsIncrement()) {
                // increment year
                afterTimeCalendar.add(Calendar.YEAR, 1);
            }
            // Value has been changed
            if (previousMonth != valueResult.getResult() || valueResult.needsIncrement()) {
                // Set the hour field to the found value
                afterTimeCalendar.set(Calendar.MONTH, valueResult.getResult());

                // Change to 0
                afterTimeCalendar.set(Calendar.SECOND, 0);
                afterTimeCalendar.set(Calendar.MINUTE, 0);
                afterTimeCalendar.set(Calendar.HOUR_OF_DAY, 0);
                afterTimeCalendar.set(Calendar.DAY_OF_MONTH, 1);
                daysDone = false;
                continue;
            }

            /**
             * Year
             */
            valueResult = this.easyBeansScheduleExpression.getYear().getTimeAfter(afterTimeCalendar);
            int previousYear = afterTimeCalendar.get(Calendar.YEAR);
            if (valueResult.needsIncrement()) {
                // increment year
                afterTimeCalendar.add(Calendar.YEAR, 1);
            }
            // Value has been changed
            if (previousYear != valueResult.getResult() || valueResult.needsIncrement()) {
                // Set the hour field to the found value
                afterTimeCalendar.set(Calendar.YEAR, valueResult.getResult());

                // Change to 0
                afterTimeCalendar.set(Calendar.SECOND, 0);
                afterTimeCalendar.set(Calendar.MINUTE, 0);
                afterTimeCalendar.set(Calendar.HOUR_OF_DAY, 0);
                afterTimeCalendar.set(Calendar.MONTH, 0);
                afterTimeCalendar.set(Calendar.DAY_OF_MONTH, 1);
                daysDone = false;
                continue;
            }




            // dayOfMonth != "*" and dayOfWeek = "*"
            if (!daysDone) {
                if (!(dayOfMonthScheduleValue instanceof ScheduleValueWildCard) && dayOfWeekScheduleValue instanceof ScheduleValueWildCard) {


                    /**
                     * We have only a day-Of-Month value
                     */
                    valueResult = dayOfMonthScheduleValue.getTimeAfter(afterTimeCalendar);
                    int previousDayOfMonth = afterTimeCalendar.get(Calendar.DAY_OF_MONTH);

                    if (startTime != null && startTime.after(afterTimeCalendar.getTime())) {
                        valueResult.setNeedsIncrement(true);
                    }

                    if (valueResult.needsIncrement()) {
                        // increment month
                        afterTimeCalendar.add(Calendar.MONTH, 1);
                    }

                    // Value has been changed
                    if (previousDayOfMonth != valueResult.getResult() || valueResult.needsIncrement()) {
                        // Set the dayOfMonth field to the found value
                        afterTimeCalendar.set(Calendar.DAY_OF_MONTH, valueResult.getResult());


                        // Change to 0
                        afterTimeCalendar.set(Calendar.SECOND, 0);
                        afterTimeCalendar.set(Calendar.MINUTE, 0);
                        afterTimeCalendar.set(Calendar.HOUR_OF_DAY, 0);
                    }
                } else if ((dayOfMonthScheduleValue instanceof ScheduleValueWildCard) && !(dayOfWeekScheduleValue instanceof ScheduleValueWildCard)) {
                    // dayOfMonth == "*" and dayOfWeek != "*"

                    /**
                     * We have only a day-Of-Week value
                     */

                    valueResult = dayOfWeekScheduleValue.getTimeAfter(afterTimeCalendar);
                    int previousDayOfWeek = afterTimeCalendar.get(Calendar.DAY_OF_WEEK);


                    if (valueResult.needsIncrement())  {
                        // increment week in the month
                        afterTimeCalendar.add(Calendar.DAY_OF_WEEK_IN_MONTH, 1);
                    }



                    Calendar now = new GregorianCalendar();
                    now.set(Calendar.MILLISECOND, 0);

                    Date previousFireTime = getPreviousFireTime();
                    if (afterTimeCalendar.equals(now) && previousFireTime == null && afterTimeCalendar.equals(originalDate)) {

                        // needs to report to the next week
                        afterTimeCalendar.add(Calendar.DAY_OF_WEEK_IN_MONTH, 1);

                        if (this.easyBeansScheduleExpression.getMonth() instanceof ScheduleValueAttribute && this.easyBeansScheduleExpression.getYear() instanceof ScheduleValueAttribute) {
                            int wantedMonth = Integer.parseInt(this.scheduleExpression.getMonth());
                            int currentMonth = afterTimeCalendar.get(Calendar.MONTH) + 1;
                            // It means that we can't go further so step back
                            if (wantedMonth != currentMonth) {
                                afterTimeCalendar.add(Calendar.DAY_OF_WEEK_IN_MONTH, -1);
                                return afterTimeCalendar.getTime();
                            }
                        }


                        return afterTimeCalendar.getTime();
                    }

                    // We're past than the original date so we need to upgrade the day of week
                    if (originalDate.after(afterTimeCalendar)) {
                        afterTimeCalendar.add(Calendar.DAY_OF_WEEK_IN_MONTH, 1);

                        // Check that it's still a valid value
                        if (this.easyBeansScheduleExpression.getMonth() instanceof ScheduleValueAttribute && this.easyBeansScheduleExpression.getYear() instanceof ScheduleValueAttribute) {
                            int wantedMonth = Integer.parseInt(this.scheduleExpression.getMonth());
                            int currentMonth = afterTimeCalendar.get(Calendar.MONTH) + 1;
                            // It means that we can't go further so step back
                            if (wantedMonth != currentMonth) {
                                afterTimeCalendar.add(Calendar.DAY_OF_WEEK_IN_MONTH, -1);
                                return afterTimeCalendar.getTime();
                            }
                        }

                        return afterTimeCalendar.getTime();
                    }




                    // Value has been changed
                    if (previousDayOfWeek != valueResult.getResult() || valueResult.needsIncrement()) {
                        // Set the dayOfWeek field to the found value
                        afterTimeCalendar.set(Calendar.DAY_OF_WEEK, valueResult.getResult());


                        // Change to 0
                        afterTimeCalendar.set(Calendar.SECOND, 0);
                        afterTimeCalendar.set(Calendar.MINUTE, 0);
                        afterTimeCalendar.set(Calendar.HOUR_OF_DAY, 0);
                    }

                } else if (dayOfMonthScheduleValue instanceof ScheduleValueWildCard && dayOfWeekScheduleValue instanceof ScheduleValueWildCard) {
                    // We do nothing as we've a wildcard

                } else {
                    // dayOfMonth != "*" and dayOfWeek != "*" (we've a value for the both values
                    // For this case we've to match one of the value which is the first coming date

                    ValueResult valueResultDayOfMonth = dayOfMonthScheduleValue.getTimeAfter(afterTimeCalendar);
                    ValueResult valueResultDayOfWeek = dayOfWeekScheduleValue.getTimeAfter(afterTimeCalendar);

                    // which one is the value that is matching today ?
                    int currentDayOfMonth = afterTimeCalendar.get(Calendar.DAY_OF_MONTH);
                    int currentDayOfWeek = afterTimeCalendar.get(Calendar.DAY_OF_WEEK);

                    // We're matching the day of month
                    if (currentDayOfMonth == valueResultDayOfMonth.getResult()) {
                        // Set the dayOfMonth field to the found value
                        afterTimeCalendar.set(Calendar.DAY_OF_MONTH, valueResultDayOfMonth.getResult());
                    } else if (currentDayOfWeek == valueResultDayOfWeek.getResult()) {
                        //We're matching the day of week

                        // Set the dayOfWeek field to the found value
                        afterTimeCalendar.set(Calendar.DAY_OF_WEEK, valueResultDayOfWeek.getResult());
                    } else {
                        // No match
                        throw new IllegalStateException("No expression matching the current day of week and day of month");
                    }
                }

                daysDone = true;
                continue;
            }


            gotOne = true;
        }

        return checkIfBeforeEnd(updateIfAlreadySent(afterTimeCalendar.getTime()));
    }

    /**
     * Ensure the given date is before the end.
     * @param fireTime the given date
     * @return the given date of null if before
     */
    protected Date checkIfBeforeEnd(final Date fireTime) {
        if (fireTime == null) {
            return null;
        }

        Date end = this.scheduleExpression.getEnd();
        if (end == null) {
            return fireTime;
        }
        // It will never expires
        if (end.before(fireTime)) {
            this.easyBeansTimer.setInvalid();
            return null;
        }
        return fireTime;
    }


    /**
     * @param fireTime the expected date
     * @return null if this value has already been sent
     */
    protected Date updateIfAlreadySent(final Date fireTime) {
        Date previousTime = getPreviousFireTime();
        if (previousTime != null) {
            // We've already sent this value so it's time to stop
            if (fireTime.equals(previousTime)) {
                return null;
            }
        }

        return fireTime;
    }


    /**
     * Clone the current trigger.
     */
    @Override
    public Object clone() {
        EasyBeansScheduleTrigger copy = (EasyBeansScheduleTrigger) super.clone();
        if (this.easyBeansTimer != null) {
            copy.setEasyBeansTimer(this.easyBeansTimer);
        }
        return copy;
    }


    private EasyBeansTimer easyBeansTimer = null;

    public EasyBeansTimer getEasyBeansTimer() {
        return this.easyBeansTimer;
    }


    public void setEasyBeansTimer(final EasyBeansTimer easyBeansTimer) {
        this.easyBeansTimer = easyBeansTimer;
    }

}
