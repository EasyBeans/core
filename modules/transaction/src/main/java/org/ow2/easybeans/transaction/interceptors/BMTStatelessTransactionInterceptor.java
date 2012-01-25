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
 * $Id: BMTStatelessTransactionInterceptor.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.transaction.interceptors;

import static javax.transaction.Status.STATUS_COMMITTED;

import javax.ejb.EJBException;
import javax.transaction.InvalidTransactionException;
import javax.transaction.SystemException;
import javax.transaction.Transaction;

import org.ow2.easybeans.api.EasyBeansInvocationContext;
import org.ow2.util.log.Log;
import org.ow2.util.log.LogFactory;

/**
 * Defines an interceptor for method that are in Bean managed mode and then in Bean Managed Transaction.
 * It is used by stateless session bean.
 * @author Florent Benoit
 */
public class BMTStatelessTransactionInterceptor extends AbsTransactionInterceptor {

    /**
     * Logger.
     */
    private Log logger = LogFactory.getLog(BMTStatelessTransactionInterceptor.class);

    /**
     * Constructor.<br>
     * Acquire the transaction manager.
     */
    public BMTStatelessTransactionInterceptor() {
        super();
    }

    /**
     * Execute transaction as specified for BMT.
     * @param invocationContext context with useful attributes on the current
     *        invocation
     * @return result of the next invocation (to chain interceptors)
     * @throws Exception if interceptor fails
     * @see <a href="http://www.jcp.org/en/jsr/detail?id=220">EJB 3.0
     *      specification ?12.6.1</a>
     */
    @Override
    public Object intercept(final EasyBeansInvocationContext invocationContext) throws Exception {
        logger.debug("Calling BMT TX interceptor");

        // Get current transaction
        Transaction transaction;
        try {
            transaction = getTransactionManager().getTransaction();
        } catch (SystemException se) {
            throw new EJBException("Cannot get the current transaction on transaction manager.", se);
        }

        logger.debug("Transaction found = {0}", transaction);

        /*
         * When a client invokes a business method via one of the enterprise
         * bean's client view interfaces, the container suspends any transaction
         * that may be associated with the client request. If there is a
         * transaction associated with the instance (this would happen if a
         * stateful session bean instance started the transaction in some
         * previous business method), the container associates the method
         * execution with this transaction. If there are interceptor methods
         * associated with the bean instances, these actions are taken before
         * the interceptor methods are invoked.
         */

        Transaction suspendedTransaction = null;
        if (transaction != null) {
            try {
                logger.debug("Suspending transaction {0}", transaction);
                suspendedTransaction = getTransactionManager().suspend();
            } catch (SystemException se) {
                throw new EJBException("Cannot call suspend() on the transaction manager.", se);
            }
        }

        boolean gotBusinessException = false;
        try {
            return invocationContext.proceed();
        } catch (Exception e) {
            gotBusinessException = true;
            handleBeanManagedException(invocationContext, e);
            // Shouldn't come here
            return null;
        } finally {
            if (!gotBusinessException) {
                /**
                 * If a stateless session bean instance starts a transaction in a
                 * business method or interceptor method, it must commit the
                 * transaction before the business method (or all its interceptor
                 * methods) returns. The container must detect the case in which a
                 * transaction was started, but not completed, in the business
                 * method or interceptor method for the business method, and handle
                 * it as follows:
                 * <ul>
                 * <li>Log this as an application error to alert the System
                 * Administrator.</li>
                 * <li>Roll back the started transaction</li>
                 * <li>Discard the instance of the session bean</li>
                 * <li>Throw the javax.ejb.EJBException[53]. If the EJB 2.1 client
                 * view is used, the container should throw java.rmi.RemoteException
                 * if the client is a remote client, or throw the
                 * javax.ejb.EJBException if the client is a local client.</li>
                 */
                Transaction transactionAfter = null;
                try {
                    transactionAfter = getTransactionManager().getTransaction();
                } catch (SystemException se) {
                    throw new EJBException("Cannot get the current transaction on transaction manager.", se);
                }
                if (transactionAfter != null) {
                    int transactionStatus = transactionAfter.getStatus();
                    // There is a transaction and it was not committed
                    if (transactionStatus != STATUS_COMMITTED) {
                        String errMsg = "Transaction started by the bean but not committed.";
                        // Log error
                        logger.error(errMsg);
                        // Rollback
                        transactionAfter.rollback();
                        //TODO: discard
                        // Throw Exception
                        throw new EJBException(errMsg);
                    }
                }
            }

            /*
             * The container resumes the suspended association when the business
             * method has completed.
             */
            if (suspendedTransaction != null) {

                logger.debug("Resuming transaction {0}", transaction);

                try {
                    getTransactionManager().resume(suspendedTransaction);
                } catch (InvalidTransactionException ite) {
                    throw new EJBException(
                            "Cannot call resume() on the given transaction. There is an invalid transaction", ite);
                } catch (IllegalStateException ise) {
                    throw new EJBException(
                            "Cannot call resume() on the given transaction. There is another associated transaction",
                            ise);
                } catch (SystemException se) {
                    throw new EJBException("Cannot call resume() on the given transaction. Unexpected error condition",
                            se);
                }
            }
        }
    }

}
