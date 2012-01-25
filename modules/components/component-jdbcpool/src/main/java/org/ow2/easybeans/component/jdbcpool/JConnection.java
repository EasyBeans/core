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
 * $Id: JConnection.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.component.jdbcpool;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;

import org.ow2.util.log.Log;
import org.ow2.util.log.LogFactory;

/**
 * This class represent a connection linked to the physical and XA connections.
 * All errors are reported to the managed connection. This connection is
 * returned to the client.
 * @author Philippe Durieux
 * @author Florent Benoit
 */
public class JConnection extends AbsProxy {

    /**
     * Logger used for debug.
     */
    private static Log logger = LogFactory.getLog(JConnection.class);

    /**
     * JDBC connection provided by the DriverManager.
     */
    private Connection physicalConnection = null;

    /**
     * XA connection which receive events.
     */
    private IManagedConnection xaConnection = null;

    /**
     * PreparedStatement method of xaConnection.
     */
    private static Method prepareStatementMethod = null;


    /**
     * Buils a Connection (viewed by the user) which rely on a Managed
     * connection and a physical connection.
     * @param xaConnection the XA connection.
     * @param physicalConnection the connection to the database.
     */
    public JConnection(final IManagedConnection xaConnection, final Connection physicalConnection) {
        this.xaConnection = xaConnection;
        this.physicalConnection = physicalConnection;
    }

    /**
     * Gets the physical connection to the database.
     * @return physical connection to the database
     */
    public Connection getConnection() {
        return physicalConnection;
    }

    /**
     * @return true if the connection to the database is closed or not.
     * @throws SQLException if a database access error occurs
     */
    public boolean isPhysicallyClosed() throws SQLException {
        return physicalConnection.isClosed();
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

        // Methods that are part of the IConnection interface
        if ("getConnection".equals(method.getName())) {
            return getConnection();
        } else if ("isPhysicallyClosed".equals(method.getName())) {
            return Boolean.valueOf(isPhysicallyClosed());
        } else if ("close".equals(method.getName())) {
            xaConnection.notifyClose();
            return null;
        } else if ("prepareStatement".equals(method.getName()) && method.getParameterTypes().length == 1) {
            // Use the xaConnection Object (which allow to have the preparedstatement pool) but only for method with SQL
            try {
                return getPrepareStatementMethod().invoke(xaConnection, args);
            } catch (InvocationTargetException e) {
                // Check if it is an SQLException
                Throwable targetException = e.getTargetException();
                if (targetException instanceof SQLException) {
                    logger.debug("Exception while calling method {0} on object {1}", method, xaConnection);
                    xaConnection.notifyError((SQLException) targetException);
                }
                // Rethrow Exception
                throw targetException;
            }
        } else {
            // Else delegate to the physicalConnection object
            try {
                return method.invoke(physicalConnection, args);
            } catch (InvocationTargetException e) {
                // Check if it is an SQLException
                Throwable targetException = e.getTargetException();
                if (targetException instanceof SQLException) {
                    logger.debug("Exception while calling method {0} on object {1}", method, physicalConnection);
                    xaConnection.notifyError((SQLException) targetException);
                }
                // Rethrow Exception
                throw targetException;
            }

        }
    }

    /**
     * @return the prepared statement method of the XAConnection interface.
     */
    protected static Method getPrepareStatementMethod() {
        if (prepareStatementMethod != null) {
            return prepareStatementMethod;
        }

        try {
            prepareStatementMethod = IManagedConnection.class.getMethod("prepareStatement", String.class);
        } catch (SecurityException e) {
            throw new IllegalStateException("Cannot find the prepareStatement method on XAConnection interface", e);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("Cannot find the prepareStatement method on XAConnection interface", e);
        }

        return prepareStatementMethod;


    }

}
