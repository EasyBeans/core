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
 * $Id: TransactionResolver.java 5643 2010-10-18 15:17:00Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.deployment.annotations.helper.bean;

import static javax.ejb.TransactionManagementType.BEAN;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagementType;

import org.ow2.easybeans.asm.Type;
import org.ow2.easybeans.deployment.metadata.ejbjar.EasyBeansEjbJarClassMetadata;
import org.ow2.easybeans.deployment.metadata.ejbjar.EasyBeansEjbJarMethodMetadata;
import org.ow2.easybeans.transaction.interceptors.BMTStatefulTransactionInterceptor;
import org.ow2.easybeans.transaction.interceptors.BMTStatelessTransactionInterceptor;
import org.ow2.easybeans.transaction.interceptors.BMTTransactionInterceptor;
import org.ow2.easybeans.transaction.interceptors.CMTMandatoryTransactionInterceptor;
import org.ow2.easybeans.transaction.interceptors.CMTNeverTransactionInterceptor;
import org.ow2.easybeans.transaction.interceptors.CMTNotSupportedTransactionInterceptor;
import org.ow2.easybeans.transaction.interceptors.CMTRequiredTransactionInterceptor;
import org.ow2.easybeans.transaction.interceptors.CMTRequiresNewTransactionInterceptor;
import org.ow2.easybeans.transaction.interceptors.CMTSupportsTransactionInterceptor;
import org.ow2.easybeans.transaction.interceptors.ListenerSessionSynchronizationInterceptor;
import org.ow2.easybeans.transaction.interceptors.MDBCMTRequiredTransactionInterceptor;
import org.ow2.util.ee.metadata.ejbjar.api.IJClassInterceptor;
import org.ow2.util.ee.metadata.ejbjar.impl.JClassInterceptor;
import org.ow2.util.log.Log;
import org.ow2.util.log.LogFactory;
import org.ow2.util.scan.api.metadata.structures.JMethod;

/**
 * This class adds the interceptor for transaction on a given method.
 * @author Florent Benoit
 */
public final class TransactionResolver {

    /**
     * Logger.
     */
    private static Log logger = LogFactory.getLog(TransactionResolver.class);

    /**
     * Signature of EasyBeans interceptors.
     */
    private static final JMethod EASYBEANS_INTERCEPTOR = new JMethod(0, "intercept",
            "(Lorg/ow2/easybeans/api/EasyBeansInvocationContext;)Ljava/lang/Object;", null,
            new String[] {"java/lang/Exception"});

    /**
     * CMT Required transaction interceptor.
     */
    private static final String CMT_REQUIRED_INTERCEPTOR = Type.getInternalName(CMTRequiredTransactionInterceptor.class);

    /**
     * CMT Mandatory transaction interceptor.
     */
    private static final String CMT_MANDATORY_INTERCEPTOR = Type.getInternalName(CMTMandatoryTransactionInterceptor.class);

    /**
     * CMT Never transaction interceptor.
     */
    private static final String CMT_NEVER_INTERCEPTOR = Type.getInternalName(CMTNeverTransactionInterceptor.class);

    /**
     * CMT NotSupported transaction interceptor.
     */
    private static final String CMT_NOT_SUPPORTED_INTERCEPTOR = Type
            .getInternalName(CMTNotSupportedTransactionInterceptor.class);

    /**
     * CMT Supports transaction interceptor.
     */
    private static final String CMT_SUPPORTS_INTERCEPTOR = Type.getInternalName(CMTSupportsTransactionInterceptor.class);

    /**
     * CMT RequiresNew transaction interceptor.
     */
    private static final String CMT_REQUIRES_NEW_INTERCEPTOR = Type.getInternalName(CMTRequiresNewTransactionInterceptor.class);

    /**
     * BMT transaction interceptor.
     */
    private static final String BMT_INTERCEPTOR = Type.getInternalName(BMTTransactionInterceptor.class);

    /**
     * BMT stateful transaction interceptor.
     */
    private static final String BMT_STATEFUL_INTERCEPTOR = Type.getInternalName(BMTStatefulTransactionInterceptor.class);

    /**
     * BMT stateless transaction interceptor.
     */
    private static final String BMT_STATELESS_INTERCEPTOR = Type.getInternalName(BMTStatelessTransactionInterceptor.class);

    /**
     * MDB enlist resource interceptor.
     */
    private static final String MDB_CMT_REQUIRED_INTERCEPTOR = Type.getInternalName(MDBCMTRequiredTransactionInterceptor.class);

    /**
     * ListenerSessionSynchronizationInterceptor transaction interceptor.
     */
    private static final String LISTENER_SESSION_SYNCHRO_INTERCEPTOR = Type
            .getInternalName(ListenerSessionSynchronizationInterceptor.class);

    /**
     * javax.ejb.SessionSynchronization interface.
     */
    private static final String SESSION_SYNCHRONIZATION_INTERFACE = "javax/ejb/SessionSynchronization";

    /**
     * Helper class, no public constructor.
     */
    private TransactionResolver() {
    }

    /**
     * Adds the right transaction interceptor depending of the transactional
     * attribute set by the user.
     * @param bean the given bean on which set the transactional interceptor.
     */
    public static void resolve(final EasyBeansEjbJarClassMetadata bean) {
        for (EasyBeansEjbJarMethodMetadata method : bean.getMethodMetadataCollection()) {
            resolveMethod(bean, method);
        }
    }

