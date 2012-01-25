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
 * $Id: TxEntityManagerHandler.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.persistence;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.Transaction;

import org.ow2.easybeans.transaction.JTransactionManager;
import org.ow2.util.log.Log;
import org.ow2.util.log.LogFactory;

/**
 * This class is used to handle different entity manager for Transaction Scoped
 * Persistence Context. It associates one EntityManager to a given transaction
 * @author Florent Benoit
 */
public class TxEntityManagerHandler {

    /**
     * Logger.
     */
    private Log logger = LogFactory.getLog(TxEntityManagerHandler.class);

    /**
     * EntityManager factory that will create entity manager.
     */
    private EntityManagerFactory entityManagerFactory = null;

    /**
     * Map between transactions and the entity manager.
     */
    private Map<Transaction, EntityManager> entityManagers = null;

    /**
     * Build a new handler of these entity managers.
     * @param entityManagerFactory Factory used to create the entity manager (if
     *        no entity manager is already associated to the current
     *        transaction)
     */
    public TxEntityManagerHandler(final EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
        this.entityManagers = new HashMap<Transaction, EntityManager>();
    }

    /**
     * Gets the current entity manager (or create one) for the given transaction
     * if there is one.
     * @return an entitymanager
     */
    public synchronized EntityManager getCurrent() {
        EntityManager current = null;

        // Get current transaction (if any)
        Transaction currentTx = null;
        try {
            currentTx = JTransactionManager.getTransactionManager().getTransaction();
        } catch (SystemException e) {
            throw new IllegalStateException("Cannot get current transaction", e);
        }

        /**
         * Called outside a transaction, return a default entity manager.
         */
        if (currentTx == null) {
            return buildNoTxEntityManager();
        }

        // Check if there is an existing entity manager that is scoped to this TX
        current = this.entityManagers.get(currentTx);

        /**
         * If the entity manager is called and there is an existing persistence
         * context bound to the current JTA transaction, the call takes place in
         * that context.
         */
        if (current == null) {
            /**
             * If the entity manager is called and there is no persistence
             * context associated with the current JTA transaction, a new
             * persistence context will be created and bound to the JTA
             * transaction, and the call will take place in that context.
             */
            current = buildNewTxEntityManager(currentTx);
        }

        return current;
    }

    /**
     * Builds a new Entity manager for the given transaction.
     * @param tx transaction on which associate the entity manager
     * @return built entity manager.
     */
    private EntityManager buildNewTxEntityManager(final Transaction tx) {
        EntityManager entityManager = this.entityManagerFactory.createEntityManager();

        /**
         * The persistence context is created and then associated with the
         * current JTA transaction. The persistence context ends when the
         * associated JTA transaction commits or rolls back,
         */
        try {
            tx.registerSynchronization(new TxEntityManagerLifeCycle(entityManager, tx, this));
        } catch (IllegalStateException e) {
            throw new IllegalStateException("Cannot register Entity manager lifecycle", e);
        } catch (RollbackException e) {
            throw new IllegalStateException("Cannot register Entity manager lifecycle", e);
        } catch (SystemException e) {
            throw new IllegalStateException("Cannot register Entity manager lifecycle", e);
        }

        // add in the managed entitymanager
        this.entityManagers.put(tx, entityManager);

        return entityManager;
    }

    /**
     * Builds a new Entity manager used when there is no transaction.
     * @return built entity manager.
     */
    private EntityManager buildNoTxEntityManager() {
        return this.entityManagerFactory.createEntityManager();
    }

    /**
     * Release the entity manager associated to the given tx.
     * @param tx tx to removed
     */
    public void release(final Transaction tx) {
        this.entityManagers.remove(tx);
    }
}
