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
 * $Id: ResolverHelper.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.deployment.annotations.helper;

import static org.ow2.easybeans.deployment.annotations.helper.bean.InheritanceInterfacesHelper.JAVA_LANG_OBJECT;

import java.util.ArrayList;
import java.util.List;

import org.ow2.easybeans.api.EZBServer;
import org.ow2.easybeans.deployment.annotations.exceptions.ResolverException;
import org.ow2.easybeans.deployment.annotations.helper.bean.BusinessMethodResolver;
import org.ow2.easybeans.deployment.annotations.helper.bean.EJB21Finder;
import org.ow2.easybeans.deployment.annotations.helper.bean.InheritanceInterfacesHelper;
import org.ow2.easybeans.deployment.annotations.helper.bean.InheritanceMethodResolver;
import org.ow2.easybeans.deployment.annotations.helper.bean.InterceptorsClassResolver;
import org.ow2.easybeans.deployment.annotations.helper.bean.InterfaceAnnotatedHelper;
import org.ow2.easybeans.deployment.annotations.helper.bean.SecurityResolver;
import org.ow2.easybeans.deployment.annotations.helper.bean.SessionBeanHelper;
import org.ow2.easybeans.deployment.annotations.helper.bean.TimedObjectInterface;
import org.ow2.easybeans.deployment.annotations.helper.bean.TransactionResolver;
import org.ow2.easybeans.deployment.annotations.helper.bean.checks.TimerBeanValidator;
import org.ow2.easybeans.deployment.annotations.helper.bean.mdb.MDBBeanHelper;
import org.ow2.easybeans.deployment.annotations.helper.bean.mdb.MDBListenerBusinessMethodResolver;
import org.ow2.easybeans.deployment.metadata.ejbjar.EasyBeansEjbJarClassMetadata;
import org.ow2.easybeans.deployment.metadata.ejbjar.EasyBeansEjbJarMethodMetadata;
import org.ow2.easybeans.deployment.metadata.ejbjar.EjbJarArchiveMetadata;
import org.ow2.util.scan.api.metadata.structures.JMethod;

/**
 * This class handle some steps that need to be done after the meta-data
 * generation.
 * @author Florent Benoit
 */
public final class ResolverHelper {

    /**
     * Helper class, no public constructor.
     */
    private ResolverHelper() {

    }

    /**
     * The helper will analyze datas of a given EjbJarAnnotationMetadata object.
     * @param ejbJarAnnotationMetadata object to analyze
     * @throws ResolverException if one of resolver fails
     */
    public static void resolve(final EjbJarArchiveMetadata ejbJarAnnotationMetadata, final EZBServer server) throws ResolverException {

        // For each bean class
        List<String> beanNames = ejbJarAnnotationMetadata.getBeanNames();
        for (String beanName : beanNames) {
            List<String> keys = ejbJarAnnotationMetadata.getClassesnameForBean(beanName);
            for (String key : keys) {
                EasyBeansEjbJarClassMetadata classMetaData =  ejbJarAnnotationMetadata.getClassForBean(beanName, key);

                if (classMetaData.isBean()) {

                    // Inheritance on interfaces
                    InheritanceInterfacesHelper.resolve(classMetaData);
                    InterfaceAnnotatedHelper.resolve(classMetaData);
                    InheritanceMethodResolver.resolve(classMetaData);

                    // Analyze EJB 2.1x home/localhome for finding interface used
                    EJB21Finder.resolve(classMetaData);

                    // Find business method
                    if (classMetaData.isSession()) {
                        BusinessMethodResolver.resolve(classMetaData);
                    } else if (classMetaData.isMdb()) {
                        MDBListenerBusinessMethodResolver.resolve(classMetaData);
                    }

                    // Security
                    SecurityResolver.resolve(classMetaData);

                    // Transaction
                    TransactionResolver.resolve(classMetaData);

                    // Interceptors
                    InterceptorsClassResolver.resolve(classMetaData, server);

                    // Check the timer methods (only one by bean) and if the signature is valid
                    TimedObjectInterface.resolve(classMetaData);
                    TimerBeanValidator.validate(classMetaData);

                }

                // for each bean, call sub helper
                if (classMetaData.isSession()) {
                    SessionBeanHelper.resolve(classMetaData);
                } else if (classMetaData.isMdb()) {
                    MDBBeanHelper.resolve(classMetaData);
                }
            }
        }
    }


