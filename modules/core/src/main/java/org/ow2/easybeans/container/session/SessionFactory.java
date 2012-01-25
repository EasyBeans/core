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
 * $Id: SessionFactory.java 5747 2011-02-28 17:12:27Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.container.session;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.ow2.easybeans.api.EZBContainer;
import org.ow2.easybeans.api.FactoryException;
import org.ow2.easybeans.api.bean.EasyBeansSB;
import org.ow2.easybeans.api.bean.info.IBeanInfo;
import org.ow2.easybeans.api.container.EZBSessionContext;
import org.ow2.easybeans.api.event.bean.EZBEventBeanInvocation;
import org.ow2.easybeans.container.AbsFactory;
import org.ow2.easybeans.container.info.SessionBeanInfo;
import org.ow2.easybeans.event.bean.EventBeanInvocationEnd;
import org.ow2.easybeans.event.bean.EventBeanInvocationError;
import org.ow2.easybeans.naming.J2EEManagedObjectNamingHelper;
import org.ow2.easybeans.proxy.client.ClientRPCInvocationHandler;
import org.ow2.easybeans.rpc.EJBLocalRequestImpl;
import org.ow2.easybeans.rpc.JEJBResponse;
import org.ow2.easybeans.rpc.api.EJBLocalRequest;
import org.ow2.easybeans.rpc.api.EJBRemoteRequest;
import org.ow2.easybeans.rpc.api.EJBResponse;
import org.ow2.util.auditreport.api.IAuditID;
import org.ow2.util.log.Log;
import org.ow2.util.log.LogFactory;
import org.ow2.util.pool.api.PoolException;
import org.ow2.util.pool.impl.enhanced.api.basic.CreatePoolItemException;

/**
 * This class manages the session bean and its creation/lifecycle.
 * @param <PoolType> the type of bean instance.
 * @author Florent Benoit
 */
public abstract class SessionFactory<PoolType extends EasyBeansSB<PoolType>> extends AbsFactory<PoolType> {

    /**
     * Flag used to recreate EasyBeans Proxy on the client side as with IIOP/JacORB the dynamic proxy serialization is failing.
     */
    private static final boolean RECREATE_DYNAMIC_PROXY = Boolean.getBoolean("easybeans.recreate.dynamic.proxy");

    /**
     * Logger.
     */
    private static Log logger = LogFactory.getLog(SessionFactory.class);

    /**
     * Session Desc (deployment info).
     */
    private SessionBeanInfo sessionBeanInfo = null;

    /**
     * Inherited Local thread used to keep the invoked business interface.
     */
    private InheritableThreadLocal<String> invokedBusinessInterfaceNameThreadLocal;


    /**
     * Builds a new factory with a given name and its container.
     * @param className name of this factory (name of class that is managed)
     * @param container the root component of this factory.
     * @throws FactoryException if class can't be loaded.
     */
    public SessionFactory(final String className, final EZBContainer container) throws FactoryException {
        super(className, container);
        this.invokedBusinessInterfaceNameThreadLocal = new InheritableThreadLocal<String>();
    }

    /**
     * Stops the factory.
     */
    @Override
    public void stop() {
        // stop pool
        try {
            getPool().stop();
        } catch (PoolException e) {
            logger.error("Problem when stopping the factory", e);
        } finally {
            super.stop();
        }

    }

    /**
     * @return information of the current bean.
     */
    public IBeanInfo getBeanInfo() {
        return this.sessionBeanInfo;
    }

    /**
     * @return information of the current bean.
     */
    public SessionBeanInfo getSessionBeanInfo() {
        return this.sessionBeanInfo;
    }

    /**
     * Sets the information object for a session bean.
     * @param sessionBeanInfo information on the bean.
     */
    public void setSessionBeanInfo(final SessionBeanInfo sessionBeanInfo) {
        this.sessionBeanInfo = sessionBeanInfo;
    }

