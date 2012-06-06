/**
 * EasyBeans
 * Copyright (C) 2012 Bull S.A.S.
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
 * $Id$
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.transaction.interceptors;

import java.util.List;

import javax.ejb.EJBException;
import javax.transaction.SystemException;
import javax.transaction.Transaction;

import org.ow2.easybeans.api.EZBStatefulSessionFactory;
import org.ow2.easybeans.api.EasyBeansInvocationContext;
import org.ow2.easybeans.api.bean.EasyBeansSFSB;
import org.ow2.easybeans.persistence.api.EZBExtendedEntityManager;

/**
 * Interceptor used to join the current TX for extended persistence contexts.
 * @author Florent Benoit
 */
public class ExtendedPersistenceContextInterceptor extends AbsTransactionInterceptor {

    /**
     * Register the extended persistence contexts on the current transaction (if any).
     * @param invocationContext context with useful attributes on the current
     *        invocation
     * @return result of the next invocation (to chain interceptors).
     * @throws Exception if interceptor fails
     * @see <a href="http://www.jcp.org/en/jsr/detail?id=220">EJB 3.0
     *      specification ?12.6.2.2</a>
     */
    @SuppressWarnings("unchecked")
    @Override
    public Object intercept(final EasyBeansInvocationContext invocationContext) throws Exception {

        EZBStatefulSessionFactory<EasyBeansSFSB, Long> statefulSessionFactory
        = (EZBStatefulSessionFactory<EasyBeansSFSB, Long>) invocationContext.getFactory();

        // Do we have extended persistence contexts that are registered ?
        List<EZBExtendedEntityManager> extendedPersistenceContexts = statefulSessionFactory
                .getExtendedPersistenceContexts(((EasyBeansSFSB) invocationContext.getTarget()));

        // No extended persistence contexts, proceed the next invocation
        if (extendedPersistenceContexts == null || extendedPersistenceContexts.isEmpty()) {
            return invocationContext.proceed();
        }

        // Get current transaction
        Transaction transaction;
        try {
            transaction = getTransactionManager().getTransaction();
        } catch (SystemException se) {
            throw new EJBException("Cannot get the current transaction on transaction manager.", se);
        }

        // The extended persistence context should join the current transaction.
        if (transaction != null) {
            for (EZBExtendedEntityManager extendedEntityManager : extendedPersistenceContexts) {
                extendedEntityManager.joinTransaction();
            }
        }

        // Proceed the next invocation
        return invocationContext.proceed();
    }

}
