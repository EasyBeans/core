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
 * $Id: AccessInterceptor.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.security.interceptors;

import java.util.Arrays;

import javax.ejb.EJBAccessException;
import javax.security.jacc.PolicyContext;

import org.ow2.easybeans.api.EZBPermissionManager;
import org.ow2.easybeans.api.EasyBeansInterceptor;
import org.ow2.easybeans.api.EasyBeansInvocationContext;
import org.ow2.easybeans.security.propagation.context.SecurityCurrent;


/**
 * This interceptor checks that the role is allowed to call the given method.
 * @author Florent Benoit
 */
public class AccessInterceptor implements EasyBeansInterceptor {


    /**
     * Grant access to the given method by checking roles.
     * @param invocationContext context with useful attributes on the current
     *        invocation
     * @return result of the next invocation (to chain interceptors)
     * @throws Exception if interceptor fails
     */
    public Object intercept(final EasyBeansInvocationContext invocationContext) throws Exception {
        String oldContextId = PolicyContext.getContextID();
        boolean accessGranted = true;
        boolean runAsBean = invocationContext.getFactory().getBeanInfo().getSecurityInfo().getRunAsRole() != null;
        try {
            EZBPermissionManager permissionManager = invocationContext.getFactory().getContainer().getPermissionManager();
            if (permissionManager != null) {
                accessGranted = permissionManager.checkSecurity(invocationContext, runAsBean);
            }
        } finally {
            PolicyContext.setContextID(oldContextId);
        }
        if (!accessGranted) {
            StringBuffer errMsg = new StringBuffer("Access Denied on bean '");
            errMsg.append(invocationContext.getFactory().getBeanInfo().getName());
            errMsg.append("' contained in the URL '");
            errMsg.append(invocationContext.getFactory().getContainer().getArchive());
            errMsg.append("'. ");
            errMsg.append(" Method = '");
            errMsg.append(invocationContext.getMethod());
            errMsg.append("'. ");
            errMsg.append("Current caller's principal is '");
            errMsg.append(SecurityCurrent.getCurrent().getSecurityContext().getCallerPrincipal(runAsBean));
            errMsg.append("' with roles '");
            errMsg.append(Arrays.asList(SecurityCurrent.getCurrent().getSecurityContext().getCallerRoles(runAsBean)));
            errMsg.append("'.");
            throw new EJBAccessException(errMsg.toString());
        }

        return invocationContext.proceed();
    }

}
