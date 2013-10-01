/**
 * EasyBeans
 * Copyright (C) 2006-2009 Bull S.A.S.
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
 * $Id: EZBServer.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.api;

import java.util.List;
import java.util.Map;

import javax.naming.Context;

import org.ow2.easybeans.api.components.EZBComponentManager;
import org.ow2.easybeans.api.pool.EZBManagementPool;
import org.ow2.easybeans.component.api.EZBComponent;
import org.ow2.easybeans.resolver.api.EZBServerJNDIResolver;
import org.ow2.util.archive.api.IArchive;
import org.ow2.util.ee.deploy.api.deployable.IDeployable;
import org.ow2.util.ee.deploy.api.deployer.IDeployerManager;

/**
 * Defines an EasyBeans server.
 * @author Florent Benoit
 */
public interface EZBServer extends EZBJ2EEManagedObject {

    /**
     * Gets the id of this embedded server.
     * @return the id of this server.
     */
    Integer getID();

    /**
     * Gets a description of the embedded server.
     * @return a description of the embedded server
     */
    String getDescription();

    /**
     * Creates and adds an ejb3 container to the managed container.
     * @param deployable the container deployable.
     * @return the created container.
     */
    EZBContainer createContainer(final IDeployable<?> deployable);

    /**
     * Add an already created container.
     * @param container the EZBContainer to be added.
     */
    void addContainer(final EZBContainer container);

    /**
     * Gets a container managed by this server.
     * @param id the container id.
     * @return the container if it is found, else null.
     */
    EZBContainer getContainer(final String id);

    /**
     * Gets a container managed by this server.
     * @param archive the archive used by the given container.
     * @return the container if it is found, else null.
     */
    EZBContainer findContainer(final IArchive archive);

    /**
     * Remove a given container.
     * @param container the container to be removed.
     */
    void removeContainer(final EZBContainer container);

    /**
     * @return Returns the deployed containers.
     */
    Map<String, EZBContainer> getContainers();

    /**
     * @return the ComponentManager used by this instance.
     */
    EZBComponentManager getComponentManager();

    /**
     * @return the JNDI Resolver of this server.
     */
    EZBServerJNDIResolver getJNDIResolver();

    /**
     * @return the Deployer manager.
     */
    IDeployerManager getDeployerManager();

    /**
     * Allows to set the deployer manager.
     * @param deployerManager the Deployer manager.
     */
    void setDeployerManager(IDeployerManager deployerManager);

    /**
     * @return the management pool used to managed the pools (creating instance, etc.)
     */
    EZBManagementPool getManagementThreadPool();

    /**
     * Defines the management thread pool.
     * @param managementThreadPool the management pool used to managed the pools (creating instance, etc.)
     */
    void setManagementThreadPool(EZBManagementPool managementThreadPool);

    /**
     * Stop the server.
     * @throws EZBServerException if there is a failure.
     */
    void stop() throws EZBServerException;

    /**
     * @return true if EasyBeans has been stopped.
     */
    boolean isStopped();

    /**
     * @return true if EasyBeans is being stopped.
     */
    boolean isStopping();

    /**
     * @return true if EasyBeans has been started.
     */
    boolean isStarted();

    /**
     * @return the server config
     */
    EZBServerConfig getServerConfig();

    /**
     * List of global interceptors classes to use.
     * @return list of classes
     */
    List<Class<? extends EasyBeansInterceptor>> getGlobalInterceptorsClasses();

    /**
     * Get a reference to the first component matching the interface.
     * @param <T> The interface type.
     * @param itf The interface class.
     * @return The component.
     */
    <T extends EZBComponent> T getComponent(final Class<T> itf);

    /**
     * This is the topic where naming events will be send.
     * Interested IEventListeners should register to this topic.
     */
    static final String NAMING_EXTENSION_POINT = "/easybeans/container/factory/context";

    /**
     * @return the initial context.
     */
    Context getInitialContext();

}
