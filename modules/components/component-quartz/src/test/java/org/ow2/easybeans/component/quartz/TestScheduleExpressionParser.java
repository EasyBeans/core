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

import javax.ejb.ScheduleExpression;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test the parser.
 * @author Florent Benoit
 */
public class TestScheduleExpressionParser {

    private ScheduleExpressionParser scheduleExpressionParser;

    @BeforeClass
    public void initParser() {
        this.scheduleExpressionParser = new ScheduleExpressionParser();
    }


    @Test
    public void testParser() {

        ScheduleExpression scheduleExpression = new ScheduleExpression().second("*/5").minute("*").hour("*");

        EasyBeansScheduleExpression easyBeansScheduleExpression = this.scheduleExpressionParser.parse(scheduleExpression);
        Assert.assertNotNull(easyBeansScheduleExpression);

        Assert.assertTrue(easyBeansScheduleExpression.getSecond() instanceof ScheduleValueIncrements);
        Assert.assertTrue(easyBeansScheduleExpression.getMinute() instanceof ScheduleValueWildCard);
        Assert.assertTrue(easyBeansScheduleExpression.getHour() instanceof ScheduleValueWildCard);
        Assert.assertTrue(easyBeansScheduleExpression.getDayOfMonth() instanceof ScheduleValueWildCard);

    }

    @Test
    public void testNDaysDayOfMonth() {

        ScheduleExpression scheduleExpression = new ScheduleExpression().second("*").minute("*").hour("*").dayOfMonth("1st mon");

        EasyBeansScheduleExpression easyBeansScheduleExpression = this.scheduleExpressionParser.parse(scheduleExpression);


        Assert.assertNotNull(easyBeansScheduleExpression);

        Assert.assertTrue(easyBeansScheduleExpression.getSecond() instanceof ScheduleValueWildCard);
        Assert.assertTrue(easyBeansScheduleExpression.getMinute() instanceof ScheduleValueWildCard);
        Assert.assertTrue(easyBeansScheduleExpression.getHour() instanceof ScheduleValueWildCard);
        Assert.assertTrue(easyBeansScheduleExpression.getDayOfMonth() instanceof ScheduleValueAttributeNDays);
    }


}
