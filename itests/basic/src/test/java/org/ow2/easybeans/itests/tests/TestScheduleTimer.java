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

package org.ow2.easybeans.itests.tests;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.ow2.easybeans.application.timer.ISimpleScheduleBean;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Tests timer added on EJBs.
 * @author Florent Benoit
 */
public class TestScheduleTimer {

    /**
     * Second.
     */
    private static final long SECOND = 1000L;

    /**
     * Bean with annotation.
     */
    private ISimpleScheduleBean simpleScheduleBean = null;

    @BeforeClass
    public void getBean() throws NamingException {
        this.simpleScheduleBean = (ISimpleScheduleBean) new InitialContext().lookup("SimpleScheduleBean");
    }


    @Test
    public void testSchedule1() throws InterruptedException {
        int count = this.simpleScheduleBean.getMethodCalledCount();
        // wait
        Thread.sleep(10 * SECOND);
        // Check we've some new count
        int newCount = this.simpleScheduleBean.getMethodCalledCount();

        Assert.assertTrue(newCount > count);
        Assert.assertTrue(newCount > 1);
        long avg = this.simpleScheduleBean.getAverageElapsedTime();

        // Timer should be between every 2 and 4 seconds
        Assert.assertTrue(avg > 2000L);
        Assert.assertTrue(avg < 4000L);

    }

}