    /**
     * Creates an instance.
     * @throws CreatePoolItemException if instance cannot be created.
     * @return the created instance.
     */
    public PoolType createPoolItem() throws CreatePoolItemException {
        PoolType instance = null;
        ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(getContainer().getClassLoader());
        try {
            try {
                instance = getBeanClass().newInstance();
            } catch (InstantiationException e) {
                logger.error("Unable to create a new instance of the class ''{0}''", getBeanClass().getName(), e);
                throw new CreatePoolItemException(WAITING_TIME_BEFORE_CREATION, "Cannot create a new instance", e);
            } catch (IllegalAccessException e) {
                logger.error("Unable to create a new instance of the class ''{0}''", getBeanClass().getName(), e);
                throw new CreatePoolItemException(WAITING_TIME_BEFORE_CREATION, "Cannot create a new instance", e);
            } catch (RuntimeException e) {
                logger.error("Unable to create a new instance of the class ''{0}''", getBeanClass().getName(), e);
                throw new CreatePoolItemException(WAITING_TIME_BEFORE_CREATION, "Cannot create a new instance", e);
            } catch (Exception e) {
                logger.error("Unable to create a new instance of the class ''{0}''", getBeanClass().getName(), e);
                throw new CreatePoolItemException(WAITING_TIME_BEFORE_CREATION, "Cannot create a new instance", e);
            } catch (Error e) {
                logger.error("Unable to create a new instance of the class ''{0}''", getBeanClass().getName(), e);
                // null as factory is broken
                throw new CreatePoolItemException(WAITING_TIME_BEFORE_CREATION, "Cannot create a new instance", e);
            }
        } finally {
            Thread.currentThread().setContextClassLoader(oldClassLoader);
        }

        // Set the factory
        instance.setEasyBeansFactory(this);
        instance.setEasyBeansInvocationContextFactory(getInvocationContextFactory());


        // Init the session Context
        EZBSessionContext<SessionFactory<?>> sessionContext = new EasyBeansSessionContext<SessionFactory<?>>(this);
        instance.setEasyBeansContext(sessionContext);

        oldClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(getContainer().getClassLoader());
        try {
            // Call injection
            try {
                injectResources(instance);
            } catch (PoolException e) {
                logger.error("Unable to perform injection of resources in the instance of the class ''{0}''", getBeanClass()
                        .getName(), e);
                throw new CreatePoolItemException(WAITING_TIME_BEFORE_CREATION,
                        "Cannot perform injection of resources in the instance of the class '" + getBeanClass().getName()
                                + "'.", e);
            } catch (RuntimeException e) {
                logger.error("Unable to perform injection of resources in the instance of the class ''{0}''", getBeanClass()
                        .getName(), e);
                throw new CreatePoolItemException(WAITING_TIME_BEFORE_CREATION,
                        "Cannot perform injection of resources in the instance of the class '" + getBeanClass().getName()
                                + "'.", e);
            } catch (Exception e) {
                logger.error("Unable to perform injection of resources in the instance of the class ''{0}''", getBeanClass()
                        .getName(), e);
                throw new CreatePoolItemException(WAITING_TIME_BEFORE_CREATION,
                        "Cannot perform injection of resources in the instance of the class '" + getBeanClass().getName()
                                + "'.", e);
            } catch (Error e) {
                logger.error("Unable to perform injection of resources in the instance of the class ''{0}''", getBeanClass()
                        .getName(), e);
                throw new CreatePoolItemException(WAITING_TIME_BEFORE_CREATION,
                        "Cannot perform injection of resources in the instance of the class '" + getBeanClass().getName()
                                + "'.", e);
            }

            // post construct callback
            postConstruct(instance);
        } catch (RuntimeException e) {
            logger.error("Unable to perform postconstruct on a new instance of the class ''{0}''", getBeanClass().getName(), e);
            throw new CreatePoolItemException(WAITING_TIME_BEFORE_CREATION, "Cannot perform postConstruct on the new instance",
                    e);
        } catch (Exception e) {
            logger.error("Unable to perform postconstruct on a new instance of the class ''{0}''", getBeanClass().getName(), e);
            throw new CreatePoolItemException(WAITING_TIME_BEFORE_CREATION, "Cannot perform postConstruct on the new instance",
                    e);
        } catch (Error e) {
            logger.error("Unable to perform postconstruct on a new instance of the class ''{0}''", getBeanClass().getName(), e);
            throw new CreatePoolItemException(WAITING_TIME_BEFORE_CREATION, "Cannot perform postConstruct on the new instance",
                    e);
        } finally {
            Thread.currentThread().setContextClassLoader(oldClassLoader);
        }

        return instance;
    }

