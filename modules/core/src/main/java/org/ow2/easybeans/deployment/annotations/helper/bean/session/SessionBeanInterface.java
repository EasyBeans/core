/**
 * EasyBeans
 * Copyright (C) 2006-2007 Bull S.A.S.
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
 * $Id: SessionBeanInterface.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.deployment.annotations.helper.bean.session;

import static org.ow2.easybeans.asm.Opcodes.ACC_PUBLIC;
import static org.ow2.easybeans.deployment.annotations.helper.ResolverHelper.getAllInterfacesFromClass;
import static org.ow2.easybeans.deployment.annotations.helper.ResolverHelper.getMethod;

import java.util.List;

import org.ow2.easybeans.deployment.metadata.ejbjar.EasyBeansEjbJarClassMetadata;
import org.ow2.easybeans.deployment.metadata.ejbjar.EasyBeansEjbJarMethodMetadata;
import org.ow2.util.ee.metadata.common.impl.struct.JAnnotationResource;
import org.ow2.util.scan.api.metadata.structures.JMethod;

/**
 * This class analyze interfaces of the session bean. If the session bean
 * implements javax.ejb.SessionBean interface, add lifecycle callbacks and add
 * resource injection for setSessionContext method.
 * @author Florent Benoit
 */
public final class SessionBeanInterface {

    /**
     * SessionBean interface.
     */
    private static final String SESSION_BEAN_INTERFACE = "javax/ejb/SessionBean";


    /**
     * setSessionContext() method.
     */
    private static final JMethod SETSESSIONCONTEXT_METHOD = new JMethod(ACC_PUBLIC, "setSessionContext",
            "(Ljavax/ejb/SessionContext;)V", null, new String[] {"javax/ejb/EJBException", "java/rmi/RemoteException"});


    /**
     * ejbRemove() method.
     */
    private static final JMethod EJBREMOVE_METHOD = new JMethod(ACC_PUBLIC, "ejbRemove",
            "()V", null, new String[] {"javax/ejb/EJBException", "java/rmi/RemoteException"});

    /**
     * ejbActivate() method.
     */
    private static final JMethod EJBACTIVATE_METHOD = new JMethod(ACC_PUBLIC, "ejbActivate",
            "()V", null, new String[] {"javax/ejb/EJBException", "java/rmi/RemoteException"});


    /**
     * ejbPassivate() method.
     */
    private static final JMethod EJBPASSIVATE_METHOD = new JMethod(ACC_PUBLIC, "ejbPassivate",
            "()V", null, new String[] {"javax/ejb/EJBException", "java/rmi/RemoteException"});


    /**
     * Helper class, no public constructor.
     */
    private SessionBeanInterface() {
    }

    /**
     * Try to see if bean implements javax.ejb.SessionBean interface.
     * @param sessionBean Session bean to analyze
     */
    public static void resolve(final EasyBeansEjbJarClassMetadata sessionBean) {
        // Make a list of interfaces
        List<String> allInterfaces = getAllInterfacesFromClass(sessionBean);

        // if SESSION_BEAN_INTERFACE is contained in the list, add some metadata
        if (allInterfaces.contains(SESSION_BEAN_INTERFACE)) {
            // first add dependency injection for setSessionContext method.
            JAnnotationResource jAnnotationResource = new JAnnotationResource();

            // add resource on setSessionContext method
            EasyBeansEjbJarMethodMetadata setCtxMethod = getMethod(sessionBean, SETSESSIONCONTEXT_METHOD, false,
                    SESSION_BEAN_INTERFACE);
            setCtxMethod.setJAnnotationResource(jAnnotationResource);


            // ejbRemove() method
            EasyBeansEjbJarMethodMetadata ejbRemoveMethod = getMethod(sessionBean, EJBREMOVE_METHOD, true, SESSION_BEAN_INTERFACE);
            ejbRemoveMethod.setPreDestroy(true);
            if (!sessionBean.getPreDestroyMethodsMetadata().contains(ejbRemoveMethod)) {
                sessionBean.addPreDestroyMethodMetadata(ejbRemoveMethod);
            }

            // ejbActivate() method
            EasyBeansEjbJarMethodMetadata ejbActivateMethod = getMethod(sessionBean, EJBACTIVATE_METHOD, true, SESSION_BEAN_INTERFACE);
            ejbRemoveMethod.setPostActivate(true);
            if (!sessionBean.getPostActivateMethodsMetadata().contains(ejbActivateMethod)) {
                sessionBean.addPostActivateMethodMetadata(ejbActivateMethod);
            }

            // ejbPassivate() method
            EasyBeansEjbJarMethodMetadata ejbPassivateMethod = getMethod(sessionBean, EJBPASSIVATE_METHOD, true,
                    SESSION_BEAN_INTERFACE);
            ejbRemoveMethod.setPrePassivate(true);
            if (!sessionBean.getPrePassivateMethodsMetadata().contains(ejbPassivateMethod)) {
                sessionBean.addPrePassivateMethodMetadata(ejbPassivateMethod);
            }

        }


    }



}
