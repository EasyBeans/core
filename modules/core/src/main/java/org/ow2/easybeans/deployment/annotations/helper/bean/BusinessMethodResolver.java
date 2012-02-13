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
 * $Id: BusinessMethodResolver.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.deployment.annotations.helper.bean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.ow2.easybeans.deployment.metadata.ejbjar.EasyBeansEjbJarClassMetadata;
import org.ow2.easybeans.deployment.metadata.ejbjar.EasyBeansEjbJarMethodMetadata;
import org.ow2.util.ee.metadata.ejbjar.api.struct.IJLocal;
import org.ow2.util.ee.metadata.ejbjar.api.struct.IJRemote;
import org.ow2.util.log.Log;
import org.ow2.util.log.LogFactory;
import org.ow2.util.scan.api.metadata.structures.JMethod;

/**
 * This class resolves the business method for bean class by looking at the
 * interfaces.
 * @author Florent Benoit
 */
public final class BusinessMethodResolver {

    /**
     * Name of the method used in constructor.
     * This has to be ignored as this is never a business interface
     */
    public static final String CLASS_INIT = "<clinit>";

    /**
     * Ignore constructor "method" of interfaces.
     * Not a business interface.
     */
    public static final String CONST_INIT = "<init>";

    /**
     * Logger.
     */
    private static Log logger = LogFactory.getLog(BusinessMethodResolver.class);

    /**
     * Helper class, no public constructor.
     */
    private BusinessMethodResolver() {

    }

    /**
     * Found all business methods of a bean.<br>
     * A business method is a method from one of the local or remote interfaces.
     * @param classAnnotationMetadata class to analyze
     */
    public static void resolve(final EasyBeansEjbJarClassMetadata classAnnotationMetadata) {
        loop(classAnnotationMetadata, classAnnotationMetadata, new ArrayList<String>());
    }

    /**
     * While there are super interfaces, loop on them.
     * @param beanclassAnnotationMetadata the class that is analyzed
     * @param visitingclassAnnotationMetadata classes from where we get
     *        interfaces
     */
    private static void loop(final EasyBeansEjbJarClassMetadata beanclassAnnotationMetadata,
            final EasyBeansEjbJarClassMetadata visitingclassAnnotationMetadata, final List<String> visitedInterfaces) {
        // first, need to analyze all methods of interfaces used by this class
        // then, set them as business method

        // As the Bean class may not implement the interface, adds also the local and remote business interfaces
        List<String> businessInterfaces = new ArrayList<String>();
        // Add implemented interfaces
        businessInterfaces.addAll(Arrays.asList(visitingclassAnnotationMetadata.getInterfaces()));
        // Add local interfaces
        IJLocal localInterfaces = beanclassAnnotationMetadata.getLocalInterfaces();
        if (localInterfaces != null) {
            for (String itf : localInterfaces.getInterfaces()) {
                if (!businessInterfaces.contains(itf)) {
                    businessInterfaces.add(itf);
                }
            }
        }
        // Remote interfaces
        IJRemote remoteInterfaces = beanclassAnnotationMetadata.getRemoteInterfaces();
        if (remoteInterfaces != null) {
            for (String itf : remoteInterfaces.getInterfaces()) {
                if (!businessInterfaces.contains(itf)) {
                    businessInterfaces.add(itf);
                }
            }
        }

        for (String itf : businessInterfaces) {
            if (itf.startsWith("javax/ejb/") || itf.startsWith("java/io/Serializable")
                    || itf.startsWith("java/io/Externalizable")) {
                continue;
            }

            // Already analyzed
            if (visitedInterfaces.contains(itf)) {
                continue;
            }
            // add as visited
            visitedInterfaces.add(itf);

            // get meta data of the interface
            EasyBeansEjbJarClassMetadata itfMetadata = visitingclassAnnotationMetadata.getLinkedClassMetadata(itf);

            if (itfMetadata == null) {
                logger.warn("No class was found for interface {0}.", itf);
                continue;
            }

            // for each method of the interface, set the business method to true
            // in bean
            for (EasyBeansEjbJarMethodMetadata methodData : itfMetadata.getMethodMetadataCollection()) {
                JMethod itfMethod = methodData.getJMethod();

                // Ignore class init method
                if (itfMethod.getName().equals(CLASS_INIT) || itfMethod.getName().equals(CONST_INIT)) {
                    continue;
                }

                // take the method from the bean class
                EasyBeansEjbJarMethodMetadata beanMethod = beanclassAnnotationMetadata.getMethodMetadata(itfMethod);
                if (beanMethod == null) {
                    // TODO: I18n
                    throw new IllegalStateException("No method was found for method " + itfMethod + " in class "
                            + beanclassAnnotationMetadata.getClassName());
                }
                beanMethod.setBusinessMethod(true);
            }

            // loop again
            if (itfMetadata.getInterfaces() != null) {
                loop(beanclassAnnotationMetadata, itfMetadata, visitedInterfaces);
            }
        }
    }

}
