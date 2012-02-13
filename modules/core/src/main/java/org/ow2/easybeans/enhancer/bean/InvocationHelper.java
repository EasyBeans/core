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

package org.ow2.easybeans.enhancer.bean;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.ejb.EJBException;

/**
 * Helper used when generating local proxy.
 * @author Florent Benoit
 */
public final class InvocationHelper {

    /**
     * Static class.
     */
    private InvocationHelper() {

    }

    /**
     * Allows to get a method from the given class.
     * @param clazz the given class
     * @param methodName the name of the method
     * @param args the args
     * @return the method
     */
    public static Method getMethod(final Class<?> clazz, final String methodName, final Class<?>[] args) {
        try {
            return clazz.getMethod(methodName, args);
        } catch (SecurityException e) {
            throw new EJBException("Unable to get method '" + methodName + "'", e);
        } catch (NoSuchMethodException e) {
            throw new EJBException("Unable to get method '" + methodName + "'", e);
        }
    }

    /**
     * Allows to invoke the given method on the instance.
     * @param instance the instance to use
     * @param m the method that is invoked
     * @param handler the callback handler
     * @param args the given arguments of the method
     * @return the result if any
     * @throws Throwable if there is any exception
     */
    public static Object invoke(final Object instance, final Method m, final InvocationHandler handler, final Object[] args)
            throws Throwable {
        m.setAccessible(true);
        try {
            return handler.invoke(instance, m, args);
        } catch (Throwable e) {
            if (e instanceof InvocationTargetException) {
                throw e.getCause();
            }
            throw e;
        } finally {
            m.setAccessible(false);
        }

    }
}
