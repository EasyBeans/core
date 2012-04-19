/**
 * EasyBeans
 * Copyright (C) 2009-2012 Bull S.A.S.
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
 * $Id: SingletonSessionFactory.java 5747 2011-02-28 17:12:27Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.container.session.singleton;

import static org.ow2.easybeans.api.OperationState.BUSINESS_METHOD;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.ejb.NoSuchEJBException;
import javax.ejb.Timer;

import org.ow2.easybeans.api.EZBContainer;
import org.ow2.easybeans.api.FactoryException;
import org.ow2.easybeans.api.OperationState;
import org.ow2.easybeans.api.bean.EasyBeansSingletonSB;
import org.ow2.easybeans.api.bean.info.IAccessTimeoutInfo;
import org.ow2.easybeans.api.bean.info.IApplicationExceptionInfo;
import org.ow2.easybeans.api.bean.info.ILockTypeInfo;
import org.ow2.easybeans.api.bean.info.IMethodInfo;
import org.ow2.easybeans.api.event.bean.EZBEventBeanInvocation;
import org.ow2.easybeans.container.session.JPoolWrapperFactory;
import org.ow2.easybeans.container.session.PoolWrapper;
import org.ow2.easybeans.container.session.SessionFactory;
import org.ow2.easybeans.container.session.stateful.ConcurrentBuilderException;
import org.ow2.easybeans.event.bean.EventBeanInvocationEnd;
import org.ow2.easybeans.event.bean.EventBeanInvocationError;
import org.ow2.easybeans.naming.J2EEManagedObjectNamingHelper;
import org.ow2.easybeans.rpc.JEJBResponse;
import org.ow2.easybeans.rpc.api.EJBLocalRequest;
import org.ow2.easybeans.rpc.api.EJBResponse;
import org.ow2.easybeans.rpc.api.RPCException;
import org.ow2.util.auditreport.api.IAuditID;
import org.ow2.util.log.Log;
import org.ow2.util.log.LogFactory;
import org.ow2.util.pool.api.PoolException;
import org.ow2.util.pool.impl.JPool;
import org.ow2.util.pool.impl.enhanced.EnhancedPool;
import org.ow2.util.pool.impl.enhanced.PoolConfiguration;
import org.ow2.util.pool.impl.enhanced.manager.optional.IPoolItemRemoveManager;

/**
 * Defines the factory that will manage singleton session beans.
 * @author Florent Benoit
 */
