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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.ejb.ConcurrentAccessException;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.ow2.easybeans.application.accesstimeout.IAccessTimeout;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author Florent Benoit
 */
public class TestAccessTimeout {

    /**
     * Stateful test Bean.
     */
    private IAccessTimeout annotationStatefulBean = null;

    /**
     * Singleton test Bean.
     */
    private IAccessTimeout annotationSingletonBean = null;

    @BeforeClass
    public void getBeans() throws NamingException {
        this.annotationStatefulBean = (IAccessTimeout) new InitialContext().lookup("AnnotationStatefulAccessTimeout");
        this.annotationSingletonBean = (IAccessTimeout) new InitialContext().lookup("AnnotationSingletonAccessTimeout");
    }

    @Test
    public void testNoConcurrentStatefulAccessTimeout() {
        testNoConcurrentAccessTimeout(this.annotationStatefulBean);
    }

    @Test(dependsOnMethods="testNoConcurrentStatefulAccessTimeout")
    public void testStatefulDefaultTimeout() {
        testDefaultTimeout(this.annotationStatefulBean);
    }

    @Test
    public void testNoConcurrentSingletonAccessTimeout() {
        testNoConcurrentAccessTimeout(this.annotationSingletonBean);
    }

    @Test(dependsOnMethods="testNoConcurrentSingletonAccessTimeout")
    public void testSingletonDefaultTimeout() {
        testDefaultTimeout(this.annotationSingletonBean);
    }

    public void testNoConcurrentAccessTimeout(final IAccessTimeout bean) {

        ExecutorService executorService = Executors.newFixedThreadPool(3);
        List<Future<String>> lst = new ArrayList<Future<String>>();
        try {
            NoTimeoutBeanCallable call1 = new NoTimeoutBeanCallable(bean, "Florent");
            NoTimeoutBeanCallable call2 = new NoTimeoutBeanCallable(bean, "Benoit");
            lst.add(executorService.submit(call1));
            lst.add(executorService.submit(call2));
            while (executorService.isTerminated()) {
                try {
                    Thread.sleep(100L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } finally {
            executorService.shutdown();
        }

        try {
            Assert.assertEquals(lst.get(0).get(), "Florent");
        } catch (InterruptedException e) {
            throw new IllegalStateException("Not expected", e);
        } catch (ExecutionException e) {
            throw new IllegalStateException("Not expected", e);
        }

        try {
            String value = lst.get(1).get();
            Assert.fail("Shouldn't be able to get the value due to access timeout. Found '" + value + "'");
        } catch (InterruptedException e) {
            throw new IllegalStateException("Not expected", e);
        } catch (ExecutionException e) {
            // Check cause
            Throwable t = e.getCause();
            if (!(t instanceof ConcurrentAccessException)) {
                throw new IllegalStateException("Check the given exception", e);
            }
        }

    }




    public void testDefaultTimeout(final IAccessTimeout bean) {
        ExecutorService executorService = Executors.newFixedThreadPool(3);
        List<Future<String>> lst = new ArrayList<Future<String>>();
        try {
            DefaultTimeoutBeanCallable call1 = new DefaultTimeoutBeanCallable(bean, "Florent");
            DefaultTimeoutBeanCallable call2 = new DefaultTimeoutBeanCallable(bean, "Benoit");
            lst.add(executorService.submit(call1));
            lst.add(executorService.submit(call2));
            while (executorService.isTerminated()) {
                try {
                    Thread.sleep(100L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } finally {
            executorService.shutdown();
        }

        try {
            Assert.assertEquals(lst.get(0).get(), "Florent");
        } catch (InterruptedException e) {
            throw new IllegalStateException("Not expected", e);
        } catch (ExecutionException e) {
            throw new IllegalStateException("Not expected", e);
        }

        // Timeout should have been passed (processing time of 2s < 6s of the
        // method timeout)
        try {
            Assert.assertEquals(lst.get(1).get(), "Benoit");
        } catch (InterruptedException e) {
            throw new IllegalStateException("Not expected", e);
        } catch (ExecutionException e) {
            throw new IllegalStateException("Not expected", e);
        }

    }

    public class DefaultTimeoutBeanCallable implements Callable<String> {

        private IAccessTimeout bean = null;

        private String value = null;

        public DefaultTimeoutBeanCallable(final IAccessTimeout bean, final String value) {
            this.bean = bean;
            this.value = value;
        }

        public String call() throws Exception {
            return this.bean.defaultTimeout(this.value);
        }

    }

    public class NoTimeoutBeanCallable implements Callable<String> {

        private IAccessTimeout bean = null;

        private String value = null;

        public NoTimeoutBeanCallable(final IAccessTimeout bean, final String value) {
            this.bean = bean;
            this.value = value;
        }

        public String call() throws Exception {
            return this.bean.noTimeout(this.value);
        }

    }

}
