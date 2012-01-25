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
 * $Id: BMTTransactionInterceptor.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.transaction.interceptors;

import javax.ejb.EJBException;
import javax.transaction.InvalidTransactionException;
import javax.transaction.SystemException;
import javax.transaction.Transaction;

import org.ow2.easybeans.api.EasyBeansInvocationContext;
import org.ow2.util.log.Log;
import org.ow2.util.log.LogFactory;

/**
 * Defines an interceptor for method that are in Bean managed mode and then in Bean Managed Transaction.
 * @author Florent Benoit
 */
public class BMTTransactionInterceptor extends AbsTransactionInterceptor {

    /**
     * Logger.
     */
    private Log logger = LogFactory.getLog(BMTTransactionInterceptor.class);

    /**
     * Constructor.<br>
     * Acquire the transaction manager.
     */
    public BMTTransactionInterceptor() {
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
         * bean?s client view interfaces, the container suspends any transaction
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


        try {
            return invocationContext.proceed();
        } finally {

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
