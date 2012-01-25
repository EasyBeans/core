/**
 * EasyBeans
 * Copyright (C) 2006-2007 Bull S.A.S.
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
 * $Id: JManagedConnection.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.component.jdbcpool;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import javax.sql.ConnectionEvent;
import javax.sql.ConnectionEventListener;
import javax.transaction.Transaction;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.ow2.util.log.Log;
import org.ow2.util.log.LogFactory;

/**
 * This class represents the connection managed by the pool. This connection is
 * a managed connection and is notified of the transaction events.
 * @author Philippe Durieux
 * @author Florent Benoit
 */
public class JManagedConnection extends AbsProxy {

    /**
     * Logger.
     */
    private static Log logger = LogFactory.getLog(JManagedConnection.class);

    /**
     * Connection to the database.
     */
    private Connection physicalConnection = null;

    /**
     * Connection returned to the user.
     */
    private IConnection implConn = null;

    /**
     * Maximum of prepared statements.
     */
    private int pstmtmax = 0;

    /**
     * Current number of opened prepared statements.
     */
    private int psOpenNb = 0;

    /**
     * Event listeners (of PooledConnection).
     */
    private Vector<ConnectionEventListener> eventListeners = new Vector<ConnectionEventListener>();

    /**
     * count of opening this connection. >0 if open.
     */
    private int open = 0;

    /**
     * Transaction timeout value.
     */
    private int timeout = 0;

    /**
     * Transaction the connection is involved with.
     */
    private Transaction tx = null;

    /**
     * Counter of all managed connections created.
     */
    private static int objcount = 0;

    /**
     * Identifier of this connection.
     */
    private final int identifier;

    /**
     * Prepared statements that were reused.
     */
    private int reUsedPreparedStatements = 0;

    /**
     * List of PreparedStatement in the pool.
     */
    private Map<String, IPreparedStatement> psList = null;

    /**
     * Link to the connection manager.
     */
    private ConnectionManager ds = null;

    /**
     * Time of the death for this connection.
     */
    private long deathTime = 0;

    /**
     * Time for closing this connection.
     */
    private long closeTime = 0;

    /**
     * Builds a new managed connection on a JDBC connection.
     * @param physicalConnection the physical JDBC Connection.
     * @param ds the connection manager
     */
    public JManagedConnection(final Connection physicalConnection, final ConnectionManager ds) {
        this.physicalConnection = physicalConnection;
        this.ds = ds;

        // make a proxy on our object.
        IManagedConnection managedConnectionProxy = (IManagedConnection) Proxy.newProxyInstance(IManagedConnection.class
                .getClassLoader(), new Class[] {IManagedConnection.class}, this);

        // An XAConnection holds 2 objects: 1 Connection + 1 XAResource
        this.implConn = (IConnection) Proxy.newProxyInstance(IConnection.class.getClassLoader(), new Class[] {IConnection.class},
                new JConnection(managedConnectionProxy, physicalConnection));

        open = 0;
        deathTime = System.currentTimeMillis() + ds.getMaxAgeMilli();

        identifier = objcount++;

        // Prepared statement.
        pstmtmax = ds.getPstmtMax();
        psOpenNb = 0;
        psList = Collections.synchronizedMap(new HashMap<String, IPreparedStatement>());

    }



