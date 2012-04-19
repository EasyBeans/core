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
import java.util.concurrent.TimeUnit;

import javax.ejb.ConcurrentAccessException;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.ow2.easybeans.application.accesstimeout.IAccessTimeout;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;

/**
 * @author Florent Benoit
 */
public class TestAccessTimeout {

    /**
     * MAX wait time for the executor.
     */
    private static final long MAX_WAIT_TIME = 30;

    /**
     * Stateful test Bean.
     */
    private IAccessTimeout annotationStatefulBean1 = null;

    /**
     * Stateful test Bean.
     */
    private IAccessTimeout annotationStatefulBean2 = null;

    /**
     * Singleton test Bean.
     */
    private IAccessTimeout annotationSingletonBean1 = null;

    /**
     * Singleton test Bean.
     */
    private IAccessTimeout annotationSingletonBean2 = null;

    @BeforeClass
    public void getBeans() throws NamingException {
        this.annotationStatefulBean1 = (IAccessTimeout) new InitialContext().lookup("AnnotationStatefulAccessTimeout");
        this.annotationStatefulBean2 = (IAccessTimeout) new InitialContext().lookup("AnnotationStatefulAccessTimeout");
        this.annotationSingletonBean1 = (IAccessTimeout) new InitialContext().lookup("AnnotationSingletonAccessTimeout");
        this.annotationSingletonBean2 = (IAccessTimeout) new InitialContext().lookup("AnnotationSingletonAccessTimeout");
    }


    public void testNoConcurrentStatefulAccessTimeout() throws InterruptedException {
        testNoConcurrentAccessTimeout(this.annotationStatefulBean1);
    }

    public void testStatefulDefaultTimeout() throws InterruptedException {
        testDefaultTimeout(this.annotationStatefulBean2);
    }

    public void testNoConcurrentSingletonAccessTimeout() throws InterruptedException {
        testNoConcurrentAccessTimeout(this.annotationSingletonBean1);
    }

    public void testSingletonDefaultTimeout() throws InterruptedException {
        testDefaultTimeout(this.annotationSingletonBean2);
    }

    public void testNoConcurrentAccessTimeout(final IAccessTimeout bean) throws InterruptedException {

        ExecutorService executorService = Executors.newFixedThreadPool(2);
        List<Future<String>> lst = new ArrayList<Future<String>>();
        NoTimeoutBeanCallable call1 = new NoTimeoutBeanCallable(bean, "Florent");
        NoTimeoutBeanCallable call2 = new NoTimeoutBeanCallable(bean, "Benoit");
        lst.add(executorService.submit(call1));
        lst.add(executorService.submit(call2));

        executorService.shutdown();
        executorService.awaitTermination(MAX_WAIT_TIME, TimeUnit.SECONDS);


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


    /**
     * Try to invoke the given bean and check that there is a timeout when there are concurrent threads.
     * @param bean the bean instance to check
     * @throws InterruptedException if executor service fails
     */
    public void testDefaultTimeout(final IAccessTimeout bean) throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        List<Future<String>> lst = new ArrayList<Future<String>>();

        DefaultTimeoutBeanCallable call1 = new DefaultTimeoutBeanCallable(bean, "Florent");
        DefaultTimeoutBeanCallable call2 = new DefaultTimeoutBeanCallable(bean, "Benoit");
        lst.add(executorService.submit(call1));
        lst.add(executorService.submit(call2));

        executorService.shutdown();
        executorService.awaitTermination(MAX_WAIT_TIME, TimeUnit.SECONDS);

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
