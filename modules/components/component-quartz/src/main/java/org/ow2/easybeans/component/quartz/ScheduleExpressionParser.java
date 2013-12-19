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
import static org.ow2.easybeans.component.quartz.SchedulePatterns.PATTERN_DAY_OF_MONTH;
import static org.ow2.easybeans.component.quartz.SchedulePatterns.PATTERN_DAY_OF_WEEK;
import static org.ow2.easybeans.component.quartz.SchedulePatterns.PATTERN_DAY_OF_WEEK_0_7;
import static org.ow2.easybeans.component.quartz.SchedulePatterns.PATTERN_DAY_OF_WEEK_VALUES;
import static org.ow2.easybeans.component.quartz.SchedulePatterns.PATTERN_HOUR;
import static org.ow2.easybeans.component.quartz.SchedulePatterns.PATTERN_INCREMENTS;
import static org.ow2.easybeans.component.quartz.SchedulePatterns.PATTERN_MINUTE;
import static org.ow2.easybeans.component.quartz.SchedulePatterns.PATTERN_MONTH;
import static org.ow2.easybeans.component.quartz.SchedulePatterns.PATTERN_MONTH_1_12;
import static org.ow2.easybeans.component.quartz.SchedulePatterns.PATTERN_MONTH_VALUES;
import static org.ow2.easybeans.component.quartz.SchedulePatterns.PATTERN_NDAYS;
import static org.ow2.easybeans.component.quartz.SchedulePatterns.PATTERN_RANGE;
import static org.ow2.easybeans.component.quartz.SchedulePatterns.PATTERN_SECOND;
import static org.ow2.easybeans.component.quartz.SchedulePatterns.PATTERN_YEAR;
import static org.ow2.easybeans.component.quartz.SchedulePatterns.WILDCARD_CHARACTER;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import javax.ejb.ScheduleExpression;

import org.ow2.util.log.Log;
import org.ow2.util.log.LogFactory;

/**
 *
 * @author Florent Benoit
 */
public class ScheduleExpressionParser {

    /**
     * Logger.
     */
    private static final Log LOGGER = LogFactory.getLog(ScheduleExpressionParser.class);


    /**
     * Allows to convert a month name to its int counterpart value.
     */
    private Map<String, Integer> monthNameToInt = null;

    /**
     * Allows to convert a dayOfWeek day to its int counterpart value.
     */
    private Map<String, Integer> dayOfWeeksToInt = null;

    /**
     * Init the parser and the converters.
     */
    @SuppressWarnings("boxing")
    public ScheduleExpressionParser() {

        // Initialize month name to int values
        this.monthNameToInt = new HashMap<String, Integer>();
        this.monthNameToInt.put("jan", Calendar.JANUARY);
        this.monthNameToInt.put("feb", Calendar.FEBRUARY);
        this.monthNameToInt.put("mar", Calendar.MARCH);
        this.monthNameToInt.put("apr", Calendar.APRIL);
        this.monthNameToInt.put("may", Calendar.MAY);
        this.monthNameToInt.put("jun", Calendar.JUNE);
        this.monthNameToInt.put("jul", Calendar.JULY);
        this.monthNameToInt.put("aug", Calendar.AUGUST);
        this.monthNameToInt.put("sep", Calendar.SEPTEMBER);
        this.monthNameToInt.put("oct", Calendar.OCTOBER);
        this.monthNameToInt.put("nov", Calendar.NOVEMBER);
        this.monthNameToInt.put("dec", Calendar.DECEMBER);



        // -1 to all values as for EJB spec sunday = 0;
        this.dayOfWeeksToInt = new HashMap<String, Integer>();
        this.dayOfWeeksToInt.put("sun", Calendar.SUNDAY - 1);
        this.dayOfWeeksToInt.put("mon", Calendar.MONDAY - 1);
        this.dayOfWeeksToInt.put("tue", Calendar.TUESDAY - 1);
        this.dayOfWeeksToInt.put("wed", Calendar.WEDNESDAY - 1);
        this.dayOfWeeksToInt.put("thu", Calendar.THURSDAY - 1);
        this.dayOfWeeksToInt.put("fri", Calendar.FRIDAY - 1);
        this.dayOfWeeksToInt.put("sat", Calendar.SATURDAY - 1);

    }

    /**
     * Cleanup the given string by removing all spaces and by converting the values to lowercase.
     * @param input the value to treat
     * @return the cleanup values
     */
    protected String cleanupInput(final String input) {
        if (input == null) {
            return null;
        }
        // remove the whitespaces (cf 18.2.1.2)
        String updatedInput = input.replaceAll("\\s+", "");

        // Use tolower as we've to be case insensitive (cf 18.2.1.2)
        return updatedInput.toLowerCase();
    }

