/**
 * EasyBeans
 * Copyright (C) 2007 Bull S.A.S.
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
 * $Id: EasyBeansOSGiExtension.java 5371 2010-02-24 15:02:00Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.osgi.extension;

import org.osgi.framework.BundleContext;
import org.ow2.easybeans.api.EZBContainerConfig;
import org.ow2.easybeans.api.EZBConfigurationExtension;

/**
 * Extension for adding Callbacks/Injectors in OSGi mode.
 * @author Florent BENOIT
 */
public class EasyBeansOSGiExtension implements EZBConfigurationExtension {

    /**
     * Instance of a bundle context.
     */
    private BundleContext bundleContext;


    /**
     * Gets the bundle context.
     * @return the bundle context
     */
    public BundleContext getBundleContext() {
        return bundleContext;
    }

    /**
     * Sets the given bundle context.
     * @param bundleContext the given context
     */
    public void setBundleContext(final BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    /**
     * Adapt the OSGi resources injector.
     * @param containerConfig the given ContainerConfig instance.
     */
    public void configure(final EZBContainerConfig containerConfig) {
        // Services dependency injector
        OSGiDependencyResourceInjector injector = new OSGiDependencyResourceInjector();
        if (bundleContext != null) {
            injector.setDefaultBundleContext(bundleContext);
        }
        containerConfig.addInjectors(injector);

    }

}
