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

package org.ow2.easybeans.tests.embeddable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.ejb.EJBException;
import javax.ejb.embeddable.EJBContainer;
import javax.naming.Context;

import org.testng.Assert;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;


/**
 * Test of the embeddable container.
 * @author Florent Benoit
 */
public class TestEmbeddable {

    /**
     * Context used to perform lookup.
     */
    private Context context = null;

    /**
     * Instance of the embeddable container.
     */
    private EJBContainer ejbContainer  = null;


    /**
     * Launch the container before any tests.
     */
    @BeforeSuite
    public void startContainer() {
        this.ejbContainer = EJBContainer.createEJBContainer();
        this.context = this.ejbContainer.getContext();
    }

    /**
     * Test simple no interface bean.
     * @throws Exception if it fails
     */
    @Test
    public void testNoInterfaceView() throws Exception {
        CalculatorBean bean = (CalculatorBean) this.context.lookup("java:global/test-classes/CalculatorBean");
        Assert.assertEquals(bean.add(0, 1), 1);
    }

    /**
     * Test a bean with an interceptor.
     * @throws Exception if it fails
     */
    @Test
    public void testBeanWithInterceptor() throws Exception {
        BeanWithInterceptor bean = (BeanWithInterceptor) this.context.lookup("java:global/test-classes/BeanWithInterceptor");
        Assert.assertEquals(bean.getValue("florent"), BeanWithInterceptor.HAS_BEEN_REPLACED);
    }

    /**
     * Test to call a method that shouldn't be called.
     * @throws Exception if it fails
     */
    @Test
    public void testNoInterfaceViewProtectedMethod() throws Exception {
        Method m = CalculatorBean.class.getDeclaredMethod("methodShouldntBeCalled");
        CalculatorBean bean = (CalculatorBean) this.context.lookup("java:global/test-classes/CalculatorBean");
        try {
            // Should expect an EJBException
            m.invoke(bean);
            Assert.fail("Cannot call a protected method");
        } catch (InvocationTargetException e) {
            Assert.assertTrue(e.getCause() instanceof EJBException);
        }
    }

    /**
     * Test to call beans that have used EJB lookup.
     * @throws Exception if it fails
     */
    @Test
    public void testLookupBean() throws Exception {
        SingletonLookupBean bean = (SingletonLookupBean) this.context.lookup("java:global/test-classes/SingletonLookupBean");
        Assert.assertNotNull(bean.getCalculatorBean1(), "Bean has not be injected");
        Assert.assertNotNull(bean.getCalculatorBean2(), "Bean has not be injected");

        Assert.assertEquals(bean.calcWithBean1(-1, 2), 1);
        Assert.assertEquals(bean.calcWithBean2(2, -1), 1);

    }

    /**
     * Stop the container after all the tests.
     */
    @AfterSuite
    public void stopContainer() {
        if (this.ejbContainer != null) {
            this.ejbContainer.close();
        }
    }



}
