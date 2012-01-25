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
 * $Id: SecurityResolver.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.deployment.annotations.helper.bean;

import java.util.ArrayList;
import java.util.List;

import org.ow2.easybeans.asm.Type;
import org.ow2.easybeans.deployment.metadata.ejbjar.EasyBeansEjbJarClassMetadata;
import org.ow2.easybeans.deployment.metadata.ejbjar.EasyBeansEjbJarMethodMetadata;
import org.ow2.easybeans.security.interceptors.AccessInterceptor;
import org.ow2.easybeans.security.interceptors.DenyAllInterceptor;
import org.ow2.easybeans.security.interceptors.RunAsAccessInterceptor;
import org.ow2.util.ee.metadata.ejbjar.api.IJClassInterceptor;
import org.ow2.util.ee.metadata.ejbjar.impl.JClassInterceptor;
import org.ow2.util.scan.api.metadata.structures.JMethod;

/**
 * This class adds the interceptor for the security (if required) on a given method.
 * @author Florent Benoit
 */
public final class SecurityResolver {

    /**
     * Signature of EasyBeans interceptors.
     */
    private static final JMethod EASYBEANS_INTERCEPTOR = new JMethod(0, "intercept",
            "(Lorg/ow2/easybeans/api/EasyBeansInvocationContext;)Ljava/lang/Object;", null,
            new String[] {"java/lang/Exception"});

    /**
     * DenyAll interceptor.
     */
    private static final String DENYALL_INTERCEPTOR = Type
            .getInternalName(DenyAllInterceptor.class);


    /**
     * RunAs interceptor.
     */
    private static final String RUNAS_INTERCEPTOR = Type
            .getInternalName(RunAsAccessInterceptor.class);


    /**
     * Role based interceptor.
     */
    private static final String ROLEBASED_INTERCEPTOR = Type
            .getInternalName(AccessInterceptor.class);


    /**
     * Helper class, no public constructor.
     */
    private SecurityResolver() {
    }

    /**
     * Adds the right transaction interceptor depending of the transactional
     * attribute set by the user.
     * @param bean the given bean on which set the transactional interceptor.
     */
    public static void resolve(final EasyBeansEjbJarClassMetadata bean) {

        // Class values
        boolean beanPermitAll = bean.hasPermitAll();
        List<String> beanRolesAllowed = bean.getRolesAllowed();

        String runAs = bean.getRunAs();
        String superClassName = bean.getSuperName();
        // Search in super class
        while (runAs == null && !superClassName.equals(Type.getInternalName(Object.class))) {
            EasyBeansEjbJarClassMetadata superMetadata = bean.getLinkedClassMetadata(superClassName);
            if (superMetadata != null) {
                runAs = superMetadata.getRunAs();
                superClassName = superMetadata.getSuperName();
                // Set with the super class value
                if (runAs != null) {
                    bean.setRunAs(runAs);
                }
            }
        }




        // Inheritance for DeclaredRoles
        List<String> declaredRoles = bean.getDeclareRoles();
        superClassName = bean.getSuperName();
        // if null, search on super classes.
        while (declaredRoles == null && !superClassName.equals(Type.getInternalName(Object.class))) {
            EasyBeansEjbJarClassMetadata superMetadata = bean.getLinkedClassMetadata(superClassName);
            if (superMetadata != null) {
                declaredRoles = superMetadata.getDeclareRoles();
                superClassName = superMetadata.getSuperName();
                // Set with the super class value
                if (declaredRoles != null) {
                    bean.setDeclareRoles(declaredRoles);
                }
            }
        }


        for (EasyBeansEjbJarMethodMetadata method : bean.getMethodMetadataCollection()) {
            List<IJClassInterceptor> interceptors = new ArrayList<IJClassInterceptor>();

            List<? extends IJClassInterceptor> previousInterceptors = method.getInterceptors();
            if (previousInterceptors != null) {
                interceptors.addAll(previousInterceptors);
            }

            // DenyAll ?
            boolean denyAll = method.hasDenyAll();

            // PermitAll ?
            boolean permitAll = method.hasPermitAll();
            // not defined on the method, check inheritance or bean's value
            if (!permitAll) {
                if (method.isInherited()) {
                    permitAll = method.getOriginalClassMetadata().hasPermitAll();
                    method.setPermitAll(permitAll);
                } else {
                    permitAll = beanPermitAll;
                }
            }

            // roles allowed.
            List<String> rolesAllowed = method.getRolesAllowed();
            if (rolesAllowed == null) {
                if (method.isInherited()) {
                    rolesAllowed = method.getOriginalClassMetadata().getRolesAllowed();
                    method.setRolesAllowed(rolesAllowed);
                } else {
                    // Method roles are Bean's roles.
                    rolesAllowed = beanRolesAllowed;
                    // Update the method value
                    method.setRolesAllowed(beanRolesAllowed);
                }
            }

            // runAs ?
            if (runAs != null) {
                interceptors.add(new JClassInterceptor(RUNAS_INTERCEPTOR, EASYBEANS_INTERCEPTOR));
            }

            if (denyAll) {
                interceptors.add(new JClassInterceptor(DENYALL_INTERCEPTOR, EASYBEANS_INTERCEPTOR));
            } else if (!permitAll && rolesAllowed != null) {
                // only if permitAll is not set as no interceptor is added in this case
                interceptors.add(new JClassInterceptor(ROLEBASED_INTERCEPTOR, EASYBEANS_INTERCEPTOR));
            }
            method.setInterceptors(interceptors);
        }
    }
}
