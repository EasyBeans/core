/**
 * EasyBeans
 * Copyright (C) 2008 Bull S.A.S.
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
 * $Id: EZBInvocationContextFactory.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.api.interceptor;

import org.ow2.easybeans.api.EasyBeansInvocationContext;
import org.ow2.easybeans.api.bean.EasyBeansBean;

/**
 * Invocation context factory provides the interceptor manager factory and the
 * invocation context generation.
 * @author Florent Benoit
 */
public interface EZBInvocationContextFactory {
    /**
     * Gets the interceptor manager factory.
     * @return an instance of the manager factory
     */
    EZBInterceptorManagerFactory getInterceptorManagerFactory();

    /**
     * Gets an invocation context for the given method.
     * @param instance the bean's instance
     * @param interceptorManager the manager of the interceptors
     * @param interceptorType the type of the interceptor
     * @param methodSignature the key in order to find data for the given method
     * @param parameters the parameters of the method
     * @return an invocation context implementation
     */
    EasyBeansInvocationContext getContext(final EasyBeansBean instance, final EZBInterceptorManager interceptorManager,
            final String interceptorType, final String methodSignature, final Object... parameters);

}
