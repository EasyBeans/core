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

package org.ow2.easybeans.deployment.annotations.helper.bean.session;

import static org.ow2.easybeans.asm.Opcodes.ACC_PUBLIC;
import static org.ow2.easybeans.deployment.annotations.helper.ResolverHelper.getMethod;

import org.ow2.easybeans.deployment.metadata.ejbjar.EasyBeansEjbJarClassMetadata;
import org.ow2.easybeans.deployment.metadata.ejbjar.EasyBeansEjbJarMethodMetadata;
import org.ow2.util.scan.api.metadata.structures.IMethod;
import org.ow2.util.scan.impl.metadata.JMethod;

/**
 * For stateful session beans, flag methods that are part of session synchronization.
 * @author Florent Benoit
 */
public final class SessionSynchronizationResolver {

    /**
     * javax.ejb.SessionSynchronization interface.
     */
    private static final String SESSION_SYNCHRONIZATION_INTERFACE = "javax/ejb/SessionSynchronization";


    /**
     * afterBegin() method.
     */
    private static final IMethod AFTER_BEGIN_METHOD = new JMethod(ACC_PUBLIC, "afterBegin",
            "()V", null, new String[] {"javax/ejb/EJBException", "java/rmi/RemoteException"});


    /**
     * beforeCompletion() method.
     */
    private static final IMethod BEFORE_COMPLETION_METHOD = new JMethod(ACC_PUBLIC, "beforeCompletion",
            "()V", null, new String[] {"javax/ejb/EJBException", "java/rmi/RemoteException"});

    /**
     * afterCompletion() method.
     */
    private static final IMethod AFTER_COMPLETION_METHOD = new JMethod(ACC_PUBLIC, "afterCompletion",
            "(Z)V", null, new String[] {"javax/ejb/EJBException", "java/rmi/RemoteException"});


    /**
     * Helper class, no public constructor.
     */
    private SessionSynchronizationResolver() {
    }

    /**
     * Flag methods that are part of session synchronization.
     * @param statefulSessionBean Session bean to analyze
     */
    public static void resolve(final EasyBeansEjbJarClassMetadata statefulSessionBean) {

        // Do nothing
        if (!statefulSessionBean.isStateful()) {
            return;
        }

        // Check if bean is implenting the interface
        boolean implementsSessionSynchronization = false;
        String[] interfaces = statefulSessionBean.getInterfaces();
        if (interfaces != null) {
            for (String itf : interfaces) {
                if (SESSION_SYNCHRONIZATION_INTERFACE.equals(itf)) {
                    implementsSessionSynchronization = true;
                    break;

                }
            }
        }

        // Flag methods of the interface
        if (implementsSessionSynchronization) {
            // afterBegin method
            EasyBeansEjbJarMethodMetadata afterBeginMethod = getMethod(statefulSessionBean, AFTER_BEGIN_METHOD, true,
                    SESSION_SYNCHRONIZATION_INTERFACE);
            afterBeginMethod.setAfterBegin();

            // beforeCompletion method
            EasyBeansEjbJarMethodMetadata beforeCompletionMethod = getMethod(statefulSessionBean, BEFORE_COMPLETION_METHOD,
                    true, SESSION_SYNCHRONIZATION_INTERFACE);
            beforeCompletionMethod.setBeforeCompletion();

            // afterCompletion method
            EasyBeansEjbJarMethodMetadata afterCompletionMethod = getMethod(statefulSessionBean, AFTER_COMPLETION_METHOD, true,
                    SESSION_SYNCHRONIZATION_INTERFACE);
            afterCompletionMethod.setAfterCompletion();

        }
    }

}