    /**
     * Processes a method invocation on a proxy instance and returns the result.
     * This method will be invoked on an invocation handler when a method is
     * invoked on a proxy instance that it is associated with.
     * @param proxy the proxy instance that the method was invoked on
     * @param method the <code>Method</code> instance corresponding to the
     *        interface method invoked on the proxy instance.
     * @param args an array of objects containing the values of the arguments
     *        passed in the method invocation on the proxy instance, or
     *        <code>null</code> if interface method takes no arguments.
     * @return the value to return from the method invocation on the proxy
     *         instance.
     * @throws Throwable the exception to throw from the method invocation on
     *         the proxy instance. The exception's type must be assignable
     *         either to any of the exception types declared in the
     *         <code>throws</code> clause of the interface method or to the
     *         unchecked exception types <code>java.lang.RuntimeException</code>
     *         or <code>java.lang.Error</code>. If a checked exception is
     *         thrown by this method that is not assignable to any of the
     *         exception types declared in the <code>throws</code> clause of
     *         the interface method, then an UndeclaredThrowableException
     *         containing the exception that was thrown by this method will be
     *         thrown by the method invocation on the proxy instance.
     */
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {

        // Methods on the Object.class are not send on the connection impl
        if (method.getDeclaringClass().getName().equals("java.lang.Object")) {
            return handleObjectMethods(method, args);
        }

        String methodName = method.getName();

        // Methods that are part of the IManagedConnection interface
        if ("getIdentifier".equals(methodName)) {
            // return the identifier
            return Integer.valueOf(getIdentifier());
        } else if ("getXAResource".equals(methodName)) {
            // the XAResource is the proxy
            return proxy;
        } else if ("commit".equals(methodName)) {
            // commit
            commit((Xid) args[0], ((Boolean) args[1]).booleanValue(), (IManagedConnection) proxy);
            return null;
        } else if ("end".equals(methodName)) {
            // end
            end((Xid) args[0], ((Integer) args[1]).intValue());
            return null;
        } else if ("forget".equals(methodName)) {
            // forget
            forget((Xid) args[0]);
            return null;
        } else if ("prepare".equals(methodName)) {
            // prepare
            return Integer.valueOf(prepare((Xid) args[0]));
        } else if ("getTransactionTimeout".equals(methodName)) {
            return Integer.valueOf(getTransactionTimeout());
        } else if ("isSameRM".equals(methodName)) {
            // Compare object
            return Boolean.valueOf(isSameRM(args[0]));
        } else if ("recover".equals(methodName)) {
            // recover
            return recover(((Integer) args[0]).intValue());
        } else if ("rollback".equals(methodName)) {
            // forget
            rollback((Xid) args[0], (IManagedConnection)  proxy);
            return null;
        } else if ("setTransactionTimeout".equals(methodName)) {
            // setTransactionTimeout
            setTransactionTimeout(((Integer) args[0]).intValue());
            return null;
        } else if ("start".equals(methodName)) {
            // start
            start((Xid) args[0], ((Integer) args[1]).intValue());
            return null;
        } else if ("getXAResource".equals(methodName)) {
            // return the proxy object as it's the XA Resource
            return proxy;
        } else if ("notifyClose".equals(methodName)) {
            notifyClose((IManagedConnection) proxy);
            return null;
        } else if ("notifyError".equals(methodName)) {
            notifyError((IManagedConnection) proxy, (SQLException) args[0]);
            return null;
        } else if ("compareTo".equals(methodName)) {
            return Integer.valueOf(compareTo((IManagedConnection) proxy, (IManagedConnection) args[0]));
        } else if ("getReUsedPreparedStatements".equals(methodName)) {
            return Integer.valueOf(getReUsedPreparedStatements());
        } else if ("setPstmtMax".equals(methodName)) {
            setPstmtMax(((Integer) args[0]).intValue());
            return null;
        } else if ("getConnection".equals(methodName)) {
            return getConnection();
        } else if ("close".equals(methodName)) {
            close();
            return null;
        } else if ("addConnectionEventListener".equals(methodName)) {
            addConnectionEventListener((ConnectionEventListener) args[0]);
            return null;
        } else if ("removeConnectionEventListener".equals(methodName)) {
            removeConnectionEventListener((ConnectionEventListener) args[0]);
            return null;
        } else if ("beforeCompletion".equals(methodName)) {
            beforeCompletion();
            return null;
        } else if ("afterCompletion".equals(methodName)) {
            afterCompletion(((Integer) args[0]).intValue());
            return null;
        } else if ("isAged".equals(methodName)) {
            return Boolean.valueOf(isAged());
        } else if ("isOpen".equals(methodName)) {
            return Boolean.valueOf(isOpen());
        } else if ("getOpenCount".equals(methodName)) {
            return Integer.valueOf(getOpenCount());
        } else if ("inactive".equals(methodName)) {
            return Boolean.valueOf(inactive());
        } else if ("isClosed".equals(methodName)) {
            return Boolean.valueOf(isClosed());
        } else if ("hold".equals(methodName)) {
            hold();
            return null;
        } else if ("release".equals(methodName)) {
            return Boolean.valueOf(release());
        } else if ("setTx".equals(methodName)) {
            setTx(((Transaction) args[0]));
            return null;
        } else if ("getTx".equals(methodName)) {
            return getTx();
        } else if ("remove".equals(methodName)) {
            remove();
            return null;
        } else if ("addStatementEventListener".equals(methodName) || "removeStatementEventListener".equals(methodName)) {
            throw new UnsupportedOperationException("JDK 6.0 / JDBC 4.0 API Not supported");
        } else if ("notifyPsClose".equals(methodName)) {
            notifyPsClose((JStatement) args[0]);
        } else if ("prepareStatement".equals(methodName)) {
            // Single arg method
            if (args.length == 1) {
                return prepareStatement((String) args[0]);
            }
            // multiple arg method
            return prepareStatement((String) args[0], ((Integer) args[1]).intValue(), ((Integer) args[2]).intValue());
        }


        // Should not go here as we should have redirected all methods
        logger.error("Method ''{0}'' not handled by the proxy", method);
        return null;
    }


