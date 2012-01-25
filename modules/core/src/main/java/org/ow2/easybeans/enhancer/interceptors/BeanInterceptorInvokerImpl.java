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
 * $Id: BeanInterceptorInvokerImpl.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.enhancer.interceptors;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

import org.ow2.easybeans.api.EasyBeansInvocationContext;
import org.ow2.easybeans.api.interceptor.EZBInterceptorInvoker;
import org.ow2.easybeans.api.interceptor.EZBInterceptorManager;
import org.ow2.util.log.Log;
import org.ow2.util.log.LogFactory;
import org.ow2.util.scan.api.metadata.structures.JMethod;

/**
 * Invoker of an interceptor (that is on a bean).
 * @author Florent Benoit
 */
public class BeanInterceptorInvokerImpl implements EZBInterceptorInvoker {

    /**
     * Logger.
     */
    private static final Log LOGGER = LogFactory.getLog(BeanInterceptorInvokerImpl.class);

    /**
     * Method of the interceptor.
     */
    private Method method = null;

    /**
     * Name of the class of the bean.
     */
    private String beanClassname = null;

    /**
     * Build an interceptor invoker for the given interceptor class.
     * @param beanClassname the name of the class of the bean
     * @param jMethod the method that will be used for the interceptor
     * @param classLoader the given classloader used to load the class
     */
    public BeanInterceptorInvokerImpl(final String beanClassname, final JMethod jMethod, final ClassLoader classLoader) {
        this.beanClassname = beanClassname;
        this.method = MethodHelper.getMethod(beanClassname, jMethod, classLoader);
    }

    /**
     * Invocation of the given invoker and it will return a result (or throw an
     * exception if there is a failure).
     * @param invocationContext the invocation context for the given invocation
     * @param interceptorManager the manager of interceptors (in order to ask
     *        instance of intereptors)
     * @return the invocation result
     * @throws Exception if there is a failure
     */
    public Object invoke(final EasyBeansInvocationContext invocationContext, final EZBInterceptorManager interceptorManager)
            throws Exception {
        // Check if method is accessible or not
        boolean isAccessible = this.method.isAccessible();

        LOGGER.debug("Calling bean with invocationContext ''{0}'' on bean ''{1}'' with parameters ''{2}''", invocationContext,
                invocationContext.getTarget(), Arrays.asList(invocationContext.getParameters()));

        // If accessible, call it directly
        if (isAccessible) {
            return invoke(invocationContext);
        }

        // Else, set accessible flag before calling the object
        this.method.setAccessible(true);
        try {
            return invoke(invocationContext);
        } finally {
            this.method.setAccessible(isAccessible);
        }
    }


    /**
     * Invoke the method with the invocation context as parameter.
     * @param invocationContext the invocation context given as an argument of the method
     * @return the result of the invocation
     * @throws Exception if invocation fails
     */
   private Object invoke(final EasyBeansInvocationContext invocationContext) throws Exception {
       try {
           return this.method.invoke(invocationContext.getTarget(), invocationContext);
       } catch (InvocationTargetException e) {
           Throwable t = e.getTargetException();
           if (t instanceof Exception) {
               throw (Exception) t;
           }
           throw e;
       }
   }

    /**
     * @return string representation of this interceptor.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(this.getClass().getName());
        sb.append("[className=");
        sb.append(this.beanClassname);
        sb.append(", method=");
        sb.append(this.method);
        return sb.toString();
    }


}
