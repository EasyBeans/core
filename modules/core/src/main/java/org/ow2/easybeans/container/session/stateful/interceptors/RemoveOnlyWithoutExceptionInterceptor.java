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
 * $Id: RemoveOnlyWithoutExceptionInterceptor.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.container.session.stateful.interceptors;

import org.ow2.easybeans.api.EasyBeansInterceptor;
import org.ow2.easybeans.api.EasyBeansInvocationContext;

/**
 * This class manages the removal of the stateful session bean but only if there was no exception.
 * @author Florent Benoit
 */
public class RemoveOnlyWithoutExceptionInterceptor extends AbsRemoveInterceptor  implements EasyBeansInterceptor {

    /**
     * Remove the current bean.
     * @param invocationContext context with useful attributes on the current
     *        invocation
     * @return result of the next invocation (to chain interceptors)
     * @throws Exception if interceptor fails
     * @see <a href="http://www.jcp.org/en/jsr/detail?id=220">EJB 3.0
     *      specification ?12.6.1</a>
     */
    public Object intercept(final EasyBeansInvocationContext invocationContext) throws Exception {
        // Object that will store the result of the proceed() method
        Object returnObject = null;

        // invoke the chain
        returnObject = invocationContext.proceed();

        // if there was no error, remove current bean
        // No remove in case of exception
        remove(invocationContext);

        // return invocation chain value
        return returnObject;
    }


}
