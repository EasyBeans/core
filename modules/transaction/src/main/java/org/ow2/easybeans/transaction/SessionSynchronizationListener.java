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
 * $Id: SessionSynchronizationListener.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.transaction;

import static javax.transaction.Status.STATUS_COMMITTED;

import java.rmi.RemoteException;

import javax.ejb.EJBException;
import javax.ejb.SessionSynchronization;
import javax.transaction.Synchronization;
import javax.transaction.Transaction;

import org.ow2.easybeans.api.EZBStatefulSessionFactory;
import org.ow2.easybeans.api.OperationState;

/**
 * This listener will be notified by the transaction manager and will call
 * methods on the bean.
 * @author Florent Benoit
 */
public class SessionSynchronizationListener implements Synchronization {

    /**
     * Bean on which synchonization will be done.
     */
    private SessionSynchronization synchronizedBean = null;

    /**
     * Stateful session Factory of the bean.
     */
    private EZBStatefulSessionFactory factory = null;

    private Transaction tx = null;

    /**
     * Creates a listener which will act on the given bean.
     * @param synchronizedBean bean on which call synchronization methods.
     * @param factory the EasyBeans factory.
     */
    public SessionSynchronizationListener(final SessionSynchronization synchronizedBean, final EZBStatefulSessionFactory factory, final Transaction tx) {
        this.synchronizedBean = synchronizedBean;
        this.factory = factory;
        this.tx = tx;
    }

    /**
     * 4.3.11 Interceptors for Session Beans.<br>
     * For stateful session beans that implement the SessionSynchronization
     * interface, afterBegin occurs before any AroundInvoke method invocation,
     * and beforeCompletion after all AroundInvoke invocations are finished.<br>
     * The beforeCompletion method is called by the transaction manager prior to
     * the start of the two-phase transaction commit process. This call is
     * executed with the transaction context of the transaction that is being
     * committed.
     */
    public void beforeCompletion() {
        // Set the operation state
        OperationState oldState = this.factory.getOperationState();
        this.factory.getOperationStateThreadLocal().set(OperationState.BEFORE_COMPLETION);
        try {
            this.synchronizedBean.beforeCompletion();
        } catch (EJBException e) {
            throw e;
        } catch (RemoteException e) {
            throw new EJBException("Error in beforeCompletion()", e);
        } finally {
            this.factory.getOperationStateThreadLocal().set(oldState);
        }

        this.factory.unsetSessionSynchronizationListener(this.tx);

    }

    /**
     * This method is called by the transaction manager after the transaction is
     * committed or rolled back.
     * @param status The status of the transaction completion.
     */
    public void afterCompletion(final int status) {
        // Set the operation state
        OperationState oldState = this.factory.getOperationState();
        this.factory.getOperationStateThreadLocal().set(OperationState.AFTER_COMPLETION);
        try {
            this.synchronizedBean.afterCompletion(status == STATUS_COMMITTED);
        } catch (EJBException e) {
            throw e;
        } catch (RemoteException e) {
            throw new EJBException("Error in afterCompletion()", e);
        } finally {
            this.factory.getOperationStateThreadLocal().set(oldState);
        }

    }

}
