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
 * $Id: InheritanceMethodResolver.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.deployment.annotations.helper.bean;

import java.util.LinkedList;

import org.ow2.easybeans.asm.Opcodes;
import org.ow2.easybeans.asm.Type;
import org.ow2.easybeans.deployment.annotations.exceptions.ResolverException;
import org.ow2.easybeans.deployment.metadata.ejbjar.EasyBeansEjbJarClassMetadata;
import org.ow2.easybeans.deployment.metadata.ejbjar.EasyBeansEjbJarMethodMetadata;
import org.ow2.util.log.Log;
import org.ow2.util.log.LogFactory;
import org.ow2.util.scan.api.metadata.structures.JMethod;

/**
 * This class adds method meta data to bean class from the super class.<br>
 * @see <a href="http://www.jcp.org/en/jsr/detail?id=220">EJB 3.0 Spec ?4.6.2</a><br><p>
 *      A super class can't be a bean class (stateless, stateful, etc) so the
 *      method metadata don't need to be cloned</p>
 * @author Florent Benoit
 */
public final class InheritanceMethodResolver {

    /**
     * java.lang.object internal name.
     */
    private static final String JAVA_LANG_OBJECT = Type.getInternalName(Object.class);

    /**
     * Logger.
     */
    private static Log logger = LogFactory.getLog(InheritanceMethodResolver.class);


    /**
     * Helper class, no public constructor.
     */
    private InheritanceMethodResolver() {

    }

    /**
     * Found all method meta data of the super class and adds them to the class
     * being analyzed.
     * @param classAnnotationMetadata class to analyze
     * @throws ResolverException if the super class in not in the given ejb-jar
     */
    public static void resolve(final EasyBeansEjbJarClassMetadata classAnnotationMetadata) throws ResolverException {
        addMethodMetadata(classAnnotationMetadata, classAnnotationMetadata, 1);
    }

