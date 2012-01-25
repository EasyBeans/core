/**
 * EasyBeans
 * Copyright (C) 2006-2010 Bull S.A.S.
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
 * $Id: JPersistenceContext.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.persistence;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.ow2.easybeans.persistence.xml.JPersistenceUnitInfo;

/**
 * This class manages persistence contexts associated to a persistence unit.
 * @author Florent Benoit
 */
public class JPersistenceContext {

    /**
     * Persistence unit info used by this persistence context.
     */
    private JPersistenceUnitInfo jPersistenceUnitInfo;

    /**
     * EntityManager factory.
     */
    private EntityManagerFactory entityManagerFactory = null;

    /**
     * Tx entity manager handler.
     */
    private TxEntityManagerHandler txEntityManagerHandler = null;

    /**
     * Tx entity manager.
     */
    private TxEntityManager txEntityManager = null;

    /**
     * Build a new persistence context based on a given persistence-unit info.
     * @param jPersistenceUnitInfo information on the persistence unit.
     */
    public JPersistenceContext(final JPersistenceUnitInfo jPersistenceUnitInfo) {
        this.jPersistenceUnitInfo = jPersistenceUnitInfo;
        init();
    }

    /**
     * Initialize entity manager (and some factoriese) used by Java EE components.
     */
    private void init() {
        this.entityManagerFactory = this.jPersistenceUnitInfo.getPersistenceProvider().createContainerEntityManagerFactory(
                this.jPersistenceUnitInfo, null);
        this.txEntityManagerHandler = new TxEntityManagerHandler(this.entityManagerFactory);
        this.txEntityManager = new TxEntityManager(this.txEntityManagerHandler);
    }

    /**
     * Gets the EntityManager used for Transaction-Scoped.
     * @return the EntityManager used for Transaction-Scoped
     */
    public EntityManager getTxEntityManager() {
        return new ContainerManagedEntityManager(this.txEntityManager);
    }

    /**
     * Gets the EntityManager factory.
     * @return the EntityManager factory
     */
    public EntityManagerFactory getEntityManagerFactory() {
        return this.entityManagerFactory;
    }

}
