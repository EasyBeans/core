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
 * $Id: MethodInjectorListener.java 4199 2008-10-13 12:19:18Z sauthieg $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.osgi.binder.listener;

import java.lang.reflect.Method;

import org.osgi.framework.ServiceReference;
import org.ow2.easybeans.osgi.binder.util.ReflectionHelper;

/**
 * @author Guillaume
 *
 */
public class MethodInjectorListener implements IDependencyListener {

    private final Object bean;
    private final Method binder;
    private final Method unbinder;

    public MethodInjectorListener(final Object instance,
                                  final Method bindMethod,
                                  final Method unbindMethod) {
        this.bean = instance;
        this.binder = bindMethod;
        this.unbinder = unbindMethod;
    }

    /* (non-Javadoc)
     * @see org.ow2.easybeans.osgi.binder.internal.IServiceDependencyListener#added(org.osgi.framework.ServiceReference, java.lang.Object)
     */
    public void added(final ServiceReference ref, final Object service) {
        if (!ReflectionHelper.isNullableObject(service) && binder != null) {
            ReflectionHelper.invokeMethod(binder, bean, service);
        }
    }

    /* (non-Javadoc)
     * @see org.ow2.easybeans.osgi.binder.internal.IServiceDependencyListener#changed(org.osgi.framework.ServiceReference, java.lang.Object)
     */
    public void changed(final ServiceReference ref, final Object service) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.ow2.easybeans.osgi.binder.internal.IServiceDependencyListener#removed(org.osgi.framework.ServiceReference, java.lang.Object)
     */
    public void removed(final ServiceReference ref, final Object service) {
        if (!ReflectionHelper.isNullableObject(service) && unbinder != null) {
            Object value = null;
            if (!unbinder.equals(binder)) {
                value = service;
            }
            ReflectionHelper.invokeMethod(unbinder, bean, value);
        }

    }

}
