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
 * $Id: InterceptorsClassResolver.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.deployment.annotations.helper.bean;

import static org.ow2.easybeans.deployment.annotations.helper.bean.InheritanceInterfacesHelper.JAVA_LANG_OBJECT;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import javax.ejb.Remove;

import org.ow2.easybeans.api.EZBServer;
import org.ow2.easybeans.api.EasyBeansInterceptor;
import org.ow2.easybeans.asm.Opcodes;
import org.ow2.easybeans.asm.Type;
import org.ow2.easybeans.container.session.stateful.interceptors.RemoveAlwaysInterceptor;
import org.ow2.easybeans.container.session.stateful.interceptors.RemoveOnlyWithoutExceptionInterceptor;
import org.ow2.easybeans.deployment.annotations.exceptions.ResolverException;
import org.ow2.easybeans.deployment.metadata.ejbjar.EasyBeansEjbJarClassMetadata;
import org.ow2.easybeans.deployment.metadata.ejbjar.EasyBeansEjbJarMethodMetadata;
import org.ow2.easybeans.deployment.metadata.ejbjar.EjbJarArchiveMetadata;
import org.ow2.easybeans.naming.interceptors.ENCManager;
import org.ow2.util.ee.metadata.common.api.struct.IJInterceptors;
import org.ow2.util.ee.metadata.ejbjar.api.IJClassInterceptor;
import org.ow2.util.ee.metadata.ejbjar.api.InterceptorType;
import org.ow2.util.ee.metadata.ejbjar.impl.JClassInterceptor;
import org.ow2.util.scan.api.metadata.structures.JMethod;

/**
 * This class sets the EasyBeans interceptors used when invoking business methods and also for life cycle events.
 * @author Florent Benoit
 */
public final class InterceptorsClassResolver {

    /**
     * Signature of EasyBeans interceptors.
     */
    private static final JMethod EASYBEANS_INTERCEPTOR = new JMethod(0, "intercept",
            "(Lorg/ow2/easybeans/api/EasyBeansInvocationContext;)Ljava/lang/Object;",
            null, new String[] {"java/lang/Exception"});


    /**
     * Helper class, no public constructor.
     */
    private InterceptorsClassResolver() {

    }


