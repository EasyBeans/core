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
 * $Id: Interceptor.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.tests.enhancer.interceptors.business.bean;

import java.util.Map;

import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;


/**
 * Acts as an interceptor.
 * @author Florent Benoit
 */
public class Interceptor {

    /**
     * @param invocationContext contains attributes of invocation
     * @return method's invocation result
     * @throws Exception if invocation fails
     */
    @AroundInvoke
    @SuppressWarnings("unchecked")
    public Object outsideIntercepted(final InvocationContext invocationContext) throws Exception {
        Object o = invocationContext.getTarget();

        if (o instanceof StatelessBean) {
            StatelessBean bean = (StatelessBean) o;
            bean.incrementOtherInterceptorCounter();
        }

        // Add a key in the map
        Map contextData = invocationContext.getContextData();
        contextData.put("KEY", "TEST");

        return invocationContext.proceed();
    }
}
