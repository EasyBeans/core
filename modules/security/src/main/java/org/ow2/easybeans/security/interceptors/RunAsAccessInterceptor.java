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
 * $Id: RunAsAccessInterceptor.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.security.interceptors;

import javax.security.auth.Subject;

import org.ow2.easybeans.api.EasyBeansInterceptor;
import org.ow2.easybeans.api.EasyBeansInvocationContext;
import org.ow2.easybeans.security.propagation.context.SecurityCurrent;

/**
 * Push run-as subject for new calls.
 * @author Florent Benoit
 */
public class RunAsAccessInterceptor implements EasyBeansInterceptor {

    /**
     * Adds run-as role before invoking next methods.
     * @param invocationContext context with useful attributes on the current
     *        invocation
     * @return result of the next invocation (to chain interceptors)
     * @throws Exception if interceptor fails
     */
    public Object intercept(final EasyBeansInvocationContext invocationContext) throws Exception {
        Subject runAsSubject = invocationContext.getFactory().getBeanInfo().getSecurityInfo().getRunAsSubject();
        Subject previousSubject = SecurityCurrent.getCurrent().getSecurityContext().enterRunAs(runAsSubject);
        try {
            return invocationContext.proceed();
        } finally {
            SecurityCurrent.getCurrent().getSecurityContext().endsRunAs(previousSubject);
        }
    }
}
