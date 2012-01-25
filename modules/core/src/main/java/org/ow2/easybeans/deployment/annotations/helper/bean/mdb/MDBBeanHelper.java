/**
 * EasyBeans
 * Copyright (C) 2009 Bull S.A.S.
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
 * $Id: MDBBeanHelper.java 5859 2011-04-21 14:13:24Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.deployment.annotations.helper.bean.mdb;

import static org.ow2.easybeans.asm.Opcodes.ACC_PUBLIC;
import static org.ow2.easybeans.deployment.annotations.helper.ResolverHelper.getMethod;

import java.util.List;

import org.ow2.easybeans.deployment.annotations.exceptions.ResolverException;
import org.ow2.easybeans.deployment.metadata.ejbjar.EasyBeansEjbJarClassMetadata;
import org.ow2.easybeans.deployment.metadata.ejbjar.EasyBeansEjbJarMethodMetadata;
import org.ow2.util.ee.metadata.ejbjar.api.struct.IJMessageDriven;
import org.ow2.util.ee.metadata.ejbjar.impl.struct.JMessageDriven;
import org.ow2.util.log.Log;
import org.ow2.util.log.LogFactory;
import org.ow2.util.scan.api.metadata.structures.JMethod;

/**
 * Helper class for MDB.
 * @author Florent Benoit
 */
public final class MDBBeanHelper {

    /**
     * MDB lifecylce Methods that a bean can defined.
     */
    private static final String MDB_METHODS = "ejbCreate()/ejbRemove() methods";

    /**
     * Logger.
     */
    private static Log logger = LogFactory.getLog(MDBBeanHelper.class);

    /**
     * ejbRemove() method.
     */
    private static final JMethod EJBCREATE_METHOD = new JMethod(ACC_PUBLIC, "ejbCreate",
            "()V", null, new String[] {"javax/ejb/CreateException"});

    /**
     * ejbRemove() method.
     */
    private static final JMethod EJBREMOVE_METHOD = new JMethod(ACC_PUBLIC, "ejbRemove",
            "()V", null, new String[] {"javax/ejb/EJBException", "java/rmi/RemoteException"});

    /**
     * Utility class.
     */
    private MDBBeanHelper() {

    }

    /**
     * Apply all helper.
     * @param mdbBean MDB bean to analyze
     * @throws ResolverException if there is a failure in a resolver
     */
    public static void resolve(final EasyBeansEjbJarClassMetadata mdbBean) throws ResolverException {

        // Resolver for the javax.ejb.MessageDrivenBean interface
        MessageDrivenBeanInterface.resolve(mdbBean);

        // ejbCreate() method
        EasyBeansEjbJarMethodMetadata ejbCreateMethod = getMethod(mdbBean, EJBCREATE_METHOD, true, MDB_METHODS, false);
        if (ejbCreateMethod != null) {
            ejbCreateMethod.setPostConstruct(true);
            if (!mdbBean.getPostConstructMethodsMetadata().contains(ejbCreateMethod)) {
                mdbBean.addPostConstructMethodMetadata(ejbCreateMethod);
            }
        }



        // ejbRemove() method
        EasyBeansEjbJarMethodMetadata ejbRemoveMethod = getMethod(mdbBean, EJBREMOVE_METHOD, true, MDB_METHODS, false);
        if (ejbRemoveMethod != null) {
            ejbRemoveMethod.setPreDestroy(true);
            if (!mdbBean.getPreDestroyMethodsMetadata().contains(ejbRemoveMethod)) {
                mdbBean.addPreDestroyMethodMetadata(ejbRemoveMethod);
            }
        }


        // Listener interface already specified ?
        IJMessageDriven jMessageDriven = mdbBean.getJMessageDriven();
        if (jMessageDriven == null) {
            jMessageDriven = new JMessageDriven();
        }
        // Listener interface specified, no need to find it
        if (jMessageDriven.getMessageListenerInterface() != null) {
            return;
        }


        // The following interfaces are excluded when determining whether
        // the bean class has
        // more than one interface: java.io.Serializable;
        // java.io.Externalizable;
        // any of the interfaces defined by the javax.ejb package.
        String[] interfaces = mdbBean.getInterfaces();
        List<String> inheritedInterfaces = mdbBean.getInheritedInterfaces();

        int numberItfFound = 0;
        String itfFound = null;
        for (String itf : interfaces) {
            if (!itf.equals(java.io.Serializable.class.getName().replace(".", "/"))
                    && !itf.equals(java.io.Externalizable.class.getName().replace(".", "/")) && !itf.startsWith("javax/ejb")
                    // Should not be inherited
                    && !inheritedInterfaces.contains(itf)) {
                itfFound = itf;
                numberItfFound++;
            }
        }

        // No Listener interface found but there is only one inherited
        // interface, use it as listener interface
        if (numberItfFound == 0 && inheritedInterfaces != null && inheritedInterfaces.size() == 1) {
            itfFound = inheritedInterfaces.get(0);
            numberItfFound = 1;
        }

        // No Listener interface found in metadata
        if (numberItfFound == 0) {
            throw new IllegalStateException("No message listener interface has been found on bean class '"
                    + mdbBean.getClassName() + "' It needs to be specified with annotation or XML.");
        }

        if (numberItfFound > 1) {
            throw new IllegalStateException("More than 1 message listener interface on the class '" + mdbBean.getClassName()
                    + "' The message listener interface needs to be specified with annotation or XML..");
        }

        // If bean class implements a single interface, that interface is
        // assumed to be the listener interface of the bean
        jMessageDriven.setMessageListenerInterface(itfFound.replace("/", "."));

    }

}
