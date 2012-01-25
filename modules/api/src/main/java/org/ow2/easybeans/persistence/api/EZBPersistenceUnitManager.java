/**
 * EasyBeans
 * Copyright (C) 2006 Bull S.A.S.
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
 * $Id: EZBPersistenceUnitManager.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.persistence.api;


import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContextType;

/**
 * Allows to get persistence units and allow to return EntityManager or
 * EntityManagerFactory.
 * @author Florent Benoit
 */
public interface EZBPersistenceUnitManager {

    /**
     * Gets an entity manager for the given unit name and the extra attributes.
     * @param unitName the name of the persistence unit
     * @param type the type of the persistence context
     * @return entity manager corresponding to arguments
     */
    EntityManager getEntityManager(final String unitName, final PersistenceContextType type);

    /**
     * Gets an entity manager factory for the given unit name.
     * @param unitName the name of the persistence unit
     * @return entity manager factory.
     */
    EntityManagerFactory getEntityManagerFactory(final String unitName);

    /**
     * Merge the persistence context of a an other persistent unit manager in
     * this one. Note that as specified in chapter 6.2.2 (persistence unit
     * scope), an EAR level component level will only be seen by a subcomponent
     * if it was not redefined. In our case : don't merge the given unit-name if
     * the current manager defines this unit-name.
     * @param otherPersistenceUnitManager the other persistence unit manager
     *        that will be merged into this one.
     */
    void merge(final EZBPersistenceUnitManager otherPersistenceUnitManager);
}
