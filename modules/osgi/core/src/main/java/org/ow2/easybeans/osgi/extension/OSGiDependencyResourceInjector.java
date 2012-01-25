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
 * $Id: OSGiDependencyResourceInjector.java 5530 2010-06-02 16:12:07Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.osgi.extension;

import java.util.Hashtable;
import java.util.Map;

import org.apache.felix.dependencymanager.Service;
import org.osgi.framework.BundleContext;
import org.ow2.easybeans.api.EZBContainer;
import org.ow2.easybeans.api.bean.EasyBeansBean;
import org.ow2.easybeans.api.injection.ResourceInjector;
import org.ow2.easybeans.container.EmptyResourceInjector;
import org.ow2.easybeans.osgi.binder.DefaultDependencyManager;

/**
 * This {@link ResourceInjector} will handle OSGi service injection.
 * There is only one instance per {@link EZBContainer}.
 * @author Guillaume Sauthier
 */
public class OSGiDependencyResourceInjector extends EmptyResourceInjector {

    /**
     * This container {@link DefaultDependencyManager} that will
     * track services dependencies.
     */
    private DefaultDependencyManager manager = null;

    /**
     * All beans managed (with dependencies tracking) instances.
     */
    private final Map<Integer, Service> services;

    /**
     * Store the default bundle context for EJB3 not deployed as Bundles.
     * Usually this is the easybeans' BundleContext.
     */
    private BundleContext defaultBundleContext;

    /**
     * Creates a new injector.
     */
    public OSGiDependencyResourceInjector() {
        services = new Hashtable<Integer, Service>();
    }

    /**
     * This callback will start dependency tracking for OSGi services.
     * @see org.ow2.easybeans.container.EmptyResourceInjector#postEasyBeansInject(org.ow2.easybeans.api.bean.EasyBeansBean)
     */
    @Override
    public void postEasyBeansInject(final EasyBeansBean bean) {
        if (manager == null) {
            EZBContainer container = bean.getEasyBeansFactory().getContainer();
            BundleContext context = container.getExtension(BundleContext.class);

            // If there is no BundleContext extension available
            // use the default BundleContext
            if (context == null) {
                context = defaultBundleContext;
            }
            manager = new DefaultDependencyManager(context);
        }
        Integer beanID = Integer.valueOf(System.identityHashCode(bean));
        Service service = manager.createService(bean);
        services.put(beanID, service);
        manager.startTracking(service);
    }

    /**
     * This callback will stop dependency tracking for the given bean.
     * @see org.ow2.easybeans.container.EmptyResourceInjector#postEasyBeansDestroy(org.ow2.easybeans.api.bean.EasyBeansBean)
     */
    @Override
    public void postEasyBeansDestroy(final EasyBeansBean bean) {
        // The manager has to be initialized before
        Integer beanID = Integer.valueOf(System.identityHashCode(bean));
        Service service = services.remove(beanID);
        if (service != null) {
            manager.stopTracking(service);
        }
    }

    public void setDefaultBundleContext(BundleContext defaultBundleContext) {
        this.defaultBundleContext = defaultBundleContext;
    }
}
