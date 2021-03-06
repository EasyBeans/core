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
 * $Id: EZBContainer.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.api;


import java.util.List;
import java.util.Map;

import org.ow2.easybeans.persistence.EZBExtendedEntityManager;
import org.ow2.easybeans.persistence.EZBPersistenceUnitManager;
import org.ow2.util.archive.api.IArchive;

/**
 * This interface represents an EasyBeans EJB Container.
 * @author Florent Benoit
 *
 */
public interface EZBContainer extends EZBManageableContainer, EZBExtensor, EZBJ2EEManagedObject {

    /**
     * Gets a factory with its given name.
     * @param factoryName the factory name.
     * @return the factory found or null.
     */
    Factory getFactory(String factoryName);

    /**
     * Gets the classloader of the container. May change at each restart of the container.
     * @return a classloader.
     */
    ClassLoader getClassLoader();

    /**
     * @return Returns the Container Configuration.
     */
    EZBContainerConfig getConfiguration();

    /**
     * Gets the persistence manager object which manages all persistence-unit associated to this container.
     * @return persistence unit manager object
     */
    EZBPersistenceUnitManager getPersistenceUnitManager();

    /**
     * Sets the classloader. (if it was not already set else exception).
     * @param classLoader to be used by the container
     */
    void setClassLoader(ClassLoader classLoader);

    /**
     * Sets the persistence manager object which manages all persistence-unit associated to this container.
     * @param persistenceUnitManager persistence unit manager object to set.
     */
    void setPersistenceUnitManager(EZBPersistenceUnitManager persistenceUnitManager);

    /**
     * Gets the permission manager (that manages EJB permissions).
     * @return permission manager.
     */
    EZBPermissionManager getPermissionManager();

    /**
     * Sets the permission manager (that manages EJB permissions).
     * @param ezbPermissionManager the EasyBeans permission manager.
     */
    void setPermissionManager(EZBPermissionManager ezbPermissionManager);

    /**
     * Add extra archives for finding classes.
     * @param extraArchives the given archives.
     */
    void setExtraArchives(final List<IArchive> extraArchives);

    /**
     * Run the enhancer on the selected container.
     * @param createBeanFactories if needs to enhance class or also create/bind factories
     * @throws EZBContainerException if enhancement fails
     */
    void enhance(boolean createBeanFactories) throws EZBContainerException;

    /**
     * @return the extended persistence context for the current container.
     * This return the current map from a thread local so the data is only on a current thread.
     * The key of the returned map is the name of the persistence unit
     */
    Map<String, EZBExtendedEntityManager> getCurrentExtendedPersistenceContexts();

    /**
     * Sets the data on the current thread.
     * @param extendedPersistenceContexts a map between the persistence unit name and the associated extended persistence context.
     */
    void setCurrentExtendedPersistenceContexts(Map<String, EZBExtendedEntityManager> extendedPersistenceContexts);

}
