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

import javax.naming.Context;

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
    List<EZBNamingStrategy> getNamingStrategies();

    /**
     * Define naming strategies to use.
     * @param strategies the naming strategies
     */
    void setNamingStrategies(List<EZBNamingStrategy> strategies);

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

    /**
     * Sets the Module Name of this container.
     * @param moduleName the name of this container.
     */
    void setModuleName(final String moduleName);

    /**
     * Gets the module name of this container.
     * @return the module name.
     */
    String getModuleName();

    /**
     * Gets the application name of this container.
     * @return the application name.
     */
    String getApplicationName();

    /**
     * Sets the Application Name of this container (EAR case).
     * @param applicationName the name of the application of this container.
     */
    void setApplicationName(final String applicationName);

    /**
     * @return the java:module context.
     */
    Context getModuleContext();

    /**
     * @return the java:app context.
     */
    Context getAppContext();

    /**
     * @return the java:comp/env context.
     */
    Context getEnvContext();

    /**
     * Sets the ENC context.
     * @param compEnvContext the java:comp/env context.
     */
    void setEnvContext(final Context compEnvContext);

    /**
     * Sets the module context.
     * @param moduleContext the java:module context.
     */
    void setModuleContext(final Context moduleContext);

    /**
     * Sets the app context.
     * @param appContext the java:module context.
     */
    void setAppContext(final Context appContext);

}
