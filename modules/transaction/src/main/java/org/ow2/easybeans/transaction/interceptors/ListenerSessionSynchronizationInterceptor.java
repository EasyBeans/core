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
 * $Id: ListenerSessionSynchronizationInterceptor.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.transaction.interceptors;

import java.rmi.RemoteException;
import java.util.Map;
import java.util.WeakHashMap;

import javax.ejb.EJBException;
import javax.ejb.SessionSynchronization;
import javax.ejb.TransactionRolledbackLocalException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;

import org.ow2.easybeans.api.EasyBeansInvocationContext;
import org.ow2.easybeans.api.OperationState;
import org.ow2.easybeans.transaction.SessionSynchronizationListener;
import org.ow2.util.log.Log;
import org.ow2.util.log.LogFactory;

/**
 * This interceptor will add on the current transaction an object which will
 * listen the transaction synchronization and call methods on a bean.
 * @author Florent Benoit
 */
public class ListenerSessionSynchronizationInterceptor extends AbsTransactionInterceptor {

    /**
     * Logger.
     */
    private Log logger = LogFactory.getLog(ListenerSessionSynchronizationInterceptor.class);

    /**
     * Listener which will receive event of the transaction manager.
     */
    private Map<Object, SessionSynchronizationListener> listeners = new WeakHashMap<Object, SessionSynchronizationListener>();

    /**
     * Adds a listener object receiving calls from the transaction manager.
     * @param invocationContext context with useful attributes on the current
     *        invocation
     * @return result of the next invocation (to chain interceptors).
     * @throws Exception if interceptor fails
     * @see <a href="http://www.jcp.org/en/jsr/detail?id=220">EJB 3.0
     *      specification ?12.6.2.2</a>
     */
    @Override
    public Object intercept(final EasyBeansInvocationContext invocationContext) throws Exception {
        this.logger.debug("Calling ListenerSessionSynchronizationInterceptor interceptor");
        if (getTransactionManager().getTransaction() != null) {
            addSynchronization(invocationContext);
        } else {
            this.logger.warn("No transaction but the bean is implementing session synchonization interface.");
        }
        return invocationContext.proceed();
    }

    /**
     * Add a synchronization listener to the transaction manager in order to be
     * notified and send actions on the bean. It should be done only once until
     * transaction is completed.
     * @param invocationContext the context on the current invocation.
     */
    private void addSynchronization(final EasyBeansInvocationContext invocationContext) {
        Object o = invocationContext.getTarget();
        if (!(o instanceof SessionSynchronization)) {
            throw new IllegalArgumentException("This interceptor should not have been added on this "
                    + "bean which doesn't implement SessionSynchronization interface.");
        }
        SessionSynchronization bean = (SessionSynchronization) o;

        /**
         * 4.3.11 Interceptors for Session Beans.<br>
         * For stateful session beans that implement the SessionSynchronization
         * interface, afterBegin occurs before any AroundInvoke method
         * invocation, and beforeCompletion after all AroundInvoke invocations
         * are finished.<br>
         * The beforeCompletion method is called by the transaction manager
         * prior to the start of the two-phase transaction commit process. This
         * call is executed with the transaction context of the transaction that
         * is being committed.
         */

        // TODO: THE COMPLETED information should be retrieved on the bean
        // context, not on the session listener.
        SessionSynchronizationListener sessionSynchronizationListener = this.listeners.get(bean);
        if (sessionSynchronizationListener == null) {
            sessionSynchronizationListener =  new SessionSynchronizationListener(bean, invocationContext.getFactory());
            this.listeners.put(bean, sessionSynchronizationListener);
        }

        // add only once until
        if (sessionSynchronizationListener.isReady()) {

            try {
                getTransactionManager().getTransaction().registerSynchronization(sessionSynchronizationListener);
            } catch (IllegalStateException e) {
                throw new EJBException("Cannot register the synchronization", e);
            } catch (RollbackException e) {
                throw new TransactionRolledbackLocalException("Session rolled back");
            } catch (SystemException e) {
                throw new EJBException("Cannot register the synchronization", e);
            }

            // Change state
            OperationState oldState = invocationContext.getFactory().getOperationState();
            invocationContext.getFactory().getOperationStateThreadLocal().set(OperationState.AFTER_BEGIN);

            // Call method
            try {
                bean.afterBegin();
            } catch (EJBException e) {
                throw e;
            } catch (RemoteException e) {
                throw new EJBException("Cannot call afterBegin method", e);
            } finally {
                invocationContext.getFactory().getOperationStateThreadLocal().set(oldState);
            }
            sessionSynchronizationListener.inTX();
        }

    }
}
