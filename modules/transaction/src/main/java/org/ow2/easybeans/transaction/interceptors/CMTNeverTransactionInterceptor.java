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
 * $Id: CMTNeverTransactionInterceptor.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.transaction.interceptors;

import javax.ejb.EJBException;
import javax.transaction.SystemException;
import javax.transaction.Transaction;

import org.ow2.easybeans.api.EasyBeansInvocationContext;
import org.ow2.util.log.Log;
import org.ow2.util.log.LogFactory;

/**
 * Defines an interceptor for method using the NEVER attribute.
 * @author Florent Benoit
 */
public class CMTNeverTransactionInterceptor extends AbsTransactionInterceptor {

    /**
     * Logger.
     */
    private Log logger = LogFactory.getLog(CMTNeverTransactionInterceptor.class);

    /**
     * Constructor.<br>
     * Acquire the transaction manager.
     */
    public CMTNeverTransactionInterceptor() {
        super();
    }

    /**
     * Execute transaction as specified with the NEVER attribute.
     * @param invocationContext context with useful attributes on the current
     *        invocation
     * @return result of the next invocation (to chain interceptors)
     * @throws Exception if interceptor fails
     * @see <a href="http://www.jcp.org/en/jsr/detail?id=220">EJB 3.0
     *      specification ?12.6.2.6</a>
     */
    @Override
    public Object intercept(final EasyBeansInvocationContext invocationContext) throws Exception {
        logger.debug("Calling Never TX interceptor");

        // Get current transaction
        Transaction transaction;
        try {
            transaction = getTransactionManager().getTransaction();
        } catch (SystemException se) {
            throw new EJBException("Cannot get the current transaction on transaction manager.", se);
        }

        logger.debug("Transaction found = {0}", transaction);

        /*
         * If the client calls with a transaction context, the container throws
         * the javax.ejb.EJBException[ 55]. If the EJB 2.1 client view is used,
         * the container throws the java.rmi.RemoteException exception if the
         * client is a remote client, or the javax.ejb.EJBException if the
         * client is a local client.
         */
        if (transaction != null) {
            logger.warn("Mandatory as transaction attribute but within a transaction.");
            throw new EJBException("Client should not call within a transaction context by using NEVER mode.");
        }
        // else
        /*
         * If the client calls without a transaction context, the container
         * performs the same steps as described in the NOT_SUPPORTED case.
         */
        try {
            return invocationContext.proceed();
        } catch (Exception e) {
            handleUnspecifiedTransactionContext(invocationContext, e);
            // Shouldn't come here
            return null;
        }
    }

}