public class SingletonSessionFactory extends SessionFactory<EasyBeansSingletonSB> implements
        IPoolItemRemoveManager<EasyBeansSingletonSB> {

    /**
     * Logger.
     */
    private static final Log LOGGER = LogFactory.getLog(SingletonSessionFactory.class);

    /**
     * Lock used by this factory.
     */
    private ReadWriteLock lock = null;

    /**
     * Singleton Bean Instance.
     */
    private EasyBeansSingletonSB singletonBean = null;


    /**
     * Builds a new factory with a given name and its container.
     * @param className name of this factory (name of class that is managed)
     * @param container the root component of this factory.
     * @throws FactoryException if class can't be loaded.
     */
    public SingletonSessionFactory(final String className, final EZBContainer container) throws FactoryException {
        super(className, container);

        // Only one instance
        PoolConfiguration poolConfig = new PoolConfiguration();
        poolConfig.setMax(1);
        poolConfig.setMin(1);
        poolConfig.setSpare(0);

        // Use of the old pool ?
        if (Boolean.getBoolean(OLD_POOL)) {
            JPool<EasyBeansSingletonSB, Long> jPool = new JPool<EasyBeansSingletonSB, Long>(
                    new JPoolWrapperFactory<EasyBeansSingletonSB, Long>(this));
            jPool.setPoolConfiguration(poolConfig);
            setPool(jPool);
        } else {
            // new pool
            EnhancedPool<EasyBeansSingletonSB> singletonPool = getManagementPool().getEnhancedPoolFactory().createEnhancedPool(
                    this);
            singletonPool.setPoolConfiguration(poolConfig);
            setPool(new PoolWrapper<EasyBeansSingletonSB>(singletonPool));
        }

        // pool
        this.lock = new ReentrantReadWriteLock();
    }


    /**
     * Gets a bean for the given id.
     * @param beanId id of the expected bean.
     * @return a Stateless bean.
     * @throws IllegalArgumentException if bean is not found.
     */
    @Override
    protected EasyBeansSingletonSB getBean(final Long beanId) throws IllegalArgumentException {
        try {
            return getPool().get();
        } catch (PoolException e) {
            throw new IllegalArgumentException("Cannot get element in the pool", e);
        }
    }

    /**
     * Do a local call on a method of this factory.
     * @param localCallRequest the given request
     * @return response with the value of the call and the bean ID (if any)
     */
    @Override
    public EJBResponse localCall(final EJBLocalRequest localCallRequest) {
        // build EJB Response
        EJBResponse ejbResponse = new JEJBResponse();

        // get method hash
        Long methodHash = localCallRequest.getMethodHash();

        // Get data about the invoked method
        IMethodInfo methodInfo = getMethodInfoHashes().get(methodHash);
        IAccessTimeoutInfo accessTimeout = null;

        // Locking strategy
        ILockTypeInfo methodLockType = null;

        // Update methods info
        if (methodInfo != null) {
            accessTimeout = methodInfo.getAccessTimeout();
            methodLockType = methodInfo.getLockType();
        }

        // Define lock based on the strategy.
        Lock lock = null;
        if (ILockTypeInfo.READ == methodLockType) {
            lock = this.lock.readLock();
        } else {
            lock = this.lock.writeLock();
        }

        // getAccess is used if accessTimeout with value >=0 is used
        boolean getAccess = true;
        if (accessTimeout != null) {
            // Infinite wait
            if (accessTimeout.value() == -1) {
                lock.lock();
            } else if (accessTimeout.value() >= 0) {
                try {
                    LOGGER.debug("Trying to lock bean with value ''{0}'' and timeunit ''{1}''", Long.valueOf(accessTimeout
                            .value()), accessTimeout.unit());
                    getAccess = lock.tryLock(accessTimeout.value(), accessTimeout.unit());
                } catch (InterruptedException e) {
                    ejbResponse.setRPCException(new RPCException("Cannot get a lock for the stateful instance", e));
                    return ejbResponse;
                }
            }
        } else {
            // Serialize concurrent calls, so wait until the lock is liberated
            lock.lock();
        }

        // We've tried to get the lock for the given time and this has been denied.
        // Do not need to unlock as the lock was not obtained
        if (!getAccess) {
            // Timeout exception
            if (accessTimeout != null) {
                RPCException rpcException = null;
                if (accessTimeout.value() == 0) {
                    // Concurrent access is denied
                    rpcException = new RPCException(ConcurrentBuilderException
                            .buildConcurrentException("Unable to get a concurrent access on bean '" + getClassName()
                                    + "' and method '" + getHashes().get(methodHash) + "'."));
                } else {
                    // Unable to get access during the elapsed time, so throw a
                    // ConcurrentAccessTimeoutException
                    rpcException = new RPCException(ConcurrentBuilderException
                            .buildConcurrentTimeoutException("Unable to get a concurrent access with an accessTimeout of '"
                                    + accessTimeout + "' on bean '" + getClassName() + "' and method '"
                                    + getHashes().get(methodHash) + "'."));
                }
                ejbResponse.setRPCException(rpcException);
                return ejbResponse;
            }
        }

        // If we're here, it means that we've got the lock. so don't forget to unlock
        try {

            // Get an instance of the bean
            if (this.singletonBean == null) {
                try {
                    this.singletonBean = getBean(null);
                } catch (IllegalArgumentException e) {
                    ejbResponse.setRPCException(new RPCException("Cannot get element in the pool", e));
                    return ejbResponse;
                } catch (NoSuchEJBException e) {
                    ejbResponse.setRPCException(new RPCException("Bean has been removed", e));
                    return ejbResponse;
                }
            }

            Method m = getHashes().get(methodHash);

            if (m == null) {
                ejbResponse.setRPCException(new RPCException("Cannot find method called on the bean '" + getClassName() + "'."));
                return ejbResponse;
            }

            Object value = null;

            // set ClassLoader
            ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader(getContainer().getClassLoader());

            // Busines interface
            String oldInvokedBusinessInterface = getInvokedBusinessInterfaceNameThreadLocal().get();
            getInvokedBusinessInterfaceNameThreadLocal().set(localCallRequest.getInvokedBusinessInterfaceName());

            // Operation state
            OperationState oldState = getOperationState();
            getOperationStateThreadLocal().set(BUSINESS_METHOD);



            // Dispatch the bean invocation begin event.
            String methodEventProviderId = getJ2EEManagedObjectId() + "/" + J2EEManagedObjectNamingHelper.getMethodSignature(m)
            + "@Local";
            boolean enabledEvent = !localCallRequest.isCalledFromRemoteRequest();

            EZBEventBeanInvocation event = null;
            long number = 0;
            IAuditID previousID = null;

            // Compute and send begin event only if required
            if (enabledEvent) {
                // Invocation ID
                if (getCurrentInvocationID() != null) {
                    previousID = getCurrentInvocationID().newInvocation();
                }
                event = getInvocationEventBegin(methodEventProviderId, localCallRequest.getMethodArgs());
                number = event.getInvocationNumber();
                getEventDispatcher().dispatch(event);
            }

            try {
                value = m.invoke(this.singletonBean, localCallRequest.getMethodArgs());
            } catch (IllegalArgumentException e) {
                ejbResponse.setRPCException(new RPCException(e));
                if (enabledEvent) {
                    getEventDispatcher().dispatch(new EventBeanInvocationError(methodEventProviderId, number, e));
                }
            } catch (IllegalAccessException e) {
                ejbResponse.setRPCException(new RPCException(e));
                if (enabledEvent) {
                    getEventDispatcher().dispatch(new EventBeanInvocationError(methodEventProviderId, number, e));
                }
            } catch (InvocationTargetException e) {
                Throwable cause = e.getCause();
                RPCException rpcException = new RPCException(cause);
                // ApplicationException ?
                IApplicationExceptionInfo applicationException = getBeanInfo().getApplicationException(cause);
                if (applicationException != null) {
                    rpcException.setApplicationException();
                }
                ejbResponse.setRPCException(rpcException);
                if (enabledEvent) {
                    getEventDispatcher().dispatch(new EventBeanInvocationError(methodEventProviderId, number, e));
                }
            } finally {
                if (enabledEvent) {
                    getEventDispatcher().dispatch(new EventBeanInvocationEnd(methodEventProviderId, number, value));
                    // Restore previous ID
                    if (getCurrentInvocationID() != null) {
                        getCurrentInvocationID().setAuditID(previousID);
                    }
                }

                Thread.currentThread().setContextClassLoader(oldClassLoader);
                getInvokedBusinessInterfaceNameThreadLocal().set(oldInvokedBusinessInterface);
                getOperationStateThreadLocal().set(oldState);

            }
            ejbResponse.setValue(value);
        } finally {
            // release lock
            lock.unlock();
        }
        return ejbResponse;

    }

    /**
     * Start the factory.
     * @throws FactoryException if the startup fails.
     */
    @Override
    public void start() throws FactoryException {
        super.start();

        if (getSessionBeanInfo().isStartup()) {
            try {
                this.singletonBean = getBean(null);
            } catch (RuntimeException e) {
                throw new FactoryException("Cannot initialize Singleton bean", e);
            }
        }
    }



    /**
     * Notified when the timer service send a Timer object.
     * It has to call the Timed method.
     * @param timer the given timer object that will be given to the timer method.
     */
    public void notifyTimeout(final Timer timer) {
        // Call the EasyBeans timer method on a given bean instance
        if (this.singletonBean == null) {
            this.singletonBean = getBean(null);
        }

        //set ClassLoader
        ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(getContainer().getClassLoader());

        // Call the timer method on the bean
        try {
            this.singletonBean.timeoutCallByEasyBeans(timer);
        } finally {
            // Reset classloader
            Thread.currentThread().setContextClassLoader(oldClassLoader);
        }
    }


    /**
     * Stops the factory.
     */
    @Override
    public void stop() {

        // push back into the pool
        if (this.singletonBean != null) {
            try {
                getPool().release(this.singletonBean);
            } catch (PoolException e) {
                LOGGER.error("Unable to release singleton bean", e);
            }
        }
        super.stop();
    }

}
