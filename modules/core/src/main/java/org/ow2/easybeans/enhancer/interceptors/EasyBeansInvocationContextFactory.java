/**
 * EasyBeans
 * Copyright (C) 2008-2009 Bull S.A.S.
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
 * $Id: EasyBeansInvocationContextFactory.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.enhancer.interceptors;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ow2.easybeans.api.EasyBeansInvocationContext;
import org.ow2.easybeans.api.bean.EasyBeansBean;
import org.ow2.easybeans.api.interceptor.EZBInterceptorInvoker;
import org.ow2.easybeans.api.interceptor.EZBInterceptorManager;
import org.ow2.easybeans.api.interceptor.EZBInterceptorManagerFactory;
import org.ow2.easybeans.api.interceptor.EZBInvocationContextFactory;
import org.ow2.easybeans.deployment.metadata.ejbjar.EasyBeansEjbJarClassMetadata;
import org.ow2.easybeans.deployment.metadata.ejbjar.EasyBeansEjbJarMethodMetadata;
import org.ow2.easybeans.enhancer.lib.MethodRenamer;
import org.ow2.util.ee.metadata.ejbjar.api.IJClassInterceptor;
import org.ow2.util.ee.metadata.ejbjar.api.InterceptorType;
import org.ow2.util.log.Log;
import org.ow2.util.log.LogFactory;
import org.ow2.util.scan.api.metadata.structures.JMethod;

/**
 * This class will manage the creation of the invocation context objects.
 * @author Florent Benoit
 */
public class EasyBeansInvocationContextFactory implements EZBInvocationContextFactory {

    /**
     * Logger.
     */
    private static final Log LOGGER = LogFactory.getLog(EasyBeansInvocationContextFactory.class);

    /**
     * Map between the signature of a method and all invokers defined for this
     * method. (for each interceptor type)
     */
    private Map<String, Map<String, List<EZBInterceptorInvoker>>> methodsInterceptorInvokerList;

    /**
     * Map between the signature of a method and its java.lang.reflect.Method
     * object. (cache)
     */
    private Map<String, Method> methods;

    /**
     * List of classes that needs to be instantiate in the interceptor manager.
     */
    private List<String> interceptorClasses = null;

    /**
     * ClassLoader used to load classes.
     */
    private ClassLoader classLoader = null;