    /**
     * @return The identifier of this JManagedConnection
     */
    public int getIdentifier() {
        return identifier;
    }

    /**
     * Dynamically change the prepared statement pool size.
     * @param max the maximum of prepared statement.
     */
    public void setPstmtMax(final int max) {
        pstmtmax = max;
        if (psList == null) {
            psList = Collections.synchronizedMap(new HashMap<String, IPreparedStatement>(pstmtmax));
        }
    }

    /**
     * Commit the global transaction specified by xid.
     * @param xid transaction xid
     * @param onePhase true if one phase commit
     * @param proxy the proxy used to notify errors
     * @throws XAException XA protocol error
     */
    public void commit(final Xid xid, final boolean onePhase, final IManagedConnection proxy) throws XAException {
        logger.debug("XA-COMMIT for {0}", xid);

        // Commit the transaction
        try {
            physicalConnection.commit();
        } catch (SQLException e) {
            logger.error("Cannot commit transaction", e);
            notifyError(proxy, e);
            throw new XAException("Error on commit");
        }
    }

    /**
     * Ends the work performed on behalf of a transaction branch.
     * @param xid transaction xid
     * @param flags currently unused
     * @throws XAException XA protocol error
     */
    public void end(final Xid xid, final int flags) throws XAException {
        logger.debug("XA-END for {0}", xid);
    }

    /**
     * Tell the resource manager to forget about a heuristically completed
     * transaction branch.
     * @param xid transaction xid
     * @throws XAException XA protocol error
     */
    public void forget(final Xid xid) throws XAException {
        logger.debug("XA-FORGET for {0}", xid);
    }

    /**
     * Obtain the current transaction timeout value set for this XAResource
     * instance.
     * @return the current transaction timeout in seconds
     * @throws XAException XA protocol error
     */
    public int getTransactionTimeout() throws XAException {
        logger.debug("getTransactionTimeout for {0}", this);
        return timeout;
    }

    /**
     * Determine if the resource manager instance represented by the target
     * object is the same as the resource manager instance represented by the
     * parameter xares.
     * @param xares An XAResource object
     * @return True if same RM instance, otherwise false.
     * @throws XAException XA protocol error
     */
    public boolean isSameRM(final Object xares) throws XAException {

        // In this pseudo-driver, we must return true only if
        // both objects refer to the same XAResource, and not
        // the same Resource Manager, because actually, we must
        // send commit/rollback on each XAResource involved in
        // the transaction.
        if (xares.equals(this)) {
            logger.debug("isSameRM = true {0}", this);
            return true;
        }
        logger.debug("isSameRM = false {0}", this);
        return false;
    }

    /**
     * Ask the resource manager to prepare for a transaction commit of the
     * transaction specified in xid.
     * @param xid transaction xid
     * @throws XAException XA protocol error
     * @return always OK
     */
    public int prepare(final Xid xid) throws XAException {
        logger.debug("XA-PREPARE for {0}", xid);
        // No 2PC on standard JDBC drivers
        return XAResource.XA_OK;
    }

    /**
     * Obtain a list of prepared transaction branches from a resource manager.
     * @param flag unused parameter.
     * @return an array of transaction Xids
     * @throws XAException XA protocol error
     */
    public Xid[] recover(final int flag) throws XAException {
        logger.debug("XA-RECOVER for {0}", this);
        // Not implemented
        return null;
    }

