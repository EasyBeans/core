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

package org.ow2.easybeans.api;

import java.util.List;

import javax.transaction.Synchronization;
import javax.transaction.Transaction;

import org.ow2.easybeans.api.bean.EasyBeansSFSB;
import org.ow2.easybeans.persistence.api.EZBExtendedEntityManager;

/**
 * Interface for Stateful session factory.
 * @param <PoolType> the type of Pool
 * @param <Clue> the clue key
 * @author Florent Benoit
 */
public interface EZBStatefulSessionFactory<PoolType, Clue> extends Factory<PoolType, Clue> {

    /**
     * Gets the current session synchronization listener on the given transaction if any.
     * @param tx the given transaction
     * @return the current session synchronization listener
     */
    Synchronization getSessionSynchronizationListener(Transaction tx);


    /**
     * Sets the current session synchronization listener on the given transaction.
     * @param tx the given transaction
     * @param sessionSynchronizationListener the session synchronization listener
     */
    void setSessionSynchronizationListener(Transaction tx, Synchronization sessionSynchronizationListener);

    /**
     * Unsets the current session synchronization listener on the given transaction.
     * @param tx the given transaction
     */
    void unsetSessionSynchronizationListener(Transaction tx);

    /**
     * Gets the extended persistence context registered for the given stateful session bean.
     * @param statefulSessionBean the given bean
     * @return the extended persistence contexts
     */
    List<EZBExtendedEntityManager> getExtendedPersistenceContexts(final EasyBeansSFSB statefulSessionBean);

    /**
     * Adds the given extended persistence context for the given stateful
     * session bean.
     * @param statefulSessionBean the given bean
     * @param extendedEntityManager the given extended persistence context
     */
    void addExtendedPersistenceContext(final EasyBeansSFSB statefulSessionBean,
            final EZBExtendedEntityManager extendedEntityManager);

}
