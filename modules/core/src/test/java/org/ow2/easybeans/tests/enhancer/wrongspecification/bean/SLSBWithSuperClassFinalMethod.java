/**
 * EasyBeans
 * Copyright (C) 2009 Bull S.A.S.
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
 * $Id: SLSBWithSuperClassFinalMethod.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.tests.enhancer.wrongspecification.bean;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;

/**
 * Statelesss with a super class that has a final method.
 * @author Florent Benoit
 */
@Stateless
@Remote(ItfOneMethod.class)
public class SLSBWithSuperClassFinalMethod extends SuperClassBeanWithFinal implements ItfOneMethod {

    /**
     * Returns boolean.
     * @return true or false
     */
    public boolean getBool() {
        new Exception().printStackTrace();
        return true;
    }

    /**
     * Interceptor to check if class has been enhanced.
     * @param invocationContext
     * @throws Exception
     */
    @AroundInvoke
    protected Object intercept(final InvocationContext invocationContext) throws Exception {
        String methodName = invocationContext.getMethod().getName();
        if ("getBool".equals(methodName)) {
            return Boolean.FALSE;
        } else if ("dummyFinalMethod".equals(methodName)) {
            throw new IllegalStateException("Shouldn't have been intercepted !");
        }
        return invocationContext.proceed();
    }

}
