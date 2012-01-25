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
 * $Id: StatelessBean3.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.tests.enhancer.interceptors.lifecycle.bean;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Stateless;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;

/**
 * Simple class for testing lifecycle events.
 * Defines lifecycle inside the bean class + business interceptor.
 * @author Florent Benoit
 */
@Stateless
public class StatelessBean3 implements SessionBeanItf {

    /**
     * Counter to be increased by postconstruct and preDestroy.
     */
    private int counter = 0;

    /**
     * Increment the counter.
     */
    @PostConstruct
    public void increment() {
        counter++;
    }

    /**
     * Decrement the counter.
     */
    @PreDestroy
    public void decrement() {
        counter--;
    }


    /**
     * @return the value of the internal counter.
     */
    public int getCounter() {
        return counter;
    }

    /**
     * Dummy interceptor.
     * @param iCtx the invocation context object
     * @return invocation result
     * @throws Exception if proceed() fails
     */
    @AroundInvoke
    protected Object dummyInterceptor(final InvocationContext iCtx) throws Exception {
        return iCtx.proceed();
    }


}
