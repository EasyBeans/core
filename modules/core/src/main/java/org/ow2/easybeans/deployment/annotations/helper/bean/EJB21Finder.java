/**
 * EasyBeans
 * Copyright (C) 2006-2009 Bull S.A.S.
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
 * $Id: EJB21Finder.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.deployment.annotations.helper.bean;

import java.util.ArrayList;
import java.util.List;

import org.ow2.easybeans.asm.Opcodes;
import org.ow2.easybeans.asm.Type;
import org.ow2.easybeans.deployment.metadata.ejbjar.EasyBeansEjbJarClassMetadata;
import org.ow2.easybeans.deployment.metadata.ejbjar.EasyBeansEjbJarMethodMetadata;
import org.ow2.easybeans.deployment.metadata.ejbjar.EjbJarArchiveMetadata;
import org.ow2.util.ee.metadata.ejbjar.impl.struct.JRemove;
import org.ow2.util.scan.api.metadata.structures.IMethod;
import org.ow2.util.scan.impl.metadata.JMethod;

/**
 * This class finds the business interface that are used as return type in the
 * Home or LocalHome interface of the Bean.
 * @author Florent Benoit
 */
public final class EJB21Finder {

    /**
     * Signature of the remove method.
     */
    private static final IMethod REMOVE_METHOD = new JMethod(Opcodes.ACC_PUBLIC, "remove", "()V", null,
            new String[] {"javax/ejb/RemoveException"});

    /**
     * Signature of the isIdentical(EJBObject) method.
     */
    private static final IMethod ISIDENTICAL_METHOD = new JMethod(Opcodes.ACC_PUBLIC, "isIdentical", "(Ljavax/ejb/EJBObject;)Z",
            null, new String[] {"java/rmi/RemoteException"});

    /**
     * Signature of the isIdentical(EJBLocalObject) method.
     */
    private static final IMethod ISIDENTICAL_LOCAL_METHOD = new JMethod(Opcodes.ACC_PUBLIC, "isIdentical",
            "(Ljavax/ejb/EJBLocalObject;)Z", null, new String[] {"javax/ejb/EJBException"});

    /**
     * Signature of the getHandle method.
     */
    private static final IMethod GETHANDLE_METHOD = new JMethod(Opcodes.ACC_PUBLIC, "getHandle", "()Ljavax/ejb/Handle;", null,
            new String[] {"java/rmi/RemoteException"});

    /**
     * Signature of the getHandle method.
     */
    private static final IMethod GETPRIMARYKEY_METHOD = new JMethod(Opcodes.ACC_PUBLIC, "getPrimaryKey", "()Ljava/lang/Object;",
            null, null);

    /**
     * Helper class, no public constructor.
     */
    private EJB21Finder() {
    }