    /**
     * Build a new InvocationContext factory by analyzing each business method
     * and lifecycle method and creating the invokers.
     * @param classMetadata the metadata of the bean class that will be analyzed
     * @param classLoader the
     */
    public EasyBeansInvocationContextFactory(final EasyBeansEjbJarClassMetadata classMetadata, final ClassLoader classLoader) {
        this.classLoader = classLoader;

        // Init Map/List
        this.methodsInterceptorInvokerList = new HashMap<String, Map<String, List<EZBInterceptorInvoker>>>();
        this.methods = new HashMap<String, Method>();
        this.interceptorClasses = new ArrayList<String>();

        // Analyze each business method and build the invokers for each method /
        // bean method.
        Collection<EasyBeansEjbJarMethodMetadata> methodsMetadata = classMetadata.getMethodMetadataCollection();

        // Defines the types of interceptors to manage
        List<InterceptorType> interceptorTypes = new ArrayList<InterceptorType>();
        interceptorTypes.add(InterceptorType.AROUND_INVOKE);
        interceptorTypes.add(InterceptorType.POST_CONSTRUCT);
        interceptorTypes.add(InterceptorType.PRE_DESTROY);


        // Compute interceptors for each business method and for each kind of interceptor
        for (EasyBeansEjbJarMethodMetadata methodMetadata : methodsMetadata) {
            if (methodMetadata.isBusinessMethod() || methodMetadata.isLifeCycleMethod()) {

                // Ignore init/clinit methods
                if (methodMetadata.getJMethod().getName().startsWith("<")) {
                    continue;
                }

                // get signature
                String methodSignature = MethodHelper.getSignature(methodMetadata);

                // Build Map
                Map<String, List<EZBInterceptorInvoker>> interceptorTypeInvokers = new HashMap<String, List<EZBInterceptorInvoker>>();
                this.methodsInterceptorInvokerList.put(methodSignature, interceptorTypeInvokers);

                // Loop on some interceptor types
                for (InterceptorType interceptorType : interceptorTypes) {

                    // Get list of interceptors
                    List<IJClassInterceptor> allInterceptors = new MethodInterceptorsBuilder(methodMetadata, interceptorType).getAllInterceptors();

                    // build invokers
                    List<EZBInterceptorInvoker> invokers = new ArrayList<EZBInterceptorInvoker>();


                    // Store the invoker
                    interceptorTypeInvokers.put(interceptorType.toString(), invokers);

                    for (IJClassInterceptor interceptor : allInterceptors) {
                        String classname = interceptor.getClassName();
                        JMethod jMethod = interceptor.getJMethod();

                        // interceptor on the bean or outside ?
                        if (classMetadata.getClassName().equals(classname)) {
                            // interceptor is in the bean
                            // Add the invoker
                            invokers.add(new BeanInterceptorInvokerImpl(classname, jMethod, classLoader));
                        } else {
                            // outside of the bean
                            // Add the invoker
                            invokers.add(new StandaloneInterceptorInvokerImpl(classname, jMethod, classLoader));

                            // An interceptor instance will be required for this
                            // interceptor class
                            addInterceptorClass(classname);
                        }
                    }

                    // Add the method invocation call
                    JMethod interceptedMethod = methodMetadata.getJMethod();

                    String methodName = interceptedMethod.getName();
                    if (!methodMetadata.isLifeCycleMethod() && !methodName.contains("$generated")) {
                        methodName = MethodRenamer.encode(interceptedMethod.getName());
                    }

                    JMethod originalMethod = new JMethod(interceptedMethod.getAccess(), methodName, interceptedMethod.getDescriptor(), interceptedMethod.getSignature(), interceptedMethod
                            .getExceptions());
                    invokers.add(new BeanBusinessMethodInvokerImpl(classMetadata.getClassName(), originalMethod, classLoader));

                    // Put in cache the business methods of the bean
                    this.methods.put(methodSignature, MethodHelper.getMethod(classMetadata.getClassName(), methodMetadata
                            .getJMethod(), classLoader));
                }
            }
        }

    }

    /**
     * Add the given classname to the list of the classes that will be managed
     * by the interceptor manager.
     * @param classname the name of the class
     */
    protected void addInterceptorClass(final String classname) {
        // Add it if class is not yet in the list
        if (!this.interceptorClasses.contains(classname)) {
            this.interceptorClasses.add(classname);
        }
    }

    /**
     * Gets the interceptor manager factory.
     * @return an instance of the manager factory
     */
    public EZBInterceptorManagerFactory getInterceptorManagerFactory() {
        return new EasyBeansInterceptorManagerFactory(this.interceptorClasses, this.classLoader);
    }

    /**
     * Gets an invocation context for the given method.
     * @param instance the bean's instance
     * @param interceptorManager the manager of the interceptors
     * @param interceptorType the type of the interceptor
     * @param methodSignature the key in order to find data for the given method
     * @param parameters the parameters of the method
     * @return an invocation context implementation
     */
    public EasyBeansInvocationContext getContext(final EasyBeansBean instance, final EZBInterceptorManager interceptorManager,
            final String interceptorType, final String methodSignature, final Object... parameters) {
        LOGGER.debug("Calling getContext for instance ''{0}'', interceptor manager ''{1}'', type ''{2}'' signature ''{3}'' and parameters ''{4}'' with interceptor invoker list set to ''{5}''", instance, interceptorManager, interceptorType, methodSignature, parameters, this.methodsInterceptorInvokerList);
        return new DynamicInvocationContextImpl(instance, this.methodsInterceptorInvokerList.get(methodSignature).get(interceptorType),
                interceptorManager, !interceptorType.equals(InterceptorType.AROUND_INVOKE.toString()), this.methods.get(methodSignature), parameters);
    }

}
