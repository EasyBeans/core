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
 * $Id: SingleMethodInterceptor.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.tests.enhancer.interceptors.business.bean;

import java.lang.reflect.Method;
import java.util.Map;

import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;

/**
 * Simple interceptor class which should be launched only on single method.
 * @author Florent Benoit
 */
public class SingleMethodInterceptor {

    /**
     * Test that the interceptor is only called on a stateless
     * bean and on singleMethodIntercepted method.
     * @param invocationContext contains attributes of invocation
     * @return method's invocation result
     * @throws Exception if invocation fails
     */
    @AroundInvoke
    public Object onlySingleMethod(final InvocationContext invocationContext) throws Exception {
        Method m = invocationContext.getMethod();
        // Change the returned value for add method
        if (!m.getName().equals("singleMethodIntercepted")) {
            throw new Exception("Interceptor should only be called on a single method");
        }

        // increment the counter
        Object o = invocationContext.getTarget();
        if (o instanceof StatelessBean) {
            StatelessBean bean = (StatelessBean) o;
            bean.incrementSingleMethodInterceptedCounter();
        } else {
            throw new Exception("Bean is not a stateless bean");
        }

        // Also, check that there is a key shared across all interceptors.
        Map<?, ?> contextData = invocationContext.getContextData();
        Object obj = contextData.get("KEY");
        if (obj == null) {
            throw new Exception("Objects are not propagated in the Map ContextData");
        }
        if (obj instanceof String) {
            String value = (String) obj;
            if (!value.equals("TEST")) {
                throw new Exception("Object value has been changed in the map ContextData.");
            }
        } else {
            throw new Exception("Object type has been changed in the map ContextData.");
        }

        return invocationContext.proceed();
    }

}
