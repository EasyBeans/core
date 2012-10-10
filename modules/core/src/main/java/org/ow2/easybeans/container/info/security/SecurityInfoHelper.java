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
 * $Id: SecurityInfoHelper.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.container.info.security;

import java.util.Collection;
import java.util.List;

import javax.security.jacc.EJBMethodPermission;

import org.ow2.easybeans.api.bean.info.IMethodSecurityInfo;
import org.ow2.easybeans.api.bean.info.ISecurityInfo;
import org.ow2.easybeans.deployment.metadata.ejbjar.EasyBeansEjbJarClassMetadata;
import org.ow2.easybeans.deployment.metadata.ejbjar.EasyBeansEjbJarMethodMetadata;

/**
 * Class that creates the runtime security info from the bean metadata.
 * @author Florent Benoit
 */
public final class SecurityInfoHelper {

    /**
     * Utility class, no public constructor.
     */
    private SecurityInfoHelper() {

    }

    /**
     * Extract security info from the bean's metadata.
     * @param bean the metadata of the current bean.
     * @return the security info.
     */
    public static ISecurityInfo getSecurityInfo(final EasyBeansEjbJarClassMetadata bean) {
        ISecurityInfo securityInfo = new SecurityInfo(bean);

        // Add each declared role
        securityInfo.setDeclaredRole(bean.getDeclareRoles());

        // Sets the run-as role.
        String runAsRole = bean.getRunAs();
        if (runAsRole != null) {
            securityInfo.setRunAsRole(runAsRole);
        }


        // For each business method, add info.
        Collection<? extends EasyBeansEjbJarMethodMetadata> methods = bean.getMethodMetadataCollection();
        // No methods, break now
        if (methods == null) {
            return securityInfo;
        }

        for (EasyBeansEjbJarMethodMetadata method : methods) {
            // Match only business method
            if (!method.isBusinessMethod()) {
                continue;
            }

            IMethodSecurityInfo methodSecurityInfo = new MethodSecurityInfo();
            securityInfo.addMethodSecurityInfo(methodSecurityInfo);

            // Set meta-info
            methodSecurityInfo.setExcluded(method.hasDenyAll());
            methodSecurityInfo.setUnchecked(method.hasPermitAll());
            List<String> roles = method.getRolesAllowed();
            if (roles != null) {
                for (String role : roles) {
                    methodSecurityInfo.addRole(role);
                }
            }

            // Build permission
            String ejbName;
            if (bean.isSession() || bean.isMdb()) {
                ejbName = bean.getJCommonBean().getName();
            } else {
                // ManagedBean
                ejbName = bean.getManagedBeanName();
            }
            String methodName = method.getMethodName();
            //TODO: fixme
            String methodInterface = null;
            //TODO: fixme
            String[] methodParams = null;

            EJBMethodPermission permission = new EJBMethodPermission(ejbName, methodName, methodInterface, methodParams);
            methodSecurityInfo.setPermission(permission);
        }

        return securityInfo;
    }
}