    /**
     * Inform the resource manager to roll back work done on behalf of a
     * transaction branch.
     * @param xid transaction xid
     * @param proxy the proxy used to notify errors
     * @throws XAException XA protocol error
     */
    public void rollback(final Xid xid, final IManagedConnection proxy) throws XAException {
        logger.debug("XA-ROLLBACK for {0}", xid);

        // Make sure that we are not in AutoCommit mode
        try {
            if (physicalConnection.getAutoCommit()) {
                logger.error("Rollback called on XAResource with AutoCommit set");
                throw (new XAException(XAException.XA_HEURCOM));
            }
        } catch (SQLException e) {
            logger.error("Cannot getAutoCommit", e);
            notifyError(proxy, e);
            throw (new XAException("Error on getAutoCommit"));
        }

        // Rollback the transaction
        try {
            physicalConnection.rollback();
        } catch (SQLException e) {
            logger.error("Cannot rollback transaction", e);
            notifyError(proxy, e);
            throw (new XAException("Error on rollback"));
        }
    }

    /**
     * Set the current transaction timeout value for this XAResource instance.
     * @param seconds timeout value, in seconds.
     * @return always true
     * @throws XAException XA protocol error
     */
    @SuppressWarnings("boxing")
    public boolean setTransactionTimeout(final int seconds) throws XAException {
        logger.debug("setTransactionTimeout to {0} for {1}", seconds, this);
        timeout = seconds;
        return true;
    }

    /**
     * Start work on behalf of a transaction branch specified in xid.
     * @param xid transaction xid
     * @param flags unused parameter
     * @throws XAException XA protocol error
     */
    public void start(final Xid xid, final int flags) throws XAException {
        logger.debug("XA-START for {0}", xid);
    }

    /**
     * Compares this object with another specified object.
     * @param current the current managed connection (proxy)
     * @param other the object to compare
     * @return a value detecting if these objects are matching or not.
     */
    public int compareTo(final IManagedConnection current, final IManagedConnection other) {
        int diff = current.getReUsedPreparedStatements() - other.getReUsedPreparedStatements();
        if (diff == 0) {
            return current.getIdentifier() - other.getIdentifier();
        }
        return diff;
    }

    /**
     * @return value of reused prepared statement.
     */
    public int getReUsedPreparedStatements() {
        return reUsedPreparedStatements;
    }

    /**
     * Create an object handle for a database connection.
     * @exception SQLException - if a database-access error occurs
     * @return connection used by this managed connection
     */
    public IConnection getConnection() throws SQLException {
        // Just return the already created object.
        return implConn;
    }

    /**
     * Close the database connection.
     * @exception SQLException - if a database-access error occurs
     */
    public void close() throws SQLException {

        // Close the actual Connection here.
        if (physicalConnection != null) {
            physicalConnection.close();
        } else {
            logger.error("Connection already closed. Stack of this new close()", new Exception());
        }
        physicalConnection = null;
        implConn = null;
    }

    /**
     * Add an event listener.
     * @param listener event listener
     */
    public void addConnectionEventListener(final ConnectionEventListener listener) {
        eventListeners.addElement(listener);
    }

    /**
     * Remove an event listener.
     * @param listener event listener
     */
    public void removeConnectionEventListener(final ConnectionEventListener listener) {
        eventListeners.removeElement(listener);
    }

    /**
     * synchronization implementation. {@inheritDoc}
     */
    public void beforeCompletion() {
        // nothing to do
    }

    /**
     * synchronization implementation. {@inheritDoc}
     */
    public void afterCompletion(final int status) {
        if (tx != null) {
            ds.freeConnections(tx);
        } else {
            logger.error("NO TX!");
        }
    }

    /**
     * @return true if connection max age has expired
     */
    public boolean isAged() {
        return (deathTime < System.currentTimeMillis());
    }

    /**
     * @return true if connection is still open
     */
    public boolean isOpen() {
        return (open > 0);
    }

    /**
     * @return open count
     */
    public int getOpenCount() {
        return open;
    }

    /**
     * Check if the connection has been unused for too long time. This occurs
     * usually when the caller forgot to call close().
     * @return true if open time has been reached, and not involved in a tx.
     */
    public boolean inactive() {
        return (open > 0 && tx == null && closeTime < System.currentTimeMillis());
    }

    /**
     * @return true if connection is closed
     */
    public boolean isClosed() {
        return (open <= 0);
    }

    /**
     * Notify as opened.
     */
    public void hold() {
        open++;
        closeTime = System.currentTimeMillis() + ds.getMaxOpenTimeMilli();
    }

    /**
     * notify as closed.
     * @return true if normal close.
     */
    public boolean release() {
        open--;
        if (open < 0) {
            logger.warn("connection was already closed");
            open = 0;
            return false;
        }
        if (tx == null && open > 0) {
            logger.error("connection-open counter overflow");
            open = 0;
        }
        return true;
    }

