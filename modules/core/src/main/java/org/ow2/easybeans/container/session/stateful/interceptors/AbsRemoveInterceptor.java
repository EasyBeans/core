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
 * $Id: AbsRemoveInterceptor.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.container.session.stateful.interceptors;

import org.ow2.easybeans.api.EasyBeansInvocationContext;
import org.ow2.easybeans.api.Factory;
import org.ow2.easybeans.api.bean.EasyBeansSB;
import org.ow2.easybeans.container.session.SessionFactory;
import org.ow2.util.pool.api.Pool;
import org.ow2.util.pool.api.PoolException;


/**
 * Defines a method that discard the bean found in the invocation context.
 * @author Florent Benoit
 */
public abstract class AbsRemoveInterceptor {

    /**
     * Remove from the factory's pool the bean found in the current invocation context.
     * @param invocationContext the context of the current invocation
     * @throws PoolException if removal is failing
     */
    @SuppressWarnings("unchecked")
    protected void remove(final EasyBeansInvocationContext invocationContext) throws PoolException {
        // Gets the factory
        Factory factory = invocationContext.getFactory();

        // factory is a session factory.
        SessionFactory sessionFactory = (SessionFactory) factory;

        // get pool
        Pool<EasyBeansSB, Long> pool = sessionFactory.getPool();

        // Get the bean
        EasyBeansSB bean = (EasyBeansSB) invocationContext.getTarget();

        // discard instance
        pool.discard(bean);
    }
}
