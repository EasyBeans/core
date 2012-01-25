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
 * $Id: JContainerConfig.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */
package org.ow2.easybeans.container;

import java.util.ArrayList;
import java.util.List;

import org.ow2.easybeans.api.EZBContainerConfig;
import org.ow2.easybeans.api.EZBContainerLifeCycleCallback;
import org.ow2.easybeans.api.EZBServer;
import org.ow2.easybeans.api.injection.ResourceInjector;
import org.ow2.easybeans.api.naming.EZBNamingStrategy;
import org.ow2.easybeans.naming.strategy.DefaultNamingStrategy;
import org.ow2.easybeans.resolver.ContainerJNDIResolver;
import org.ow2.easybeans.resolver.api.EZBContainerJNDIResolver;
import org.ow2.util.archive.api.IArchive;
import org.ow2.util.ee.deploy.api.deployable.IDeployable;

/**
 * Store Configuration for a {@link JContainer3} instance.
 * @author Guillaume Sauthier
 *         Contributors:
 *             S. Ali Tokmen (JNDI naming strategy)
 */
public class JContainerConfig implements EZBContainerConfig {

    /**
     * EjbJar deployable.
     */
    private IDeployable deployable;

    /**
     * Embedded server.
     */
    private EZBServer embedded;

    /**
     * Callback List.
     */
    private List<EZBContainerLifeCycleCallback> callbacks;

    /**
     * Resource Injectors List.
     */
    private List<ResourceInjector> injectors;

    /**
     * JNDI naming strategy.
     */
    private EZBNamingStrategy namingStrategy;

    /**
     * Link to Container JNDI Resolver.
     */
    private EZBContainerJNDIResolver containerJNDIResolver = null;


    /**
     * Constructor.
     * @param archive the deployable to process.
     */
    public JContainerConfig(final IDeployable deployable) {
        this.namingStrategy = new DefaultNamingStrategy();
        this.deployable = deployable;
        this.containerJNDIResolver = new ContainerJNDIResolver(deployable.getArchive());
        this.callbacks = new ArrayList<EZBContainerLifeCycleCallback>();
        this.injectors = new ArrayList<ResourceInjector>();
    }

    /**
     * @return the callbacks
     */
    public List<EZBContainerLifeCycleCallback> getCallbacks() {
        return this.callbacks;
    }

    /**
     * @param callback the callbacks to add.
     */
    public void addCallback(final EZBContainerLifeCycleCallback callback) {
        this.callbacks.add(callback);
    }
    
    /**
     * @return the deployable
     */
    public IDeployable getDeployable() {
        return this.deployable;
    }

    /**
     * @return the archive
     */
    public IArchive getArchive() {
        return this.deployable.getArchive();
    }

    /**
     * @return the injectors
     */
    public List<ResourceInjector> getInjectors() {
        return this.injectors;
    }

    /**
     * @param injector the injectors to set
     */
    public void addInjectors(final ResourceInjector injector) {
        this.injectors.add(injector);
    }

    /**
     * @return the embedded server
     */
    public EZBServer getEZBServer() {
        return this.embedded;
    }

    /**
     * Sets the embedded server.
     * @param embedded the embedded server of this config
     */
    public void setEZBServer(final EZBServer embedded) {
        this.embedded = embedded;
    }

    /**
     * @return JNDI naming strategy in use.
     */
    public EZBNamingStrategy getNamingStrategy() {
        return this.namingStrategy;
    }

    /**
     * @param strategy JNDI naming strategy to use.
     */
    public void setNamingStrategy(final EZBNamingStrategy strategy) {
        this.namingStrategy = strategy;
    }

    /**
     * @return the JNDI Resolver.
     */
    public EZBContainerJNDIResolver getContainerJNDIResolver() {
        return this.containerJNDIResolver;
    }

    /**
     * Sets the JNDI resolver.
     * @param containerJNDIResolver the given resolver.
     */
    public void setContainerJNDIResolver(final EZBContainerJNDIResolver containerJNDIResolver) {
        this.containerJNDIResolver = containerJNDIResolver;
    }
}
