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

package org.ow2.easybeans.deployment;

import org.ow2.easybeans.api.EZBContainerConfig;
import org.ow2.easybeans.deployment.api.EZBDeployableInfo;
import org.ow2.easybeans.resolver.api.EZBApplicationJNDIResolver;

/**
 * Deployable Info used for EJB3 deployable.
 * @author Florent Benoit
 */
public class EasyBeansDeployableInfo implements EZBDeployableInfo {


    private ClassLoader classLoader = null;

    private Boolean hasToBeUnpacked = null;

    private EZBApplicationJNDIResolver applicationJNDIResolver = null;

    /**
     * Container configuration.
     */
    private EZBContainerConfig containerConfiguration = null;


    public EZBApplicationJNDIResolver getApplicationJNDIResolver() {
        return this.applicationJNDIResolver;
    }

    public void setApplicationJNDIResolver(final EZBApplicationJNDIResolver applicationJNDIResolver) {
        this.applicationJNDIResolver = applicationJNDIResolver;
    }

    /**
     * ClassLoader to use for the container.
     * @return the classloader to use for the container
     */
    public ClassLoader getClassLoader() {
        return this.classLoader;
    }

    public void setClassLoader(final ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    /**
     * @return true if deployable needs to be unpacked
     */
    public Boolean hasToBeUnpacked() {
        return this.hasToBeUnpacked;
    }

    public void setHasToBeUnpacked(final Boolean hasToBeUnpacked) {
        this.hasToBeUnpacked = hasToBeUnpacked;
    }

    /**
     * Defines the configuration for this container.
     * @param containerConfiguration the container Configuration.
     */
    public void setContainerConfiguration(final EZBContainerConfig containerConfiguration) {
        this.containerConfiguration = containerConfiguration;
    }


    /**
     * @return container Configuration.
     */
    public EZBContainerConfig getContainerConfiguration() {
        return this.containerConfiguration;
    }
}
