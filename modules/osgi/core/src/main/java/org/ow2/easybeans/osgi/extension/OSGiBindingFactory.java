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
 * $Id: OSGiBindingFactory.java 3850 2008-09-01 08:38:37Z sauthieg $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.osgi.extension;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import javax.ejb.EJBHome;
import javax.ejb.EJBLocalHome;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ManagedService;
import org.ow2.easybeans.api.EZBContainer;
import org.ow2.easybeans.api.Factory;
import org.ow2.easybeans.api.binding.BindingException;
import org.ow2.easybeans.api.binding.EZBBindingFactory;
import org.ow2.easybeans.api.binding.EZBRef;
import org.ow2.easybeans.container.session.stateless.StatelessSessionFactory;
import org.ow2.easybeans.osgi.handler.ManagedServiceEJBLocalHomeInvocationHandler;
import org.ow2.easybeans.osgi.handler.ManagedServiceLocalCallInvocationHandler;
import org.ow2.easybeans.osgi.handler.Registrable;
import org.ow2.easybeans.proxy.client.LocalCallInvocationHandler;

/**
 * This {@link EZBBindingFactory} registers the EJB interfaces as OSGi services.
 * This instance is JVM scoped (ie shared by everyone) !
 * @author Guillaume Sauthier
 */
public class OSGiBindingFactory implements EZBBindingFactory {

    /**
     * Store registrations.
     */
    private final Map<EZBRef, ServiceRegistration> registrations;

    @Deprecated
    public OSGiBindingFactory(final BundleContext context) {
        this();
    }

    public OSGiBindingFactory() {
        registrations = new HashMap<EZBRef, ServiceRegistration>();
    }

    /**
     * Binds the reference as an OSGi service.
     * @see org.ow2.easybeans.api.binding.EZBBindingFactory#bind(org.ow2.easybeans.api.binding.EZBRef)
     */
    public void bind(final EZBRef ref) throws BindingException {
        Factory<?, ?> factory = ref.getFactory();
        if (factory instanceof StatelessSessionFactory) {
            EZBContainer container = factory.getContainer();

            // Get the BundleContext of the EjbJar
            BundleContext context = container.getExtension(BundleContext.class);
            if (context != null) {

                // Only expose services from EjbJars wrapped as Bundles (with a BundleContext).
                // Reasonning:
                // That's almost a non-sense to expose business interfaces of EJB
                // that are not part of bundle-enabled EjbJars: As no packages can
                // be exported, no-one can use them, so why bother registering them ?

                ClassLoader loader = container.getClassLoader();

                String interfaceName = ref.getItfClassName();
                // load interface
                Class<?> clz = null;
                try {
                    clz = loader.loadClass(interfaceName);
                } catch (ClassNotFoundException e) {
                    throw new BindingException("Cannot find the class '" + interfaceName
                                               + "' in Classloader '" + loader + "'.", e);
                }

                LocalCallInvocationHandler handler = null;

                if (clz.isAssignableFrom(EJBLocalHome.class)) {
                    handler = new ManagedServiceEJBLocalHomeInvocationHandler();
                } else if (clz.isAssignableFrom(EJBHome.class)) {
                    // TODO add warning: we're about to register a
                    // remote interface just like a local interface
                    handler = new ManagedServiceEJBLocalHomeInvocationHandler();
                } else {
                    // TODO add something in the EZBRef API to know
                    // if the interface is remote or not
                    handler = new ManagedServiceLocalCallInvocationHandler();
                }

                handler.setInterfaceClass(clz);
                handler.setFactory(factory);

                // Register all local interfaces as services
                Object proxy = Proxy.newProxyInstance(loader, new Class[] {clz, ManagedService.class}, handler);
                ServiceRegistration registration = ((Registrable) handler).registerService(proxy, context);
                registrations.put(ref, registration);
            }

        }

    }

    /* (non-Javadoc)
     * @see org.ow2.easybeans.api.binding.EZBBindingFactory#unbind(org.ow2.easybeans.api.binding.EZBRef)
     */
    public void unbind(final EZBRef ref) throws BindingException {
        ServiceRegistration reg = registrations.get(ref);
        if (reg != null) {
            reg.unregister();
        }
    }

}
