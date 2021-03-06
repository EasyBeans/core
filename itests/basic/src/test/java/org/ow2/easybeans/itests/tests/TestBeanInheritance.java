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

import org.ow2.easybeans.application.beaninheritance.ITestBeanInheritance;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test bean inheritance on beans.
 * @author Florent Benoit
 */
public class TestBeanInheritance {


    /**
     * Test Bean.
     */
    private ITestBeanInheritance testBean = null;


    @BeforeClass
    public void getBean() throws NamingException {
        this.testBean = (ITestBeanInheritance) new InitialContext().lookup("BeanInheritanceTestBean");
    }


    @Test
    public void testStateless() {
        this.testBean.checkStateless();
    }

    @Test
    public void testStateful() {
        this.testBean.checkStateful();
    }

    @Test
    public void testSingleton() {
        this.testBean.checkSingleton();
    }
}