    /**
     * Found all interceptors of the class (including business and lifecycle events) and also set EasyBeans interceptors.
     * @param classAnnotationMetadata class to analyze
     * @param ezbServer server that is specifying config
     * @throws ResolverException if metadata is missing
     */
    public static void resolve(final EasyBeansEjbJarClassMetadata classAnnotationMetadata, final EZBServer ezbServer)
            throws ResolverException {

        // First, EasyBeans interceptors
        List<String> easyBeansInterceptorsClasses = new ArrayList<String>();
        // Add EasyBeans interceptors for all methods
        easyBeansInterceptorsClasses.add(Type.getInternalName(ENCManager.getInterceptorClass()));

        // Add other global interceptors
        if (ezbServer != null) {
            for (Class<? extends EasyBeansInterceptor> clazz : ezbServer.getGlobalInterceptorsClasses()) {
                easyBeansInterceptorsClasses.add(Type.getInternalName(clazz));
            }
        }

        // Set list of global interceptors (applied on all business methods)
        List<JClassInterceptor> easyBeansGlobalInterceptors = new ArrayList<JClassInterceptor>();
        for (String easyBeansInterceptor : easyBeansInterceptorsClasses) {
            easyBeansGlobalInterceptors.add(new JClassInterceptor(easyBeansInterceptor, EASYBEANS_INTERCEPTOR));
        }
        classAnnotationMetadata.setGlobalEasyBeansInterceptors(easyBeansGlobalInterceptors);


        // Default interceptors (only once as it is stored in the ejb metadata)
        EjbJarArchiveMetadata ejbJarDeployableMetadata = classAnnotationMetadata.getEjbJarDeployableMetadata();

        IJInterceptors defaultInterceptorsClasses = ejbJarDeployableMetadata.getDefaultInterceptorsClasses();
        Map<InterceptorType, List<? extends IJClassInterceptor>> mapDefaultInterceptors =
            ejbJarDeployableMetadata.getDefaultInterceptors();
        if (mapDefaultInterceptors == null && defaultInterceptorsClasses != null && defaultInterceptorsClasses.size() > 0) {
            Map<InterceptorType, List<? extends IJClassInterceptor>> defaultInterceptors =
                new HashMap<InterceptorType, List<? extends IJClassInterceptor>>();
            defaultInterceptors.putAll(getInterceptors(classAnnotationMetadata.getClassName(), classAnnotationMetadata,
                    defaultInterceptorsClasses.getClasses()));
            ejbJarDeployableMetadata.setDefaultInterceptors(defaultInterceptors);
        }


        // And then, set the user interceptors (found in external class)
        List<String> externalInterceptorsClasses = new ArrayList<String>();

        // Interceptors from other classes (will be analyzed after)
        // See 3.5.3 of simplified EJB 3.0 spec : multiple interceptors
        // Add interceptor classes found on super classes (if any)


        // Invert list of the inheritance on the current class
        LinkedList<EasyBeansEjbJarClassMetadata> invertedInheritanceClassesList =
            getInvertedSuperClassesMetadata(classAnnotationMetadata);
        // Add interceptors found on these classes (order is super super class
        // before super class, ie : top level first)
        for (EasyBeansEjbJarClassMetadata superMetaData : invertedInheritanceClassesList) {
            IJInterceptors classIinterceptors = superMetaData.getAnnotationInterceptors();
            if (classIinterceptors != null) {
                for (String cls : classIinterceptors.getClasses()) {
                    externalInterceptorsClasses.add(cls);
                }
            }
        }

        // Get the interceptors defined by the user on external classes
        Map<InterceptorType, List<? extends IJClassInterceptor>> externalMapClassInterceptors = new HashMap<InterceptorType, List<? extends IJClassInterceptor>>();
        externalMapClassInterceptors.putAll(getInterceptors(classAnnotationMetadata.getClassName(), classAnnotationMetadata,
                externalInterceptorsClasses));
        classAnnotationMetadata.setExternalUserInterceptors(externalMapClassInterceptors);


        // interceptor in the bean class ? (LifeCycle event interceptors are not in the bean
        // because they don't take an InvocationContext as parameter, this is the intercepted method
        List<String> internalInterceptorsClasses = new ArrayList<String>();

        if (classAnnotationMetadata.isAroundInvokeMethodMetadata()) {
            internalInterceptorsClasses.add(classAnnotationMetadata.getClassName());
        }
        // Get the interceptors defined by the user on the class
        Map<InterceptorType, List<? extends IJClassInterceptor>> internalMapClassInterceptors =
            new HashMap<InterceptorType, List<? extends IJClassInterceptor>>();
        internalMapClassInterceptors.putAll(getInterceptors(classAnnotationMetadata.getClassName(), classAnnotationMetadata,
                internalInterceptorsClasses));
        classAnnotationMetadata.setInternalUserInterceptors(internalMapClassInterceptors);

        // Now, analyze each interceptors found on methods.
        for (EasyBeansEjbJarMethodMetadata methodAnnotationMetaData
                : classAnnotationMetadata.getMethodMetadataCollection()) {

            // Set global interceptors for a given method (ie : Remove)
            Remove remove = methodAnnotationMetaData.getJRemove();
            if (remove != null) {
                List<JClassInterceptor> easyBeansMethodGlobalInterceptors = new ArrayList<JClassInterceptor>();
                String classType = null;
                // choose right interceptor class
                if (remove.retainIfException()) {
                    classType = Type.getInternalName(RemoveOnlyWithoutExceptionInterceptor.class);
                } else {
                    classType = Type.getInternalName(RemoveAlwaysInterceptor.class);
                }
                easyBeansMethodGlobalInterceptors.add(new JClassInterceptor(classType, EASYBEANS_INTERCEPTOR));

                // set list
                methodAnnotationMetaData.setGlobalEasyBeansInterceptors(easyBeansMethodGlobalInterceptors);
            }

            IJInterceptors methodAnnotationInterceptors = methodAnnotationMetaData.getAnnotationInterceptors();
            if (methodAnnotationInterceptors != null) {
                List<String> methodInterceptorsClasses = new ArrayList<String>();
                for (String cls : methodAnnotationInterceptors.getClasses()) {
                    methodInterceptorsClasses.add(cls);
                }
                Map<InterceptorType, List<IJClassInterceptor>> mapMethodInterceptors = getInterceptors(classAnnotationMetadata
                        .getClassName()
                        + "/Method " + methodAnnotationMetaData.getMethodName(), classAnnotationMetadata,
                        methodInterceptorsClasses);
                Map<InterceptorType, List<? extends IJClassInterceptor>> userInterceptors = new HashMap<InterceptorType, List<? extends IJClassInterceptor>>();
                userInterceptors.putAll(mapMethodInterceptors);
                methodAnnotationMetaData.setUserInterceptors(userInterceptors);
            }

        }

    }


