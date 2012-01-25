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
 * $Id: MethodsDependencyDescription.java 3493 2008-06-13 22:08:22Z sauthieg $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.osgi.binder.desc;

import java.lang.reflect.Method;

import org.ow2.easybeans.osgi.binder.listener.IDependencyListener;
import org.ow2.easybeans.osgi.binder.listener.MethodInjectorListener;

/**
 * Describe a dependency that will uses methods for injection/outjection.
 * @author Guillaume Sauthier
 */
public class MethodsDependencyDescription extends DependencyDescription {

    /**
     * Method to be used for injected/binding.
     */
    private Method bindMethod;

    /**
     * Method to be used for outjection/unbinding.
     */
    private Method unbindMethod;

    /**
     * @param name name of the dependency
     */
    public MethodsDependencyDescription(final String name) {
        super(name);
    }

    /**
     * @return the bindMethod
     */
    public Method getBindMethod() {
        return bindMethod;
    }

    /**
     * @param bindMethod the bindMethod to set
     */
    public void setBindMethod(final Method bindMethod) {
        this.bindMethod = bindMethod;
    }

    /**
     * @return the unbindMethod
     */
    public Method getUnbindMethod() {
        return unbindMethod;
    }

    /**
     * @param unbindMethod the unbindMethod to set
     */
    public void setUnbindMethod(final Method unbindMethod) {
        this.unbindMethod = unbindMethod;
    }

    @Override
    public IDependencyListener createListener(final Object instance) {
        return new MethodInjectorListener(instance,
                                          bindMethod,
                                          unbindMethod);
    }
}
