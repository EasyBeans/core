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
 * $Id: ConnectionManager.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.component.jdbcpool;

import java.io.PrintWriter;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeSet;

import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.Referenceable;
import javax.naming.StringRefAddr;
import javax.sql.ConnectionEvent;
import javax.sql.ConnectionEventListener;
import javax.sql.DataSource;
import javax.sql.XAConnection;
import javax.sql.XADataSource;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.xa.XAResource;

import org.ow2.util.log.Log;
import org.ow2.util.log.LogFactory;

/**
 * DataSource implementation. Manage a pool of connections.
 * @author Philippe Durieux
 * @author Florent Benoit
 */
public class ConnectionManager implements DataSource, XADataSource, Referenceable, ConnectionEventListener {

    /**
     * Logger.
     */
    private Log logger = LogFactory.getLog(ConnectionManager.class);

    /**
     * Milliseconds.
     */
    private static final long MILLI = 1000L;

    /**
     * One minute in milliseconds.
     */
    private static final long ONE_MIN_MILLI = 60L * MILLI;

    /**
     * Default timeout.
     */
    private static final int DEFAULT_TIMEOUT = 60;

    /**
     * Default timeout for waiters (10s).
     */
    private static final long WAITER_TIMEOUT = 10 * MILLI;

    /**
     * Max waiters (by default).
     */
    private static final int DEFAULT_MAX_WAITERS = 1000;

    /**
     * Default prepare statement.
     */
    private static final int DEFAULT_PSTMT = 12;

    /**
     * Default sampling period.
     */
    private static final int DEFAULT_SAMPLING = 60;

    /**
     * List of all datasources.
     */
    private static Map<String, ConnectionManager> cmList = new HashMap<String, ConnectionManager>();

    /**
     * Transaction manager.
     */
    private TransactionManager tm = null;

    /**
     * List of IManagedConnection not currently used. This avoids closing and
     * reopening physical connections. We try to keep a minimum of minConPool
     * elements here.
     */
    private TreeSet<IManagedConnection> freeList = new TreeSet<IManagedConnection>();

    /**
     * Total list of IManagedConnection physically opened.
     */
    private LinkedList<IManagedConnection> mcList = new LinkedList<IManagedConnection>();

    /**
     * This HashMap gives the IManagedConnection from its transaction Requests
     * with same tx get always the same connection.
     */
    private Map<Transaction, IManagedConnection> tx2mc = new HashMap<Transaction, IManagedConnection>();

    /**
     * Login timeout (DataSource impl).
     */
    private int loginTimeout = DEFAULT_TIMEOUT;

    /**
     * PrintWriter used logging (DataSource impl).
     */
    private PrintWriter log = null;

    /**
     * Constructor for Factory.
     */
    public ConnectionManager() {

    }

    /**
     * Gets the ConnectionManager matching the DataSource name.
     * @param dsname datasource name.
     * @return a connection manager impl.
     */
    public static ConnectionManager getConnectionManager(final String dsname) {
        ConnectionManager cm = cmList.get(dsname);
        return cm;
    }

    /**
     * Datasource name.
     */
    private String dSName = null;

    /**
     * @return Jndi name of the datasource
     */
    public String getDSName() {
        return this.dSName;
    }

    /**
     * @param s Jndi name for the datasource
     */
    public void setDSName(final String s) {
        this.dSName = s;
        // Add it to the list
        cmList.put(s, this);
    }

    /**
     * @serial datasource name
     */
    private String dataSourceName;

    /**
     * Gets the name of the datasource.
     * @return the name of the datasource
     */
    public String getDatasourceName() {
        return this.dataSourceName;
    }

    /**
     * Sets the name of the datasource.
     * @param dataSourceName the name of the datasource
     */
    public void setDatasourceName(final String dataSourceName) {
        this.dataSourceName = dataSourceName;
    }

    /**
     * url for database.
     */
    private String url = null;

    /**
     * @return the url used to get the connection.
     */
    public String getUrl() {
        return this.url;
    }

    /**
     * Sets the url to get connections.
     * @param url the url for JDBC connections.
     */
    public void setUrl(final String url) {
        this.url = url;
    }

    /**
     * JDBC driver Class.
     */
    private String className = null;

    /**
     * @return the JDBC driver class name.
     */
    public String getClassName() {
        return this.className;
    }

    /**
     * Sets the driver class for JDBC.
     * @param className the name of the JDBC driver
     * @throws ClassNotFoundException if driver is not found
     */
    public void setClassName(final String className) throws ClassNotFoundException {
        this.className = className;

        // Loads standard JDBC driver and keeps it loaded (via driverClass)
        this.logger.debug("Load JDBC driver {0}", className);
        try {
            Class.forName(className);
        } catch (java.lang.ClassNotFoundException e) {
            this.logger.error("Cannot load JDBC driver", e);
            throw e;
        }
    }

    /**
     * default user.
     */
    private String userName = null;

    /**
     * @return the user used for getting connections.
     */
    public String getUserName() {
        return this.userName;
    }

    /**
     * Sets the user for getting connections.
     * @param userName the name of the user.
     */
    public void setUserName(final String userName) {
        this.userName = userName;
    }

    /**
     * default passwd.
     */
    private String password = null;

    /**
     * @return the password used for connections.
     */
    public String getPassword() {
        return this.password;
    }

    /**
     * Sets the password used to get connections.
     * @param password the password value.
     */
    public void setPassword(final String password) {
        this.password = password;
    }

    /**
     * Isolation level for JDBC.
     */
    private int isolationLevel = -1;

    /**
     * Isolation level (but String format).
     */
    private String isolationStr = null;

