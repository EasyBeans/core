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
 * $Id: MyLifeCycleInterceptorStateless.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.tests.enhancer.interceptors.lifecycle.bean;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.interceptor.InvocationContext;

/**
 * Defines some callback methods.
 * @author Florent Benoit
 */
public class MyLifeCycleInterceptorStateless {

    /**
     * &#64;{@link javax.ejb.PostConstruct} method.
     * @param invocationContext the invocation context
     */
    @PostConstruct
    public void postConstMethod(final InvocationContext invocationContext) {
        Object o = invocationContext.getTarget();
        if (o instanceof AbsSessionBean) {
            AbsSessionBean bean = (AbsSessionBean) o;
            bean.calledPostConstruct();
            try {
                invocationContext.proceed();
            } catch (Exception e) {
                throw new RuntimeException("Cannot proceed invocationContext", e);
            }
        }
    }

    /**
     * &#64;{@link javax.ejb.PreDestroy} method.
          * @param invocationContext the invocation context
     */
    @PreDestroy
    public void onePreDestroyMethod(final InvocationContext invocationContext) {
        Object o = invocationContext.getTarget();
        if (o instanceof AbsSessionBean) {
            AbsSessionBean bean = (AbsSessionBean) o;
            bean.calledPreDestroy();
            try {
                invocationContext.proceed();
            } catch (Exception e) {
                throw new RuntimeException("Cannot proceed invocationContext", e);
            }
        }
    }
}
