/**
 * EasyBeans
 * Copyright (C) 2011 Bull S.A.S.
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
 * $Id: MessageDrivenBeanInterface.java 5859 2011-04-21 14:13:24Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.deployment.annotations.helper.bean.mdb;

import static org.ow2.easybeans.asm.Opcodes.ACC_PUBLIC;
import static org.ow2.easybeans.deployment.annotations.helper.ResolverHelper.getAllInterfacesFromClass;
import static org.ow2.easybeans.deployment.annotations.helper.ResolverHelper.getMethod;

import java.util.List;

import org.ow2.easybeans.deployment.metadata.ejbjar.EasyBeansEjbJarClassMetadata;
import org.ow2.easybeans.deployment.metadata.ejbjar.EasyBeansEjbJarMethodMetadata;
import org.ow2.util.ee.metadata.common.impl.struct.JAnnotationResource;
import org.ow2.util.scan.api.metadata.structures.JMethod;

/**
 * This class analyze interfaces of the MDB. If the MDB
 * implements javax.ejb.MessageDrivenBean interface, add lifecycle callbacks and add
 * resource injection for setMessageDrivenContext method.
 * @author Florent Benoit
 */
public class MessageDrivenBeanInterface {


    /**
     * javax.ejb.MessageDrivenBean interface.
     */
    private static final String MESSAGE_DRIVEN_BEAN_INTERFACE = "javax/ejb/MessageDrivenBean";

    /**
     * setMessageDrivenContext() method.
     */
    private static final JMethod SETMESSAGEDRIVENCONTEXT_METHOD = new JMethod(ACC_PUBLIC, "setMessageDrivenContext",
            "(Ljavax/ejb/MessageDrivenContext;)V", null, new String[] {"javax/ejb/EJBException"});


    /**
     * ejbRemove() method.
     */
    private static final JMethod EJBREMOVE_METHOD = new JMethod(ACC_PUBLIC, "ejbRemove",
            "()V", null, new String[] {"javax/ejb/EJBException"});


    /**
     * Helper class, no public constructor.
     */
    private MessageDrivenBeanInterface() {
    }

    /**
     * Try to see if bean implements javax.ejb.MessageDrivenBean interface.
     * @param messageDrivenBean Message Driven bean to analyze
     */
    public static void resolve(final EasyBeansEjbJarClassMetadata messageDrivenBean) {
        // Make a list of interfaces
        List<String> allInterfaces = getAllInterfacesFromClass(messageDrivenBean);

        // if MESSAGE_DRIVEN_BEAN_INTERFACE is contained in the list, add some metadata
        if (allInterfaces.contains(MESSAGE_DRIVEN_BEAN_INTERFACE)) {
            // first add dependency injection for setMessageDrivenContext method.
            JAnnotationResource jAnnotationResource = new JAnnotationResource();

            // add resource on setMessageDrivenContext method
            EasyBeansEjbJarMethodMetadata setCtxMethod = getMethod(messageDrivenBean, SETMESSAGEDRIVENCONTEXT_METHOD, false,
                    MESSAGE_DRIVEN_BEAN_INTERFACE);
            setCtxMethod.setJAnnotationResource(jAnnotationResource);


            // ejbRemove() method
            EasyBeansEjbJarMethodMetadata ejbRemoveMethod = getMethod(messageDrivenBean, EJBREMOVE_METHOD, true, MESSAGE_DRIVEN_BEAN_INTERFACE);
            ejbRemoveMethod.setPreDestroy(true);
            if (!messageDrivenBean.getPreDestroyMethodsMetadata().contains(ejbRemoveMethod)) {
                messageDrivenBean.addPreDestroyMethodMetadata(ejbRemoveMethod);
            }
        }


    }


}
