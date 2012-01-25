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
 * $Id: AbsProxy.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.component.jdbcpool;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * Defines common stuff for the proxy.
 * @author Florent BENOIT
 */
public abstract class AbsProxy implements InvocationHandler {

    /**
     * Manages all methods of java.lang.Object class.
     * @param method the <code>Method</code> instance corresponding to the
     *        interface method invoked on the proxy instance. The declaring
     *        class of the <code>Method</code> object will be the interface
     *        that the method was declared in, which may be a superinterface of
     *        the proxy interface that the proxy class inherits the method
     *        through.
     * @param args an array of objects containing the values of the arguments
     *        passed in the method invocation on the proxy instance
     * @return the value of the called method.
     */
    protected Object handleObjectMethods(final Method method, final Object[] args) {
        String methodName = method.getName();

        if (methodName.equals("equals")) {
            return Boolean.valueOf(this.equals(args[0]));
        } else if (methodName.equals("toString")) {
            return this.toString();
        } else if (methodName.equals("hashCode")) {
            return Integer.valueOf(this.hashCode());
        } else {
            throw new IllegalStateException("Method '" + methodName + "' is not present on Object.class.");
        }
    }
}
