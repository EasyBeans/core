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
 * $Id: TMComponent.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.component.itf;

import javax.resource.spi.XATerminator;
import javax.transaction.NotSupportedException;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;
import javax.transaction.xa.XAException;
import javax.transaction.xa.Xid;

import org.ow2.easybeans.component.api.EZBComponent;

/**
 * Defines the interface for a TM provider.
 * @author Florent Benoit
 */
public interface TMComponent extends EZBComponent {

    /**
     * JNDI name of the transaction manager.
     */
    String JNDI_NAME = "javax.transaction.UserTransaction";

    /**
     * Gets the transaction manager object.
     * @return instance of the transaction manager
     */
    TransactionManager getTransactionManager();

    /**
     * Set the Transaction Timeout.
     * @param timeout Timeout (in seconds)
     */
    void setTimeout(int timeout);

    /**
     * @return the XA terminator.
     * @throws XAException if the terminator is not available
     */
    XATerminator getXATerminator() throws XAException;

    /**
     * Creates a new inflow transaction and associates it with the current
     * thread.
     * @param xid <code>Xid</code> of the inflow transaction.
     * @throws NotSupportedException Thrown if the thread is already associated
     *         with a transaction. (nested transaction are not supported)
     * @throws SystemException Thrown if the transaction manager encounters an
     *         unexpected error condition
     */
    void begin(final Xid xid) throws NotSupportedException, SystemException;

    /**
     * Creates a new inflow transaction and associates it with the current
     * thread.
     * @param xid <code>Xid</code> of the inflow transaction.
     * @param timeout value of the timeout (in seconds). If the value is less
     *        than or equal to zero, the value will be set to the default value.
     * @throws NotSupportedException Thrown if the thread is already associated
     *         with a transaction. (nested transaction are not supported)
     * @throws SystemException Thrown if the transaction manager encounters an
     *         unexpected error condition
     */
    void begin(Xid xid, long timeout) throws NotSupportedException, SystemException;


    /**
     * Clear transaction from this thread if not known.
     * Useful when another thread completes the current thread's transaction
     */
    void clearThreadTx();
}
