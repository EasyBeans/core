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
 * $Id: InterceptorsValidator.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.deployment.annotations.helper.bean.session.checks;

import static org.ow2.easybeans.asm.Opcodes.ACC_FINAL;
import static org.ow2.easybeans.asm.Opcodes.ACC_STATIC;
import static org.ow2.easybeans.deployment.annotations.helper.bean.checks.AccessChecker.ensureNoAccess;

import java.util.List;

import org.ow2.easybeans.deployment.annotations.exceptions.InterceptorsValidationException;
import org.ow2.easybeans.deployment.metadata.ejbjar.EasyBeansEjbJarClassMetadata;
import org.ow2.easybeans.deployment.metadata.ejbjar.EasyBeansEjbJarMethodMetadata;
import org.ow2.easybeans.deployment.metadata.ejbjar.EjbJarArchiveMetadata;
import org.ow2.util.ee.metadata.common.api.struct.IJInterceptors;
import org.ow2.util.scan.api.metadata.structures.JMethod;

/**
 * This class ensures that the interceptors have the correct signature.
 * @author Florent Benoit
 */
public final class InterceptorsValidator {

    /**
     * Signature for an AroundInvoke interceptor (InvocationContext).
     */
    private static final String AROUND_INVOKE_DESCRIPTOR_EJB = "(Ljavax/interceptor/InvocationContext;)Ljava/lang/Object;";

    /**
     * Signature for a lifecycle interceptor inside the bean (void type).
     */
    private static final String LIFECYCLE_DESCRIPTOR_OUTSIDEBEAN = "(Ljavax/interceptor/InvocationContext;)V";

    /**
     * Signature for a lifecycle interceptor inside the bean (void type).
     */
    private static final String LIFECYCLE_DESCRIPTOR_BEAN = "()V";

    /**
     * Exception required in AroundInvoke interceptor.
     */
    private static final String AROUND_INVOKE_EXCEPTION = "java/lang/Exception";

    /**
     * Constructor without args.
     */
    private static final String DEFAULT_CONSTRUCTOR_DESCRIPTOR = "()V";

    /**
     * Default constructor method's name.
     */
    private static final String CONSTRUCTOR_METHOD = "<init>";

    /**
     * Helper class, no public constructor.
     */
    private InterceptorsValidator() {
    }

    /**
     * Validate a bean.
     * @param bean bean to validate.
     */
    public static void validate(final EasyBeansEjbJarClassMetadata bean) {

        // Root metadata
        EjbJarArchiveMetadata ejbMetaData = bean.getEjbJarDeployableMetadata();

        // Interceptors in the bean
        if (bean.isBean()) {
            for (EasyBeansEjbJarMethodMetadata method : bean.getMethodMetadataCollection()) {

                // lifecycle
                if (method.isLifeCycleMethod()) {
                    validateJMethod(method.getJMethod(), LIFECYCLE_DESCRIPTOR_BEAN, null, bean.getClassName());
                } else if (method.isAroundInvoke()) {
                    validateJMethod(method.getJMethod(), AROUND_INVOKE_DESCRIPTOR_EJB, AROUND_INVOKE_EXCEPTION, bean
                            .getClassName());
                }

                // Interceptors defined on the bean's methods
                IJInterceptors methodInterceptors = method.getAnnotationInterceptors();
                // Look in the interceptor class
                if (methodInterceptors != null) {
                    for (String className : methodInterceptors.getClasses()) {
                        analyzeInterceptorClass(ejbMetaData, bean.getLinkedClassMetadata(className));
                    }
                }
            }

            // Now, check interceptors outside the bean
            IJInterceptors methodInterceptors = bean.getAnnotationInterceptors();
            // Look in the interceptor class
            if (methodInterceptors != null) {
                for (String className : methodInterceptors.getClasses()) {
                    analyzeInterceptorClass(ejbMetaData, bean.getLinkedClassMetadata(className));
                }
            }

            // Analyze interfaces and check that there are no methods with
            // @AroundInvoke or @PostConstruct
            String[] interfaces = bean.getInterfaces();
            if (interfaces != null) {
                for (String itf : interfaces) {
                    EasyBeansEjbJarClassMetadata interfaceMetaData = bean.getLinkedClassMetadata(itf);
                    if (interfaceMetaData != null) {
                        for (EasyBeansEjbJarMethodMetadata method : interfaceMetaData
                                .getMethodMetadataCollection()) {
                            // no AroundInvoke or PostConstruct
                            if (method.isAroundInvoke()) {
                                throw new InterceptorsValidationException("The method '" + method
                                        + "' in the bean class '" + bean.getClassName()
                                        + "' cannot be an AroundInvoke as it is an interface");
                            }
                            if (method.isLifeCycleMethod()) {
                                throw new InterceptorsValidationException("The method '" + method
                                        + "' in the bean class '" + bean.getClassName()
                                        + "' cannot be a lifecycle as it is an interface");
                            }
                        }
                    }
                }
            }
        }

        // Standalone interceptors
        if (bean.isInterceptor()) {
            analyzeInterceptorClass(ejbMetaData, bean);
        }

    }

