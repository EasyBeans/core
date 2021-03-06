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
 * $Id: CMTRequiredTransactionInterceptor.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.transaction.interceptors;

import static javax.transaction.Status.STATUS_ACTIVE;
import static javax.transaction.Status.STATUS_MARKED_ROLLBACK;

import javax.ejb.EJBException;
import javax.ejb.TransactionRolledbackLocalException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.Transaction;

import org.ow2.easybeans.api.EasyBeansInvocationContext;
import org.ow2.easybeans.api.OperationState;
import org.ow2.util.log.Log;
import org.ow2.util.log.LogFactory;

/**
 * Defines an interceptor for method using the REQUIRED attribute.
 * @author Florent Benoit
 */
public class CMTRequiredTransactionInterceptor extends AbsTransactionInterceptor {

    /**
     * Logger.
     */
    private Log logger = LogFactory.getLog(CMTRequiredTransactionInterceptor.class);

    /**
     * Constructor.<br>
     * Acquire the transaction manager.
     */
    public CMTRequiredTransactionInterceptor() {
        super();
    }

    /**
     * Execute transaction as specified with the REQUIRED attribute.
     * @param invocationContext
     *            context with useful attributes on the current invocation
     * @return result of the next invocation (to chain interceptors)
     * @throws Exception
     *             if interceptor fails
     * @see <a href="http://www.jcp.org/en/jsr/detail?id=220">EJB 3.0
     *      specification ?12.6.2.2</a>
     */
    @Override
    public Object intercept(final EasyBeansInvocationContext invocationContext) throws Exception {
        this.logger.debug("Calling Required TX interceptor");

        // Needs to ignore the transaction part if this interceptor is
        // called through a SessionSynchronization
        OperationState state = invocationContext.getFactory().getOperationState();
        if (OperationState.AFTER_BEGIN == state || OperationState.BEFORE_COMPLETION == state
                || OperationState.AFTER_COMPLETION == state) {
            return invocationContext.proceed();
        }


        // Get current transaction
        Transaction transaction;
        try {
            transaction = getTransactionManager().getTransaction();
        } catch (SystemException se) {
            throw new EJBException("Cannot get the current transaction on transaction manager.", se);
        }

        this.logger.debug("Transaction found = {0}", transaction);

        /*
         * If the client invokes the enterprise bean's method while the client
         * is not associated with a transaction context, the container
         * automatically starts a new transaction before delegating a method
         * call to the enterprise bean business method.
         */
        boolean startedTransaction = false;
        if (transaction == null) {
            try {
                getTransactionManager().begin();
                startedTransaction = true;
            } catch (NotSupportedException nse) {
                throw new EJBException("Transaction Manager implementation does not support nested transactions.", nse);
            } catch (SystemException se) {
                throw new EJBException("Cannot call begin() on the transaction manager.", se);
            }
        }
        // else
        /*
         * If a client invokes the enterprise bean's method while the client is
         * associated with a transaction context, the container invokes the
         * enterprise bean's method in the client's transaction context.
         */
        boolean gotBusinessException = false;
        try {
            return invocationContext.proceed();

        } catch (Exception e) {
            gotBusinessException = true;

            // Chapter 14.3.1
            // Runs in the context of the client
            if (!startedTransaction) {
                handleContextClientTransaction(invocationContext, e);
            } else {
                // Container's transaction
                handleContextContainerTransaction(invocationContext, e);
            }
            // Shouldn't come here
            return null;
        } finally {
            if (!gotBusinessException) {

                // only do some operations if transaction has been started
                // before
                // invoking the method.
                if (startedTransaction) {

                    // sanity check.
                    Transaction transactionAfter = null;
                    try {
                        transactionAfter = getTransactionManager().getTransaction();
                    } catch (SystemException se) {
                        throw new EJBException("Cannot get the current transaction on transaction manager.", se);
                    }

                    if (transactionAfter == null) {
                        throw new RuntimeException("Transaction disappeared.");
                    }

                    /*
                     * The container attempts to commit the transaction when the
                     * business method has completed. The container performs the
                     * commit protocol before the method result is sent to the
                     * client.
                     */
                    try {
                        switch (getTransactionManager().getStatus()) {
                        case STATUS_ACTIVE:
                            getTransactionManager().commit();
                            break;
                        case STATUS_MARKED_ROLLBACK:
                            getTransactionManager().rollback();
                            break;
                        default:
                            throw new RuntimeException("Unexpected transaction status" + getTransactionManager().getStatus());
                        }
                    } catch (RollbackException e) {
                        throw new TransactionRolledbackLocalException("Could not commit transaction", e);
                    } catch (Exception e) {
                        throw new EJBException("Container exception", e);
                    }
                }
            }
        }

    }

}
