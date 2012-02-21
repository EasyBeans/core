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
 * $Id: AbsTransactionInterceptor.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.transaction.interceptors;

import static javax.transaction.Status.STATUS_MARKED_ROLLBACK;

import java.lang.reflect.Method;

import javax.ejb.EJBException;
import javax.ejb.EJBTransactionRolledbackException;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.ow2.easybeans.api.EasyBeansInterceptor;
import org.ow2.easybeans.api.EasyBeansInvocationContext;
import org.ow2.easybeans.api.Factory;
import org.ow2.easybeans.api.bean.EasyBeansSFSB;
import org.ow2.easybeans.api.bean.info.IApplicationExceptionInfo;
import org.ow2.easybeans.api.bean.info.IBeanInfo;
import org.ow2.easybeans.transaction.JTransactionManager;
import org.ow2.util.log.Log;
import org.ow2.util.log.LogFactory;
import org.ow2.util.pool.api.Pool;
import org.ow2.util.pool.api.PoolException;

/**
 * Defines an abstract interceptor for transaction with common code used by all
 * transaction interceptors.
 * @author Florent Benoit
 */
public abstract class AbsTransactionInterceptor implements EasyBeansInterceptor {

    /**
     * Logger.
     */
    private Log logger = LogFactory.getLog(AbsTransactionInterceptor.class);

    /**
     * Transaction manager.
     */
    private TransactionManager transactionManager = null;

    /**
     * Constructor.<br>
     * Acquire the transaction manager.
     */
    public AbsTransactionInterceptor() {
        this.transactionManager = JTransactionManager.getTransactionManager();
    }

    /**
     * Defines the code used by the transaction interceptor on a given method.
     * @param invocationContext context with useful attributes on the current
     *        invocation
     * @return result of the next invocation (to chain interceptors)
     * @throws Exception if interceptor fails
     */
    public abstract Object intercept(final EasyBeansInvocationContext invocationContext) throws Exception;

    /**
     * Gets the transaction manager.
     * @return TM.
     */
    public TransactionManager getTransactionManager() {
        return this.transactionManager;
    }

    /**
     * Gets the application exception (if any) for the given invocation context
     * and the given exception.<br />
     * Note that a checked Exception is by default an application exception.
     * @param invocationContext context that provides access to the Method
     *        object.
     * @param e the exception to check
     * @return the application exception object, else null.
     */
    protected IApplicationExceptionInfo getApplicationException(final EasyBeansInvocationContext invocationContext,
            final Exception e) {

        IBeanInfo beanInfo = invocationContext.getFactory().getBeanInfo();

        // Application exception ?
        IApplicationExceptionInfo applicationExceptionInfo = beanInfo.getApplicationException(e);
        if (applicationExceptionInfo != null) {
            return applicationExceptionInfo;
        }

        // If runtime exception, not an application by default
        if (e instanceof RuntimeException) {
            return null;
        }
        // Is it a checked Exception ?
        Method method = invocationContext.getMethod();
        if (method != null) {
            Class<?>[] exceptions = method.getExceptionTypes();
            if (exceptions != null) {
                for (Class<?> clazz : exceptions) {
                    // Is an Exception but not a runtime exception
                    if (clazz.isInstance(e) && !(e instanceof RuntimeException)) {
                        // Checked exception, so application exception with
                        // rollback = false (default)
                        return beanInfo.getDefaultCheckedException();
                    }
                }
            }
        }
        // Was not a checked exception.
        return null;

    }




    /**
     * Remove from the factory's pool the bean found in the current invocation
     * context.
     * @param invocationContext the context of the current invocation
     * @throws PoolException if removal is failing
     */
    @SuppressWarnings("unchecked")
    protected void discard(final EasyBeansInvocationContext invocationContext) throws PoolException {

        Object target = invocationContext.getTarget();

        // factory is a stateful factory ?
        if (target instanceof EasyBeansSFSB) {
            // Gets the factory
            Factory factory = invocationContext.getFactory();

            // get pool
            Pool<EasyBeansSFSB, Long> pool = factory.getPool();

            // Bean is a stateful bean
            EasyBeansSFSB bean = (EasyBeansSFSB) invocationContext.getTarget();

            // discard instance
            pool.discard(bean);
        } else {
            this.logger.debug("Instance not discarded as it is not a stateful bean");
        }

    }

    /**
     * Marks the transaction for rollback.
     */
    protected void markTransactionRollback() {
        // Look if there is a transaction
        Transaction transaction;
        try {
            transaction = getTransactionManager().getTransaction();
        } catch (SystemException se) {
            throw new EJBException("Cannot get the current transaction on transaction manager.", se);
        }
        if (transaction != null) {
            try {
                this.transactionManager.setRollbackOnly();
            } catch (IllegalStateException e) {
                this.logger.warn("Cannot mark transaction as rollbackOnly", e);
            } catch (SystemException e) {
                this.logger.warn("Cannot mark transaction as rollbackOnly", e);
            }
        }
    }

    /**
     * Test if current transaction is with status STATUS_MARKED_ROLLBACK.
     * @return true if status == STATUS_MARKED_ROLLBACK
     */
    protected boolean isMarkedRollbackOnly() {
        try {
            return (STATUS_MARKED_ROLLBACK == this.transactionManager.getStatus());
        } catch (SystemException e) {
            this.logger.warn("Cannot get transaction status", e);
            return false;
        }
    }

    /**
     * Rollback the current transaction.
     */
    protected void rollback() {
        try {
            this.transactionManager.rollback();
        } catch (IllegalStateException e) {
            this.logger.warn("Cannot rollback the transaction", e);
        } catch (SecurityException e) {
            this.logger.warn("Cannot rollback the transaction", e);
        } catch (SystemException e) {
            this.logger.warn("Cannot rollback the transaction", e);
        }
    }

