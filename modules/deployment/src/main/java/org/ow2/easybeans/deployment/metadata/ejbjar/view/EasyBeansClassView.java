/**
 * EasyBeans
 * Copyright (C) 2006-2008 Bull S.A.S.
 * Copyright 2013 Peergreen S.A.S.
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
 * $Id:EjbJarClassMetadata.java 2372 2008-02-08 18:18:37Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.deployment.metadata.ejbjar.view;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.ow2.easybeans.asm.Opcodes;
import org.ow2.easybeans.deployment.annotations.exceptions.InterceptorsValidationException;
import org.ow2.easybeans.deployment.metadata.ejbjar.EasyBeansEjbJarMethodMetadata;
import org.ow2.util.ee.metadata.common.api.struct.IJavaxPersistenceContext;
import org.ow2.util.ee.metadata.common.api.struct.IJavaxPersistenceContextType;
import org.ow2.util.ee.metadata.common.api.view.ICommonView;
import org.ow2.util.ee.metadata.ejbjar.api.InterceptorType;
import org.ow2.util.ee.metadata.ejbjar.api.view.IEJBClassView;
import org.ow2.util.ee.metadata.ejbjar.impl.view.EJBView;
import org.ow2.util.pool.api.IPoolConfiguration;
import org.ow2.util.pool.api.IPoolMetadata;
import org.ow2.util.scan.api.metadata.IClassMetadata;
import org.ow2.util.scan.api.metadata.IFieldMetadata;
import org.ow2.util.scan.api.metadata.IMetadata;
import org.ow2.util.scan.api.metadata.IMethodMetadata;
import org.ow2.util.scan.api.metadata.structures.IMethod;
import org.ow2.util.scan.impl.metadata.JMethod;

/**
 * This class represents the annotation metadata of a Bean.<br>
 * From this class, we can access to all methods of a bean with its associated
 * information.
 * @author Florent Benoit
 */
public class EasyBeansClassView extends EJBView implements IPoolMetadata, IEJBClassView {


    private final IMetadata classMetadata;

    /**
     * View is based on the given class metadata
     */
    public EasyBeansClassView(final IMetadata classMetadata) {
        super(classMetadata);
        this.classMetadata = classMetadata;
    }

    /**
     * @return the method metadata with annotation &#64;{@link javax.interceptor.AroundInvoke}.
     */
    public boolean isAroundInvokeMethodMetadata() {
        return (this.classMetadata.getProperty("javax.interceptor.AroundInvoke") != null);
    }

    /**
     * @return the list of methods metadata with annotation &#64;{@link javax.interceptor.AroundInvoke}.
     */
    public LinkedList<IMethodMetadata> getDelegateAroundInvokeMethodMetadatas() {
        return this.classMetadata.getProperty("javax.interceptor.AroundInvoke");
    }


    /**
     * Add a given method metadata if not yet added
     * @param methodMetadata the method.
     * @param property the given property to use.
     */
    public void addPropertyMetadata(final IMethodMetadata methodMetadata, final String property, final boolean checkExists) {
        LinkedList<IMethodMetadata> existingMethodsMetadata = this.classMetadata.getProperty(property);

        if (existingMethodsMetadata == null) {
            existingMethodsMetadata = new LinkedList<IMethodMetadata>();
            this.classMetadata.setProperty(property, existingMethodsMetadata);
        }
        // Check not yet present
        if (checkExists) {
            for (IMethodMetadata foundMethodMetadata : existingMethodsMetadata) {
                if (methodMetadata.getClassMetadata().getClassName().equals(foundMethodMetadata.getClassMetadata().getClassName())) {
                    if (methodMetadata.getJMethod().equals(foundMethodMetadata.getJMethod())) {
                        return;
                    }
                }
            }
        }

        existingMethodsMetadata.addFirst(methodMetadata);
    }