    /**
     * Sets the transaction isolation level of the connections.
     * @param level the level of isolation.
     */
    public void setTransactionIsolation(final String level) {
        if (level.equals("serializable")) {
            this.isolationLevel = Connection.TRANSACTION_SERIALIZABLE;
        } else if (level.equals("none")) {
            this.isolationLevel = Connection.TRANSACTION_NONE;
        } else if (level.equals("read_committed")) {
            this.isolationLevel = Connection.TRANSACTION_READ_COMMITTED;
        } else if (level.equals("read_uncommitted")) {
            this.isolationLevel = Connection.TRANSACTION_READ_UNCOMMITTED;
        } else if (level.equals("repeatable_read")) {
            this.isolationLevel = Connection.TRANSACTION_REPEATABLE_READ;
        } else {
            this.isolationStr = "default";
            return;
        }
        this.isolationStr = level;
    }

    /**
     * Gets the transaction isolation level.
     * @return transaction isolation level.
     */
    public String getTransactionIsolation() {
        return this.isolationStr;
    }

    /**
     * count max waiters during current period.
     */
    private int waiterCount = 0;

    /**
     * count max waiting time during current period.
     */
    private long waitingTime = 0;

    /**
     * count max busy connection during current period.
     */
    private int busyMax = 0;

    /**
     * count min busy connection during current period.
     */
    private int busyMin = 0;

    /**
     * High Value for no limit for the connection pool.
     */
    private static final int NO_LIMIT = 99999;

    /**
     * Nb of milliseconds in a day.
     */
    private static final long ONE_DAY = 1440L * 60L * 1000L;

    /**
     * max number of remove at once in the freelist We avoid removing too much
     * mcs at once for perf reasons.
     */
    private static final int MAX_REMOVE_FREELIST = 10;

    /**
     * minimum size of the connection pool.
     */
    private int poolMin = 0;

    /**
     * @return min pool size.
     */
    public synchronized int getPoolMin() {
        return this.poolMin;
    }

    /**
     * @param min minimum connection pool size to be set.
     */
    public synchronized void setPoolMin(final int min) {
        if (this.poolMin != min) {
            this.poolMin = min;
            adjust();
        }
    }

    /**
     * maximum size of the connection pool. default value is "NO LIMIT".
     */
    private int poolMax = NO_LIMIT;

    /**
     * @return actual max pool size
     */
    public synchronized int getPoolMax() {
        return this.poolMax;
    }

    /**
     * @param max max pool size. -1 means "no limit".
     */
    public synchronized void setPoolMax(final int max) {
        if (this.poolMax != max) {
            if (max < 0 || max > NO_LIMIT) {
                if (this.currentWaiters > 0) {
                    notify();
                }
                this.poolMax = NO_LIMIT;
            } else {
                if (this.currentWaiters > 0 && this.poolMax < max) {
                    notify();
                }
                this.poolMax = max;
                adjust();
            }
        }
    }

    /**
     * Max age of a Connection in milliseconds. When the time is elapsed, the
     * connection will be closed. This avoids keeping connections open too long
     * for nothing.
     */
    private long maxAge = ONE_DAY;

    /**
     * Same value in mns.
     */
    private int maxAgeMn;

    /**
     * @return max age for connections (in mm).
     */
    public int getMaxAge() {
        return this.maxAgeMn;
    }

    /**
     * @return max age for connections (in millisec).
     */
    public long getMaxAgeMilli() {
        return this.maxAge;
    }

    /**
     * @param mn max age of connection in minutes.
     */
    public void setMaxAge(final int mn) {
        this.maxAgeMn = mn;
        // set times in milliseconds
        this.maxAge = mn * ONE_MIN_MILLI;
    }

    /**
     * max open time for a connection, in millisec.
     */
    private long maxOpenTime = ONE_DAY;

    /**
     * Same value in mn.
     */
    private int maxOpenTimeMn;

    /**
     * @return max age for connections (in mns).
     */
    public int getMaxOpenTime() {
        return this.maxOpenTimeMn;
    }

    /**
     * @return max age for connections (in millisecs).
     */
    public long getMaxOpenTimeMilli() {
        return this.maxOpenTime;
    }

    /**
     * @param mn max time of open connection in minutes.
     */
    public void setMaxOpenTime(final int mn) {
        this.maxOpenTimeMn = mn;
        // set times in milliseconds
        this.maxOpenTime = mn * ONE_MIN_MILLI;
    }

    /**
     * max nb of milliseconds to wait for a connection when pool is empty.
     */
    private long waiterTimeout = WAITER_TIMEOUT;

    /**
     * @return waiter timeout in seconds.
     */
    public int getMaxWaitTime() {
        return (int) (this.waiterTimeout / MILLI);
    }

    /**
     * @param sec max time to wait for a connection, in seconds.
     */
    public void setMaxWaitTime(final int sec) {
        this.waiterTimeout = sec * MILLI;
    }

    /**
     * max nb of waiters allowed to wait for a Connection.
     */
    private int maxWaiters = DEFAULT_MAX_WAITERS;

    /**
     * @return max nb of waiters
     */
    public int getMaxWaiters() {
        return this.maxWaiters;
    }

    /**
     * @param nb max nb of waiters
     */
    public void setMaxWaiters(final int nb) {
        this.maxWaiters = nb;
    }

    /**
     * sampling period in sec.
     */
    private int samplingPeriod = DEFAULT_SAMPLING; // default sampling period

    /**
     * @return sampling period in sec.
     */
    public int getSamplingPeriod() {
        return this.samplingPeriod;
    }

    /**
     * @param sec sampling period in sec.
     */
    public void setSamplingPeriod(final int sec) {
        if (sec > 0) {
            this.samplingPeriod = sec;
        }
    }

