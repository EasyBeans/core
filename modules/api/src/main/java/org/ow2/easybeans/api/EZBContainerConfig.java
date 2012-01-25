/**
 * EasyBeans
 * Copyright (C) 2006-2008 Bull S.A.S.
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
 * $Id: EZBContainerConfig.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.api;

import java.util.List;

import org.ow2.easybeans.api.injection.ResourceInjector;
import org.ow2.easybeans.api.naming.EZBNamingStrategy;
import org.ow2.easybeans.resolver.api.EZBContainerJNDIResolver;
import org.ow2.util.archive.api.IArchive;
import org.ow2.util.ee.deploy.api.deployable.IDeployable;

/**
 * Configuration for a Container.
 * @author Florent Benoit
 *         Contributors:
 *             S. Ali Tokmen (JNDI naming strategy)
 */
public interface EZBContainerConfig {

    /**
     * @return the callbacks
     */
    List<EZBContainerLifeCycleCallback> getCallbacks();

    /**
     * @param callback the callbacks to add.
     */
    void addCallback(final EZBContainerLifeCycleCallback callback);

    /**
     * @return the deployable.
     */
    IDeployable getDeployable();

    /**
     * @return the archive
     */
    IArchive getArchive();

    /**
     * @return JNDI naming strategy in use.
     */
    EZBNamingStrategy getNamingStrategy();

    /**
     * @param strategy JNDI naming strategy to use.
     */
    void setNamingStrategy(EZBNamingStrategy strategy);

    /**
     * @return the injectors
     */
    List<ResourceInjector> getInjectors();

    /**
     * @param injector the injectors to set
     */
    void addInjectors(final ResourceInjector injector);

    /**
     * @return the easybeans server
     */
    EZBServer getEZBServer();

    /**
     * Sets the easybeans server.
     * @param easybeansServer the embedded server of this config
     */
    void setEZBServer(final EZBServer easybeansServer);

    /**
     * @return the JNDI Resolver.
     */
    EZBContainerJNDIResolver getContainerJNDIResolver();

    /**
     * Sets the JNDI resolver.
     * @param containerJNDIResolver the given resolver.
     */
    void setContainerJNDIResolver(final EZBContainerJNDIResolver containerJNDIResolver);
}
