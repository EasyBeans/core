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

package org.ow2.easybeans.tests.embeddable;

import javax.ejb.Stateless;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;

/**
 * Dummy bean with an interceptor.
 * @author Florent Benoit
 */
@Stateless
public class BeanWithInterceptor {

    /**
     * Constant.
     */
    public static final String HAS_BEEN_REPLACED = "replaced";

    /**
     * Return the same value of the parameter.
     * @param test the value to return
     * @return the parameter input
     */
    public String getValue(final String test) {
        return test;
    }

    /**
     * Intercept the given method by replacing the arguments.
     * @param context the given interception context
     * @return next call
     * @throws Exception if fails
     */
    @AroundInvoke
    protected Object myInterceptor(final InvocationContext context) throws Exception {
        // replace content of the arguments
        context.setParameters(new String[] {HAS_BEEN_REPLACED});
        return context.proceed();
    }

}
