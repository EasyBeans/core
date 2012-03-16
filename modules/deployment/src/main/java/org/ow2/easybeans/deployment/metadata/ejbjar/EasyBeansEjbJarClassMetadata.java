/**
 * EasyBeans
 * Copyright (C) 2006-2008 Bull S.A.S.
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

package org.ow2.easybeans.deployment.metadata.ejbjar;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.ow2.easybeans.asm.Opcodes;
import org.ow2.easybeans.deployment.annotations.exceptions.InterceptorsValidationException;
import org.ow2.util.ee.deploy.api.deployable.EJB3Deployable;
import org.ow2.util.ee.metadata.ejbjar.api.InterceptorType;
import org.ow2.util.ee.metadata.ejbjar.impl.EjbJarClassMetadata;
import org.ow2.util.pool.api.IPoolConfiguration;
import org.ow2.util.pool.api.IPoolMetadata;
import org.ow2.util.scan.api.metadata.structures.JMethod;

/**
 * This class represents the annotation metadata of a Bean.<br>
 * From this class, we can access to all methods of a bean with its associated
 * information.
 * @author Florent Benoit
 */
public class EasyBeansEjbJarClassMetadata
        extends
        EjbJarClassMetadata<
            EJB3Deployable,
            EjbJarArchiveMetadata,
            EasyBeansEjbJarClassMetadata,
            EasyBeansEjbJarMethodMetadata,
            EasyBeansEjbJarFieldMetadata>
        implements IPoolMetadata {

    /**
     * Serial Version UID.
     */
    private static final long serialVersionUID = -1080073251168278784L;

    /**
     * List of inherited interfaces (present in the super classes).
     */
    private List<String> inheritedInterfaces = null;

    /**
     * List of &#64;{@link javax.interceptor.AroundInvoke} methods on this
     * class (should be only one per class, validating occurs after).
     */
    private LinkedList<EasyBeansEjbJarMethodMetadata> aroundInvokeMethodsMetadata = null;

    /**
     * Methods used for &#64;{@link javax.annotation.PostConstruct} on this
     * class (only one per class but may be defined in super classes).
     */
    private LinkedList<EasyBeansEjbJarMethodMetadata> postConstructMethodsMetadata = null;

    /**
     * Methods used for &#64;{@link javax.annotation.PreDestroy} on this class
     * (only one per class but may be defined in super classes).
     */
    private LinkedList<EasyBeansEjbJarMethodMetadata> preDestroyMethodsMetadata = null;

    /**
     * Methods used for &#64;{@link javax.ejb.PostActivate} on this class (only
     * one per class but may be defined in super classes).
     */
    private LinkedList<EasyBeansEjbJarMethodMetadata> postActivateMethodsMetadata = null;

    /**
     * Methods used for &#64;{@link javax.ejb.PrePassivate} on this class (only
     * one per class but may be defined in super classes).
     */
    private LinkedList<EasyBeansEjbJarMethodMetadata> prePassivateMethodsMetadata = null;

    /**
     * Pool's configuration.
     */
    private IPoolConfiguration poolConfiguration;

    /**
     * Cluster's configuration.
     */
    private Object cluster;

    /**
     * If this bean is a stateless bean exposed as a webservice, user
     * may have set a desired endpoint-address.
     */
    private String webServiceEndpointAddress;

    /**
     * Web services generated web context.
     */
    private String webServiceContextRoot;

    /**
     * name of the realm if the EJB is secured.
     */
    private String webServiceRealmName;

    /**
     * Transport guarantee value (NONE, INTEGRAL, CONFIDENTIAL).
     */
    private String webServiceTransportGuarantee;

    /**
     * Authentication method (NONE, BASIC, DIGEST, CLIENT-CERT).
     */
    private String webServiceAuthMethod;

    /**
     * List of http-methods.
     */
    private List<String> webServiceHttpMethods = new ArrayList<String>();

    /**
     * No-Arg Constructor.
     */
    public EasyBeansEjbJarClassMetadata() {
        super();
        this.postConstructMethodsMetadata = new LinkedList<EasyBeansEjbJarMethodMetadata>();
        this.preDestroyMethodsMetadata = new LinkedList<EasyBeansEjbJarMethodMetadata>();
        this.postActivateMethodsMetadata = new LinkedList<EasyBeansEjbJarMethodMetadata>();
        this.prePassivateMethodsMetadata = new LinkedList<EasyBeansEjbJarMethodMetadata>();
        this.inheritedInterfaces = new ArrayList<String>();
    }

    /**
     * @return the method metadata with annotation &#64;{@link javax.interceptor.AroundInvoke}.
     */
    public boolean isAroundInvokeMethodMetadata() {
        return (this.aroundInvokeMethodsMetadata != null);
    }

    /**
     * @return the list of methods metadata with annotation &#64;{@link javax.interceptor.AroundInvoke}.
     */
    public List<EasyBeansEjbJarMethodMetadata> getAroundInvokeMethodMetadatas() {
        return this.aroundInvokeMethodsMetadata;
    }

    /**
     * Add a &#64;{@link javax.interceptor.AroundInvoke} method of this class.
     * @param aroundInvokeMethodMetadata the method.
     */
    public void addAroundInvokeMethodMetadata(final EasyBeansEjbJarMethodMetadata aroundInvokeMethodMetadata) {
        if (this.aroundInvokeMethodsMetadata == null) {
            this.aroundInvokeMethodsMetadata = new LinkedList<EasyBeansEjbJarMethodMetadata>();
        }
        // Check not yet present
        for (EasyBeansEjbJarMethodMetadata methodMetadata : this.aroundInvokeMethodsMetadata) {
            if (aroundInvokeMethodMetadata.getClassMetadata().getClassName().equals(methodMetadata.getClassMetadata().getClassName())) {
                if (aroundInvokeMethodMetadata.getJMethod().equals(methodMetadata.getJMethod())) {
                    return;
                }
            }
        }

        this.aroundInvokeMethodsMetadata.addFirst(aroundInvokeMethodMetadata);
    }

    /**
     * @return the methods metadata with annotation &#64;{@link javax.annotation.PostConstruct}.
     */
    public LinkedList<EasyBeansEjbJarMethodMetadata> getPostConstructMethodsMetadata() {
        return this.postConstructMethodsMetadata;
    }

    /**
     * Adds a &#64;{@link javax.annotation.PostConstruct} method of this class.
     * @param postConstructMethodMetadata the method.
     */
    public void addPostConstructMethodMetadata(final EasyBeansEjbJarMethodMetadata postConstructMethodMetadata) {
        if (checkLifeCycleDuplicate(postConstructMethodMetadata, InterceptorType.POST_CONSTRUCT, getPostConstructMethodsMetadata())) {
            this.postConstructMethodsMetadata.addFirst(postConstructMethodMetadata);
        }
    }

    /**
     * Checks that only method at one level of a class is present.
     * @param methodMetadata method to check
     * @param itcType the type of interceptor (used for the error)
     * @param existingList current list of methods
     */
    private boolean checkLifeCycleDuplicate(final EasyBeansEjbJarMethodMetadata methodMetadata,
            final InterceptorType itcType, final List<EasyBeansEjbJarMethodMetadata> existingList) {

        // First case : not inherited
        EasyBeansEjbJarClassMetadata wantToAddClassMetadata = methodMetadata.getClassMetadata();
        if (methodMetadata.isInherited()) {
            wantToAddClassMetadata = methodMetadata.getOriginalClassMetadata();
        }
        for (EasyBeansEjbJarMethodMetadata method : existingList) {
            EasyBeansEjbJarClassMetadata compareMetaData;
            if (method.isInherited()) {
                compareMetaData = method.getOriginalClassMetadata();
            } else {
                compareMetaData = method.getClassMetadata();
            }
            if (compareMetaData.equals(wantToAddClassMetadata)) {
                JMethod jMethod = method.getJMethod();
                JMethod otherMethod = new JMethod(Opcodes.ACC_PUBLIC, jMethod.getName()
                        + compareMetaData.getClassName().replace("/", ""), jMethod.getDescriptor(), jMethod
                        .getSignature(), jMethod.getExceptions());
                if (!methodMetadata.getJMethod().equals(method.getJMethod()) && !methodMetadata.getJMethod().equals(otherMethod)) {
                    throw new InterceptorsValidationException("Class " + getClassName() + " has already a " + itcType
                        + " method which is " + method.getMethodName() + ", cannot set new method "
                        + methodMetadata.getMethodName());
                }
                return false;
            }
        }
        return true;
    }

    /**
     * @return the methods metadata with annotation &#64;{@link javax.annotation.PreDestroy}.
     */
    public LinkedList<EasyBeansEjbJarMethodMetadata> getPreDestroyMethodsMetadata() {
        return this.preDestroyMethodsMetadata;
    }

    /**
     * Adds a &#64;{@link javax.annotation.PreDestroy} method of this class.
     * @param preDestroyMethodMetadata the method.
     */
    public void addPreDestroyMethodMetadata(final EasyBeansEjbJarMethodMetadata preDestroyMethodMetadata) {
        checkLifeCycleDuplicate(preDestroyMethodMetadata, InterceptorType.PRE_DESTROY, getPreDestroyMethodsMetadata());
        this.preDestroyMethodsMetadata.addFirst(preDestroyMethodMetadata);
    }

    /**
     * @return the methods metadata with annotation &#64;{@link javax.ejb.PostActivate}.
     */
    public LinkedList<EasyBeansEjbJarMethodMetadata> getPostActivateMethodsMetadata() {
        return this.postActivateMethodsMetadata;
    }

    /**
     * Adds a &#64;{@link javax.ejb.PostActivate} method of this class.
     * @param postActivateMethodMetadata the method.
     */
    public void addPostActivateMethodMetadata(final EasyBeansEjbJarMethodMetadata postActivateMethodMetadata) {
        checkLifeCycleDuplicate(postActivateMethodMetadata, InterceptorType.POST_ACTIVATE, getPostActivateMethodsMetadata());
        this.postActivateMethodsMetadata.addFirst(postActivateMethodMetadata);
    }

    /**
     * @return the method metadata with annotation &#64;{@link javax.ejb.PrePassivate}.
     */
    public LinkedList<EasyBeansEjbJarMethodMetadata> getPrePassivateMethodsMetadata() {
        return this.prePassivateMethodsMetadata;
    }

    /**
     * Adds a &#64;{@link javax.ejb.PrePassivate} method of this class.
     * @param prePassivateMethodMetadata the method.
     */
    public void addPrePassivateMethodMetadata(final EasyBeansEjbJarMethodMetadata prePassivateMethodMetadata) {
        checkLifeCycleDuplicate(prePassivateMethodMetadata, InterceptorType.PRE_PASSIVATE, getPrePassivateMethodsMetadata());
        this.prePassivateMethodsMetadata.addFirst(prePassivateMethodMetadata);
    }

    /**
     * Is that this class is an interceptor class ?
     * @return true if it the case, else false.
     */
    public boolean isInterceptor() {
        return (this.aroundInvokeMethodsMetadata != null && this.aroundInvokeMethodsMetadata.size() > 0)
                || (this.postConstructMethodsMetadata != null && this.postConstructMethodsMetadata.size() > 0)
                || (this.preDestroyMethodsMetadata != null && this.preDestroyMethodsMetadata.size() > 0)
                || (this.prePassivateMethodsMetadata != null && this.prePassivateMethodsMetadata.size() > 0)
                || (this.postActivateMethodsMetadata != null && this.postActivateMethodsMetadata.size() > 0);
    }

    /**
     * @return the pool configuration for this metadata.
     */
    public IPoolConfiguration getPoolConfiguration() {
        return this.poolConfiguration;
    }

    /**
     * Sets the pool configuration for this metadata.
     * @param poolConfiguration the given configuration.
     */
    public void setPoolConfiguration(final IPoolConfiguration poolConfiguration) {
        this.poolConfiguration = poolConfiguration;
    }


    /**
     * @return the iherited interfaces of the super classes.
     */
    public List<String> getInheritedInterfaces() {
        return this.inheritedInterfaces;
    }

    /**
     * Sets the interfaces used in the super classes.
     * @param inheritedInterfaces the given interfaces
     */
    public void setInheritedInterfaces(final List<String> inheritedInterfaces) {
        this.inheritedInterfaces = inheritedInterfaces;
    }

    /**
     * Gets the cluster configuration.
     * @return the cluster
     */
    public Object getCluster() {
        return this.cluster;
    }

    /**
     * Sets the cluster configuration.
     * @param cluster the clusterConfiguration to set
     */
    public void setCluster(final Object cluster) {
        this.cluster = cluster;
    }

    /**
     * @return the endpoint address associated with this bean configuration (may be null).
     */
    public String getWebServiceEndpointAddress() {
        return this.webServiceEndpointAddress;
    }

    /**
     * Set the web service endpoint address.
     * @param webServiceEndpointAddress URI address
     */
    public void setWebServiceEndpointAddress(final String webServiceEndpointAddress) {
        this.webServiceEndpointAddress = webServiceEndpointAddress;
    }

    public String getWebServiceContextRoot() {
        return this.webServiceContextRoot;
    }

    public void setWebServiceContextRoot(final String webServiceContextRoot) {
        this.webServiceContextRoot = webServiceContextRoot;
    }

    public String getWebServiceRealmName() {
        return this.webServiceRealmName;
    }

    public void setWebServiceRealmName(final String webServiceRealmName) {
        this.webServiceRealmName = webServiceRealmName;
    }

    public String getWebServiceTransportGuarantee() {
        return this.webServiceTransportGuarantee;
    }

    public void setWebServiceTransportGuarantee(final String webServiceTransportGuarantee) {
        this.webServiceTransportGuarantee = webServiceTransportGuarantee;
    }

    public String getWebServiceAuthMethod() {
        return this.webServiceAuthMethod;
    }

    public void setWebServiceAuthMethod(final String webServiceAuthMethod) {
        this.webServiceAuthMethod = webServiceAuthMethod;
    }

    public List<String> getWebServiceHttpMethods() {
        return this.webServiceHttpMethods;
    }

    public void setWebServiceHttpMethods(final List<String> webServiceHttpMethods) {
        this.webServiceHttpMethods = webServiceHttpMethods;
    }
}