    /**
     * Level of checking on connections when got from the pool. this avoids
     * reusing bad connections because too old, for example when database was
     * restarted... 0 = no checking 1 = check that still physically opened. 2 =
     * try a null statement.
     */
    private int checkLevel = 0; // default = 0

    /**
     * @return connection checking level
     */
    public int getCheckLevel() {
        return this.checkLevel;
    }

    /**
     * @param level jdbc connection checking level (0, 1, or 2)
     */
    public void setCheckLevel(final int level) {
        this.checkLevel = level;
    }

    /**
     * PreparedStatement pool size per managed connection.
     */
    private int pstmtMax = DEFAULT_PSTMT;

    /**
     * @return PreparedStatement cache size.
     */
    public int getPstmtMax() {
        return this.pstmtMax;
    }

    /**
     * @param nb PreparedStatement cache size.
     */
    public void setPstmtMax(final int nb) {
        this.pstmtMax = nb;
        // Set the value in each connection.
        for (Iterator<IManagedConnection> i = this.mcList.iterator(); i.hasNext();) {
            IManagedConnection mc = i.next();
            mc.setPstmtMax(this.pstmtMax);
        }
    }

    /**
     * test statement used when checkLevel = 2.
     */
    private String testStatement;

    /**
     * @return test statement used when checkLevel = 2.
     */
    public String getTestStatement() {
        return this.testStatement;
    }

    /**
     * @param s test statement
     */
    public void setTestStatement(final String s) {
        this.testStatement = s;
    }

