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

package org.ow2.easybeans.deployment.api;

import org.ow2.easybeans.resolver.api.EZBApplicationJNDIResolver;
import org.ow2.util.ee.deploy.api.deployable.IDeployableInfo;

/**
 * Deployable info or the deployable.
 * @author Florent Benoit
 */
public interface EZBDeployableInfo extends IDeployableInfo {

    /**
     * ClassLoader to use for the container.
     * @return the classloader to use for the container
     */
    ClassLoader getClassLoader();

    /**
     * @return true if deployable needs to be unpacked
     * May return null if nothing is specified
     */
    Boolean hasToBeUnpacked();

    /**
     * @return the Application JNDI Resolver
     */
    EZBApplicationJNDIResolver getApplicationJNDIResolver();

}