    /**
     * Adds the right transaction interceptor depending of the transactional
     * attribute set by the user.
     * @param bean the given bean on which set the transactional interceptor.
     */
    public static void resolveMethod(final EasyBeansEjbJarClassMetadata bean, final EasyBeansEjbJarMethodMetadata method) {

        // Checks if Synchronization is needed for this stateful bean
        boolean addSynchro = false;
        if (bean.isStateful()) {
            String[] interfaces = bean.getInterfaces();
            if (interfaces != null) {
                for (String itf : interfaces) {
                    if (SESSION_SYNCHRONIZATION_INTERFACE.equals(itf)) {
                        addSynchro = true;
                        break;
                    }
                }
            }

        }

        TransactionAttributeType beanTxType = bean.getTransactionAttributeType();
        TransactionManagementType beanTxManaged = bean.getTransactionManagementType();

        List<? extends IJClassInterceptor> previousInterceptors = method.getInterceptors();

        List<IJClassInterceptor> interceptors = new ArrayList<IJClassInterceptor>();
        if (previousInterceptors != null) {
            interceptors.addAll(previousInterceptors);
        }

        // Bean managed or container managed ?
        if (beanTxManaged.equals(BEAN)) {
            // BMT
            if (bean.isStateful()) {
                interceptors.add(new JClassInterceptor(BMT_STATEFUL_INTERCEPTOR, EASYBEANS_INTERCEPTOR));
            } else if (bean.isStateless()) {
                interceptors.add(new JClassInterceptor(BMT_STATELESS_INTERCEPTOR, EASYBEANS_INTERCEPTOR));
            } else {
                interceptors.add(new JClassInterceptor(BMT_INTERCEPTOR, EASYBEANS_INTERCEPTOR));
            }
        } else {
            // CMT
            TransactionAttributeType methodTx = method.getTransactionAttributeType();

            // Set method tx attribute to the class tx attribute if none was
            // set.
            if (methodTx == null) {
                if (!method.isInherited()) {
                    methodTx = beanTxType;
                } else {
                    // inherited method, take value of the original class
                    methodTx = method.getOriginalClassMetadata().getTransactionAttributeType();
                }
            }

            // Apply MDB interceptors and performs checks for authorized modes
            if (bean.isMdb()) {
                switch (methodTx) {
                case REQUIRED:
                case NOT_SUPPORTED:
                    break;
                case MANDATORY:
                case NEVER:
                case REQUIRES_NEW:
                case SUPPORTS:
                default:
                    logger.error("For MDB, the TX attribute '" + methodTx
                            + "' is not a valid attribute (only Required or Not supported is available). "
                            + "The error is on the method '" + method.getMethodName() + "' of class '"
                            + method.getClassMetadata().getClassName() + "' for the bean '"
                            + method.getClassMetadata().getLinkedBean() + "'. Sets to the default REQUIRED mode.");
                    methodTx = TransactionAttributeType.REQUIRED;
                    break;
                }

                if (TransactionAttributeType.NOT_SUPPORTED == methodTx) {
                    interceptors.add(new JClassInterceptor(CMT_NOT_SUPPORTED_INTERCEPTOR, EASYBEANS_INTERCEPTOR));
                } else if (TransactionAttributeType.REQUIRED == methodTx) {
                    method.setTransacted(true);
                    interceptors.add(new JClassInterceptor(MDB_CMT_REQUIRED_INTERCEPTOR, EASYBEANS_INTERCEPTOR));
                } else {
                    // invalid case
                    throw new IllegalStateException("Shouldn't be in another mode. Expected NOT_Supported/Required and got '"
                            + methodTx + "' for the method '" + method.getMethodName() + "' of class '"
                            + method.getClassMetadata().getClassName() + "' for the bean '"
                            + method.getClassMetadata().getLinkedBean() + "'. Sets to the default REQUIRED mode.");
                }

            } else {

                switch (methodTx) {
                case MANDATORY:
                    method.setTransacted(true);
                    interceptors.add(new JClassInterceptor(CMT_MANDATORY_INTERCEPTOR, EASYBEANS_INTERCEPTOR));
                    break;
                case NEVER:
                    interceptors.add(new JClassInterceptor(CMT_NEVER_INTERCEPTOR, EASYBEANS_INTERCEPTOR));
                    break;
                case NOT_SUPPORTED:
                    interceptors.add(new JClassInterceptor(CMT_NOT_SUPPORTED_INTERCEPTOR, EASYBEANS_INTERCEPTOR));
                    break;
                case REQUIRED:
                    method.setTransacted(true);
                    interceptors.add(new JClassInterceptor(CMT_REQUIRED_INTERCEPTOR, EASYBEANS_INTERCEPTOR));
                    break;
                case REQUIRES_NEW:
                    method.setTransacted(true);
                    interceptors.add(new JClassInterceptor(CMT_REQUIRES_NEW_INTERCEPTOR, EASYBEANS_INTERCEPTOR));
                    break;
                case SUPPORTS:
                    method.setTransacted(true);
                    interceptors.add(new JClassInterceptor(CMT_SUPPORTS_INTERCEPTOR, EASYBEANS_INTERCEPTOR));
                    break;
                default:
                    throw new IllegalStateException("Invalid tx attribute on method '" + method.getMethodName()
                            + "', value = '" + methodTx + "'.");
                }

            }

            // Add listener interceptor for stateul bean only if the bean
            // implements SessionSynchronization interface
            if (addSynchro) {
                interceptors.add(new JClassInterceptor(LISTENER_SESSION_SYNCHRO_INTERCEPTOR, EASYBEANS_INTERCEPTOR));
            }
            // End CMT
        }

        method.setInterceptors(interceptors);
    }
}
