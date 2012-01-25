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
 * $Id: InheritanceInterfacesHelper.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.deployment.annotations.helper.bean;

import java.util.ArrayList;
import java.util.List;

import org.ow2.easybeans.deployment.annotations.exceptions.ResolverException;
import org.ow2.easybeans.deployment.metadata.ejbjar.EasyBeansEjbJarClassMetadata;
import org.ow2.util.ee.metadata.ejbjar.api.struct.IJLocal;
import org.ow2.util.ee.metadata.ejbjar.api.struct.IJRemote;
import org.ow2.util.ee.metadata.ejbjar.impl.struct.JLocal;
import org.ow2.util.ee.metadata.ejbjar.impl.struct.JRemote;

/**
 * Analyze classes and if there are super classes, set all super interfaces to
 * the current class.
 * @author Florent Benoit
 */
public final class InheritanceInterfacesHelper {

    /**
     * Defines java.lang.Object class.
     */
    public static final String JAVA_LANG_OBJECT = "java/lang/Object";

    /**
     * Helper class, no public constructor.
     */
    private InheritanceInterfacesHelper() {

    }

    /**
     * Found all method meta data of the super class and adds them to the bean's class.
     * Delegates to loop method.
     * @param classAnnotationMetadata bean' class to analyze.
     * @throws ResolverException if the super class in not in the given ejb-jar
     */
    public static void resolve(final EasyBeansEjbJarClassMetadata classAnnotationMetadata) throws ResolverException {
        loop(classAnnotationMetadata, classAnnotationMetadata);

    }


    /**
     * Found all method meta data of the super class and adds them to the bean's class.
     * @param beanClassAnnotationMetadata class where to report interfaces
     * @param visitingClassAnnotationMetadata class to analyze
     * @throws ResolverException if the super class in not in the given ejb-jar
     */
    public static void loop(final EasyBeansEjbJarClassMetadata beanClassAnnotationMetadata,
            final EasyBeansEjbJarClassMetadata visitingClassAnnotationMetadata) throws ResolverException {
          String superClass = visitingClassAnnotationMetadata.getSuperName();
        if (superClass != null) {
            // try to see if it was analyzed (and not java.lang.Object)
            if (!superClass.equals(JAVA_LANG_OBJECT)) {
                EasyBeansEjbJarClassMetadata superMetadata = beanClassAnnotationMetadata.getLinkedClassMetadata(superClass);

                if (superMetadata == null) {
                    throw new IllegalStateException("No super class named '" + superClass
                            + "' was analyzed. But it is referenced from '" + visitingClassAnnotationMetadata.getClassName()
                            + "'.");
                }

                // Add in a new list the existing interfaces
                List<String> newInterfacesLst = new ArrayList<String>();
                String[] currentInterfaces = beanClassAnnotationMetadata.getInterfaces();
                if (currentInterfaces != null) {
                    for (String itf : currentInterfaces) {
                        newInterfacesLst.add(itf);
                    }
                }

                // Add the interfaces of the super class only if there aren't
                // yet present
                String[] superInterfaces = superMetadata.getInterfaces();
                List<String> inheritedInterfaces = beanClassAnnotationMetadata.getInheritedInterfaces();

                if (superInterfaces != null) {
                    for (String itf : superInterfaces) {
                        if (!inheritedInterfaces.contains(itf) && !newInterfacesLst.contains(itf)) {
                            inheritedInterfaces.add(itf);
                        }

                        if (!newInterfacesLst.contains(itf)) {
                            newInterfacesLst.add(itf);
                        }


                    }
                }

                // Set the updated list.
                beanClassAnnotationMetadata.setInterfaces(newInterfacesLst.toArray(new String[newInterfacesLst.size()]));
                beanClassAnnotationMetadata.setInheritedInterfaces(inheritedInterfaces);

                // The local and remote interfaces need to be reported from the superclass to the current class.
                // Start with the local interfaces.
                IJLocal currentLocalInterfaces = beanClassAnnotationMetadata.getLocalInterfaces();
                IJLocal superLocalInterfaces = superMetadata.getLocalInterfaces();
                if (superLocalInterfaces != null) {
                    if (currentLocalInterfaces == null) {
                        currentLocalInterfaces = new JLocal();
                        beanClassAnnotationMetadata.setLocalInterfaces(currentLocalInterfaces);
                    }
                    for (String itf : superLocalInterfaces.getInterfaces()) {
                        if (!currentLocalInterfaces.getInterfaces().contains(itf)) {
                            currentLocalInterfaces.addInterface(itf);
                        }
                    }
                }

                // And then, with the remote interfaces
                IJRemote currentRemoteInterfaces = beanClassAnnotationMetadata.getRemoteInterfaces();
                IJRemote superRemoteInterfaces = superMetadata.getRemoteInterfaces();
                if (superRemoteInterfaces != null) {
                    if (currentRemoteInterfaces == null) {
                        currentRemoteInterfaces = new JRemote();
                        beanClassAnnotationMetadata.setRemoteInterfaces(currentRemoteInterfaces);
                    }
                    for (String itf : superRemoteInterfaces.getInterfaces()) {
                        if (!currentRemoteInterfaces.getInterfaces().contains(itf)) {
                            currentRemoteInterfaces.addInterface(itf);
                        }
                    }
                }

                // Loop again until java.lang.Object super class is found
                if (!superMetadata.getClassName().equals(JAVA_LANG_OBJECT)) {
                    loop(beanClassAnnotationMetadata, superMetadata);
                }
            }
        }
    }
}
