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

import javax.naming.Context;

import org.ow2.easybeans.api.EZBContainerConfig;
import org.ow2.easybeans.api.EZBContainerLifeCycleCallback;
import org.ow2.easybeans.api.EZBServer;
import org.ow2.easybeans.api.injection.ResourceInjector;
import org.ow2.easybeans.api.naming.EZBNamingStrategy;
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
    private IDeployable<?> deployable;

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
    private List<EZBNamingStrategy> namingStrategies;

    /**
     * Link to Container JNDI Resolver.
     */
    private EZBContainerJNDIResolver containerJNDIResolver = null;

    /**
     * Module name.
     */
    private String moduleName = null;

    /**
     * Name of the application (EAR case).
     */
    private String applicationName = null;

    /**
     * JNDI module context of this module.
     */
    private Context moduleContext = null;

    /**
     * JNDI App context of this module.
     */
    private Context appContext = null;

    /**
     * Constructor.
     * @param deployable the deployable to process.
     */
    public JContainerConfig(final IDeployable<?> deployable) {
        this.namingStrategies = new ArrayList<EZBNamingStrategy>();
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
    public IDeployable<?> getDeployable() {
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
    public List<EZBNamingStrategy> getNamingStrategies() {
        return this.namingStrategies;
    }

    /**
     * Define naming strategies to use.
     * @param namingStrategies the given strategies
     */
    public void setNamingStrategies(final List<EZBNamingStrategy> namingStrategies) {
        this.namingStrategies = namingStrategies;
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

    /**
     * Sets the Module Name of this container.
     * @param moduleName the name of this container.
     */
    public void setModuleName(final String moduleName) {
        this.moduleName  = moduleName;
    }

    /**
     * Gets the module name of this container.
     * @return the module name.
     */
    public String getModuleName() {
        return this.moduleName;
    }

    /**
     * @return the java:module context.
     */
    public Context getModuleContext() {
        return this.moduleContext;
    }

    /**
     * Sets the module context.
     * @param moduleContext the java:module context.
     */
    public void setModuleContext(final Context moduleContext) {
        this.moduleContext = moduleContext;
    }

    /**
     * @return the java:app context.
     */
    public Context getAppContext() {
        return this.appContext;
    }

    /**
     * Sets the app context.
     * @param appContext the java:module context.
     */
    public void setAppContext(final Context appContext) {
        this.appContext = appContext;
    }

    /**
     * Sets the Application Name of this container (EAR case).
     * @param applicationName the name of the application of this container.
     */
    public void setApplicationName(final String applicationName) {
        this.applicationName  = applicationName;
    }

    /**
     * Gets the Application Name of this container (EAR case).
     * @return the name of the application of this container.
     */
    public String getApplicationName() {
        return this.applicationName;
    }
}