    /**
     * Found interceptors method in the given class. It will analyze each
     * interceptor class and fill a structure with a mapping between the
     * annotation type and the corresponding interceptors.
     * @param referencingName name of the class/method that reference these
     *        interceptors
     * @param classMetadata the referencing classmetadata
     * @param interceptorsClasses list of classes that contains interceptors
     * @return the map between the type of interceptor (PostConstrut,
     *         AroundInvoke, ...) and the JClassInterceptor objects
     * @throws ResolverException if analyze fails
     */
    private static Map<InterceptorType, List<IJClassInterceptor>> getInterceptors(final String referencingName,
            final EasyBeansEjbJarClassMetadata classMetadata,
            final List<String> interceptorsClasses)
            throws ResolverException {
        // Define the mapping object.
        Map<InterceptorType, List<IJClassInterceptor>> mapInterceptors = new HashMap<InterceptorType, List<IJClassInterceptor>>();
        // Init the map for each interceptor type
        for (InterceptorType type : InterceptorType.values()) {
            mapInterceptors.put(type, new ArrayList<IJClassInterceptor>());
        }

        int interceptorClassAnalyzed = 0;

        // For each interceptors classes, take the method with @AroundInvoke or @PostConstruct, etc. and build list
        for (String className : interceptorsClasses) {
            EasyBeansEjbJarClassMetadata interceptorMetadata = classMetadata.getLinkedClassMetadata(className);
            if (interceptorMetadata == null) {
                throw new ResolverException("No medata for interceptor class " + className
                        + " referenced by " + referencingName);
            }

            // Another interceptor class
            interceptorClassAnalyzed++;

            // No inner class of the bean
            if (interceptorMetadata.getClassName().contains("$")) {
                throw new IllegalStateException("Interceptor can't be defined in an inner class.");
            }


            // Takes all methods of the super class and add them to the current class.
            InheritanceMethodResolver.resolve(interceptorMetadata);


            // Invert list of the inheritance on the current class
            LinkedList<EasyBeansEjbJarClassMetadata> invertedInheritanceClassesList =
                getInvertedSuperClassesMetadata(interceptorMetadata);

            // For each class (starting super class first, add the interceptor methods)
            for (EasyBeansEjbJarClassMetadata currentMetaData : invertedInheritanceClassesList) {

                // Analyze methods of the interceptor meta-data and add it in the map
                for (EasyBeansEjbJarMethodMetadata method : currentMetaData.getMethodMetadataCollection()) {
                    // Don't look inherited methods.
                    if (method.isInherited()) {
                        continue;
                    }
                    JClassInterceptor jInterceptor = new JClassInterceptor(className, method.getJMethod(),
                            interceptorClassAnalyzed);

                    // If the method is overriden, take care of using the
                    // annotation of the lower class in the inheritance classes.
                    // As the method is only add once for a single interceptor
                    // class.
                    EasyBeansEjbJarMethodMetadata analyzedMethod = method;
                    EasyBeansEjbJarMethodMetadata methodSubClass = findLastRedefinedMethod(method,
                            invertedInheritanceClassesList);
                    boolean superMethodIsPrivate = (analyzedMethod.getJMethod().getAccess()
                            & Opcodes.ACC_PRIVATE) == Opcodes.ACC_PRIVATE;
                    if (methodSubClass != null && !superMethodIsPrivate && !methodSubClass.isInherited()) {
                        analyzedMethod = methodSubClass;
                    }

                    // check if method is redefined in a super class

                    // Skip super method that we have also as it will be added in a generated method
                    if (methodSubClass != null && !methodSubClass.isInherited()
                            && methodSubClass.getClassMetadata().equals(interceptorMetadata) && superMethodIsPrivate
                            && !currentMetaData.equals(interceptorMetadata)
                            && !invertedInheritanceClassesList.contains(classMetadata)) {
                        continue;
                    }

                    // A method can be designed to be used for all annotation, so no "else if" !
                    if (analyzedMethod.isAroundInvoke()) {
                        addOnlyIfNotPresent(mapInterceptors.get(InterceptorType.AROUND_INVOKE), jInterceptor);
                    }

                    // Only if interceptor class is not a bean's class. Else, it is only simple methods.
                    // Also check if the interceptor class is not a super class of the bean
                    if (!currentMetaData.isBean() && !invertedInheritanceClassesList.contains(classMetadata)) {
                        // build interceptor object.
                        if (analyzedMethod.isPostActivate()) {
                            addOnlyIfNotPresent(mapInterceptors.get(InterceptorType.POST_ACTIVATE), jInterceptor);
                        }
                        if (analyzedMethod.isPostConstruct()) {
                            addOnlyIfNotPresent(mapInterceptors.get(InterceptorType.POST_CONSTRUCT), jInterceptor);
                        }
                        if (analyzedMethod.isPreDestroy()) {
                            addOnlyIfNotPresent(mapInterceptors.get(InterceptorType.PRE_DESTROY), jInterceptor);
                        }
                        if (analyzedMethod.isPrePassivate()) {
                            addOnlyIfNotPresent(mapInterceptors.get(InterceptorType.PRE_PASSIVATE), jInterceptor);
                        }
                    }
                }
            }

        }




        return mapInterceptors;
    }

