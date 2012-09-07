/**
 * EasyBeans
 * Copyright (C) 2007 Bull S.A.S.
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
 * $Id: TimedObjectInterface.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.deployment.annotations.helper.bean;

import static org.ow2.easybeans.asm.Opcodes.ACC_PUBLIC;
import static org.ow2.easybeans.deployment.annotations.helper.ResolverHelper.getAllInterfacesFromClass;
import static org.ow2.easybeans.deployment.annotations.helper.ResolverHelper.getMethod;

import java.util.List;

import org.ow2.easybeans.deployment.metadata.ejbjar.EasyBeansEjbJarClassMetadata;
import org.ow2.easybeans.deployment.metadata.ejbjar.EasyBeansEjbJarMethodMetadata;
import org.ow2.util.scan.api.metadata.structures.JMethod;

/**
 * This class analyze interfaces of the given bean. If the session bean
 * implements javax.ejb.TimedObject interface, the ejbTimeout method is added as Timer method.
 * @author Florent Benoit
 */
public final class TimedObjectInterface {

    /**
     * TimedObject interface.
     */
    private static final String TIMEDOBJECT_INTERFACE = "javax/ejb/TimedObject";


    /**
     * ejbTimeout() method.
     */
    private static final JMethod EJBTIMEOUT_METHOD = new JMethod(ACC_PUBLIC, "ejbTimeout",
            "(Ljavax/ejb/Timer;)V", null, null);

    /**
     * Helper class, no public constructor.
     */
    private TimedObjectInterface() {
    }

    /**
     * Try to see if the bean is implementing javax.ejb.TimedObject interface.
     * @param bean the given bean to analyze
     */
    public static void resolve(final EasyBeansEjbJarClassMetadata bean) {
        // Make a list of interfaces
        List<String> allInterfaces = getAllInterfacesFromClass(bean);

        // if TIMEDOBJECT_INTERFACE is contained in the list, add some metadata
        if (allInterfaces.contains(TIMEDOBJECT_INTERFACE)) {

            // Flag the method as the timeout method
            EasyBeansEjbJarMethodMetadata timeoutMethod = getMethod(bean, EJBTIMEOUT_METHOD, true, TIMEDOBJECT_INTERFACE);
            timeoutMethod.setTimeout(true);
        }

        // Set as business method the @Schedule methods
        for (EasyBeansEjbJarMethodMetadata methodData : bean.getMethodMetadataCollection()) {
            if (methodData.getJavaxEjbSchedules() != null && methodData.getJavaxEjbSchedules().size() > 0) {
                methodData.setBusinessMethod(true);
            }
        }



    }

}
