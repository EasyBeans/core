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
 * $Id: MDBListenerBusinessMethodResolver.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.deployment.annotations.helper.bean.mdb;

import static org.ow2.easybeans.asm.Opcodes.ACC_PUBLIC;

import org.ow2.easybeans.deployment.metadata.ejbjar.EasyBeansEjbJarClassMetadata;
import org.ow2.easybeans.deployment.metadata.ejbjar.EasyBeansEjbJarMethodMetadata;
import org.ow2.util.scan.api.metadata.structures.IMethod;
import org.ow2.util.scan.impl.metadata.JMethod;

/**
 * This class finds the listener method of the MDB and mark it as a business
 * method so that it will be intercepted.
 * @author Florent Benoit
 */
public final class MDBListenerBusinessMethodResolver {

    /**
     * onMessage method.
     */
    private static final IMethod ONMESSAGE_METHOD = new JMethod(ACC_PUBLIC, "onMessage", "(Ljavax/jms/Message;)V", null, null);

    /**
     * Helper class, no public constructor.
     */
    private MDBListenerBusinessMethodResolver() {
    }

    /**
     * Mark listener method of the interface as business method.
     * @param sessionBean Session bean to analyze
     */
    public static void resolve(final EasyBeansEjbJarClassMetadata sessionBean) {
        //TODO: use of another interface than JMS

        // Set business method for onMessage Method (JMS)
        EasyBeansEjbJarMethodMetadata onMessageMethod = sessionBean.getMethodMetadata(ONMESSAGE_METHOD);
        if (onMessageMethod != null) {
            onMessageMethod.setBusinessMethod(true);
        }

    }
}
