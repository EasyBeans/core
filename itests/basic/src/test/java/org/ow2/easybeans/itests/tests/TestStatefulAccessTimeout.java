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

import org.ow2.easybeans.application.accesstimeout.IStatefulAccessTimeout;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author Florent Benoit
 */
public class TestStatefulAccessTimeout {

    /**
     * Test Bean.
     */
    private IStatefulAccessTimeout annotationStatefulBean = null;

    @BeforeClass
    public void getBean() throws NamingException {
        this.annotationStatefulBean = (IStatefulAccessTimeout) new InitialContext().lookup("AnnotationStatefulAccessTimeout");
    }

    @Test
    public void testNoConcurrentAccessTimeout() {
        ExecutorService executorService = Executors.newFixedThreadPool(3);
        List<Future<String>> lst = new ArrayList<Future<String>>();
        try {
            NoTimeoutBeanCallable call1 = new NoTimeoutBeanCallable(this.annotationStatefulBean, "Florent");
            NoTimeoutBeanCallable call2 = new NoTimeoutBeanCallable(this.annotationStatefulBean, "Benoit");
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

    @Test(dependsOnMethods="testNoConcurrentAccessTimeout")
    public void testDefaultTimeout() {
        ExecutorService executorService = Executors.newFixedThreadPool(3);
        List<Future<String>> lst = new ArrayList<Future<String>>();
        try {
            DefaultTimeoutBeanCallable call1 = new DefaultTimeoutBeanCallable(this.annotationStatefulBean, "Florent");
            DefaultTimeoutBeanCallable call2 = new DefaultTimeoutBeanCallable(this.annotationStatefulBean, "Benoit");
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

        private IStatefulAccessTimeout bean = null;

        private String value = null;

        public DefaultTimeoutBeanCallable(final IStatefulAccessTimeout bean, final String value) {
            this.bean = bean;
            this.value = value;
        }

        public String call() throws Exception {
            return this.bean.defaultTimeout(this.value);
        }

    }

    public class NoTimeoutBeanCallable implements Callable<String> {

        private IStatefulAccessTimeout bean = null;

        private String value = null;

        public NoTimeoutBeanCallable(final IStatefulAccessTimeout bean, final String value) {
            this.bean = bean;
            this.value = value;
        }

        public String call() throws Exception {
            return this.bean.noTimeout(this.value);
        }

    }

}