    /**
     * Analyze an interceptor class and check the interceptors method.
     * @param ejbMetaData root metadata used to extract class metadata
     * @param interceptorMetaData the metadata to analyze
     */
    private static void analyzeInterceptorClass(final EjbJarArchiveMetadata ejbMetaData,
            final EasyBeansEjbJarClassMetadata interceptorMetaData) {
        List<EasyBeansEjbJarMethodMetadata> aroundInvokeList = interceptorMetaData.getAroundInvokeMethodMetadatas();
        if (aroundInvokeList != null && aroundInvokeList.size() > 1) {
            String errMsg = "There are severals @AroundInvoke in the class '" + interceptorMetaData.getClassName()
                    + "', while only one is allowed. List of Methods : '" + aroundInvokeList + "'.";
            throw new InterceptorsValidationException(errMsg);
        }

        // Ensure that interceptor has a default constructor.
        JMethod defaultConstructor = new JMethod(0, CONSTRUCTOR_METHOD, DEFAULT_CONSTRUCTOR_DESCRIPTOR, null, null);
        if (interceptorMetaData.getMethodMetadata(defaultConstructor) == null) {
            throw new InterceptorsValidationException("No default constructor in the interceptor class '"
                    + interceptorMetaData.getClassName() + "'.");
        }

        for (EasyBeansEjbJarMethodMetadata method : interceptorMetaData.getMethodMetadataCollection()) {

            // lifecycle (outside the bean)
            if (method.isLifeCycleMethod() && !method.getClassMetadata().isBean()) {
                validateJMethod(method.getJMethod(), LIFECYCLE_DESCRIPTOR_OUTSIDEBEAN, null, interceptorMetaData.getClassName());
            } else if (method.isAroundInvoke()) {
                // signature
                validateJMethod(method.getJMethod(), AROUND_INVOKE_DESCRIPTOR_EJB, AROUND_INVOKE_EXCEPTION, interceptorMetaData
                        .getClassName());

                // No final or static method
                ensureNoAccess(ACC_FINAL, method.getJMethod(), "Final", interceptorMetaData.getClassName());
                ensureNoAccess(ACC_STATIC, method.getJMethod(), "Static", interceptorMetaData.getClassName());
            }

        }

    }



    /**
     * Validate the given method with the given signature/exceptions.
     * @param jMethod method to check.
     * @param desc signature to ensure.
     * @param awaitedException exception to ensure.
     * @param className the name of the class of the given method.
     */
    private static void validateJMethod(final JMethod jMethod, final String desc, final String awaitedException,
            final String className) {

        // validate signature
        if (!jMethod.getDescriptor().equals(desc)) {
            throw new InterceptorsValidationException("Method '" + jMethod + "' of the class '" + className
                    + "' is not compliant with the signature '" + desc + "'. Signature found = '"
                    + jMethod.getDescriptor() + "'.");
        }

        // validate exceptions
        String[] exceptions = jMethod.getExceptions();
        if (awaitedException == null) {
            return;
        }

        boolean found = false;

        if (exceptions != null) {
            for (String exception : exceptions) {
                if (exception.equals(awaitedException)) {
                    found = true;
                }
            }
        }
        if (!found) {
            throw new InterceptorsValidationException("Method '" + jMethod + "' of the class '" + className
                    + "' is not compliant with the signature '" + desc + "' as the required exception '"
                    + awaitedException + "' is missing.");
        }
    }

}
