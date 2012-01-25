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
 * $Id: JOTMComponent.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.component.jotm;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.resource.spi.XATerminator;
import javax.transaction.NotSupportedException;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;
import javax.transaction.xa.XAException;
import javax.transaction.xa.Xid;

import org.objectweb.jotm.Current;
import org.objectweb.jotm.Jotm;
import org.objectweb.jotm.TimerManager;
import org.ow2.easybeans.component.api.EZBComponentException;
import org.ow2.easybeans.component.itf.TMComponent;
import org.ow2.easybeans.transaction.JTransactionManager;
import org.ow2.util.log.Log;
import org.ow2.util.log.LogFactory;

/**
 * Creates and binds the transaction factory and usertransaction object.
 * @author Florent Benoit
 */
public class JOTMComponent implements TMComponent {

    /**
     * Default Transaction timeout.
     */
    private static final int DEFAULT_TIMEOUT = 60;

    /**
     * Logger.
     */
    private static Log logger = LogFactory.getLog(JOTMComponent.class);

    /**
     * Jotm object.
     */
    private Jotm jotm = null;

    /**
     * Transaction Manager.
     */
    private TransactionManager tm = null;

    /**
     * Transaction timeout (in seconds).
     */
    private int timeout = DEFAULT_TIMEOUT;

    /**
     * Init method.<br/>
     * This method is called before the start method.
     * @throws EZBComponentException if the initialization has failed.
     */
    public void init() throws EZBComponentException {

    }

    /**
     * Start method.<br/>
     * This method is called after the init method.
     * @throws EZBComponentException if the start has failed.
     */
    public void start() throws EZBComponentException {

        // Check tm exists ?
        try {
            this.tm = (TransactionManager) new InitialContext().lookup(TMComponent.JNDI_NAME);
        } catch (NamingException e) {
            logger.debug("tm not available");
        }

        // Already exists
        if (this.tm != null) {
            logger.info("Using Application server TM component");
            return;
        }

        // Create Transaction manager
        try {
            this.jotm = new Jotm(true, false);
        } catch (NamingException e) {
            throw new EZBComponentException("Cannot init JOTM object", e);
        }

        // Set transaction timeout
        try {
            this.jotm.getTransactionManager().setTransactionTimeout(this.timeout);
        } catch (SystemException se) {
            throw new EZBComponentException("Cannot set Transaction Timeout", se);
        }

        // Bind it
        try {
            new InitialContext().rebind(JNDI_NAME, this.jotm.getTransactionManager());
        } catch (NamingException e) {
            throw new EZBComponentException("Cannot bind user transaction", e);
        }

        // Init the static Transaction manager.
        JTransactionManager.init();

        // info
        logger.info("Register {0} as transaction manager object", JNDI_NAME);
    }

    /**
     * Stop method.<br/>
     * This method is called when component needs to be stopped.
     * @throws EZBComponentException if the stop is failing.
     */
    public void stop() throws EZBComponentException {

        // Unbind user transaction object
        try {
            new InitialContext().unbind(JNDI_NAME);
        } catch (NamingException e) {
            throw new EZBComponentException("Cannot unbind user transaction", e);
        }

        // Stop timer
        TimerManager.stop(true);

        // Stop JOTM
        this.jotm.stop();
        logger.info("JOTM Component stopped");

    }

    /**
     * Gets the transaction manager object.
     * @return instance of the transaction manager
     */
    public TransactionManager getTransactionManager() {
        if (this.jotm != null) {
            return this.jotm.getTransactionManager();
        }
        return this.tm;
    }

    /**
     * Set the Transaction Timeout.
     * @param timeout Timeout (in seconds)
     */
    public void setTimeout(final int timeout) {
        this.timeout = timeout;
    }

    /**
     * @return the XA terminator.
     * @throws XAException if the terminator is not available
     */
    public XATerminator getXATerminator() throws XAException {
        return ((Current) getTransactionManager()).getXATerminator();
    }

    /**
     * Creates a new inflow transaction and associates it with the current
     * thread.
     * @param xid <code>Xid</code> of the inflow transaction.
     * @throws NotSupportedException Thrown if the thread is already associated
     *         with a transaction. (nested transaction are not supported)
     * @throws SystemException Thrown if the transaction manager encounters an
     *         unexpected error condition
     */
    public void begin(final Xid xid) throws NotSupportedException, SystemException {
        ((Current) getTransactionManager()).begin(xid);
    }

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
    public void begin(final Xid xid, final long timeout) throws NotSupportedException, SystemException {
        ((Current) getTransactionManager()).begin(xid, timeout);
    }

    /**
     * Clear transaction from this thread if not known.
     * Useful when another thread completes the current thread's transaction
     */
    public void clearThreadTx() {
        ((Current) getTransactionManager()).clearThreadTx();
    }

}
