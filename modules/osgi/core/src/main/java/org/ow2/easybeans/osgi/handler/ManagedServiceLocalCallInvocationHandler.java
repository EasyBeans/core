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
 * $Id: ManagedServiceLocalCallInvocationHandler.java 5901 2011-06-07 12:09:35Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.osgi.handler;

import java.lang.reflect.Method;
import java.util.Dictionary;

import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ManagedService;
import org.osgi.framework.BundleContext;

import org.ow2.easybeans.proxy.client.LocalCallInvocationHandler;

/**
 * React to {@link ManagedService} method calls.
 * @author Guillaume Sauthier
 */
public class ManagedServiceLocalCallInvocationHandler extends LocalCallInvocationHandler
implements Registrable {

    /**
     * Registration properties associated with the service.
     */
    private ServiceRegistration registration;

    /**
     * Temporarily store the properties from ConfigAdmin.
     */
    private Dictionary temporaryPropertiesStorage = null;

    /* (non-Javadoc)
     * @see org.ow2.easybeans.proxy.client.LocalCallInvocationHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
     */
    @SuppressWarnings("unchecked")
    @Override
    public Object invoke(final Object proxy,
            final Method method,
            final Object[] args) throws Exception {

        if (method.getDeclaringClass().equals(ManagedService.class)) {
            // There is only 1 method for ManagedService, so that's easy :)
            Dictionary dict = (Dictionary) args[0];
            // The very first time this method is called, it's possible (given that
            // is has been called asynchronously) that the registration field has
            // not been initialized yet.
            // Anyway, the OSGiBindingFactory
            if (dict == null) {
                dict = ManagedServiceUtils.getDefaults(getFactory(),
                        getInterfaceClassName());
            }

            // Seems that sometimes, we still don't have the registration object
            // when CA call us back
            // In that case, simply store the properties for future use ...
            if (registration == null) {
                temporaryPropertiesStorage = dict;
            } else {
                // Simply update the service properties ...
                registration.setProperties(dict);
            }
            // TODO we could do more:
            // * properties injection in the factory instance
            // * factory configuration (pool, ...)
            return null;
        }

        // Let the classic Handler do its job
        return super.invoke(proxy, method, args);
    }

    /**
     * Register the Object as an OSGi service.
     * @param proxy the object to be registered
     * @param context OSGi BundlContext
     */
    public ServiceRegistration registerService(final Object proxy, final BundleContext context) {
        registration = context.registerService(new String[] {getInterfaceClassName(),
                ManagedService.class.getName()},
                proxy,
                ManagedServiceUtils.getDefaults(getFactory(),
                        getInterfaceClassName()));
        // Now, the registration object is set
        // We can safely use it if needed ...
        if (temporaryPropertiesStorage != null) {
            // Propagate the properties
            registration.setProperties(temporaryPropertiesStorage);
            // Reset the storage field
            temporaryPropertiesStorage = null;
        }
        return registration;
    }

}
