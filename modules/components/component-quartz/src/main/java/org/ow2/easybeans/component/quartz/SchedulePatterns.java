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

import java.util.regex.Pattern;

/**
 * Defines constants and patterns used by the Schedule expressions.
 * @author Florent Benoit
 */
public final class SchedulePatterns {

    /**
     * Utility class.
     */
    private SchedulePatterns() {

    }

    /**
     * Wildcard character.
     */
    public static final String WILDCARD_CHARACTER = "*";

    /**
     * Wildcard pattern.
     */
    public static final String WILDCARD_PATTERN = "[*]";

    /**
     * Last keyword. (lowercase as we're converting all to lowercase)
     */
    public static final String LAST = "last";

    /**
     * Pattern for the characters allowed for seconds field.
     */
    public static final Pattern PATTERN_SECOND = Pattern.compile("[0-5]?\\d" + "|" + WILDCARD_PATTERN);

    /**
     * Pattern for the characters allowed for minutes field.
     */
    public static final Pattern PATTERN_MINUTE = PATTERN_SECOND;

    /**
     * Pattern for the characters allowed for hour_of_day field.
     */
    public static final Pattern PATTERN_HOUR = Pattern.compile("[0-1]?[\\d]|2[0-3]" + "|" + WILDCARD_PATTERN);

    /**
     * Part of a regexp for dayOfMonth in the range 1-->31.
     */
    private static final String DAY_OF_MONTH_1_31 = "0?[1-9]|[1-2][\\d]|3[0-1]";

    /**
     * Part of a regexp for dayOfMonth in the range -1 --> -7.
     */
    private static final String DAY_OF_MONTH_MINUS1_MINUS7 = "-[1-7]";

    /**
     * Part of a regexp for dayOfMonth in the given enums : 1st to last.
     */
    private static final String DAY_OF_MONTH_VALUES1 = "1st|2nd|3rd|4th|5th|last";

    /**
     * Part of a regexp for dayOfMonth in the given enums : sun to sat.
     */
    private static final String DAY_OF_MONTH_VALUES2 = "sun|mon|tue|wed|thu|fri|sat";

    /**
     * Pattern for the characters allowed for dayOfMonth field.
     */
    public static final Pattern PATTERN_DAY_OF_MONTH = Pattern.compile(DAY_OF_MONTH_1_31 + "|" + DAY_OF_MONTH_MINUS1_MINUS7
            + "|" + LAST + "|(" + DAY_OF_MONTH_VALUES1 + ")(" + DAY_OF_MONTH_VALUES2 + ")|" + WILDCARD_PATTERN);

    /**
     * Part of a regexp for month in the range 1-->12.
     */
    private static final String MONTH_1_12 = "0?[1-9]|1[0-2]";

    /**
     * Part of a regexp for month in the range jan-->dec.
     */
    private static final String MONTH_VALUES = "jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec";

    /**
     * Pattern for the characters allowed for month field from 1-->12.
     */
    public static final Pattern PATTERN_MONTH_1_12 = Pattern.compile(MONTH_1_12);

    /**
     * Pattern for the characters allowed for month field in the range
     * jan-->dec.
     */
    public static final Pattern PATTERN_MONTH_VALUES = Pattern.compile(MONTH_VALUES);

    /**
     * Pattern for the characters allowed for month field.
     */
    public static final Pattern PATTERN_MONTH = Pattern.compile(MONTH_1_12 + "|" + MONTH_VALUES + "|" + WILDCARD_PATTERN);


    /**
     * Part of a regexp for dayOfWeek in the range 0-->7.
     */
    private static final String DAY_OF_WEEK_0_7 = "[0-7]";

    /**
     * Part of a regexp for dayOfWeek in the range sun-->sat.
     */
    private static final String DAY_OF_WEEK_VALUES = "sun|mon|tue|wed|thu|fri|sat";

    /**
     * Pattern for the characters allowed for dayOfWeek field from 0-->7.
     */
    public static final Pattern PATTERN_DAY_OF_WEEK_0_7 = Pattern.compile("(" + DAY_OF_WEEK_0_7 + ")");


    /**
     * Pattern for the characters allowed for dayOfWeek in the range sun-->sat.
     */
    public static final Pattern PATTERN_DAY_OF_WEEK_VALUES = Pattern.compile("(" + DAY_OF_WEEK_VALUES + ")");

    /**
     * Pattern for the characters allowed for dayOfWeek field.
     */
    public static final Pattern PATTERN_DAY_OF_WEEK = Pattern.compile(DAY_OF_WEEK_0_7 + "|" + LAST + "|"
            + DAY_OF_WEEK_VALUES + "|" + WILDCARD_PATTERN);

    /**
     * Pattern for the characters allowed for the year field.
     */
    public static final Pattern PATTERN_YEAR = Pattern.compile("\\d{4}|" + WILDCARD_PATTERN);


    /**
     * Pattern for the characters allowed in a range.
     */
    public static final Pattern PATTERN_RANGE = Pattern.compile("(-?\\p{Alnum}+)-(-?\\p{Alnum}+)");

    /**
     * Pattern for the characters allowed in an increments.
     */
    public static final Pattern PATTERN_INCREMENTS = Pattern.compile("(" + WILDCARD_PATTERN + "|" + "\\d+)/(\\d+)");

    /**
     * Part of a regexp for the characters allowed in a nthday (number).
     */
    private static final String NUMBERS = "1st|2nd|3rd|4th|5th";

    /**
     * Part of a regexp for the characters allowed in a nthday (day).
     */
    private static final String DAYS = "sun|mon|tue|wed|thu|fri|sat";

    /**
     * Pattern for the characters allowed in a nthday.
     */
    public static final Pattern PATTERN_NDAYS = Pattern.compile("(" + LAST + "|" + NUMBERS + ")" + "(" + DAYS + ")");

}
