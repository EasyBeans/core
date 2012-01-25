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
 * $Id: DefaultDependencyManager.java 5866 2011-05-04 16:48:43Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.osgi.binder;

import java.util.List;

import org.apache.felix.dependencymanager.DependencyManager;
import org.apache.felix.dependencymanager.Service;
import org.apache.felix.dependencymanager.ServiceDependency;
import org.apache.felix.dependencymanager.Logger;
import org.osgi.framework.BundleContext;
import org.ow2.easybeans.osgi.binder.desc.DependencyDescription;
import org.ow2.easybeans.osgi.binder.listener.IDependencyListener;

public class DefaultDependencyManager {

    /**
     * Wrapped {@link DependencyManager} from Felix.
     */
    private DependencyManager manager = null;

    /**
     * The EjbJar's {@link BundleContext}.
     */
    private final BundleContext context;

    /**
     * Creates a new dependency manager.
     * @param context bundle context to be used for injection
     */
    public DefaultDependencyManager(final BundleContext context) {
        // Creates a Felix DependencyManager
        Logger dmLogger = new Logger(context);
        this.manager = new DependencyManager(context, dmLogger);
        this.context = context;
    }

    /**
     * Creates a new {@link ServiceDependency} that can be configured by the user.
     * @return a new {@link ServiceDependency} instance.
     */
    public ServiceDependency createDependency() {
        return manager.createServiceDependency().setRequired(false);
    }

    /**
     * Creates a new {@link Service} instance pre-configured from annotations.
     * @param instance instance to be injected with dependencies
     * @return a Service description with dependencies
     */
    public Service createService(final Object instance) {
        return createService(instance, true);
    }

    /**
     * Creates a new {@link Service} instance that could be pre-configured
     * from annotations.
     * @param instance instance to be injected with dependencies
     * @param configure <code>true</code> if annotations has to be read
     * @return a Service description with dependencies
     */
    public Service createService(final Object instance, final boolean configure) {
        Service service = manager.createService().setImplementation(instance);
        // Disable the callbacks else the init, start, stop, destroy will be called if present
        service.setCallbacks(null, null, null, null);
        if (configure) {
            // Look for annotations
            configureService(service, instance);
        }
        return service;
    }

    /**
     * Start dependencies tracking.
     */
    public void startTracking(final Service service) {
        // Do not manage a Service that have no declared dependencies
        List dependencies = service.getDependencies();
        if ((dependencies != null) && (!dependencies.isEmpty())) {
            manager.add(service);
        }
    }

    /**
     * Stop dependencies tracking.
     */
    public void stopTracking(final Service service) {
        // If there is no declared dependencies, the tracking did not start
        // So don't remove service from the manager (it was not in)
        List dependencies = service.getDependencies();
        if ((dependencies != null) && (!dependencies.isEmpty())) {
            manager.remove(service);
        }
    }

    /**
     * Create a configured DM {@link Service}.
     * @param service
     * @param instance
     * @return
     */
    private Service configureService(final Service service, final Object instance) {

        // Extract dependencies
        DependenciesBuilder builder = new DependenciesBuilder(instance, context);
        List<DependencyDescription> dependencies = null;
        dependencies = builder.extractDependencies();

        // Iterate on the found dependencies to create DM dependencies
        for (DependencyDescription description : dependencies) {
            ServiceDependency dependency = createDependency();
            dependency.setService(description.getServiceInterface(),
                    description.getFilter());
            IDependencyListener listener = null;

            listener = description.createListener(instance);

            dependency.setCallbacks(listener,
                    IDependencyListener.ADDED,
                    IDependencyListener.CHANGED,
                    IDependencyListener.REMOVED);
            service.add(dependency);
        }
        return service;
    }

}