    /**
     * Add a &#64;{@link javax.interceptor.AroundInvoke} method of this class.
     * @param aroundInvokeMethodMetadata the method.
     */
    public void addAroundInvokeMethodMetadata(final IMethodMetadata aroundInvokeMethodMetadata) {
        addPropertyMetadata(aroundInvokeMethodMetadata, "javax.interceptor.AroundInvoke", true);
    }

    /**
     * @return the methods metadata with annotation &#64;{@link javax.annotation.PostConstruct}.
     */
    public LinkedList<IMethodMetadata> getDelegatePostConstructMethodsMetadata() {
        return this.classMetadata.getProperty("javax.annotation.PostConstruct");
    }

    /**
     * Adds a &#64;{@link javax.annotation.PostConstruct} method of this class.
     * @param postConstructMethodMetadata the method.
     */
    public void addPostConstructMethodMetadata(final IMethodMetadata postConstructMethodMetadata) {
        if (checkLifeCycleDuplicate(postConstructMethodMetadata, InterceptorType.POST_CONSTRUCT, getDelegatePostConstructMethodsMetadata())) {
            addPropertyMetadata(postConstructMethodMetadata, "javax.annotation.PostConstruct", false);
        }
    }

    /**
     * Checks that only method at one level of a class is present.
     * @param methodMetadata method to check
     * @param itcType the type of interceptor (used for the error)
     * @param existingList current list of methods
     */
    private boolean checkLifeCycleDuplicate(final IMethodMetadata methodMetadata,
            final InterceptorType itcType, final List<IMethodMetadata> existingList) {

        // First case : not inherited
        IClassMetadata wantToAddClassMetadata = methodMetadata.getClassMetadata();
        EasyBeansEjbJarMethodMetadata methodView = methodMetadata.as(EasyBeansEjbJarMethodMetadata.class);
        if (methodView.isInherited()) {
            wantToAddClassMetadata = methodView.getOriginalClassMetadata();
        }
        if (existingList != null) {
            for (IMethodMetadata method : existingList) {
                IClassMetadata compareMetaData;
                EasyBeansEjbJarMethodMetadata localMethodView = method.as(EasyBeansEjbJarMethodMetadata.class);
                if (localMethodView.isInherited()) {
                    compareMetaData = localMethodView.getOriginalClassMetadata();
                } else {
                    compareMetaData = method.getClassMetadata();
                }
                if (compareMetaData.equals(wantToAddClassMetadata)) {
                    IMethod jMethod = method.getJMethod();
                    JMethod otherMethod = new JMethod(Opcodes.ACC_PUBLIC, jMethod.getName()
                            + compareMetaData.getClassName().replace("/", ""), jMethod.getDescriptor(), jMethod
                            .getSignature(), jMethod.getExceptions());
                    if (!methodMetadata.getJMethod().equals(method.getJMethod()) && !methodMetadata.getJMethod().equals(otherMethod)) {
                        throw new InterceptorsValidationException("Class " + compareMetaData.getClassName() + " has already a " + itcType
                                + " method which is " + method.getJMethod().getName() + ", cannot set new method "
                                + methodMetadata.getJMethod().getName());
                    }
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * @return the methods metadata with annotation &#64;{@link javax.annotation.PreDestroy}.
     */
    public LinkedList<IMethodMetadata> getDelegatePreDestroyMethodsMetadata() {
        return this.classMetadata.getProperty("javax.annotation.PreDestroy");
    }

    /**
     * Adds a &#64;{@link javax.annotation.PreDestroy} method of this class.
     * @param preDestroyMethodMetadata the method.
     */
    public void addPreDestroyMethodMetadata(final IMethodMetadata preDestroyMethodMetadata) {
        checkLifeCycleDuplicate(preDestroyMethodMetadata, InterceptorType.PRE_DESTROY, getDelegatePreDestroyMethodsMetadata());
        addPropertyMetadata(preDestroyMethodMetadata, "javax.annotation.PreDestroy", false);
    }

    /**
     * @return the methods metadata with annotation &#64;{@link javax.ejb.PostActivate}.
     */
    public LinkedList<IMethodMetadata> getDelegatePostActivateMethodsMetadata() {
        return this.classMetadata.getProperty("javax.ejb.PostActivate");
    }

    /**
     * Adds a &#64;{@link javax.ejb.PostActivate} method of this class.
     * @param postActivateMethodMetadata the method.
     */
    public void addPostActivateMethodMetadata(final IMethodMetadata postActivateMethodMetadata) {
        checkLifeCycleDuplicate(postActivateMethodMetadata, InterceptorType.POST_ACTIVATE, getDelegatePostActivateMethodsMetadata());
        addPropertyMetadata(postActivateMethodMetadata, "javax.ejb.PostActivate", false);
    }

    /**
     * @return the method metadata with annotation &#64;{@link javax.ejb.PrePassivate}.
     */
    public LinkedList<IMethodMetadata> getDelegatePrePassivateMethodsMetadata() {
        return this.classMetadata.getProperty("javax.ejb.PrePassivate");
    }

    /**
     * Adds a &#64;{@link javax.ejb.PrePassivate} method of this class.
     * @param prePassivateMethodMetadata the method.
     */
    public void addPrePassivateMethodMetadata(final IMethodMetadata prePassivateMethodMetadata) {
        checkLifeCycleDuplicate(prePassivateMethodMetadata, InterceptorType.PRE_PASSIVATE, getDelegatePrePassivateMethodsMetadata());
        addPropertyMetadata(prePassivateMethodMetadata, "javax.ejb.PrePassivate", false);

    }

    /**
     * Is that this class is an interceptor class ?
     * @return true if it the case, else false.
     */
    public boolean isInterceptor() {
        return (getDelegateAroundInvokeMethodMetadatas() != null && getDelegateAroundInvokeMethodMetadatas().size() > 0)
                || (getDelegatePostConstructMethodsMetadata() != null && getDelegatePostConstructMethodsMetadata().size() > 0)
                || (getDelegatePreDestroyMethodsMetadata() != null && getDelegatePreDestroyMethodsMetadata().size() > 0)
                || (getDelegatePrePassivateMethodsMetadata() != null && getDelegatePrePassivateMethodsMetadata().size() > 0)
                || (getDelegatePostActivateMethodsMetadata() != null && getDelegatePostActivateMethodsMetadata().size() > 0);
    }

    /**
     * @return the pool configuration for this metadata.
     */
    public IPoolConfiguration getPoolConfiguration() {
        return this.classMetadata.get(IPoolConfiguration.class);
    }

    /**
     * Sets the pool configuration for this metadata.
     * @param poolConfiguration the given configuration.
     */
    public void setPoolConfiguration(final IPoolConfiguration poolConfiguration) {
        this.classMetadata.set(IPoolConfiguration.class, poolConfiguration);
    }


    /**
     * @return the iherited interfaces of the super classes.
     */
    public List<String> getInheritedInterfaces() {
        List<String> inherited = this.classMetadata.getProperty("inheritedInterfaces");
        if (inherited == null) {
            return new ArrayList<String>();
        } else {
            return inherited;
        }
    }

    /**
     * Sets the interfaces used in the super classes.
     * @param inheritedInterfaces the given interfaces
     */
    public void setInheritedInterfaces(final List<String> inheritedInterfaces) {
        this.classMetadata.setProperty("inheritedInterfaces", inheritedInterfaces);
    }

    /**
     * Gets the cluster configuration.
     * @return the cluster
     */
    public Object getCluster() {
        return this.classMetadata.getProperty("cluster");
    }

    /**
     * Sets the cluster configuration.
     * @param cluster the clusterConfiguration to set
     */
    public void setCluster(final Object cluster) {
        this.classMetadata.setProperty("cluster", cluster);
    }

    /**
     * @return the endpoint address associated with this bean configuration (may be null).
     */
    public String getWebServiceEndpointAddress() {
        return this.classMetadata.getProperty("webServiceEndpointAddress");
    }

    /**
     * Set the web service endpoint address.
     * @param webServiceEndpointAddress URI address
     */
    public void setWebServiceEndpointAddress(final String webServiceEndpointAddress) {
        this.classMetadata.setProperty("webServiceEndpointAddress", webServiceEndpointAddress);
    }

    public String getWebServiceContextRoot() {
        return this.classMetadata.getProperty("webServiceContextRoot");
    }

    public void setWebServiceContextRoot(final String webServiceContextRoot) {
         this.classMetadata.setProperty("webServiceContextRoot", webServiceContextRoot);
    }

    public String getWebServiceRealmName() {
        return this.classMetadata.getProperty("webServiceRealmName");
    }

    public void setWebServiceRealmName(final String webServiceRealmName) {
        this.classMetadata.setProperty("webServiceRealmName", webServiceRealmName);
    }

    public String getWebServiceTransportGuarantee() {
        return this.classMetadata.getProperty("webServiceTransportGuarantee");
    }

    public void setWebServiceTransportGuarantee(final String webServiceTransportGuarantee) {
        this.classMetadata.setProperty("webServiceTransportGuarantee", webServiceTransportGuarantee);
    }

    public String getWebServiceAuthMethod() {
        return this.classMetadata.getProperty("webServiceAuthMethod");
    }

    public void setWebServiceAuthMethod(final String webServiceAuthMethod) {
        this.classMetadata.setProperty("webServiceAuthMethod", webServiceAuthMethod);
    }

    public List<String> getWebServiceHttpMethods() {
        return this.classMetadata.getProperty("webServiceHttpMethods");
    }

    public void setWebServiceHttpMethods(final List<String> webServiceHttpMethods) {
        this.classMetadata.setProperty("webServiceHttpMethods", webServiceHttpMethods);
    }

    /**
     * @return true if the class and attributes or setter methods have an extended persistence context.
     */
    public boolean hasExtendedPersistenceContext() {

        // Compute a list of persistence contexts
        List<IJavaxPersistenceContext> persistenceContexts = new ArrayList<IJavaxPersistenceContext>();

        ICommonView commonView = this.classMetadata.as(ICommonView.class);

        // On the class
        if (commonView.getJavaxPersistenceContext() != null) {
            persistenceContexts.add(commonView.getJavaxPersistenceContext());
        }
        if (commonView.getJavaxPersistencePersistenceContexts() != null && commonView.getJavaxPersistencePersistenceContexts().size() > 0) {
            persistenceContexts.addAll(commonView.getJavaxPersistencePersistenceContexts());
        }

        // Now, for all methods
        Collection<IMethodMetadata> methods = getClassMetadata().getMethodMetadataCollection();
        for (IMethodMetadata method : methods) {
            ICommonView methodView = method.as(ICommonView.class);
            if (methodView.getJavaxPersistenceContext() != null) {
                persistenceContexts.add(methodView.getJavaxPersistenceContext());
            }
        }

        // Now, for all attributes
        Collection<IFieldMetadata> fields = getClassMetadata().getFieldMetadataCollection();
        for (IFieldMetadata field : fields) {
            ICommonView fieldView = field.as(ICommonView.class);
            if (fieldView.getJavaxPersistenceContext() != null) {
                persistenceContexts.add(fieldView.getJavaxPersistenceContext());
            }
        }

        boolean hasExtendedPersistenceContext = false;
        for (IJavaxPersistenceContext persistenceContext : persistenceContexts) {
            if (IJavaxPersistenceContextType.EXTENDED == persistenceContext.getType()) {
                hasExtendedPersistenceContext = true;
                break;
            }
        }

        return hasExtendedPersistenceContext;

    }

    public IClassMetadata getClassMetadata() {
        return (IClassMetadata) this.classMetadata;
    }

    public String getClassName() {
        return getClassMetadata().getInternalClassName();
    }

}