    /**
     * A request comes to the bean factory and needs to be handled.<br>
     * A response is done which contains the answer.
     * @param request the EJB request.
     * @return a response that have been processed by the factory.
     */
    @Override
    public EJBResponse rpcInvoke(final EJBRemoteRequest request) {
        Method calledMethod = getHashes().get(request.getMethodHash());
        // Invalid method
        if (calledMethod == null) {
            logger.debug("Requested method {0} is not present on the bean class ''{1}''", request.getMethodName(),
                    getBeanClass());
            return new JEJBResponse();
        }

        // Get Args (use context classloader for the Serialization)
        Object[] args = null;
        ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(getContainer().getClassLoader());
        try {
            args = request.getMethodArgs();
        } finally {
            Thread.currentThread().setContextClassLoader(oldClassLoader);
        }

        // Invocation ID
        IAuditID previousID = null;
        if (getCurrentInvocationID() != null) {
            previousID = getCurrentInvocationID().newInvocation();
        }

        // Dispatch the bean invocation begin event.
        String methodEventProviderId = getJ2EEManagedObjectId() + "/"
            + J2EEManagedObjectNamingHelper.getMethodSignature(calledMethod) + "@Remote";
        EZBEventBeanInvocation event = getInvocationEventBegin(methodEventProviderId, args);
        long number = event.getInvocationNumber();
        getEventDispatcher().dispatch(event);

        try {
            EJBLocalRequestImpl localRequest = new EJBLocalRequestImpl(request.getMethodHash(), args, request.getBeanId(), request
                    .getInvokedBusinessInterfaceName());
            // Avoid to send events as local calls
            localRequest.setCalledFromRemoteRequest(true);

            // call the method
            EJBResponse result = localCall(localRequest);

            // Create the bean invocation end event.
            event = new EventBeanInvocationEnd(methodEventProviderId, number, result);

            // JacORB enabled ? Needs to return the invocation handler instead of the dynamic proxy to the client proxy
            if (RECREATE_DYNAMIC_PROXY) {
                Object value = result.getValue();
                if (value != null) {
                    try {
                        InvocationHandler handler = Proxy.getInvocationHandler(value);
                        if (handler instanceof ClientRPCInvocationHandler
                                && ((ClientRPCInvocationHandler) handler).isBusinessObjectMode()) {
                            result.setValue(handler);
                            return result;
                        }
                    } catch (IllegalArgumentException e) {
                        logger.debug("Not a proxy instance", e);
                    }
                }
            }

            return result;
        } catch (Exception ex) {
            // Create the bean invocation error event.
            event = new EventBeanInvocationError(methodEventProviderId, number, ex);
            throw new RuntimeException(ex);
        } finally {
            // Restore previous ID
            if (getCurrentInvocationID() != null) {
                getCurrentInvocationID().setAuditID(previousID);
            }
            getEventDispatcher().dispatch(event);
        }
    }

    /**
     * Gets a bean for the given id.
     * @param beanId id of the expected bean.
     * @return a Stateless bean.
     * @throws IllegalArgumentException if bean is not found.
     */
    protected abstract PoolType getBean(final Long beanId) throws IllegalArgumentException;


    /**
     * Do a local call on a method of this factory.
     * @param localCallRequest the given request
     * @return response with the value of the call and the bean ID (if any)
     */
    public abstract EJBResponse localCall(final EJBLocalRequest localCallRequest);

    /**
     * @return the local thread that is keeping invoked Business Interface name.
     */
    protected InheritableThreadLocal<String> getInvokedBusinessInterfaceNameThreadLocal() {
        return this.invokedBusinessInterfaceNameThreadLocal;
    }

    /**
     * Callback used when the given element will be removed.
     * @param instance the given instance to be removed
     */
    public void poolItemRemoved(final PoolType instance) {
        super.remove(instance);
        instance.setEasyBeansRemoved(true);
    }
}