    /**
     * Gets method metadata on the given class metadata for the given method.
     * @param bean the class metadata on which retrieve the method
     * @param jMethod the method to get
     * @param inherited get the correct method in super class, not inherited
     * @param interfaceName the name of the interface that the class should have
     * @param notFoundException if true, throws an exception if method is not present
     * @return the method metadata, else exception
     */
    public static EasyBeansEjbJarMethodMetadata getMethod(final EasyBeansEjbJarClassMetadata bean, final JMethod jMethod,
            final boolean inherited, final String interfaceName, final boolean notFoundException) {
        EasyBeansEjbJarMethodMetadata method = bean.getMethodMetadata(jMethod);
        if (method == null) {
            if (notFoundException) {
                throw new IllegalStateException("Bean '" + bean + "' implements " + interfaceName
                    + " but no " + jMethod + " method found in metadata");
            }
            return null;
        }
        // gets the correct method on the correct level. (not the inherited method) if we don't want the inherited method.
        if (method.isInherited() && !inherited) {
            String superClassName = bean.getSuperName();
            // loop while class is not java.lang.Object
            while (!JAVA_LANG_OBJECT.equals(superClassName)) {
                EasyBeansEjbJarClassMetadata superMetaData = bean.getLinkedClassMetadata(superClassName);
                // If the method is found in the super class and is not inherited, use this one
                if (superMetaData != null) {
                    EasyBeansEjbJarMethodMetadata superMethod = superMetaData.getMethodMetadata(jMethod);
                    if (superMethod != null && !superMethod.isInherited()) {
                        return superMethod;
                    }
                    superClassName = superMetaData.getSuperName();
                } else {
                    // break the loop
                    superClassName = JAVA_LANG_OBJECT;
                }
            }

        }

        return method;
    }


    /**
     * Gets method metadata on the given class metadata for the given method.
     * @param bean the class metadata on which retrieve the method
     * @param jMethod the method to get
     * @param inherited get the correct method in super class, not inherited
     * @param interfaceName the name of the interface that the class should have
     * @return the method metadata, else exception
     */
    public static EasyBeansEjbJarMethodMetadata getMethod(final EasyBeansEjbJarClassMetadata bean, final JMethod jMethod,
            final boolean inherited, final String interfaceName) {
        return getMethod(bean, jMethod, inherited, interfaceName, true);
    }


    /**
     * Gets all interfaces used by a class.
     * @param sessionBean the metadata to analyze.
     * @return the list of interfaces from a given class.
     */
    public static List<String> getAllInterfacesFromClass(final EasyBeansEjbJarClassMetadata sessionBean) {
        // build list
        List<String> allInterfaces = new ArrayList<String>();

        // Class to analyze
        String className = sessionBean.getClassName();

        // loop while class is not java.lang.Object
        while (!JAVA_LANG_OBJECT.equals(className)) {
            EasyBeansEjbJarClassMetadata metaData = sessionBean.getLinkedClassMetadata(className);
            // find metadata, all interfaces found
            if (metaData != null) {
                String[] interfaces = metaData.getInterfaces();
                if (interfaces != null) {
                    for (String itf : interfaces) {
                        allInterfaces.add(itf);
                    }
                }
                className = metaData.getSuperName();
            } else {
                // break the loop
                className = JAVA_LANG_OBJECT;
            }
        }
        return allInterfaces;
    }
}