    /**
     * Set the associated transaction.
     * @param tx Transaction
     */
    public void setTx(final Transaction tx) {
        this.tx = tx;
    }

    /**
     * @return the Transaction
     */
    public Transaction getTx() {
        return tx;
    }

    /**
     * remove this item, ignoring exception on close.
     */
    public void remove() {
        // Close the physical connection
        try {
            close();
        } catch (java.sql.SQLException ign) {
            logger.error("Could not close Connection: ", ign);
        }

        // remove all references (for GC)
        tx = null;

    }

    // -----------------------------------------------------------------
    // Other methods
    // -----------------------------------------------------------------

    /**
     * Try to find a PreparedStatement in the pool for the given options.
     * @param sql the sql of the prepared statement
     * @param resultSetType the type of resultset
     * @param resultSetConcurrency the concurrency of this resultset
     * @return a preparestatement object
     * @throws SQLException if an errors occurs on the database.
     */
    private PreparedStatement prepareStatement(final String sql, final int resultSetType, final int resultSetConcurrency)
            throws SQLException {

        logger.debug("sql = {0}", sql);
        if (pstmtmax == 0) {
            return physicalConnection.prepareStatement(sql, resultSetType, resultSetConcurrency);
        }
        IPreparedStatement ps = null;
        synchronized (psList) {
            ps = psList.get(sql);
            if (ps != null) {
                if (!ps.isClosed()) {
                    logger.warn("reuse an open pstmt");
                }
                ps.reuse();
                reUsedPreparedStatements++;
            } else {
                // Not found in cache. Create a new one.
                PreparedStatement aps = physicalConnection.prepareStatement(sql, resultSetType, resultSetConcurrency);
                ps = (IPreparedStatement) Proxy.newProxyInstance(IPreparedStatement.class.getClassLoader(),
                        new Class[] {IPreparedStatement.class}, new JStatement(aps, this, sql));

                psList.put(sql, ps);
            }
            psOpenNb++;
        }
        return ps;
    }

    /**
     * Try to find a PreparedStatement in the pool.
     * @param sql the given sql query.
     * @throws SQLException if an error in the database occurs.
     * @return a given prepared statement.
     */
    public PreparedStatement prepareStatement(final String sql) throws SQLException {
        return prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
    }

    /**
     * A PreparedStatement has been logically closed.
     * @param ps a prepared statement.
     */
    public void notifyPsClose(final JStatement ps) {
        logger.debug(ps.getSql());
        synchronized (psList) {
            psOpenNb--;
            if (psList.size() >= pstmtmax) {
                // Choose a closed element to remove.
                IPreparedStatement lru = null;
                Iterator<IPreparedStatement> i = psList.values().iterator();
                while (i.hasNext()) {
                    lru = i.next();
                    if (lru.isClosed()) {
                        // actually, remove the first closed element.
                        i.remove();
                        lru.forget();
                        break;
                    }
                }
            }
        }
    }

    /**
     * Notify a Close event on Connection.
     * @param proxy the proxy used to create events
     */
    @SuppressWarnings("boxing")
    public void notifyClose(final IManagedConnection proxy) {

        // Close all PreparedStatement not already closed
        // When a Connection has been closed, no PreparedStatement should
        // remain open. This can avoids lack of cursor on some databases.
        synchronized (psList) {
            if (psOpenNb > 0) {
                IPreparedStatement jst = null;
                Iterator<IPreparedStatement> i = psList.values().iterator();
                while (i.hasNext()) {
                    jst = i.next();
                    if (jst.forceClose()) {
                        psOpenNb--;
                    }
                }
                if (psOpenNb != 0) {
                    logger.warn("Bad psOpenNb value = {0}", psOpenNb);
                    psOpenNb = 0;
                }
            }
        }

        // Notify event to listeners
        for (int i = 0; i < eventListeners.size(); i++) {
            ConnectionEventListener l = eventListeners.elementAt(i);
            l.connectionClosed(new ConnectionEvent(proxy));
        }
    }

    /**
     * Notify an Error event on Connection.
     * @param proxy the proxy used to create events
     * @param ex the given exception
     */
    public void notifyError(final IManagedConnection proxy, final SQLException ex) {
        // Notify event to listeners
        for (int i = 0; i < eventListeners.size(); i++) {
            ConnectionEventListener l = eventListeners.elementAt(i);
            l.connectionErrorOccurred(new ConnectionEvent(proxy, ex));
        }
    }


}
