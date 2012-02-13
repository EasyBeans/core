/**
 * EasyBeans
 * Copyright (C) 2006-2008 Bull S.A.S.
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
 * $Id: InterfaceAnnotatedHelper.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.deployment.annotations.helper.bean;

import org.ow2.easybeans.deployment.annotations.exceptions.ResolverException;
import org.ow2.easybeans.deployment.metadata.ejbjar.EasyBeansEjbJarClassMetadata;
import org.ow2.util.ee.metadata.ejbjar.api.struct.IJLocal;
import org.ow2.util.ee.metadata.ejbjar.api.struct.IJRemote;
import org.ow2.util.ee.metadata.ejbjar.impl.struct.JLocal;
import org.ow2.util.ee.metadata.ejbjar.impl.struct.JRemote;

/**
 * Lookup the annotated interfaces of a class and report it to the class
 * metadata.
 * @author Florent Benoit
 */
public final class InterfaceAnnotatedHelper {

    /**
     * Helper class, no public constructor.
     */
    private InterfaceAnnotatedHelper() {
    }

    /**
     * Gets interface of a bean and report their types to the bean metadata.
     * @param sessionBean Session bean to analyze
     * @throws ResolverException if there is a failure in a resolver
     */
    public static void resolve(final EasyBeansEjbJarClassMetadata sessionBean) throws ResolverException {
        sessionBean.getEjbJarDeployableMetadata();

        // Local and remote interfaces of the bean.
        IJLocal currentLocalInterfaces = sessionBean.getLocalInterfaces();
        IJRemote currentRemoteInterfaces = sessionBean.getRemoteInterfaces();

        // Get all direct interfaces of the bean
        String[] interfaces = sessionBean.getInterfaces();
        for (String itf : interfaces) {

            EasyBeansEjbJarClassMetadata itfAnnotationMetadata = sessionBean.getLinkedClassMetadata(itf);

            // Interface was analyzed, try to see the type of the interface
            if (itfAnnotationMetadata != null) {
                // Report type of interface in the bean
                IJLocal jLocal = itfAnnotationMetadata.getLocalInterfaces();
                if (jLocal != null) {
                    if (currentLocalInterfaces == null) {
                        currentLocalInterfaces = new JLocal();
                        sessionBean.setLocalInterfaces(currentLocalInterfaces);
                    }
                    String itfName = itfAnnotationMetadata.getClassName();
                    if (!currentLocalInterfaces.getInterfaces().contains(itfName)) {
                        currentLocalInterfaces.addInterface(itfName);
                    }
                }

                // Report type of interface in the bean
                IJRemote jRemote = itfAnnotationMetadata.getRemoteInterfaces();
                if (jRemote != null) {
                    if (currentRemoteInterfaces == null) {
                        currentRemoteInterfaces = new JRemote();
                        sessionBean.setRemoteInterfaces(currentRemoteInterfaces);
                    }
                    String itfName = itfAnnotationMetadata.getClassName();
                    if (!currentRemoteInterfaces.getInterfaces().contains(itfName)) {
                        currentRemoteInterfaces.addInterface(itfName);
                    }
                }
            }
        }
    }

}
