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
 * $Id: TxEntityManagerLifeCycle.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.persistence;

import javax.persistence.EntityManager;
import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.Transaction;

/**
 * This class manages the lifecycle of an entity manager with transaction scoped
 * persistence context. It means that it should be closed when the transaction
 * is committed or rollbacked.
 * @author Florent Benoit
 */
public class TxEntityManagerLifeCycle implements Synchronization {

    /**
     * Entity manager that is referenced.
     */
    private EntityManager entityManager = null;

    /**
     * Handler that manages tx entity manager (to release the tx).
     */
    private TxEntityManagerHandler txEntityManagerHandler = null;

    /**
     * Tx's association to release.
     */
    private Transaction tx = null;

    /**
     * @param entityManager Entity manager that is managed (lifecycle).
     * @param tx the transaction that needs to be released in the handler.
     * @param txEntityManagerHandler handler on which release association with TX
     */
    public TxEntityManagerLifeCycle(final EntityManager entityManager, final Transaction tx,
            final TxEntityManagerHandler txEntityManagerHandler) {
        this.entityManager = entityManager;
        this.tx = tx;
        this.txEntityManagerHandler = txEntityManagerHandler;

        int statusTx = 0;
        if (tx != null) {
            try {
                statusTx = tx.getStatus();
            } catch (SystemException e) {
                throw new IllegalStateException("Cannot get the status on the current transaction", e);
            }
        }

        // Needs to join the transaction.
        if (tx != null && Status.STATUS_ACTIVE == statusTx) {
            entityManager.joinTransaction();
        }
    }

    /**
     * The beforeCompletion method is called by the transaction manager prior to
     * the start of the two-phase transaction commit process. This call is
     * executed with the transaction context of the transaction that is being
     * committed.
     */
    public void beforeCompletion() {
    }

    /**
     * This method is called by the transaction manager after the transaction is
     * committed or rolled back.
     * @param status The status of the transaction completion.
     */
    public void afterCompletion(final int status) {
        // release tx
        this.txEntityManagerHandler.release(this.tx);
        this.txEntityManagerHandler = null;

        // Close entityManager
        this.entityManager.close();

    }

}
