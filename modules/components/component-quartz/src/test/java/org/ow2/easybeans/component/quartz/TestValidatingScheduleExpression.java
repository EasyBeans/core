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

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Ensure that input is a valid format.
 * @author Florent Benoit
 */
public class TestValidatingScheduleExpression {

    private ScheduleExpressionParser converter = null;

    @BeforeClass
    void init() {
        this.converter = new ScheduleExpressionParser();
    }

    @Test
    public void testCheckSeconds() {

        // Second : 0-59 § EJB 3.1 SPEC 18.2.1
        Assert.assertTrue(this.converter.validate("0", Calendar.SECOND));
        Assert.assertFalse(this.converter.validate("-1", Calendar.SECOND));
        Assert.assertTrue(this.converter.validate("10", Calendar.SECOND));
        Assert.assertTrue(this.converter.validate("30", Calendar.SECOND));
        Assert.assertTrue(this.converter.validate("50", Calendar.SECOND));
        Assert.assertTrue(this.converter.validate("59", Calendar.SECOND));
        Assert.assertFalse(this.converter.validate("60", Calendar.SECOND));
        Assert.assertFalse(this.converter.validate("100", Calendar.SECOND));
        Assert.assertTrue(this.converter.validate("*", Calendar.SECOND));

        // Increments
        Assert.assertTrue(this.converter.validate("0/15", Calendar.SECOND));
        Assert.assertTrue(this.converter.validate("*/15", Calendar.SECOND));
        Assert.assertTrue(this.converter.validate("10/2", Calendar.SECOND));
        Assert.assertFalse(this.converter.validate("12/", Calendar.SECOND));
        Assert.assertFalse(this.converter.validate("*/62", Calendar.SECOND));
        Assert.assertFalse(this.converter.validate("62/2", Calendar.SECOND));
        Assert.assertFalse(this.converter.validate("-62/2", Calendar.SECOND));

        // Range
        Assert.assertTrue(this.converter.validate("1-10", Calendar.SECOND));
        Assert.assertTrue(this.converter.validate("1-59", Calendar.SECOND));
        Assert.assertTrue(this.converter.validate("0-10", Calendar.SECOND));
        Assert.assertTrue(this.converter.validate("0-10", Calendar.SECOND));
        Assert.assertTrue(this.converter.validate("10,20,30", Calendar.SECOND));

    }

    @Test
    public void testCheckMinutes() {

        // Minute : 0-59 § EJB 3.1 SPEC 18.2.1
        Assert.assertTrue(this.converter.validate("0", Calendar.MINUTE));
        Assert.assertFalse(this.converter.validate("-1", Calendar.MINUTE));
        Assert.assertTrue(this.converter.validate("10", Calendar.MINUTE));
        Assert.assertTrue(this.converter.validate("30", Calendar.MINUTE));
        Assert.assertTrue(this.converter.validate("50", Calendar.MINUTE));
        Assert.assertTrue(this.converter.validate("59", Calendar.MINUTE));
        Assert.assertFalse(this.converter.validate("60", Calendar.MINUTE));
        Assert.assertFalse(this.converter.validate("100", Calendar.MINUTE));

        Assert.assertTrue(this.converter.validate("*", Calendar.MINUTE));

        Assert.assertTrue(this.converter.validate("*/5", Calendar.MINUTE));

        Assert.assertTrue(this.converter.validate("1-10", Calendar.MINUTE));
        Assert.assertTrue(this.converter.validate("1-59", Calendar.MINUTE));
        Assert.assertTrue(this.converter.validate("0-10", Calendar.MINUTE));
        Assert.assertTrue(this.converter.validate("0-10", Calendar.MINUTE));
        Assert.assertTrue(this.converter.validate("10,20,30", Calendar.MINUTE));
    }

