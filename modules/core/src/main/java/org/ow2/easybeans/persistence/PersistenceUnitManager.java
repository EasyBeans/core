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
 * $Id: PersistenceUnitManager.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.persistence;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContextType;

import org.ow2.easybeans.persistence.api.EZBPersistenceUnitManager;
import org.ow2.easybeans.persistence.xml.JPersistenceUnitInfo;

/**
 * This class manages persistence units ands allow to return EntityManager or
 * EntityManagerFactory.
 * @author Florent Benoit
 */
public class PersistenceUnitManager implements EZBPersistenceUnitManager {

    /**
     * List of persistence unit objects managed by their name.
     */
    private Map<String, JPersistenceContext> persistenceContexts = null;

    /**
     * Persistence unit infos.
     */
    private JPersistenceUnitInfo[] persistenceUnitInfos;

    /**
     * Build a new manager with given persistence units.
     * @param persistenceUnitInfos a list of persistence unit infos.
     */
    public PersistenceUnitManager(final JPersistenceUnitInfo[] persistenceUnitInfos) {
        this.persistenceUnitInfos = persistenceUnitInfos;
        this.persistenceContexts = new HashMap<String, JPersistenceContext>();
        // Add each object to the map
        addExtraPersistenceUnitInfos(persistenceUnitInfos);
    }

    /**
     * Adds new persistence unit infos to this persistence unit manager.<br>
     * Note: The persistence unit name have to be different from existing persistence units.
     * @param newPersistenceUnitInfos a list of persistence unit infos.
     */
    public void addExtraPersistenceUnitInfos(final JPersistenceUnitInfo[] newPersistenceUnitInfos) {
        if (newPersistenceUnitInfos != null) {
            for (JPersistenceUnitInfo pUnitInfo : newPersistenceUnitInfos) {
                // get unit name
                String persistenceUnitName = pUnitInfo.getPersistenceUnitName();

                // Check existing ?
                JPersistenceContext jPersistenceContext = this.persistenceContexts.get(persistenceUnitName);

                // Existing one found, add new persistence unit infos on it
                if (jPersistenceContext != null) {
                    throw new IllegalArgumentException("There is already an existing persistence unit name named '"
                            + persistenceUnitName + "' in this persistence unit manager.");
                }

                // Add it
                this.persistenceContexts.put(persistenceUnitName, new JPersistenceContext(pUnitInfo));
            }
        }
    }


    /**
     * Gets the persistence context associated to a given persistence unit name.
     * @param unitName the name of the persistence unit object.
     * @return the object which is found by matching the expected name.
     */
    private JPersistenceContext getPersistenceContext(final String unitName) {
        if (unitName == null || unitName.equals("")) {
            if (this.persistenceContexts.size() == 0) {
                throw new IllegalArgumentException("No persistence-unit defined");
            } else if (this.persistenceContexts.size() > 1) {
                throw new IllegalArgumentException("Too many persistence-unit defined, cannot take the default one.");
            }
            return this.persistenceContexts.values().iterator().next();
        }
        // else, return the unit associated to the given name
        JPersistenceContext persistenceContext = this.persistenceContexts.get(unitName);
        if (persistenceContext == null) {
            throw new IllegalArgumentException("No persistence-unit with name '" + unitName + "' defined.");
        }
        return persistenceContext;
    }

    /**
     * Gets an entity manager for the given unit name and the extra attributes.
     * @param unitName the name of the persistence unit
     * @param type the type of the persistence context
     * @return entity manager corresponding to arguments
     */
    public EntityManager getEntityManager(final String unitName, final PersistenceContextType type) {
        // TODO: Manage also extended persistence context;
        return getPersistenceContext(unitName).getTxEntityManager();
    }

    /**
     * Gets an entity manager factory for the given unit name.
     * @param unitName the name of the persistence unit
     * @return entity manager factory.
     */
    public EntityManagerFactory getEntityManagerFactory(final String unitName) {
        return getPersistenceContext(unitName).getEntityManagerFactory();
    }


    /**
     * Merge the persistence context of a an other persistent unit manager in
     * this one. Note that as specified in chapter 6.2.2 (persistence unit
     * scope), an EAR level component level will only be seen by a subcomponent
     * if it was not redefined. In our case : don't merge the given unit-name if
     * the current manager defines this unit-name.
     * @param otherEZBPersistenceUnitManager the other persistence unit manager
     *        that will be merged into this one.
     */
    public void merge(final EZBPersistenceUnitManager otherEZBPersistenceUnitManager) {
        // do nothing if the given manager is null
        if (otherEZBPersistenceUnitManager == null) {
            return;
        }
        PersistenceUnitManager otherPersistenceUnitManager = null;
        if (otherEZBPersistenceUnitManager instanceof PersistenceUnitManager) {
            otherPersistenceUnitManager = (PersistenceUnitManager) otherEZBPersistenceUnitManager;
        } else {
            return;
        }

        // get all persistence contexts of the given manager
        for (Iterator<String> itContext = otherPersistenceUnitManager.persistenceContexts.keySet().iterator(); itContext
                .hasNext();) {
            String unitName = itContext.next();
            // existing in our manager?
            JPersistenceContext pContext = this.persistenceContexts.get(unitName);
            if (pContext == null) {
                // add it (not existing)
                this.persistenceContexts.put(unitName, otherPersistenceUnitManager.persistenceContexts.get(unitName));
            }
        }

    }

    /**
     * Gets the persistence unit infos used by this manager.
     * @return  the persistence unit infos used by this manager.
     */
    public JPersistenceUnitInfo[] getPersistenceUnitInfos() {
        return this.persistenceUnitInfos;
    }

    /**
     * Set a property in persistence unit. If property does not exists,
     * add it.
     * @param name property name
     * @param value property value
     */
    public void setProperty (String name, String value) {
        this.setProperty(name, value, null);
    }

    /**
     * Set a property in persistence unit. If property does not exists,
     * add it.
     * @param name property name
     * @param value property value
     * @param persistenceUnitName specific persistenceUnitName. If null, set the property
     *                            in all persistence units
     */
    public void setProperty (String name, String value, String persistenceUnitName) {
        JPersistenceUnitInfo[] persistenceUnitInfos = this.getPersistenceUnitInfos();
        for (JPersistenceUnitInfo persistenceUnitInfo : persistenceUnitInfos) {
            if (persistenceUnitName == null) {
                persistenceUnitInfo.getProperties().setProperty(name, value);
            } else {
                if (persistenceUnitInfo.getPersistenceUnitName().equals(persistenceUnitName)) {
                    persistenceUnitInfo.getProperties().setProperty(name, value);
                }
            }
        }
    }

}
