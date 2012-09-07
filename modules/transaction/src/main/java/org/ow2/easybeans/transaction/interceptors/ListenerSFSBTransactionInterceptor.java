/*
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
 * $Id:$
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.transaction.interceptors;

import javax.transaction.Transaction;

import org.ow2.easybeans.api.EasyBeansInvocationContext;
import org.ow2.easybeans.api.bean.EasyBeansSFSB;
import org.ow2.easybeans.api.container.EZBSessionContext;
import org.ow2.easybeans.transaction.SFSBTransactionEndListener;

/**
 * This interceptor detects if the bean is under transaction an updates its metadata
 *
 * @author Loic Albertin
 */
public class ListenerSFSBTransactionInterceptor extends AbsTransactionInterceptor {
    @Override
    public Object intercept(EasyBeansInvocationContext invocationContext) throws Exception {
        // Get Bean and context
        EasyBeansSFSB statefulBean = (EasyBeansSFSB) invocationContext.getTarget();
        EZBSessionContext sessionContext = (EZBSessionContext) statefulBean.getEasyBeansContext();

        // First check if there is a BMT
        Transaction tx = sessionContext.getBeanTransaction();
        if (tx == null) {
            // Else check if there is a CMT
            tx = getTransactionManager().getTransaction();
        }
        if (tx != null) {
            statefulBean.setInTransaction(true);
            tx.registerSynchronization(new SFSBTransactionEndListener(statefulBean));
        }
        try {
            return invocationContext.proceed();
        } finally {
            if (tx == null) {
                // Check if a BMT started in this business method
                tx = getTransactionManager().getTransaction();
                if (tx != null) {
                    statefulBean.setInTransaction(true);
                    tx.registerSynchronization(new SFSBTransactionEndListener(statefulBean));
                }
            }
        }
    }
}
