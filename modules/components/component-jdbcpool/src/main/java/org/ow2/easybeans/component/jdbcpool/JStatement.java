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
 * $Id: JStatement.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.component.jdbcpool;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.ow2.util.log.Log;
import org.ow2.util.log.LogFactory;

/**
 * Wrapper on a PreparedStatement. This wrapper is used to track close method in
 * order to avoid closing the statement, and putting it instead in a pool.
 * @author Philippe Durieux
 * @author Florent Benoit
 */
public class JStatement extends AbsProxy {

    /**
     * Properties of this statement has been changed ? Needs to be be cleared
     * when reused.
     */
    private boolean changed = false;

    /**
     * Is that this statement is opened ?
     */
    private boolean opened = false;

    /**
     * Being closed. (in close method).
     */
    private boolean closing = false;

    /**
     * Physical PreparedStatement object on which the wrapper is.
     */
    private PreparedStatement ps;

    /**
     * Managed Connection the Statement belongs to.
     */
    private JManagedConnection mc;

    /**
     * Hashcode computed in constructor.
     */
    private int hashCode;

    /**
     * SQL used as statement.
     */
    private String sql;

    /**
     * Logger.
     */
    private Log logger = LogFactory.getLog(JStatement.class);

    /**
     * Builds a new statement with the given wrapped statement of given
     * connection and given sql query.
     * @param ps the prepared statement.
     * @param mc managed connection
     * @param sql query.
     */
    public JStatement(final PreparedStatement ps, final JManagedConnection mc, final String sql) {
        this.ps = ps;
        this.mc = mc;
        this.sql = sql;
        hashCode = sql.hashCode();
        opened = true;
    }

    /**
     * @return Sql query used.
     */
    public String getSql() {
        return sql;
    }

    /**
     * Gets the preparedstatement used by this wrapper.
     * @return the internal prepared statement
     */
    protected PreparedStatement getInternalPreparedStatement() {
        return this.ps;
    }

    /**
     * @return hashcode of the object
     */
    @Override
    public int hashCode() {
        return hashCode;
    }

    /**
     * @param stmt given statement for comparing it
     * @return true if given object is equals to this current object
     */
    @Override
    public boolean equals(final Object stmt) {
        if (stmt == null) {
            return false;
        }
        // different hashcode, cannot be equals
        if (this.hashCode != stmt.hashCode()) {
            return false;
        }

        // if got same hashcode, try to see if cast is ok.
        if (!(stmt instanceof JStatement)) {
            logger.warn("Bad class {0}", stmt);
            return false;
        }

        // Cast object
        JStatement psw = (JStatement) stmt;
        if (sql == null && psw.getSql() != null) {
            return false;
        }
        if (sql != null && !sql.equals(psw.getSql())) {
            return false;
        }
        try {
            if (psw.getInternalPreparedStatement().getResultSetType() != ps.getResultSetType()) {
                return false;
            }
            if (psw.getInternalPreparedStatement().getResultSetConcurrency() != ps.getResultSetConcurrency()) {
                return false;
            }
        } catch (SQLException e) {
            logger.warn("Cannot compare statements", e);
            return false;
        }
        logger.debug("Found");
        return true;
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

        // Methods that are part of the IPreparedStatement interface
        if ("forceClose".equals(methodName)) {
            return Boolean.valueOf(forceClose());
        } else if ("reuse".equals(methodName)) {
            reuse();
            return null;
        } else if ("isClosed".equals(methodName)) {
            return Boolean.valueOf(isClosed());
        } else if ("forget".equals(methodName)) {
            forget();
            return null;
        } else if ("close".equals(methodName)) {
            close();
            return null;
        }

        // If there are some properties changes on the prepared statement, flag them to
        // avoid to loose time when resetting it
        if ("addBatch".equals(methodName) || "execute".equals(methodName) || "executeUpdate".equals(methodName)
                || "setFetchDirection".equals(methodName) || "setFetchSize".equals(methodName)
                || "setMaxFieldSize".equals(methodName) || "setMaxRows".equals(methodName)
                || "setQueryTimeout".equals(methodName)) {
            changed = true;
        }

        // Delegate to the prepared statement object
        try {
            return method.invoke(this.ps, args);
        } catch (InvocationTargetException e) {
            // Throw the inner exception
            Throwable targetException = e.getTargetException();
            throw targetException;
        }
    }

    /**
     * Force a close on the Prepare Statement. Usually, it's the caller that did
     * not close it explicitly
     * @return true if it was open
     */
    public boolean forceClose() {
        if (opened) {
            logger.debug("Statements should be closed explicitly.");
            opened = false;
            return true;
        }
        return false;
    }

    /**
     * Reuses this statement so reset properties.
     * @throws SQLException if reset fails
     */
    public void reuse() throws SQLException {
        ps.clearParameters();
        ps.clearWarnings();
        opened = true;
        if (changed) {
            logger.debug("Properties statement have been changed, reset default properties");
            ps.clearBatch();
            ps.setFetchDirection(ResultSet.FETCH_FORWARD);
            ps.setMaxFieldSize(0);
            ps.setMaxRows(0);
            ps.setQueryTimeout(0);
            changed = false;
        }
    }

    /**
     * @return true if this statement has been closed, else false.
     */
    public boolean isClosed() {
        return !opened && !closing;
    }

    /**
     * Physically close this Statement.
     * @throws SQLException
     */
    public void forget() {
        try {
            ps.close();
        } catch (SQLException e) {
            logger.error("Cannot close the PreparedStatement", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void close() throws SQLException {
        if (!opened) {
            logger.debug("Statement already closed");
            return;
        }
        opened = false;
        closing = true;
        mc.notifyPsClose(this);
        closing = false;
    }

}
