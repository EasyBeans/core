/*
 * EasyBeans
 * Copyright (C) 2012 Bull S.A.S.
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
 * $Id:$
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.container.managedbean;

import javax.ejb.Timer;

import org.ow2.easybeans.api.EZBContainer;
import org.ow2.easybeans.api.FactoryException;
import org.ow2.easybeans.api.bean.EasyBeansManagedBean;
import org.ow2.easybeans.api.bean.info.IBeanInfo;
import org.ow2.easybeans.api.bean.info.IMethodInfo;
import org.ow2.easybeans.container.AbsFactory;
import org.ow2.easybeans.container.EasyBeansEJBContext;
import org.ow2.easybeans.container.info.BeanInfo;
import org.ow2.easybeans.rpc.api.EJBLocalRequest;
import org.ow2.easybeans.rpc.api.EJBRemoteRequest;
import org.ow2.easybeans.rpc.api.EJBResponse;
import org.ow2.util.log.Log;
import org.ow2.util.log.LogFactory;
import org.ow2.util.pool.api.PoolException;

/**
 * Defines the factory that will manage Managed beans.
 *
 * @author Loic Albertin
 */
public class ManagedBeanFactory extends AbsFactory<EasyBeansManagedBean> {

    private static Log logger = LogFactory.getLog(ManagedBeanFactory.class);

    private BeanInfo beanInfo;

    /**
     * Builds a new factory with a given name and its container.
     *
     * @param className name of this factory (name of class that is managed)
     * @param container the root component of this factory.
     *
     * @throws org.ow2.easybeans.api.FactoryException
     *          if class can't be loaded.
     */
    public ManagedBeanFactory(final String className, final EZBContainer container) throws FactoryException {
        super(className, container);
    }

    @Override
    public EJBResponse rpcInvoke(EJBRemoteRequest request) {
        return null;
    }

    public EJBResponse localCall(EJBLocalRequest localCallRequest) {
        return null;
    }

    public void notifyTimeout(Timer timer, IMethodInfo methodInfo) {

    }

    public IBeanInfo getBeanInfo() {
        return beanInfo;
    }

    public void setBeanInfo(BeanInfo beanInfo) {
        this.beanInfo = beanInfo;
    }

    /**
     * Creates an instance.
     *
     * @return the created instance.
     */
    public EasyBeansManagedBean getObjectInstance() throws Exception {

        EasyBeansManagedBean instance = null;
        ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(getContainer().getClassLoader());
        try {
            try {
                instance = getBeanClass().newInstance();
            } catch (InstantiationException e) {
                logger.error("Unable to create a new instance of the class ''{0}''", getBeanClass().getName(), e);
                throw new RuntimeException("Cannot create a new instance", e);
            } catch (IllegalAccessException e) {
                logger.error("Unable to create a new instance of the class ''{0}''", getBeanClass().getName(), e);
                throw new RuntimeException("Cannot create a new instance", e);
            } catch (RuntimeException e) {
                logger.error("Unable to create a new instance of the class ''{0}''", getBeanClass().getName(), e);
                throw new RuntimeException("Cannot create a new instance", e);
            } catch (Exception e) {
                logger.error("Unable to create a new instance of the class ''{0}''", getBeanClass().getName(), e);
                throw new RuntimeException("Cannot create a new instance", e);
            } catch (Error e) {
                logger.error("Unable to create a new instance of the class ''{0}''", getBeanClass().getName(), e);
                // null as factory is broken
                throw new RuntimeException("Cannot create a new instance", e);
            }
        } finally {
            Thread.currentThread().setContextClassLoader(oldClassLoader);
        }

        // Set the factory
        instance.setEasyBeansFactory(this);
        instance.setEasyBeansInvocationContextFactory(getInvocationContextFactory());


        // Init the session Context
        EasyBeansEJBContext<ManagedBeanFactory> sessionContext = new EasyBeansEJBContext<ManagedBeanFactory>(this);
        instance.setEasyBeansContext(sessionContext);

        oldClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(getContainer().getClassLoader());
        try {
            // Call injection
            try {
                injectResources(instance);
            } catch (PoolException e) {
                logger.error("Unable to perform injection of resources in the instance of the class ''{0}''", getBeanClass().getName(), e);
                throw new RuntimeException(
                        "Cannot perform injection of resources in the instance of the class '" + getBeanClass().getName() + "'.", e);
            } catch (RuntimeException e) {
                logger.error("Unable to perform injection of resources in the instance of the class ''{0}''", getBeanClass().getName(), e);
                throw new RuntimeException(
                        "Cannot perform injection of resources in the instance of the class '" + getBeanClass().getName() + "'.", e);
            } catch (Exception e) {
                logger.error("Unable to perform injection of resources in the instance of the class ''{0}''", getBeanClass().getName(), e);
                throw new RuntimeException(
                        "Cannot perform injection of resources in the instance of the class '" + getBeanClass().getName() + "'.", e);
            } catch (Error e) {
                logger.error("Unable to perform injection of resources in the instance of the class ''{0}''", getBeanClass().getName(), e);
                throw new RuntimeException(
                        "Cannot perform injection of resources in the instance of the class '" + getBeanClass().getName() + "'.", e);
            }

            // post construct callback
            postConstruct(instance);

        } catch (RuntimeException e) {
            logger.error("Unable to perform postconstruct on a new instance of the class ''{0}''", getBeanClass().getName(), e);
            throw new RuntimeException("Cannot perform postConstruct on the new instance", e);
        } catch (Exception e) {
            logger.error("Unable to perform postconstruct on a new instance of the class ''{0}''", getBeanClass().getName(), e);
            throw new RuntimeException("Cannot perform postConstruct on the new instance", e);
        } catch (Error e) {
            logger.error("Unable to perform postconstruct on a new instance of the class ''{0}''", getBeanClass().getName(), e);
            throw new RuntimeException("Cannot perform postConstruct on the new instance", e);
        } finally {
            Thread.currentThread().setContextClassLoader(oldClassLoader);
        }

        return instance;
    }

}
