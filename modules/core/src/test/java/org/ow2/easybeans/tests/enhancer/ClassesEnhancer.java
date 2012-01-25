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
 * $Id: ClassesEnhancer.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */
package org.ow2.easybeans.tests.enhancer;

import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ow2.easybeans.deployment.annotations.helper.ResolverHelper;
import org.ow2.easybeans.deployment.metadata.ejbjar.EasyBeansEjbJarClassMetadata;
import org.ow2.easybeans.deployment.metadata.ejbjar.EasyBeansEjbJarDeployableFactory;
import org.ow2.easybeans.deployment.metadata.ejbjar.EasyBeansEjbJarMethodMetadata;
import org.ow2.easybeans.deployment.metadata.ejbjar.EjbJarArchiveMetadata;
import org.ow2.easybeans.enhancer.Enhancer;
import org.ow2.easybeans.loader.EasyBeansClassLoader;
import org.ow2.easybeans.resolver.ContainerJNDIResolver;
import org.ow2.easybeans.resolver.api.EZBContainerJNDIResolver;
import org.ow2.util.archive.api.IArchive;
import org.ow2.util.ee.deploy.api.deployable.EJB3Deployable;
import org.ow2.util.ee.deploy.impl.helper.DeployableHelper;


/**
 * Enhance a set of given classes.
 * Rely on core enhancer.
 * @author Florent Benoit
 */
public final class ClassesEnhancer extends Enhancer {

    /**
     * Type available (for adding adapter).
     */
    public static enum TYPE {INTERCEPTOR, CALLBACK, ALL}

    /**
     * Class extension.
     */
    public static final String EXT_CLASS = ".class";

    /**
     * Creates an new enhancer.
     * @param loader classloader where to define enhanced classes.
     * @param ejbJarAnnotationMetadata object with references to the metadata.
     * @param map a map allowing to give some objects to the enhancer.
     */
    public ClassesEnhancer(final ClassLoader loader, final EjbJarArchiveMetadata ejbJarAnnotationMetadata,
            final Map<String, Object> map) {
        super(loader, ejbJarAnnotationMetadata, map);
    }

    /**
     * Enhance the classes simulating a Bean. It uses a child classloder of the current thread.<br>
     * @param classesToEnhance the list of class on which run class adapters
     * @param type the type of adapter to run.
     * @return created classloader
     * @throws Exception if it fails
     */
    public static ClassLoader enhanceNewClassLoader(final List<String> classesToEnhance, final TYPE type) throws Exception {
        final ClassLoader loader = Thread.currentThread().getContextClassLoader();

        PrivilegedAction<EasyBeansClassLoader> privilegedAction = new PrivilegedAction<EasyBeansClassLoader>() {
            public EasyBeansClassLoader run() {
                return new EasyBeansClassLoader(new URL[]{}, loader);
            }
        };
        ClassLoader childLoader  = AccessController.doPrivileged(privilegedAction);

        Thread.currentThread().setContextClassLoader(childLoader);
        try {
            enhance(classesToEnhance, TYPE.INTERCEPTOR);
        } finally {
            Thread.currentThread().setContextClassLoader(loader);
        }

        return childLoader;
    }

    /**
     * Enhance the classes simulating a Bean.<br>
     * @param classesToEnhance the list of class on which run class adapters
     * @param type the type of adapter to run.
     * @throws Exception if it fails
     */
    public static void enhance(final List<String> classesToEnhance, final TYPE type) throws Exception {

        ClassLoader loader = Thread.currentThread().getContextClassLoader();

        IArchive archive = new ArchiveInMemory(loader, classesToEnhance);

        EasyBeansEjbJarDeployableFactory deployableFactory = new EasyBeansEjbJarDeployableFactory();
        EjbJarArchiveMetadata ejbJarAnnotationMetadata = deployableFactory
                .createDeployableMetadata(EJB3Deployable.class.cast(DeployableHelper.getDeployable(archive)));


        ResolverHelper.resolve(ejbJarAnnotationMetadata, null);

        // Remove some TX interceptors (so it works offline)
        // For each bean class
        List<String> beanNames = ejbJarAnnotationMetadata.getBeanNames();
        for (String beanName : beanNames) {
            for (EasyBeansEjbJarClassMetadata classAnnotationMetadata : ejbJarAnnotationMetadata.getClassesForBean(beanName)) {
                if (classAnnotationMetadata.isBean()) {
                    // Remove global EasyBeans interceptors
                    classAnnotationMetadata.setGlobalEasyBeansInterceptors(null);
                    for (EasyBeansEjbJarMethodMetadata m : classAnnotationMetadata.getMethodMetadataCollection()) {
                        m.setInterceptors(null);
                    }
                }
            }
        }

        Map<String, Object> map = new HashMap<String, Object>();
        map.put(EZBContainerJNDIResolver.class.getName(), new ContainerJNDIResolver(archive));
        ClassesEnhancer classesEnhancer = new ClassesEnhancer(loader, ejbJarAnnotationMetadata, map);
        classesEnhancer.enhance();
    }

}