    @Test
    public void testCheckHours() {

        // Hour : 0-23 § EJB 3.1 SPEC 18.2.1
        Assert.assertTrue(this.converter.validate("0", Calendar.HOUR_OF_DAY));
        Assert.assertFalse(this.converter.validate("-1", Calendar.HOUR_OF_DAY));
        Assert.assertTrue(this.converter.validate("10", Calendar.HOUR_OF_DAY));
        Assert.assertTrue(this.converter.validate("20", Calendar.HOUR_OF_DAY));
        Assert.assertTrue(this.converter.validate("22", Calendar.HOUR_OF_DAY));
        Assert.assertTrue(this.converter.validate("23", Calendar.HOUR_OF_DAY));
        Assert.assertFalse(this.converter.validate("24", Calendar.HOUR_OF_DAY));
        Assert.assertFalse(this.converter.validate("30", Calendar.HOUR_OF_DAY));

        Assert.assertTrue(this.converter.validate("*", Calendar.HOUR_OF_DAY));

        Assert.assertTrue(this.converter.validate("1-10", Calendar.HOUR_OF_DAY));
        Assert.assertTrue(this.converter.validate("1-23", Calendar.HOUR_OF_DAY));
        Assert.assertTrue(this.converter.validate("0-10", Calendar.HOUR_OF_DAY));
        Assert.assertTrue(this.converter.validate("0-10", Calendar.HOUR_OF_DAY));
        Assert.assertTrue(this.converter.validate("10,20,23", Calendar.HOUR_OF_DAY));

        // List with increments not allowed
        Assert.assertFalse(this.converter.validate("1, 5/10", Calendar.HOUR_OF_DAY));


    }



    @Test
    public void testCheckDayOfMonth() {

        // Range 1-31 § EJB 3.1 SPEC 18.2.1
        Assert.assertTrue(this.converter.validate("*", Calendar.DAY_OF_MONTH), "wildcard");
        Assert.assertFalse(this.converter.validate("0", Calendar.DAY_OF_MONTH));
        Assert.assertTrue(this.converter.validate("1", Calendar.DAY_OF_MONTH));
        Assert.assertTrue(this.converter.validate("9", Calendar.DAY_OF_MONTH));
        Assert.assertTrue(this.converter.validate("10", Calendar.DAY_OF_MONTH));
        Assert.assertTrue(this.converter.validate("01", Calendar.DAY_OF_MONTH));
        Assert.assertTrue(this.converter.validate("20", Calendar.DAY_OF_MONTH));
        Assert.assertTrue(this.converter.validate("29", Calendar.DAY_OF_MONTH));
        Assert.assertTrue(this.converter.validate("30", Calendar.DAY_OF_MONTH));
        Assert.assertTrue(this.converter.validate("31", Calendar.DAY_OF_MONTH));
        Assert.assertFalse(this.converter.validate("32", Calendar.DAY_OF_MONTH));
        Assert.assertFalse(this.converter.validate("36", Calendar.DAY_OF_MONTH));

        // Range -7,-1 § EJB 3.1 SPEC 18.2.1
        Assert.assertFalse(this.converter.validate("-0", Calendar.DAY_OF_MONTH));
        Assert.assertTrue(this.converter.validate("-1", Calendar.DAY_OF_MONTH));
        Assert.assertTrue(this.converter.validate("-2", Calendar.DAY_OF_MONTH));
        Assert.assertTrue(this.converter.validate("-5", Calendar.DAY_OF_MONTH));
        Assert.assertTrue(this.converter.validate("-7", Calendar.DAY_OF_MONTH));
        Assert.assertFalse(this.converter.validate("-8", Calendar.DAY_OF_MONTH));
        Assert.assertFalse(this.converter.validate("-08", Calendar.DAY_OF_MONTH));

        // Last § EJB 3.1 SPEC 18.2.1
        Assert.assertTrue(this.converter.validate("Last", Calendar.DAY_OF_MONTH));

        Assert.assertTrue(this.converter.validate("27-3", Calendar.DAY_OF_MONTH));
        Assert.assertTrue(this.converter.validate("27-Last , 1-3", Calendar.DAY_OF_MONTH));

        // List with wildcard not allowed
        Assert.assertFalse(this.converter.validate("-2, *", Calendar.DAY_OF_MONTH));

        // Null element not allowed
        Assert.assertFalse(this.converter.validate(null, Calendar.DAY_OF_MONTH));

        // Range with negative values
        Assert.assertTrue(this.converter.validate("-7--1", Calendar.DAY_OF_MONTH));

    }