    /**
     * Convert the EJB Schedule Expression to our ScheduleValues expression.
     * @param scheduleExpression the EJB expression
     * @return an updated quartz expression
     */
    public EasyBeansScheduleExpression parse(final ScheduleExpression scheduleExpression) {
        LOGGER.debug("Parsing expression ''{0}''", scheduleExpression);
        EasyBeansScheduleExpression quartzExpression = new EasyBeansScheduleExpression();

        String seconds = scheduleExpression.getSecond();
        String minutes = scheduleExpression.getMinute();
        String hours = scheduleExpression.getHour();
        String dayOfMonth = scheduleExpression.getDayOfMonth();
        String month = scheduleExpression.getMonth();
        String dayOfWeek = scheduleExpression.getDayOfWeek();
        String year = scheduleExpression.getYear();

        // Update each field
        quartzExpression.setSecond(getScheduleValue(seconds, Calendar.SECOND));
        quartzExpression.setMinute(getScheduleValue(minutes, Calendar.MINUTE));
        quartzExpression.setHour(getScheduleValue(hours, Calendar.HOUR_OF_DAY));
        quartzExpression.setDayOfMonth(getScheduleValue(dayOfMonth, Calendar.DAY_OF_MONTH));
        quartzExpression.setMonth(getScheduleValue(month, Calendar.MONTH));
        quartzExpression.setDayOfWeek(getScheduleValue(dayOfWeek, Calendar.DAY_OF_WEEK));
        quartzExpression.setYear(getScheduleValue(year, Calendar.YEAR));

        return quartzExpression;
    }

    /**
     * Extract the right schedule value by analyzing the given input value for a given calendar field.
     * @param inputValue the value to handle
     * @param calendarField the calendar field.
     * @return  the found schedulevalue
     */
    public ScheduleValue getScheduleValue(final String inputValue, final int calendarField) {

        String value = cleanupInput(inputValue);

        // First, validate the given value
        if (!validate(value, calendarField)) {
            throw new IllegalArgumentException("Unable to validate the field '" + inputValue + "' for calendar field '"
                    + calendarField + "'.");
        }

        // It has been validated, now get value

        // It's a list ?
        String[] commaSeparatedList = value.split(",");
        if (commaSeparatedList.length > 1) {
            List<String> elements = new ArrayList<String>();

            // First, remove any duplicates that can be present
            for (int i = 0; i < commaSeparatedList.length; i++) {
                String simple = commaSeparatedList[i];
                if (!elements.contains(simple)) {
                    elements.add(simple);
                }
            }

            // We've at least 2 elements
            if (elements.size() > 1) {

                // Now, for each expression, gets the schedule value
                ScheduleValueList scheduleValueList = new ScheduleValueList(calendarField);
                for (String element : elements) {
                    scheduleValueList.add(getScheduleValue(element, calendarField));
                }

                return scheduleValueList;
            }

            // Only one element, transform it to a single value
            return getScheduleValue(elements.get(0), calendarField);
        }

        Matcher incrementMatcher = PATTERN_INCREMENTS.matcher(value);
        if (incrementMatcher.matches()) {
            return new ScheduleValueIncrements(incrementMatcher.group(1), Integer.parseInt(incrementMatcher.group(2)),
                    calendarField);
        }


        Matcher rangeMatcher = PATTERN_RANGE.matcher(value);
        if (rangeMatcher.matches()) {

            String leftRange = rangeMatcher.group(1);
            String rightRange = rangeMatcher.group(2);

            // Handle special case DAY_OF_WEEK 0-7 as it's a wildcard (see spec EJB 3.1
            if (Calendar.DAY_OF_WEEK == calendarField) {
                if ("0".equals(leftRange) && "7".equals(rightRange)) {
                    return new ScheduleValueWildCard(calendarField);
                }
            }
            String left = convertToSimple(leftRange, calendarField);
            String right = convertToSimple(rightRange, calendarField);

            return new ScheduleValueRange(left, right, calendarField);

        }

        // Wildcard ?
        if (WILDCARD_CHARACTER.equals(value)) {
            return new ScheduleValueWildCard(calendarField);
        }


        // Last ?
        if (LAST.equals(value)) {
            return new ScheduleValueLast(calendarField);
        }

        // Update value to a simple value
        value = convertToSimple(value, calendarField);


        // NTh day ?
        if (PATTERN_NDAYS.matcher(value).matches()) {
            return new ScheduleValueAttributeNDays(value, calendarField);
        }

        // Simple attribute as it's the only remaining case.
        return new ScheduleValueAttribute(Integer.parseInt(value), calendarField);
    }


