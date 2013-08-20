/**
 * EasyBeans
 * Copyright (C) 2006 Bull S.A.S.
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
 * $Id: LifeCycleInterceptorsTestCase.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.tests.enhancer.interceptors.basiclifecycle;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.fail;

import org.ow2.easybeans.api.bean.lifecycle.EasyBeansSLSBLifeCycle;
import org.ow2.easybeans.tests.enhancer.interceptors.basiclifecycle.bean.StatelessBean2;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Call bean and see if callbacks are working.
 * @author Florent Benoit
 */
public class LifeCycleInterceptorsTestCase{

    /**
     * Bean (stateless2) tested.
     */
    private StatelessBean2 statelessBean2 = null;


    /**
     * Enhancing has been done ?
     */
    private static boolean enhancingDone = false;

    /**
     * Setup for test case.
     * @throws Exception if super method fails
     */
    @BeforeMethod
    protected void setUp() throws Exception {
        if (!enhancingDone) {
            LifeCycleInterceptorsClassesEnhancer.enhance();
            enhancingDone = true;
        }
        statelessBean2 = new StatelessBean2();
    }

    // =====================
    // Common methods
    // =====================
    /**

    /**
     * @return a lifecycle object for the current stateless2 bean
     */
    private EasyBeansSLSBLifeCycle getSLSB2LifeCycle() {
        if (statelessBean2 instanceof EasyBeansSLSBLifeCycle) {
            return (EasyBeansSLSBLifeCycle) statelessBean2;
        }
        fail("The stateless bean is not an instance of the interface EasyBeansSLSBLifeCycle.");
        return null;
    }

    // =====================
    // Stateless
    // =====================

    /**
     * Test the inheritance on lifecycle method.
     * Super methods should be called.
     */
    @Test
    public void testInheritanceBeanLifeCycleStateless() {
        assertEquals(0, statelessBean2.getSuperLifeCycleCounter());

        EasyBeansSLSBLifeCycle lifeCycle = getSLSB2LifeCycle();

        // postConstruct should increment the counter
        lifeCycle.postConstructEasyBeansLifeCycle();
        // and the counter of the super class
        assertEquals(1, statelessBean2.getSuperLifeCycleCounter());
    }

}