    @Test
    public void testCheckDayOfMonthNDays() {

        Assert.assertTrue(this.converter.validate("1st Mon", Calendar.DAY_OF_MONTH));
        Assert.assertTrue(this.converter.validate("2nd Mon", Calendar.DAY_OF_MONTH));
        Assert.assertTrue(this.converter.validate("3rd Mon", Calendar.DAY_OF_MONTH));
        Assert.assertTrue(this.converter.validate("4th Mon", Calendar.DAY_OF_MONTH));
        Assert.assertTrue(this.converter.validate("5th Mon", Calendar.DAY_OF_MONTH));
        Assert.assertTrue(this.converter.validate("last Mon", Calendar.DAY_OF_MONTH));

    }

    @Test
    public void testCheckMonth() {

        // Month : 1-12 § EJB 3.1 SPEC 18.2.1
        Assert.assertTrue(this.converter.validate("*", Calendar.MONTH), "wildcard");
        Assert.assertFalse(this.converter.validate("0", Calendar.MONTH));
        Assert.assertTrue(this.converter.validate("1", Calendar.MONTH));
        Assert.assertTrue(this.converter.validate("2", Calendar.MONTH));
        Assert.assertTrue(this.converter.validate("9", Calendar.MONTH));
        Assert.assertTrue(this.converter.validate("10", Calendar.MONTH));
        Assert.assertTrue(this.converter.validate("11", Calendar.MONTH));
        Assert.assertTrue(this.converter.validate("12", Calendar.MONTH));
        Assert.assertFalse(this.converter.validate("13", Calendar.MONTH));

        // Month : 1-12 § EJB 3.1 SPEC 18.2.1
        Assert.assertTrue(this.converter.validate("Jan", Calendar.MONTH));
        Assert.assertTrue(this.converter.validate("Feb", Calendar.MONTH));
        Assert.assertTrue(this.converter.validate("Mar", Calendar.MONTH));
        Assert.assertTrue(this.converter.validate("Apr", Calendar.MONTH));
        Assert.assertTrue(this.converter.validate("May", Calendar.MONTH));
        Assert.assertTrue(this.converter.validate("Jun", Calendar.MONTH));
        Assert.assertTrue(this.converter.validate("Jul", Calendar.MONTH));
        Assert.assertTrue(this.converter.validate("Aug", Calendar.MONTH));
        Assert.assertTrue(this.converter.validate("Sep", Calendar.MONTH));
        Assert.assertTrue(this.converter.validate("Oct", Calendar.MONTH));
        Assert.assertTrue(this.converter.validate("Nov", Calendar.MONTH));
        Assert.assertTrue(this.converter.validate("Dec", Calendar.MONTH));
        Assert.assertFalse(this.converter.validate("Tot", Calendar.MONTH));
    }

    @Test
    public void testCheckDayOfWeek() {

        // Day Of Week : 0-7 § EJB 3.1 SPEC 18.2.1
        Assert.assertTrue(this.converter.validate("*", Calendar.DAY_OF_WEEK), "wildcard");
        Assert.assertTrue(this.converter.validate("0", Calendar.DAY_OF_WEEK));
        Assert.assertTrue(this.converter.validate("1", Calendar.DAY_OF_WEEK));
        Assert.assertTrue(this.converter.validate("2", Calendar.DAY_OF_WEEK));
        Assert.assertTrue(this.converter.validate("3", Calendar.DAY_OF_WEEK));
        Assert.assertTrue(this.converter.validate("4", Calendar.DAY_OF_WEEK));
        Assert.assertTrue(this.converter.validate("5", Calendar.DAY_OF_WEEK));
        Assert.assertTrue(this.converter.validate("6", Calendar.DAY_OF_WEEK));
        Assert.assertTrue(this.converter.validate("7", Calendar.DAY_OF_WEEK));
        Assert.assertFalse(this.converter.validate("8", Calendar.DAY_OF_WEEK));
        Assert.assertFalse(this.converter.validate("06", Calendar.DAY_OF_WEEK));

        Assert.assertTrue(this.converter.validate("Mon,Wed,Fri", Calendar.DAY_OF_WEEK));
        Assert.assertTrue(this.converter.validate("Mon, Wed, Fri", Calendar.DAY_OF_WEEK));
        Assert.assertTrue(this.converter.validate(" Mon , Wed , Fri ", Calendar.DAY_OF_WEEK));

    }

}
