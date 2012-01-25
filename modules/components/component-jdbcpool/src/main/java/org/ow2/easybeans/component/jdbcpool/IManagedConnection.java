/**
 * EasyBeans
 * Copyright (C) 2008-2009 Bull S.A.S.
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
 * $Id: IManagedConnection.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.component.jdbcpool;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.sql.XAConnection;
import javax.transaction.Synchronization;
import javax.transaction.Transaction;
import javax.transaction.xa.XAResource;

/**
 * Specify all interface that are used by the Managed Connection.
 * @author Florent BENOIT
 */
public interface IManagedConnection extends Comparable<IManagedConnection>, XAConnection, XAResource, Synchronization {

    /**
     * @return value of reused prepared statement.
     */
    int getReUsedPreparedStatements();


    /**
     * @return The identifier of the managed connection.
     */
    int getIdentifier();


    /**
     * Notify a Close event on Connection.
     */
    void notifyClose();

    /**
     * Notify an Error event on Connection.
     * @param ex the given exception
     */
    void notifyError(final SQLException ex);

    /**
     * @return open count
     */
    int getOpenCount();

    /**
     * @return the Transaction
     */
    Transaction getTx();

    /**
     * Notify as opened.
     */
    void hold();

    /**
     * @return true if connection max age has expired
     */
    boolean isAged();

    /**
     * Check if the connection has been unused for too long time.
     * This occurs usually when the caller forgot to call close().
     * @return true if open time has been reached, and not involved in a tx.
     */
    boolean inactive();

    /**
     * @return true if connection is closed
     */
    boolean isClosed();

    /**
     * @return true if connection is still open
     */
    boolean isOpen();

    /**
     * notify as closed.
     * @return true if normal close.
     */
    boolean release();

    /**
     * remove this item, ignoring exception on close.
     */
    void remove();

    /**
     * Dynamically change the prepared statement pool size.
     * @param max the maximum of prepared statement.
     */
    void setPstmtMax(final int max);

    /**
     * Set the associated transaction.
     * @param tx Transaction
     */
    void setTx(final Transaction tx);

    /**
     * Try to find a PreparedStatement in the pool.
     * @param sql the given sql query.
     * @throws SQLException if an error in the database occurs.
     * @return a given prepared statement.
     */
    PreparedStatement prepareStatement(final String sql) throws SQLException;

}