    /**
     * Gets the method with the lower inheritance level defined in the class which is not inherited from a super class.
     * @param method the given method
     * @param classMetadatas the list of class to parse
     * @return a method if found
     */
    protected static EasyBeansEjbJarMethodMetadata findLastRedefinedMethod(final EasyBeansEjbJarMethodMetadata method,
            final LinkedList<EasyBeansEjbJarClassMetadata> classMetadatas) {
        ListIterator<EasyBeansEjbJarClassMetadata> it = classMetadatas.listIterator();
        EasyBeansEjbJarMethodMetadata foundMethod = null;
        // search method
        while (it.hasNext()) {
            EasyBeansEjbJarClassMetadata subClass = it.next();
            EasyBeansEjbJarMethodMetadata subMethod = subClass.getMethodMetadata(method.getJMethod());
            if (subMethod != null && !subMethod.isInherited()) {
                foundMethod = subMethod;
            }
        }
        if (foundMethod != null) {
            return foundMethod;
        }
        return null;
    }


    /**
     * Adds in the given interceptors list the interceptor object.
     * If the object is already present in the list, doesn't add it again.
     * @param interceptors the list of interceptors.
     * @param jInterceptor the interceptor to add.
     */
    private static void addOnlyIfNotPresent(final List<IJClassInterceptor> interceptors, final JClassInterceptor jInterceptor) {
        if (!interceptors.contains(jInterceptor)) {
            interceptors.add(jInterceptor);
        }
    }

    /**
     * Gets the inverted list of metadata for a given class (super class is the first one in the list).
     * @param classAnnotationMetadata the class to analyze
     * @return the given list
     */
    private static LinkedList<EasyBeansEjbJarClassMetadata> getInvertedSuperClassesMetadata(
            final EasyBeansEjbJarClassMetadata classAnnotationMetadata) {

        // get list of super classes
        LinkedList<EasyBeansEjbJarClassMetadata> superClassesList = new LinkedList<EasyBeansEjbJarClassMetadata>();
        String superClassName = classAnnotationMetadata.getSuperName();
        // loop while super class is not java.lang.Object
        while (!JAVA_LANG_OBJECT.equals(superClassName)) {
            EasyBeansEjbJarClassMetadata superMetaData = classAnnotationMetadata.getLinkedClassMetadata(superClassName);
            if (superMetaData != null) {
                superClassName = superMetaData.getSuperName();
                superClassesList.addFirst(superMetaData);
            } else {
                superClassName = JAVA_LANG_OBJECT;
            }
        }
        superClassesList.addLast(classAnnotationMetadata);
        return superClassesList;
    }

}