    /**
     * Commit the current transaction.
     */
    protected void commit() {
        try {
            this.transactionManager.commit();
        } catch (IllegalStateException e) {
            this.logger.warn("Cannot commit the transaction", e);
        } catch (SecurityException e) {
            this.logger.warn("Cannot commit the transaction", e);
        } catch (HeuristicMixedException e) {
            this.logger.warn("Cannot commit the transaction", e);
        } catch (HeuristicRollbackException e) {
            this.logger.warn("Cannot commit the transaction", e);
        } catch (RollbackException e) {
            this.logger.warn("Cannot commit the transaction", e);
        } catch (SystemException e) {
            this.logger.warn("Cannot commit the transaction", e);
        }
    }

    /**
     * Handle an exception for bean managed transaction.<br />
     * See Chapter 14.3.1.
     * @param invocationContext the context of the current invocation
     * @param e the exception to handle
     * @throws Exception when handling the exception.
     */
    protected void handleBeanManagedException(final EasyBeansInvocationContext invocationContext, final Exception e)
            throws Exception {
        IApplicationExceptionInfo applicationException = getApplicationException(invocationContext, e);

        // it is an application exception
        if (applicationException != null) {
            // Re-throw AppException.
            throw e;
        }

        // else, not an application exception :

        // Log the exception or error.
        this.logger.error("Bean Managed Transaction : Exception (not application exception) in business method", e);

        // Mark for rollback a transaction that has been
        // started, but not yet completed, by the instance.
        markTransactionRollback();

        // Discard instance.
        try {
            discard(invocationContext);
        } catch (PoolException pe) {
            throw new EJBException("Cannot discard the bean", pe);
        }

        // Throw EJBException to client.
        throw new EJBException("Bean Managed Transaction : Business exception which is not an application exception", e);
    }

    /**
     * Handle an exception that are in an unspecified transaction context.<br />
     * See Chapter 14.3.1.
     * @param invocationContext the context of the current invocation
     * @param e the exception to handle
     * @throws Exception when handling the exception.
     */
    protected void handleUnspecifiedTransactionContext(final EasyBeansInvocationContext invocationContext,
            final Exception e) throws Exception {

        IApplicationExceptionInfo applicationException = getApplicationException(invocationContext, e);

        // it is an application exception
        if (applicationException != null) {
            // Re-throw AppException.
            throw e;
        }

        // else, not an application exception :

        // Log the exception or error.
        this.logger.error("Exception (not application exception) in business method", e);

        // Discard instance.
        try {
            discard(invocationContext);
        } catch (PoolException pe) {
            throw new EJBException("Cannot discard the bean", pe);
        }

        // Throw EJBException to client.
        throw new EJBException("Business exception which is not an application exception", e);

    }

    /**
     * Handle an exception and the transaction is the client transaction.<br />
     * See Chapter 14.3.1.
     * @param invocationContext the context of the current invocation
     * @param e the exception to handle
     * @throws Exception when handling the exception.
     */
    protected void handleContextClientTransaction(final EasyBeansInvocationContext invocationContext, final Exception e)
            throws Exception {
        IApplicationExceptionInfo applicationException = getApplicationException(invocationContext, e);

        // An application exception ?
        if (applicationException != null) {
            /*
             * Re-throw AppException. Mark the transaction for rollback if the
             * application exception is specified as causing rollback.
             */

            // Mark the transaction for rollback.
            if (applicationException.rollback()) {
                markTransactionRollback();
            }

            // rethrow
            throw e;
        }

        // else, not an application exception :

        // Log the exception or error.
        this.logger.error("Exception (not application exception) in business method", e);

        // Mark the transaction for rollback.
        markTransactionRollback();

        // Discard instance.
        try {
            discard(invocationContext);
        } catch (PoolException pe) {
            throw new EJBException("Cannot discard the bean", pe);
        }

        // Throw javax.ejb.EJBTransactionRolledbackException to
        // client.
        EJBTransactionRolledbackException transactionException = new EJBTransactionRolledbackException(
                "System exception, The transaction has been marked for rollback only");
        transactionException.initCause(e);
        throw transactionException;
    }

    /**
     * Handle an exception and the transaction is the container transaction.
     * Bean method runs in the context of a transaction that the container
     * started immediately before dispatching the business method.<br />
     * See Chapter 14.3.1.
     * @param invocationContext the context of the current invocation
     * @param e the exception to handle
     * @throws Exception when handling the exception.
     */
    protected void handleContextContainerTransaction(final EasyBeansInvocationContext invocationContext,
            final Exception e) throws Exception {

        IApplicationExceptionInfo applicationException = getApplicationException(invocationContext, e);

        // An application exception ?
        if (applicationException != null) {
            /*
             * If the instance called setRollback-Only(), then rollback the
             * transaction, and re-throw AppException.
             */
            if (isMarkedRollbackOnly()) {
                rollback();
                throw e;
            }

            /*
             * Mark the transaction for rollback if the application exception is
             * specified as causing rollback, and then re-throw AppException.
             * Otherwise, attempt to commit the transaction, and then re-throw
             * AppException.
             */
            if (applicationException.rollback()) {
                // TODO: rollback or mark rollback ??
                rollback();
            } else {
                commit();
            }
            throw e;
        }
        // else, not an application exception :
        // Log the exception or error.
        this.logger.error("Exception (not application exception) in business method", e);

        // Rollback the container-started transaction.
        rollback();

        // Discard instance.
        try {
            discard(invocationContext);
        } catch (PoolException pe) {
            throw new EJBException("Cannot discard the bean", pe);
        }

        // Throw EJBException to client.
        throw new EJBException("Exception in a business interface with REQUIRED TX attribute", e);

    }
}