    /**
     * Adds method meta data on the first class by iterating on the second given
     * class.
     * @param beanclassAnnotationMetadata class where to add method metadata
     * @param visitingClassAnnotationMetadata takes method metadata from super
     *        class of the given class
     * @throws ResolverException if a super class metadata is not found from
     *         ejb-jar
     */
    private static void addMethodMetadata(final EasyBeansEjbJarClassMetadata beanclassAnnotationMetadata,
            final EasyBeansEjbJarClassMetadata visitingClassAnnotationMetadata, int inheritanceLevel) throws ResolverException {


        getInvertedSuperClassesMetadata(beanclassAnnotationMetadata);


        // Analyze super classes of the given class
        String superClass = visitingClassAnnotationMetadata.getSuperName();

        if (superClass != null) {

            // If super class is java.lang.Object, break the loop
            if (superClass.equals(JAVA_LANG_OBJECT)) {
                return;
            }

            // Get meta data of the super class
            EasyBeansEjbJarClassMetadata superClassMetadata = beanclassAnnotationMetadata.getLinkedClassMetadata(superClass);

            if (superClassMetadata == null) {
                // TODO : I18n
                throw new ResolverException("The class " + beanclassAnnotationMetadata + " extends the class " + superClass
                        + "but this class seems to be outside of the ejb-jar");
            }

            // Takes method metadata of the super class and adds them to the
            // bean class
            // Note : the flag inherited is set to true

            EasyBeansEjbJarMethodMetadata[] methodsToAnalyze = superClassMetadata.getMethodMetadataCollection().toArray(new EasyBeansEjbJarMethodMetadata[superClassMetadata.getMethodMetadataCollection().size()]);
            for (EasyBeansEjbJarMethodMetadata methodAnnotationMetadata : methodsToAnalyze) {

                // check that the method has not be redefined
                JMethod method = methodAnnotationMetadata.getJMethod();

                EasyBeansEjbJarMethodMetadata beanMethod = beanclassAnnotationMetadata.getMethodMetadata(method);

                // overriding ?
                boolean superMethodIsPrivate = ((method.getAccess() & Opcodes.ACC_PRIVATE) == Opcodes.ACC_PRIVATE);

                // Handle case of super method which is private and we
                // already have the method, so we need to add a generated method on the super class
                if (beanMethod != null && superMethodIsPrivate && !visitingClassAnnotationMetadata.isBean()) {


                    // Add a clone of the method to super class
                    EasyBeansEjbJarMethodMetadata clonedMethodAnnotationMetadata =
                        (EasyBeansEjbJarMethodMetadata) methodAnnotationMetadata.clone();

                    // Change metadata method name
                    JMethod oldMethod = clonedMethodAnnotationMetadata.getJMethod();
                    JMethod newMethod = new JMethod(Opcodes.ACC_PUBLIC, oldMethod.getName()
                            + superClassMetadata.getClassName().replace("/", ""), oldMethod.getDescriptor(), oldMethod
                            .getSignature(), oldMethod.getExceptions());
                    // update metadata
                    clonedMethodAnnotationMetadata.setJMethod(newMethod);
                    clonedMethodAnnotationMetadata.setPrivateSuperCallGenerated(true, superClassMetadata, 0);
                    clonedMethodAnnotationMetadata.setSuperPrivateMethodName(oldMethod.getName());

                    // Add the method
                    superClassMetadata.addStandardMethodMetadata(clonedMethodAnnotationMetadata);

                    // Update current parameters
                    methodAnnotationMetadata = clonedMethodAnnotationMetadata;
                    beanMethod = beanclassAnnotationMetadata.getMethodMetadata(newMethod);

                }


                // Add only if it is not present and super method is not private (else if super method is private, this is not an override)
                // Or if present and super method is private
                if (beanMethod == null  || (beanMethod != null && superMethodIsPrivate)) {

                    // Add a clone of the method to bean class
                    EasyBeansEjbJarMethodMetadata clonedMethodAnnotationMetadata =
                        (EasyBeansEjbJarMethodMetadata) methodAnnotationMetadata.clone();
                    // set new class linked to this method metadata
                    clonedMethodAnnotationMetadata
                            .setClassMetadata(beanclassAnnotationMetadata);

                    // method is inherited
                    clonedMethodAnnotationMetadata.setPrivateSuperCallGenerated(false, null, 0);
                    clonedMethodAnnotationMetadata.setSuperPrivateMethodName(null);
                    clonedMethodAnnotationMetadata.setInherited(true, superClassMetadata);

                    // Final method ? ignore it
                    /*if ((method.getAccess() & Opcodes.ACC_FINAL) == Opcodes.ACC_FINAL) {
                        logger.warn("Ignoring final method ''{0}'' from the class ''{1}''", method.getName(),
                                beanclassAnnotationMetadata.getClassName());
                        clonedMethodAnnotationMetadata.setIgnored(true);
                    }*/

                    // add only if overrided
                    if (beanMethod == null) {
                        beanclassAnnotationMetadata
                            .addStandardMethodMetadata(clonedMethodAnnotationMetadata);
                    }

                    // lifecycle / aroundInvoke
                    if (clonedMethodAnnotationMetadata.isPostConstruct()) {
                        beanclassAnnotationMetadata.addPostConstructMethodMetadata(clonedMethodAnnotationMetadata);
                    }
                    if (clonedMethodAnnotationMetadata.isPreDestroy()) {
                        beanclassAnnotationMetadata.addPreDestroyMethodMetadata(clonedMethodAnnotationMetadata);
                    }
                    if (clonedMethodAnnotationMetadata.isPostActivate()) {
                        beanclassAnnotationMetadata.addPostActivateMethodMetadata(clonedMethodAnnotationMetadata);
                    }
                    if (clonedMethodAnnotationMetadata.isPrePassivate()) {
                        beanclassAnnotationMetadata.addPrePassivateMethodMetadata(clonedMethodAnnotationMetadata);
                    }
                    if (clonedMethodAnnotationMetadata.isAroundInvoke()) {
                        beanclassAnnotationMetadata.addAroundInvokeMethodMetadata(clonedMethodAnnotationMetadata);
                    }
                }
            }

            // Loop again
            addMethodMetadata(beanclassAnnotationMetadata, superClassMetadata, ++inheritanceLevel);

        }
    }

    /**
     * Gets the inverted list of metadata for a given class (super class is the first one in the list).
     * @param classAnnotationMetadata the class to analyze
     * @return the given list
     */
    private static LinkedList<String> getInvertedSuperClassesMetadata(
            final EasyBeansEjbJarClassMetadata classAnnotationMetadata) {

        // get list of super classes
        LinkedList<String> superClassesList = new LinkedList<String>();
        String superClassName = classAnnotationMetadata.getSuperName();
        // loop while super class is not java.lang.Object
        while (!JAVA_LANG_OBJECT.equals(superClassName)) {
            EasyBeansEjbJarClassMetadata superMetaData = classAnnotationMetadata.getLinkedClassMetadata(superClassName);
            if (superMetaData != null) {
                superClassName = superMetaData.getSuperName();
                superClassesList.addFirst(superMetaData.getClassName());
            } else {
                superClassName = JAVA_LANG_OBJECT;
            }
        }
        superClassesList.addLast(classAnnotationMetadata.getClassName());
        return superClassesList;
    }

}