    /**
     * Finds business method in a session bean.
     * @param bean the bean to analyze
     */
    public static void resolve(final EasyBeansEjbJarClassMetadata bean) {
        // Home and RemoteHome on the bean ?
        String remoteHome = bean.getRemoteHome();
        String localHome = bean.getLocalHome();

        // First, check if there is a home or local home interface (else do
        // nothing)
        if (remoteHome == null && localHome == null) {
            return;
        }

        // EJB-JAR
        EjbJarArchiveMetadata ejbJarAnnotationMetadata = bean.getEjbJarMetadata();
        // List of interfaces found in remote home interfaces.
        List<String> interfacesList = new ArrayList<String>();

        // Get List of Interfaces from remote home
        if (remoteHome != null) {
            getInterfacesFromHome(remoteHome, interfacesList, ejbJarAnnotationMetadata);
        }

        // Get List of Interfaces from Local home
        if (localHome != null) {
            getInterfacesFromHome(localHome, interfacesList, ejbJarAnnotationMetadata);
        }

        // And then, set business method found in the createXXX() methods in
        // home interfaces
        for (String itf : interfacesList) {
            // Get metadata of this interface
            EasyBeansEjbJarClassMetadata interfaceUsed = bean.getEasyBeansLinkedClassMetadata(itf);
            if (interfaceUsed == null) {
                throw new IllegalStateException("Cannot find the metadata for the class '" + itf
                        + "' referenced in the home/localhome of the bean '" + bean.getClassName() + "'.");
            }

            // Get all methods
            for (EasyBeansEjbJarMethodMetadata methodData : interfaceUsed.getMethodMetadataCollection()) {
                IMethod itfMethod = methodData.getJMethod();

                // Ignore class init method
                if (itfMethod.getName().equals(BusinessMethodResolver.CLASS_INIT)
                        || itfMethod.getName().equals(BusinessMethodResolver.CONST_INIT)) {
                    continue;
                }

                // take the method from the bean class
                EasyBeansEjbJarMethodMetadata beanMethod = bean.getMethodMetadata(itfMethod);
                if (beanMethod == null) {
                    throw new IllegalStateException("No method was found for method " + itfMethod + " in class "
                            + bean.getClassName());
                }
                beanMethod.setBusinessMethod(true);
            }

        }

        // Add remove method
        EasyBeansEjbJarMethodMetadata metadataRemove = bean.getMethodMetadata(REMOVE_METHOD);
        // not present ? add it
        if (metadataRemove == null) {
            metadataRemove = new EasyBeansEjbJarMethodMetadata(REMOVE_METHOD, bean);
            bean.addStandardMethodMetadata(metadataRemove);
        }

        // flag method as a remove method
        metadataRemove.setRemove(new JRemove());
        metadataRemove.setBusinessMethod(true);


        // Flag ejbXXX() method as business method (so interceptors are invoked)
        for (EasyBeansEjbJarMethodMetadata methodData : bean.getMethodMetadataCollection()) {
        	IMethod method = methodData.getJMethod();
        	if (method.getName().startsWith("ejbActivate") || method.getName().startsWith("ejbCreate")) {
        		if ("()V".equals(method.getDescriptor())) {
        			methodData.setBusinessMethod(true);
        		}
        	}
        }

        // Add isIdentical on EJBObject and EJBLocalObject
        bean.addStandardMethodMetadata(new EasyBeansEjbJarMethodMetadata(ISIDENTICAL_METHOD, bean));
        bean.addStandardMethodMetadata(new EasyBeansEjbJarMethodMetadata(ISIDENTICAL_LOCAL_METHOD, bean));

        // GetHandle
        bean.addStandardMethodMetadata(new EasyBeansEjbJarMethodMetadata(GETHANDLE_METHOD, bean));

        // GetPrimaryKey
        bean.addStandardMethodMetadata(new EasyBeansEjbJarMethodMetadata(GETPRIMARYKEY_METHOD, bean));

    }

    /**
     * Found interfaces specified on return type of the createXXX() method and
     * add them in the given list.
     * @param home the name of the class to analyze.
     * @param interfacesList the given list where to add interfaces found
     * @param ejbJarAnnotationMetadata the metatada where to get metadata
     */
    private static void getInterfacesFromHome(final String home, final List<String> interfacesList,
            final EjbJarArchiveMetadata ejbJarAnnotationMetadata) {

        String encodedname = home.replace(".", "/");

        // Get metadata of the given home interface
        EasyBeansEjbJarClassMetadata homeMetadata = ejbJarAnnotationMetadata.getScannedClassMetadata(encodedname);
        if (homeMetadata == null) {
            throw new IllegalStateException("Cannot find the class '" + home
                    + "' referenced as an home/localhome interface");
        }

        // Get methods
        for (EasyBeansEjbJarMethodMetadata method : homeMetadata.getMethodMetadataCollection()) {
            // if method name begins with "create", it's matching
            if (method.getMethodName().startsWith("create")) {
                // Get return type
                IMethod jMethod = method.getJMethod();
                Type returnType = Type.getReturnType(jMethod.getDescriptor());
                String returnTypeClassname = returnType.getClassName();
                // Not yet present in the list ? add it
                if (!interfacesList.contains(returnTypeClassname)) {
                    interfacesList.add(returnTypeClassname.replace(".", "/"));
                }
            }
        }

        // Parent is not EJBHome or EJBLocalHome then loop again on the super
        // name
        String[] interfaces = homeMetadata.getInterfaces();
        if (interfaces != null) {
            for (String itf : interfaces) {
                if (!"javax/ejb/EJBHome".equals(itf) && !"javax/ejb/EJBLocalHome".equals(itf)) {
                    getInterfacesFromHome(itf, interfacesList, ejbJarAnnotationMetadata);
                }
            }
        }
    }
}