    /**
     * Makes simpler the expression receive.
     * @param inputValue the value to update
     * @param calendarField the calendar field to use
     * @return the updated value
     */
    public String convertToSimple(final String inputValue, final int calendarField) {
        String value = cleanupInput(inputValue);

        switch (calendarField) {

        // For month we've to handle the EJB spec which start month at 1
        case Calendar.MONTH:

            if (PATTERN_MONTH_VALUES.matcher(value).matches()) {
                Integer monthVal = this.monthNameToInt.get(value);
                return String.valueOf(monthVal.intValue());
            }

            if (PATTERN_MONTH_1_12.matcher(value).matches()) {
                // Needs to decrement as month are for EJB spec from 1 to 12
                return String.valueOf(Integer.parseInt(value) - 1);
            }
            break;

        // Update DayOfWeek
        case Calendar.DAY_OF_WEEK:
            if (PATTERN_DAY_OF_WEEK_VALUES.matcher(value).matches()) {
                Integer dayOfweekVal = this.dayOfWeeksToInt.get(value);
                return String.valueOf(dayOfweekVal.intValue());
            }
            if (PATTERN_DAY_OF_WEEK_0_7.matcher(value).matches()) {
                return String.valueOf(Integer.parseInt(value));
            }

            break;
        default:
            return value;
        }

        return value;
    }

    /**
     * Validate the given value for the given calendar field.
     * @param inputValue the value to handle
     * @param calendarField the calendar field to use
     * @return true if the value is OK or false if it's incorrect.
     */
    protected boolean validate(final String inputValue, final int calendarField) {

        // Null values not allowed
        if (inputValue == null) {
            return false;
        }

        String value = cleanupInput(inputValue);

        // It's a list ?
        String[] commaSeparatedList = value.split(",");
        if (commaSeparatedList.length > 1) {
            // got a list
            boolean validated = true;
            for (int i = 0; i < commaSeparatedList.length; i++) {

                // Wilcard not allowed in a list
                if (WILDCARD_CHARACTER.equals(commaSeparatedList[i])) {
                    return false;
                }

                // Increments not allowed in a list
                if (PATTERN_INCREMENTS.matcher(commaSeparatedList[i]).matches()) {
                    return false;
                }

                validated = validated && validate(commaSeparatedList[i], calendarField);
            }
            return validated;
        }

        // Match increments for second, minute and hour ?
        Matcher incrementMatcher = PATTERN_INCREMENTS.matcher(value);

        // Increments ? only for second/minute/hour
        if (incrementMatcher.matches()) {

            if (!(Calendar.SECOND == calendarField
               || Calendar.MINUTE == calendarField
               || Calendar.HOUR_OF_DAY == calendarField)) {
                throw new IllegalArgumentException(
                        "Cannot use increments pattern for an other type than Second/Minute/Hour for the value '" + value
                                + "' for the type '" + calendarField + "'.");
            }

            String leftIncrement = incrementMatcher.group(1);
            String rightIncrement = incrementMatcher.group(2);

            // Update wildcard character (* needs to be replaced by 0)
            if (WILDCARD_CHARACTER.equals(leftIncrement)) {
                leftIncrement = "0";
            }

            // Validate both cases
            return validate(leftIncrement, calendarField) && validate(rightIncrement, calendarField);

        }

        Matcher rangeMatcher = PATTERN_RANGE.matcher(value);
        if (rangeMatcher.matches()) {

            String leftRange = rangeMatcher.group(1);
            String rightRange = rangeMatcher.group(2);

            return validate(leftRange, calendarField) && validate(rightRange, calendarField);

        }

        switch (calendarField) {
        case Calendar.SECOND:
            return PATTERN_SECOND.matcher(value).matches();
        case Calendar.MINUTE:
            return PATTERN_MINUTE.matcher(value).matches();
        case Calendar.HOUR_OF_DAY:
            return PATTERN_HOUR.matcher(value).matches();
        case Calendar.DAY_OF_MONTH:
            return PATTERN_DAY_OF_MONTH.matcher(value).matches();
        case Calendar.MONTH:
            return PATTERN_MONTH.matcher(value).matches();
        case Calendar.DAY_OF_WEEK:
            return PATTERN_DAY_OF_WEEK.matcher(value).matches();
        case Calendar.YEAR:
            return PATTERN_YEAR.matcher(value).matches();
        default:
            throw new IllegalStateException("Unknown Calendar Field '" + calendarField + "'");
        }

    }
}
