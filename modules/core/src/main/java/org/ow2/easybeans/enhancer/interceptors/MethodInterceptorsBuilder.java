/**
 * EasyBeans
 * Copyright (C) 2008 Bull S.A.S.
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
 * $Id: MethodInterceptorsBuilder.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.enhancer.interceptors;

import java.util.ArrayList;
import java.util.List;

import org.ow2.easybeans.deployment.metadata.ejbjar.EasyBeansEjbJarClassMetadata;
import org.ow2.easybeans.deployment.metadata.ejbjar.EasyBeansEjbJarMethodMetadata;
import org.ow2.easybeans.deployment.metadata.ejbjar.EjbJarArchiveMetadata;
import org.ow2.util.ee.metadata.ejbjar.api.IJClassInterceptor;
import org.ow2.util.ee.metadata.ejbjar.api.InterceptorType;

/**
 * This Helper class allow to build a list of interceptors that will be used for
 * a given method.
 * @author Florent Benoit
 */
public class MethodInterceptorsBuilder {

    /**
     * List of interceptors.
     */
    private List<IJClassInterceptor> allInterceptors = null;

    /**
     * Method metadata that will be used to build the list of interceptors.
     */
    private EasyBeansEjbJarMethodMetadata methodAnnotationMetadata;

    /**
     * ClassMetadata (parent) of the method.
     */
    private EasyBeansEjbJarClassMetadata classAnnotationMetadata = null;

    /**
     * Type of the interceptor (AroundInvoke, PostConstruct, etc).
     */
    private InterceptorType interceptorType = null;

    /**
     * Constructor.<br>
     * It will generate a class for the given method metadata.
     * @param methodAnnotationMetadata method meta data
     * @param interceptorType the type of invocationContext to generate
     *        (AroundInvoke, PostConstruct, etc)
     */
    public MethodInterceptorsBuilder(final EasyBeansEjbJarMethodMetadata methodAnnotationMetadata,
            final InterceptorType interceptorType) {
        this.methodAnnotationMetadata = methodAnnotationMetadata;
        this.interceptorType = interceptorType;
        this.classAnnotationMetadata = methodAnnotationMetadata.getClassMetadata();
        // Build the list
        buildInterceptorList();
    }


    /**
     * @return the list of the interceptors that have been built.
     */
    public List<IJClassInterceptor> getAllInterceptors() {
        return this.allInterceptors;
    }


    /**
     * Build the interceptors for the given method metadata.
     * It will add the interceptor in a correct order.
     */
    private void buildInterceptorList() {
        // init list
        this.allInterceptors = new ArrayList<IJClassInterceptor>();


        if (this.classAnnotationMetadata.getGlobalEasyBeansInterceptors() != null) {
            for (IJClassInterceptor interceptor : this.classAnnotationMetadata.getGlobalEasyBeansInterceptors()) {
                this.allInterceptors.add(interceptor);
            }
        }

        // global interceptor on the method (remove annotation for example)
        if (this.methodAnnotationMetadata.getGlobalEasyBeansInterceptors() != null) {
            for (IJClassInterceptor interceptor : this.methodAnnotationMetadata.getGlobalEasyBeansInterceptors()) {
                this.allInterceptors.add(interceptor);
            }
        }

        // Get interceptors on method metadata (tx, security, etc)
        if (this.methodAnnotationMetadata.getInterceptors() != null) {
            for (IJClassInterceptor interceptor : this.methodAnnotationMetadata.getInterceptors()) {
                this.allInterceptors.add(interceptor);
            }
        }

        // Default interceptors (if they are not excluded) and that the interceptors haven't been ordered
        EjbJarArchiveMetadata ejbJarAnnotationMetadata = this.classAnnotationMetadata.getEjbJarDeployableMetadata();
        if (!this.classAnnotationMetadata.isOrderedInterceptors() && ejbJarAnnotationMetadata.getDefaultInterceptors() != null
                && !this.classAnnotationMetadata.isExcludedDefaultInterceptors()) {
            // Not excluded at method level too
            if (!this.methodAnnotationMetadata.isExcludedDefaultInterceptors()) {
                List<? extends IJClassInterceptor> defaultInterceptorslist =
                    ejbJarAnnotationMetadata.getDefaultInterceptors().get(this.interceptorType);
                if (defaultInterceptorslist != null) {
                    for (IJClassInterceptor interceptor : defaultInterceptorslist) {
                        this.allInterceptors.add(interceptor);
                    }
                }
            }
        }

        // interceptors in the interceptor classes (user) + not excluded
        if (this.classAnnotationMetadata.getExternalUserEasyBeansInterceptors() != null
                && !this.methodAnnotationMetadata.isExcludedClassInterceptors()) {
            List<? extends IJClassInterceptor> userInterceptorslist =
                this.classAnnotationMetadata.getExternalUserEasyBeansInterceptors().get(this.interceptorType);
            if (userInterceptorslist != null) {
                for (IJClassInterceptor interceptor : userInterceptorslist) {
                    this.allInterceptors.add(interceptor);
                }
            }
        }

        // interceptors on the method (user)
        if (this.methodAnnotationMetadata.getUserEasyBeansInterceptors() != null) {
            List<? extends IJClassInterceptor> userInterceptorslist =
                this.methodAnnotationMetadata.getUserEasyBeansInterceptors().get(this.interceptorType);
            if (userInterceptorslist != null) {
                for (IJClassInterceptor interceptor : userInterceptorslist) {
                    this.allInterceptors.add(interceptor);
                }
            }
        }

        // interceptors on the bean class (user) + not excluded
            if (this.classAnnotationMetadata.getInternalUserEasyBeansInterceptors() != null
                    && !this.methodAnnotationMetadata.isExcludedClassInterceptors()) {
                List<? extends IJClassInterceptor> userInterceptorslist =

                this.classAnnotationMetadata.getInternalUserEasyBeansInterceptors().get(this.interceptorType);
            if (userInterceptorslist != null) {
                this.allInterceptors.addAll(userInterceptorslist);
            }
        }
    }
}
