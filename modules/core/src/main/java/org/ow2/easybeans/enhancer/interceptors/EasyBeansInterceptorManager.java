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
 * $Id: EasyBeansInterceptorManager.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.enhancer.interceptors;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ow2.easybeans.api.container.EZBEJBContext;
import org.ow2.easybeans.api.interceptor.EZBInterceptorManager;
import org.ow2.util.log.Log;
import org.ow2.util.log.LogFactory;

/**
 * Manage lifecycle of interceptors.
 * @author Florent Benoit
 */
public class EasyBeansInterceptorManager implements EZBInterceptorManager {

    /**
     * Logger.
     */
    private static Log logger = LogFactory.getLog(EasyBeansInterceptorManager.class);

    /**
     * Map between the classname and the instance of the interceptor.
     */
    private Map<String, Object> interceptorInstances;

    /**
     * ClassLoader used to load classes.
     */
    private ClassLoader classLoader = null;

    /**
     * EasyBeans context.
     */
    private EZBEJBContext<?> easyBeansContext;

    /**
     * Build a new manager of interceptors.
     * @param classnameList the list of interceptor classes to manage
     * @param classLoader the classloader to use to load the classes
     */
    public EasyBeansInterceptorManager(final List<String> classnameList, final ClassLoader classLoader) {
        this.classLoader = classLoader;
        this.interceptorInstances = new HashMap<String, Object>();
        initInterceptors(classnameList);
    }

    /**
     * Build the instance of the interceptor classes.
     * @param classnameList the name of the interceptor classes
     */
    protected void initInterceptors(final List<String> classnameList) {
        // For each interceptor, load the class and creates a new instance of
        // the interceptor
        for (String classname : classnameList) {
            Class<?> interceptorClass = null;
            try {
                interceptorClass = this.classLoader.loadClass(classname.replace('/', '.'));
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException("The class '" + classname + "' was not found.", e);
            }

            // Create an instance of the interceptor
            try {
                this.interceptorInstances.put(classname, interceptorClass.newInstance());
            } catch (InstantiationException e) {
                throw new IllegalStateException("Unable to build an instance of the class '" + classname + "'.", e);
            } catch (IllegalAccessException e) {
                throw new IllegalStateException("Unable to build an instance of the class '" + classname + "'.", e);
            }
        }

    }

    /**
     * Initialize the interceptors instance.
     */
    public void injectedByEasyBeans() {
        for (Object interceptor : this.interceptorInstances.values()) {
            // set the EasyBeansContext
            Method setEZBContextMethod = null;
            try {
                setEZBContextMethod = interceptor.getClass().getDeclaredMethod("setEasyBeansContext", EZBEJBContext.class);
            } catch (SecurityException e) {
                throw new IllegalStateException("Unable to get setEasyBeansContext method", e);
            } catch (NoSuchMethodException e) {
                logger.debug("Unable to get setEasyBeansContext method", e);
            }
            // Method exist
            if (setEZBContextMethod != null) {
                try {
                    setEZBContextMethod.invoke(interceptor, getEasyBeansContext());
                } catch (IllegalArgumentException e) {
                    throw new IllegalStateException("Unable to call setEasyBeansContext method", e);
                } catch (IllegalAccessException e) {
                    throw new IllegalStateException("Unable to call setEasyBeansContext method", e);
                } catch (InvocationTargetException e) {
                    throw new IllegalStateException("Unable to call setEasyBeansContext method", e);
                }
            }

            // Call injectedByEasyBeans method
            Method injectedByEasyBeansMethod = null;
            try {
                injectedByEasyBeansMethod = interceptor.getClass().getDeclaredMethod("injectedByEasyBeans");
            } catch (SecurityException e) {
                throw new IllegalStateException("Unable to get injectedByEasyBeans method", e);
            } catch (NoSuchMethodException e) {
                logger.debug("Unable to get injectedByEasyBeans method", e);
            }
            if (injectedByEasyBeansMethod != null) {
                try {
                    injectedByEasyBeansMethod.invoke(interceptor);
                } catch (IllegalArgumentException e) {
                    throw new IllegalStateException("Unable to call injectedByEasyBeans method", e);
                } catch (IllegalAccessException e) {
                    throw new IllegalStateException("Unable to call injectedByEasyBeans method", e);
                } catch (InvocationTargetException e) {
                    throw new IllegalStateException("Unable to call injectedByEasyBeans method", e);
                }
            }

        }
    }

    /**
     * @return the EasyBeans Context.
     */
    public EZBEJBContext<?> getEasyBeansContext() {
        return this.easyBeansContext;
    }

    /**
     * Sets the EasyBeans Context.
     * @param ezbejbcontext the given context.
     */
    public void setEasyBeansContext(final EZBEJBContext<?> ezbejbcontext) {
        this.easyBeansContext = ezbejbcontext;
    }

    /**
     * Gets the interceptor specified by its classname.
     * @param classname the given class name
     * @return the instance of the interceptor
     */
    public Object getInterceptor(final String classname) {
        Object o = this.interceptorInstances.get(classname);
        if (o == null) {
            throw new IllegalStateException("Unable to find the interceptor with the classname '" + classname + "'");
        }
        return o;
    }
}
