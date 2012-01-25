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

package org.ow2.easybeans.tests.enhancer.interceptors.lifecycle;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;

import org.ow2.easybeans.api.bean.lifecycle.EasyBeansSFSBLifeCycle;
import org.ow2.easybeans.api.bean.lifecycle.EasyBeansSLSBLifeCycle;
import org.ow2.easybeans.tests.enhancer.interceptors.lifecycle.bean.StatefulBean;
import org.ow2.easybeans.tests.enhancer.interceptors.lifecycle.bean.StatelessBean;
import org.ow2.easybeans.tests.enhancer.interceptors.lifecycle.bean.StatelessBean2;
import org.ow2.easybeans.tests.enhancer.interceptors.lifecycle.bean.StatelessBean3;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Call bean and see if callbacks are working.
 * @author Florent Benoit
 */
public class LifeCycleInterceptorsTestCase{

    /**
     * Bean (stateless) tested.
     */
    private StatelessBean statelessBean = null;

    /**
     * Bean (stateless2) tested.
     */
    private StatelessBean2 statelessBean2 = null;

    /**
     * Bean (stateless3) tested.
     */
    private StatelessBean3 statelessBean3 = null;

    /**
     * Bean (stateful) tested.
     */
    private StatefulBean statefulBean = null;

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
        statelessBean = new StatelessBean();
        statelessBean2 = new StatelessBean2();
        statelessBean3 = new StatelessBean3();
        statefulBean = new StatefulBean();
    }

    // =====================
    // Common methods
    // =====================
    /**
     * @return a lifecycle object for the current stateless bean
     */
    private EasyBeansSLSBLifeCycle getSLSBLifeCycle() {
        if (statelessBean instanceof EasyBeansSLSBLifeCycle) {
            return (EasyBeansSLSBLifeCycle) statelessBean;
        }
        fail("The stateless bean is not an instance of the interface EasyBeansSLSBLifeCycle.");
        return null;
    }

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

    /**
     * @return a lifecycle object for the current stateless3 bean
     */
    private EasyBeansSLSBLifeCycle getSLSB3LifeCycle() {
        if (statelessBean3 instanceof EasyBeansSLSBLifeCycle) {
            return (EasyBeansSLSBLifeCycle) statelessBean3;
        }
        fail("The stateless bean is not an instance of the interface EasyBeansSLSBLifeCycle.");
        return null;
    }

    /**
     * @return a lifecycle object for the current stateful bean
     */
    private EasyBeansSFSBLifeCycle getSFSBLifeCycle() {
        if (statefulBean instanceof EasyBeansSFSBLifeCycle) {
            return (EasyBeansSFSBLifeCycle) statefulBean;
        }
        fail("The stateful bean is not an instance of the interface EasyBeansSLSBLifeCycle.");
        return null;
    }

    // =====================
    // Stateless
    // =====================

    /**
     * Test the stateless bean callbacks.
     */
    @Test
    public void testStatelessBeanCallbacks() {
        assertEquals(0, statelessBean.getCounter());
        EasyBeansSLSBLifeCycle lifeCycle = getSLSBLifeCycle();

        // postConstruct should increment the counter
        lifeCycle.postConstructEasyBeansLifeCycle();
        assertEquals(1, statelessBean.getCounter());

        // preDestroy should decrement counter
        lifeCycle.preDestroyEasyBeansLifeCycle();
        assertEquals(0, statelessBean.getCounter());
    }

    /**
     * Test the postConstruct on stateless bean.
     */
    @Test
    public void testStatelessPostConstruct() {
        assertFalse(statelessBean.isPostConstructCalled());
        EasyBeansSLSBLifeCycle lifeCycle = getSLSBLifeCycle();
        lifeCycle.postConstructEasyBeansLifeCycle();

        assertTrue(statelessBean.isPostConstructCalled());
    }

    /**
     * Test the preDestroy on stateless bean.
     */
    @Test
    public void testStatelessPreDestroy() {
        assertFalse(statelessBean.isPreDestroyCalled());
        EasyBeansSLSBLifeCycle lifeCycle = getSLSBLifeCycle();
        lifeCycle.preDestroyEasyBeansLifeCycle();

        assertTrue(statelessBean.isPreDestroyCalled());
    }


    /**
     * Test the postConstruct on stateless bean.
     */
    @Test
    public void testStateless2PostConstruct() {
        assertFalse(statelessBean2.isPostConstructCalled());
        EasyBeansSLSBLifeCycle lifeCycle = getSLSB2LifeCycle();
        lifeCycle.postConstructEasyBeansLifeCycle();

        assertTrue(statelessBean2.isPostConstructCalled());
    }

    /**
     * Test the preDestroy on stateless bean.
     */
    @Test
    public void testStateless2PreDestroy() {
        assertFalse(statelessBean2.isPreDestroyCalled());
        EasyBeansSLSBLifeCycle lifeCycle = getSLSB2LifeCycle();
        lifeCycle.preDestroyEasyBeansLifeCycle();

        assertTrue(statelessBean2.isPreDestroyCalled());
    }


    /**
     * Test the postConstruct/preDestroy on stateless bean.
     */
    @Test
    public void testStateless3() {
        assertEquals(0, statelessBean3.getCounter());

        EasyBeansSLSBLifeCycle lifeCycle = getSLSB3LifeCycle();

        // postConstruct should increment the counter
        lifeCycle.postConstructEasyBeansLifeCycle();
        assertEquals(1, statelessBean3.getCounter());

        // preDestroy should decrement the counter
        lifeCycle.preDestroyEasyBeansLifeCycle();
        assertEquals(0, statelessBean3.getCounter());
    }


    /**
     * Test the inheritance on lifecycle method.
     * Super methods should be called.
     */
    @Test
    public void testInheritanceBeanLifeCycleStateless() {
        assertEquals(0, statelessBean.getCounter());
        assertEquals(0, statelessBean.getSuperLifeCycleCounter());

        EasyBeansSLSBLifeCycle lifeCycle = getSLSBLifeCycle();

        // postConstruct should increment the counter
        lifeCycle.postConstructEasyBeansLifeCycle();
        assertEquals(1, statelessBean.getCounter());
        // and the counter of the super class
        assertEquals(1, statelessBean.getSuperLifeCycleCounter());

        // preDestroy should decrement the counter
        lifeCycle.preDestroyEasyBeansLifeCycle();
        assertEquals(0, statelessBean.getCounter());
        assertEquals(0, statelessBean.getSuperLifeCycleCounter());
    }


    // =====================
    // Stateful
    // =====================

    /**
     * Test the stateful bean callbacks.
     */
    @Test
    public void testStatefulBeanCallbacks() {
        EasyBeansSFSBLifeCycle lifeCycle = getSFSBLifeCycle();

        int internalCounter = 0;
        assertEquals(internalCounter++, statefulBean.getCounter());
        if (statefulBean instanceof EasyBeansSFSBLifeCycle) {
            lifeCycle = (EasyBeansSFSBLifeCycle) statefulBean;
        }

        // postConstruct should increment the counter
        lifeCycle.postConstructEasyBeansLifeCycle();
        assertEquals(internalCounter++, statefulBean.getCounter());

        // preDestroy should increment counter
        lifeCycle.preDestroyEasyBeansLifeCycle();
        assertEquals(internalCounter++, statefulBean.getCounter());

        // prePassivate should increment counter
        lifeCycle.prePassivateEasyBeansLifeCycle();
        assertEquals(internalCounter++, statefulBean.getCounter());

        // postActivate should increment counter
        lifeCycle.postActivateEasyBeansLifeCycle();
        assertEquals(internalCounter++, statefulBean.getCounter());

    }

    /**
     * Test the postConstruct on stateful bean.
     */
    @Test
    public void testStatefulPostConstruct() {
        assertFalse(statefulBean.isPostConstructCalled());
        EasyBeansSFSBLifeCycle lifeCycle = getSFSBLifeCycle();
        lifeCycle.postConstructEasyBeansLifeCycle();
        assertTrue(statefulBean.isPostConstructCalled());
    }

    /**
     * Test the preDestroy on stateful bean.
     */
    @Test
    public void testStatefulPreDestroy() {
        assertFalse(statefulBean.isPreDestroyCalled());
        EasyBeansSFSBLifeCycle lifeCycle = getSFSBLifeCycle();
        lifeCycle.preDestroyEasyBeansLifeCycle();
        assertTrue(statefulBean.isPreDestroyCalled());
    }

    /**
     * Test the prePassivate on stateful bean.
     */
    @Test
    public void testStatefulPrePassivate() {
        assertFalse(statefulBean.isprePassivateCalled());
        EasyBeansSFSBLifeCycle lifeCycle = getSFSBLifeCycle();
        lifeCycle.prePassivateEasyBeansLifeCycle();
        assertTrue(statefulBean.isprePassivateCalled());
    }

    /**
     * Test the PostActivate on stateful bean.
     */
    @Test
    public void testStatefulPostActivate() {
        assertFalse(statefulBean.isPostActivateCalled());
        EasyBeansSFSBLifeCycle lifeCycle = getSFSBLifeCycle();
        lifeCycle.postActivateEasyBeansLifeCycle();
        assertTrue(statefulBean.isPostActivateCalled());
    }


    /**
     * Test the inheritance on lifecycle method.
     * Super methods should be called.
     */
    @Test
    public void testInheritanceBeanLifeCycleStateful() {
        assertEquals(0, statefulBean.getCounter());
        assertEquals(0, statefulBean.getSuperLifeCycleCounter());

        EasyBeansSFSBLifeCycle lifeCycle = getSFSBLifeCycle();

        // postActivate should increment the counter
        lifeCycle.postActivateEasyBeansLifeCycle();
        assertEquals(1, statefulBean.getCounter());
        // and the counter of the super class
        assertEquals(1, statefulBean.getSuperLifeCycleCounter());

        // increment counter of default class
        lifeCycle.prePassivateEasyBeansLifeCycle();
        assertEquals(2, statefulBean.getCounter());
        // prePassivate should decrement the counter
        assertEquals(0, statefulBean.getSuperLifeCycleCounter());
    }

}