    /**
     * Configure the Connection pool. Called by the Container at init.
     * Configuration can be set in datasource.properties files.
     * @param connchecklevel JDBC connection checking level
     * @param connmaxage JDBC connection maximum age
     * @param maxopentime JDBC connection maximum open time
     * @param connteststmt SQL query for test statement
     * @param pstmtmax prepare statement pool size per managed connection
     * @param minconpool Min size for the connection pool
     * @param maxconpool Max size for the connection pool
     * @param maxwaittime Max time to wait for a connection (in seconds)
     * @param maxwaiters Max nb of waiters for a connection
     * @param samplingperiod sampling period in sec.
     */
    @SuppressWarnings("boxing")
    public void poolConfigure(final String connchecklevel, final String connmaxage, final String maxopentime,
            final String connteststmt, final String pstmtmax, final String minconpool, final String maxconpool,
            final String maxwaittime, final String maxwaiters, final String samplingperiod) {

        // Configure pool
        setCheckLevel((new Integer(connchecklevel)).intValue());
        // set con max age BEFORE min/max pool size.
        setMaxAge((new Integer(connmaxage)).intValue());
        setMaxOpenTime((new Integer(maxopentime)).intValue());
        setTestStatement(connteststmt);
        setPstmtMax((new Integer(pstmtmax)).intValue());
        setPoolMin((new Integer(minconpool)).intValue());
        setPoolMax((new Integer(maxconpool)).intValue());
        setMaxWaitTime((new Integer(maxwaittime)).intValue());
        setMaxWaiters((new Integer(maxwaiters)).intValue());
        setSamplingPeriod((new Integer(samplingperiod)).intValue());
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("ConnectionManager configured with:");
            this.logger.debug("   jdbcConnCheckLevel  = {0}", connchecklevel);
            this.logger.debug("   jdbcConnMaxAge      = {0}", connmaxage);
            this.logger.debug("   jdbcMaxOpenTime     = {0}", maxopentime);
            this.logger.debug("   jdbcTestStmt        = {0}", connteststmt);
            this.logger.debug("   jdbcPstmtMax        = {0}", pstmtmax);
            this.logger.debug("   minConPool          = {0}", getPoolMin());
            this.logger.debug("   maxConPool          = {0}", getPoolMax());
            this.logger.debug("   maxWaitTime         = {0}", getMaxWaitTime());
            this.logger.debug("   maxWaiters          = {0}", getMaxWaiters());
            this.logger.debug("   samplingPeriod      = {0}", getSamplingPeriod());
        }
    }

    /**
     * maximum nb of busy connections in last sampling period.
     */
    private int busyMaxRecent = 0;

    /**
     * @return maximum nb of busy connections in last sampling period.
     */
    public int getBusyMaxRecent() {
        return this.busyMaxRecent;
    }

    /**
     * minimum nb of busy connections in last sampling period.
     */
    private int busyMinRecent = 0;

    /**
     * @return minimum nb of busy connections in last sampling period.
     */
    public int getBusyMinRecent() {
        return this.busyMinRecent;
    }

    /**
     * nb of threads waiting for a Connection.
     */
    private int currentWaiters = 0;

    /**
     * @return current number of connection waiters.
     */
    public int getCurrentWaiters() {
        return this.currentWaiters;
    }

    /**
     * total number of opened physical connections since the datasource
     * creation.
     */
    private int openedCount = 0;

    /**
     * @return int number of physical jdbc connection opened.
     */
    public int getOpenedCount() {
        return this.openedCount;
    }

    /**
     * total nb of physical connection failures.
     */
    private int connectionFailures = 0;

    /**
     * @return int number of xa connection failures on open.
     */
    public int getConnectionFailures() {
        return this.connectionFailures;
    }

    /**
     * total nb of connection leaks. A connection leak occurs when the caller
     * never issues a close method on the connection.
     */
    private int connectionLeaks = 0;

    /**
     * @return int number of connection leaks.
     */
    public int getConnectionLeaks() {
        return this.connectionLeaks;
    }

    /**
     * total number of opened connections since the datasource creation.
     */
    private int servedOpen = 0;

    /**
     * @return int number of xa connection served.
     */
    public int getServedOpen() {
        return this.servedOpen;
    }

    /**
     * total nb of open connection failures because waiter overflow.
     */
    private int rejectedFull = 0;

    /**
     * @return int number of open calls that were rejected due to waiter
     *         overflow.
     */
    public int getRejectedFull() {
        return this.rejectedFull;
    }

    /**
     * total nb of open connection failures because timeout.
     */
    private int rejectedTimeout = 0;

    /**
     * @return int number of open calls that were rejected by timeout.
     */
    public int getRejectedTimeout() {
        return this.rejectedTimeout;
    }

    /**
     * total nb of open connection failures for any other reason.
     */
    private int rejectedOther = 0;

    /**
     * @return int number of open calls that were rejected.
     */
    public int getRejectedOther() {
        return this.rejectedOther;
    }

    /**
     * @return int number of open calls that were rejected.
     */
    public int getRejectedOpen() {
        return this.rejectedFull + this.rejectedTimeout + this.rejectedOther;
    }

    /**
     * maximum nb of waiters since datasource creation.
     */
    private int waitersHigh = 0;

    /**
     * @return maximum nb of waiters since the datasource creation.
     */
    public int getWaitersHigh() {
        return this.waitersHigh;
    }

    /**
     * maximum nb of waiters in last sampling period.
     */
    private int waitersHighRecent = 0;

    /**
     * @return maximum nb of waiters in last sampling period.
     */
    public int getWaitersHighRecent() {
        return this.waitersHighRecent;
    }

    /**
     * total nb of waiters since datasource creation.
     */
    private int totalWaiterCount = 0;

    /**
     * @return total nb of waiters since the datasource creation.
     */
    public int getWaiterCount() {
        return this.totalWaiterCount;
    }

    /**
     * total waiting time in milliseconds.
     */
    private long totalWaitingTime = 0;

    /**
     * @return total waiting time since the datasource creation.
     */
    public long getWaitingTime() {
        return this.totalWaitingTime;
    }

    /**
     * max waiting time in milliseconds.
     */
    private long waitingHigh = 0;

    /**
     * @return max waiting time since the datasource creation.
     */
    public long getWaitingHigh() {
        return this.waitingHigh;
    }

    /**
     * max waiting time in milliseconds in last sampling period.
     */
    private long waitingHighRecent = 0;

    /**
     * @return max waiting time in last sampling period.
     */
    public long getWaitingHighRecent() {
        return this.waitingHighRecent;
    }

    /**
     * {@inheritDoc}
     */
    public int getLoginTimeout() throws SQLException {
        return this.loginTimeout;
    }

    /**
     * {@inheritDoc}
     */
    public void setLoginTimeout(final int seconds) throws SQLException {
        this.loginTimeout = seconds;
    }

    /**
     * {@inheritDoc}
     */
    public PrintWriter getLogWriter() throws SQLException {
        return this.log;
    }

    /**
     * {@inheritDoc}
     */
    public void setLogWriter(final PrintWriter out) throws SQLException {
        this.log = out;
    }

    /**
     * {@inheritDoc}
     */
    public Connection getConnection() throws SQLException {
        return getConnection(this.userName, this.password);
    }

    /**
     * Attempts to establish a connection with the data source that this
     * DataSource object represents. - comes from the javax.sql.DataSource
     * interface
     * @param username - the database user on whose behalf the connection is
     *        being made
     * @param password - the user's password
     * @return a connection to the data source
     * @throws SQLException - if a database access error occurs
     */
    public Connection getConnection(final String username, final String password) throws SQLException {
        IManagedConnection mc = null;

        // Get the current Transaction
        Transaction tx = null;
        try {
            tx = this.tm.getTransaction();
        } catch (NullPointerException n) {
            // current is null: we are not in EasyBeans Server.
            this.logger.error("ConnectionManager: should not be used outside a EasyBeans Server");
        } catch (SystemException e) {
            this.logger.error("ConnectionManager: getTransaction failed", e);
        }
        this.logger.debug("Tx = {0}", tx);

        // Get a ManagedConnection in the pool for this user
        mc = openConnection(username, tx);
        Connection ret = mc.getConnection();

        // Enlist XAResource if we are actually in a transaction
        if (tx != null) {
            if (mc.getOpenCount() == 1) { // Only if first/only thread
                try {
                    this.logger.debug("enlist XAResource on {0}", tx);
                    tx.enlistResource(mc.getXAResource());
                    ret.setAutoCommit(false);
                } catch (RollbackException e) {
                    // Although tx has been marked to be rolled back,
                    // XAResource has been correctly enlisted.
                    this.logger.warn("XAResource enlisted, but tx is marked rollback", e);
                } catch (IllegalStateException e) {
                    // In case tx is committed, no need to register resource!
                    ret.setAutoCommit(true);
                } catch (Exception e) {
                    this.logger.error("Cannot enlist XAResource", e);
                    this.logger.error("Connection will not be enlisted in a transaction");
                    // should return connection in the pool XXX
                    throw new SQLException("Cannot enlist XAResource");
                }
            }
        } else {
            ret.setAutoCommit(true); // in case we do not start a Tx
        }

        // return a Connection object
        return ret;
    }

    /**
     * Attempts to establish a physical database connection that can be used in
     * a distributed transaction.
     * @return an <code>XAConnection</code> object, which represents a
     *         physical connection to a data source, that can be used in a
     *         distributed transaction
     * @exception SQLException if a database access error occurs
     */
    public XAConnection getXAConnection() throws SQLException {
        return getXAConnection(this.userName, this.password);
    }

    /**
     * Attempts to establish a physical database connection, using the given
     * user name and password. The connection that is returned is one that can
     * be used in a distributed transaction - comes from the
     * javax.sql.XADataSource interface
     * @param user - the database user on whose behalf the connection is being
     *        made
     * @param passwd - the user's password
     * @return an XAConnection object, which represents a physical connection to
     *         a data source, that can be used in a distributed transaction
     * @throws SQLException - if a database access error occurs
     */
    @SuppressWarnings("boxing")
    public XAConnection getXAConnection(final String user, final String passwd) throws SQLException {
        // Create the actual connection in the std driver
        Connection conn = null;
        try {
            if (user.length() == 0) {
                conn = DriverManager.getConnection(this.url);
                this.logger.debug("    * New Connection on {0}", this.url);
            } else {
                // Accept password of zero length.
                conn = DriverManager.getConnection(this.url, user, passwd);
                this.logger.debug("    * New Connection on {0} for user {1}", this.url, user);
            }
        } catch (SQLException e) {
            this.logger.error("Could not get Connection on {0}", this.url, e);
            throw new SQLException("Could not get Connection on url : " + this.url + " for user : " + user + " inner exception"
                    + e.getMessage());
        }

        // Attempt to set the transaction isolation level
        // Depending on the underlaying database, this may not succeed.
        if (this.isolationLevel != -1) {
            try {
                this.logger.debug("set transaction isolation to {0}", this.isolationLevel);
                conn.setTransactionIsolation(this.isolationLevel);
            } catch (SQLException e) {
                String ilstr = "?";
                switch (this.isolationLevel) {
                case Connection.TRANSACTION_SERIALIZABLE:
                    ilstr = "SERIALIZABLE";
                    break;
                case Connection.TRANSACTION_NONE:
                    ilstr = "NONE";
                    break;
                case Connection.TRANSACTION_READ_COMMITTED:
                    ilstr = "READ_COMMITTED";
                    break;
                case Connection.TRANSACTION_READ_UNCOMMITTED:
                    ilstr = "READ_UNCOMMITTED";
                    break;
                case Connection.TRANSACTION_REPEATABLE_READ:
                    ilstr = "REPEATABLE_READ";
                    break;
                default:
                    throw new SQLException("Invalid isolation level '" + ilstr + "'.");
                }
                this.logger.error("Cannot set transaction isolation to {0} for this DataSource url {1}", ilstr, this.url, e);
                this.isolationLevel = -1;
            }
        }

        // Create the IManagedConnection object
        IManagedConnection mc = (IManagedConnection) Proxy.newProxyInstance(IManagedConnection.class.getClassLoader(),
                new Class[] {IManagedConnection.class}, new JManagedConnection(conn, this));

        // return the XAConnection
        return mc;
    }

    // -----------------------------------------------------------------
    // Referenceable Implementation
    // -----------------------------------------------------------------

    /**
     * Retrieves the Reference of this object. Used at binding time by JNDI to
     * build a reference on this object.
     * @return The non-null Reference of this object.
     * @exception NamingException If a naming exception was encountered while
     *            retrieving the reference.
     */
    public Reference getReference() throws NamingException {

        Reference ref = new Reference(this.getClass().getName(), DataSourceFactory.class.getName(), null);
        // These values are used by ObjectFactory (see DataSourceFactory.java)
        ref.add(new StringRefAddr("datasource.name", getDSName()));
        ref.add(new StringRefAddr("datasource.url", getUrl()));
        ref.add(new StringRefAddr("datasource.classname", getClassName()));
        ref.add(new StringRefAddr("datasource.username", getUserName()));
        ref.add(new StringRefAddr("datasource.password", getPassword()));
        ref.add(new StringRefAddr("datasource.isolationlevel", getTransactionIsolation()));
        Integer checklevel = Integer.valueOf(getCheckLevel());
        ref.add(new StringRefAddr("connchecklevel", checklevel.toString()));
        Integer maxage = Integer.valueOf(getMaxAge());
        ref.add(new StringRefAddr("connmaxage", maxage.toString()));
        Integer maxopentime = Integer.valueOf(getMaxOpenTime());
        ref.add(new StringRefAddr("maxopentime", maxopentime.toString()));
        ref.add(new StringRefAddr("connteststmt", getTestStatement()));
        Integer pstmtmax = Integer.valueOf(getPstmtMax());
        ref.add(new StringRefAddr("pstmtmax", pstmtmax.toString()));
        Integer minpool = Integer.valueOf(getPoolMin());
        ref.add(new StringRefAddr("minconpool", minpool.toString()));
        Integer maxpool = Integer.valueOf(getPoolMax());
        ref.add(new StringRefAddr("maxconpool", maxpool.toString()));
        Integer maxwaittime = Integer.valueOf(getMaxWaitTime());
        ref.add(new StringRefAddr("maxwaittime", maxwaittime.toString()));
        Integer maxwaiters = Integer.valueOf(getMaxWaiters());
        ref.add(new StringRefAddr("maxwaiters", maxwaiters.toString()));
        Integer samplingperiod = Integer.valueOf(getSamplingPeriod());
        ref.add(new StringRefAddr("samplingperiod", samplingperiod.toString()));
        return ref;
    }

    /**
     * Notifies this <code>ConnectionEventListener</code> that the application
     * has called the method <code>close</code> on its representation of a
     * pooled connection.
     * @param event an event object describing the source of the event
     */
    public void connectionClosed(final ConnectionEvent event) {
        IManagedConnection mc = (IManagedConnection) event.getSource();
        closeConnection(mc, XAResource.TMSUCCESS);
    }

    /**
     * Notifies this <code>ConnectionEventListener</code> that a fatal error
     * has occurred and the pooled connection can no longer be used. The driver
     * makes this notification just before it throws the application the
     * <code>SQLException</code> contained in the given
     * <code>ConnectionEvent</code> object.
     * @param event an event object describing the source of the event and
     *        containing the <code>SQLException</code> that the driver is
     *        about to throw
     */
    @SuppressWarnings("boxing")
    public void connectionErrorOccurred(final ConnectionEvent event) {

        IManagedConnection mc = (IManagedConnection) event.getSource();
        this.logger.debug("mc= {0}", mc.getIdentifier());

        // remove it from the list of open connections for this thread
        // only if it was opened outside a tx.
        closeConnection(mc, XAResource.TMFAIL);
    }

    /**
     * @return int number of xa connection
     */
    public int getCurrentOpened() {
        return this.mcList.size();
    }

    /**
     * @return int number of busy xa connection.
     */
    public int getCurrentBusy() {
        return this.mcList.size() - this.freeList.size();
    }

    /**
     * compute current min/max busyConnections.
     */
    public void recomputeBusy() {
        int busy = getCurrentBusy();
        if (this.busyMax < busy) {
            this.busyMax = busy;
        }
        if (this.busyMin > busy) {
            this.busyMin = busy;
        }
    }

    /**
     * @return int number of xa connection reserved for tx.
     */
    public int getCurrentInTx() {
        return this.tx2mc.size();
    }

    /**
     * make samples with some monitoring values.
     */
    public synchronized void sampling() {
        this.waitingHighRecent = this.waitingTime;
        if (this.waitingHigh < this.waitingTime) {
            this.waitingHigh = this.waitingTime;
        }
        this.waitingTime = 0;

        this.waitersHighRecent = this.waiterCount;
        if (this.waitersHigh < this.waiterCount) {
            this.waitersHigh = this.waiterCount;
        }
        this.waiterCount = 0;

        this.busyMaxRecent = this.busyMax;
        this.busyMax = getCurrentBusy();
        this.busyMinRecent = this.busyMin;
        this.busyMin = getCurrentBusy();
    }

    /**
     * Adjust the pool size, according to poolMax and poolMin values. Also
     * remove old connections in the freeList.
     */
    @SuppressWarnings("boxing")
    public synchronized void adjust() {
        this.logger.debug(this.dSName);

        // Remove max aged elements in freelist
        // - Not more than MAX_REMOVE_FREELIST
        // - Don't reduce pool size less than poolMin
        int count = this.mcList.size() - this.poolMin;
        // In case count is null, a new connection will be
        // recreated just after
        if (count >= 0) {
            if (count > MAX_REMOVE_FREELIST) {
                count = MAX_REMOVE_FREELIST;
            }
            for (Iterator<IManagedConnection> i = this.freeList.iterator(); i.hasNext();) {
                IManagedConnection mc = i.next();
                if (mc.isAged()) {
                    this.logger.debug("remove a timed out connection");
                    i.remove();
                    destroyItem(mc);
                    count--;
                    if (count <= 0) {
                        break;
                    }
                }
            }
        }
        recomputeBusy();

        // Close (physically) connections lost (opened for too long time)
        for (Iterator<IManagedConnection> i = this.mcList.iterator(); i.hasNext();) {
            IManagedConnection mc = i.next();
            if (mc.inactive()) {
                if (this.logger.isWarnEnabled()) {
                    this.logger.warn("close a timed out open connection {0}", mc.getIdentifier());
                }
                i.remove();
                // destroy mc
                mc.remove();
                this.connectionLeaks++;
                // Notify 1 thread waiting for a Connection.
                if (this.currentWaiters > 0) {
                    notify();
                }
            }
        }

        // Shrink the pool in case of max pool size
        // This occurs when max pool size has been reduced by admin console.
        if (this.poolMax != NO_LIMIT) {
            while (this.freeList.size() > this.poolMin && this.mcList.size() > this.poolMax) {
                IManagedConnection mc = this.freeList.first();
                this.freeList.remove(mc);
                destroyItem(mc);
            }
        }
        recomputeBusy();

        // Recreate more Connections while poolMin is not reached
        while (this.mcList.size() < this.poolMin) {
            IManagedConnection mc = null;
            try {
                mc = (IManagedConnection) getXAConnection();
                this.openedCount++;
            } catch (SQLException e) {
                throw new IllegalStateException("Could not create " + this.poolMin + " mcs in the pool : ", e);
            }
            // tx = null. Assumes maxage already configured.
            this.freeList.add(mc);
            this.mcList.add(mc);
            mc.addConnectionEventListener(this);
        }
    }

    /**
     * Lookup connection in the pool for this user/tx.
     * @param user user name
     * @param tx Transaction the connection is involved
     * @return a free IManagedConnection (never null)
     * @throws SQLException Cannot open a connection because the pool's max size
     *         is reached
     */
    @SuppressWarnings("boxing")
    public synchronized IManagedConnection openConnection(final String user, final Transaction tx) throws SQLException {
        IManagedConnection mc = null;
        // If a Connection exists already for this tx, just return it.
        // If no transaction, never reuse a connection already used.
        if (tx != null) {
            mc = this.tx2mc.get(tx);
            if (mc != null) {
                this.logger.debug("Reuse a Connection for same tx");
                mc.hold();
                this.servedOpen++;
                return mc;
            }
        }
        // Loop until a valid mc is found
        long timetowait = this.waiterTimeout;
        long starttime = 0;
        while (mc == null) {
            // try to find an mc in the free list
            if (this.freeList.isEmpty()) {
                // In case we have reached the maximum limit of the pool,
                // we must wait until a connection is released.
                if (this.mcList.size() >= this.poolMax) {
                    boolean stoplooping = true;
                    // If a timeout has been specified, wait, unless maxWaiters
                    // is reached.
                    if (timetowait > 0) {
                        if (this.currentWaiters < this.maxWaiters) {
                            this.currentWaiters++;
                            // Store the maximum concurrent waiters
                            if (this.waiterCount < this.currentWaiters) {
                                this.waiterCount = this.currentWaiters;
                            }
                            if (starttime == 0) {
                                starttime = System.currentTimeMillis();
                                this.logger.debug("Wait for a free Connection, {0}", this.mcList.size());
                            }
                            try {
                                wait(timetowait);
                            } catch (InterruptedException ign) {
                                this.logger.warn("Interrupted");
                            } finally {
                                this.currentWaiters--;
                            }
                            long stoptime = System.currentTimeMillis();
                            long stillwaited = stoptime - starttime;
                            timetowait = this.waiterTimeout - stillwaited;
                            stoplooping = (timetowait <= 0);
                            if (stoplooping) {
                                // We have been waked up by the timeout.
                                this.totalWaiterCount++;
                                this.totalWaitingTime += stillwaited;
                                if (this.waitingTime < stillwaited) {
                                    this.waitingTime = stillwaited;
                                }
                            } else {
                                if (!this.freeList.isEmpty() || this.mcList.size() < this.poolMax) {
                                    // We have been notified by a connection
                                    // released.
                                    this.logger.debug("Notified after {0}", stillwaited);
                                    this.totalWaiterCount++;
                                    this.totalWaitingTime += stillwaited;
                                    if (this.waitingTime < stillwaited) {
                                        this.waitingTime = stillwaited;
                                    }
                                }
                                continue;
                            }
                        }
                    }
                    if (stoplooping && this.freeList.isEmpty() && this.mcList.size() >= this.poolMax) {
                        if (starttime > 0) {
                            this.rejectedTimeout++;
                            this.logger.warn("Cannot create a Connection - timeout");
                        } else {
                            this.rejectedFull++;
                            this.logger.warn("Cannot create a Connection");
                        }
                        throw new SQLException("No more connections in " + getDatasourceName());
                    }
                    continue;
                }
                this.logger.debug("empty free list: Create a new Connection");
                try {
                    // create a new XA Connection
                    mc = (IManagedConnection) getXAConnection();
                    this.openedCount++;
                } catch (SQLException e) {
                    this.connectionFailures++;
                    this.rejectedOther++;
                    this.logger.warn("Cannot create new Connection for tx", e);
                    throw e;
                }
                // Register the connection manager as a ConnectionEventListener
                mc.addConnectionEventListener(this);
                this.mcList.add(mc);
            } else {
                mc = this.freeList.last();
                this.freeList.remove(mc);
                // Check the connection before reusing it
                if (this.checkLevel > 0) {
                    try {
                        IConnection conn = (IConnection) mc.getConnection();
                        if (conn.isPhysicallyClosed()) {
                            this.logger.warn("The JDBC connection has been closed!");
                            destroyItem(mc);
                            starttime = 0;
                            mc = null;
                            continue;
                        }
                        if (this.checkLevel > 1) {
                            java.sql.Statement stmt = conn.createStatement();
                            stmt.execute(this.testStatement);
                            stmt.close();
                        }
                    } catch (Exception e) {
                        this.logger.error("DataSource " + getDatasourceName() + " error: removing invalid mc", e);
                        destroyItem(mc);
                        starttime = 0;
                        mc = null;
                        continue;
                    }
                }
            }
        }
        recomputeBusy();
        mc.setTx(tx);
        if (tx == null) {
            this.logger.debug("Got a Connection - no TX: ");
        } else {
            this.logger.debug("Got a Connection for TX: ");
            // register synchronization
            try {
                tx.registerSynchronization(mc);
                this.tx2mc.put(tx, mc); // only if registerSynchronization was OK.
            } catch (javax.transaction.RollbackException e) {
                // / optimization is probably possible at this point
                this.logger.warn("DataSource " + getDatasourceName() + " error: Pool mc registered, but tx is rollback only", e);
            } catch (javax.transaction.SystemException e) {
                this.logger.error("DataSource " + getDatasourceName()
                        + " error in pool: system exception from transaction manager ", e);
            } catch (IllegalStateException e) {
                // In case transaction has already committed, do as if no tx.
                this.logger.warn("Got a Connection - committed TX: ", e);
                mc.setTx(null);
            }
        }
        mc.hold();
        this.servedOpen++;
        return mc;
    }

    /**
     * The transaction has committed (or rolled back). We can return its
     * connections to the pool of available connections.
     * @param tx the non null transaction
     */
    public synchronized void freeConnections(final Transaction tx) {
        this.logger.debug("free connection for Tx = " + tx);
        IManagedConnection mc = this.tx2mc.remove(tx);
        if (mc == null) {
            this.logger.error("pool: no connection found to free for Tx = " + tx);
            return;
        }
        mc.setTx(null);
        if (mc.isOpen()) {
            // Connection not yet closed (but committed).
            this.logger.debug("Connection not closed by caller");
            return;
        }
        freeItem(mc);
    }

    /**
     * Close all connections in the pool, when server is shut down.
     */
    public synchronized void closeAllConnection() {
        // Close physically all connections
        Iterator<IManagedConnection> it = this.mcList.iterator();
        try {
            while (it.hasNext()) {
                IManagedConnection mc = it.next();
                mc.close();
            }
        } catch (java.sql.SQLException e) {
            this.logger.error("Error while closing a Connection:", e);
        }
    }

    // -----------------------------------------------------------------------
    // private methods
    // -----------------------------------------------------------------------

    /**
     * Mark a specific Connection in the pool as closed. If it is no longer
     * associated to a Tx, we can free it.
     * @param mc XAConnection being closed
     * @param flag TMSUCCESS (normal close) or TMFAIL (error) or null if error.
     * @return false if has not be closed (still in use)
     */
    private boolean closeConnection(final IManagedConnection mc, final int flag) {
        // The connection will be available only if not associated
        // to a transaction. Else, it will be reusable only for the
        // same transaction.
        if (!mc.release()) {
            return false;
        }
        if (mc.getTx() != null) {
            this.logger.debug("keep connection for same tx");
        } else {
            freeItem(mc);
        }

        // delist Resource if in transaction
        Transaction tx = null;
        try {
            tx = this.tm.getTransaction();
        } catch (NullPointerException n) {
            // current is null: we are not in EasyBeans Server.
            this.logger.error("Pool: should not be used outside a EasyBeans Server", n);
        } catch (SystemException e) {
            this.logger.error("Pool: getTransaction failed:", e);
        }
        if (tx != null && mc.isClosed()) {
            try {
                tx.delistResource(mc.getXAResource(), flag);
            } catch (Exception e) {
                this.logger.error("Pool: Exception while delisting resource:", e);
            }
        }
        return true;
    }

    /**
     * Free item and return it in the free list.
     * @param item The item to be freed
     */
    private synchronized void freeItem(final IManagedConnection item) {
        // Add it to the free list
        // Even if maxage is reached, because we avoids going under min pool
        // size.
        // PoolKeeper will manage aged connections.
        this.freeList.add(item);
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("item added to freeList: " + item.getIdentifier());
        }

        // Notify 1 thread waiting for a Connection.
        if (this.currentWaiters > 0) {
            notify();
        }
        recomputeBusy();
    }

    /**
     * Destroy an mc because connection closed or error occured.
     * @param mc The mc to be destroyed
     */
    private synchronized void destroyItem(final IManagedConnection mc) {
        this.mcList.remove(mc);
        mc.remove();
        // Notify 1 thread waiting for a Connection.
        if (this.currentWaiters > 0) {
            notify();
        }
        recomputeBusy();
    }

    /**
     * Check on a connection the test statement.
     * @param testStatement the statement to use for test
     * @return the test statement if the test succeeded, an error message
     *         otherwise
     * @throws SQLException If an error occured when trying to test (not due to
     *         the test itself, but to other preliminary or post operation).
     */
    public String checkConnection(final String testStatement) throws SQLException {
        String noError = testStatement;
        IManagedConnection mc = null;
        boolean jmcCreated = false;
        if (!this.freeList.isEmpty()) {
            // find a connection to test in the freeList
            Iterator<IManagedConnection> it = this.freeList.iterator();
            while (it.hasNext()) {
                mc = it.next();
                try {
                    IConnection conn = (IConnection) mc.getConnection();
                    if (!conn.isPhysicallyClosed()) {
                        // ok, we found a connection we can use to test
                        this.logger.debug("Use a free IManagedConnection to test with " + testStatement);
                        break;
                    }
                    mc = null;
                } catch (SQLException e) {
                    // Can't use this connection to test
                    mc = null;
                }
            }
        }
        if (mc == null) {
            // try to create mc Connection
            this.logger.debug("Create a IManagedConnection to test with " + testStatement);
            Connection conn = null;
            try {
                conn = DriverManager.getConnection(this.url, this.userName, this.password);
            } catch (SQLException e) {
                this.logger.error("Could not get Connection on " + this.url + ":", e);
            }

            // Create the IManagedConnection object
            mc = (IManagedConnection) Proxy.newProxyInstance(IManagedConnection.class.getClassLoader(),
                    new Class[] {IManagedConnection.class}, new JManagedConnection(conn, this));
            jmcCreated = true;
        }
        if (mc != null) {
            // Do the test on a the free connection or the created connection
            Connection conn = mc.getConnection();
            java.sql.Statement stmt = conn.createStatement();
            try {
                stmt.execute(testStatement);
            } catch (SQLException e) {
                // The test fails
                return e.getMessage();
            }
            stmt.close();
            if (jmcCreated) {
                mc.close();
            }
        }
        return noError;
    }

    /**
     * Sets the transaction managed used by the connections.
     * @param tm the transaction manager.
     */
    protected void setTm(final TransactionManager tm) {
        this.tm = tm;
    }

    /**
     * Returns an object that implements the given interface to allow access to
     * non-standard methods, or standard methods not exposed by the proxy. If
     * the receiver implements the interface then the result is the receiver or
     * a proxy for the receiver. If the receiver is a wrapper and the wrapped
     * object implements the interface then the result is the wrapped object or
     * a proxy for the wrapped object. Otherwise return the the result of
     * calling unwrap recursively on the wrapped object or a proxy for that
     * result. If the receiver is not a wrapper and does not implement the
     * interface, then an SQLException is thrown.
     * @param iface A Class defining an interface that the result must
     *        implement.
     * @param <T> type of object.
     * @return an object that implements the interface. May be a proxy for the
     *         actual implementing object.
     * @throws SQLException If no object found that implements the interface
     */
    public <T> T unwrap(final Class<T> iface) throws SQLException {
        return null;
    }

    /**
     * Returns true if this either implements the interface argument or is
     * directly or indirectly a wrapper for an object that does. Returns false
     * otherwise. If this implements the interface then return true, else if
     * this is a wrapper then return the result of recursively calling
     * isWrapperFor on the wrapped object. If this does not implement the
     * interface and is not a wrapper, return false. This method should be
     * implemented as a low-cost operation compared to unwrap so that callers
     * can use this method to avoid expensive unwrap calls that may fail. If
     * this method returns true then calling unwrap with the same argument
     * should succeed.
     * @param iface a Class defining an interface.
     * @return true if this implements the interface or directly or indirectly
     *         wraps an object that does.
     * @throws SQLException if an error occurs while determining whether this is
     *         a wrapper for an object with the given interface.
     */
    public boolean isWrapperFor(final Class<?> iface) throws SQLException {
        return false;
    }

}
