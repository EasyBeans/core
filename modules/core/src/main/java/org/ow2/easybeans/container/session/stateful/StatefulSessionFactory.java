/**
 * EasyBeans
 * Copyright (C) 2006-2012 Bull S.A.S.
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
 * $Id: StatefulSessionFactory.java 5518 2010-05-31 12:34:03Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.container.session.stateful;

import static org.ow2.easybeans.api.OperationState.BUSINESS_METHOD;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.ejb.EJBException;
import javax.ejb.NoSuchEJBException;
import javax.ejb.Timer;
import javax.transaction.Synchronization;
import javax.transaction.Transaction;

import org.ow2.easybeans.api.EZBContainer;
import org.ow2.easybeans.api.EZBStatefulSessionFactory;
import org.ow2.easybeans.api.FactoryException;
import org.ow2.easybeans.api.OperationState;
import org.ow2.easybeans.api.bean.EasyBeansSFSB;
import org.ow2.easybeans.api.bean.info.IAccessTimeoutInfo;
import org.ow2.easybeans.api.bean.info.IApplicationExceptionInfo;
import org.ow2.easybeans.api.bean.info.IMethodInfo;
import org.ow2.easybeans.api.event.bean.EZBEventBeanInvocation;
import org.ow2.easybeans.container.session.JPoolWrapperFactory;
import org.ow2.easybeans.container.session.SessionFactory;
import org.ow2.easybeans.event.bean.EventBeanInvocationEnd;
import org.ow2.easybeans.event.bean.EventBeanInvocationError;
import org.ow2.easybeans.naming.J2EEManagedObjectNamingHelper;
import org.ow2.easybeans.persistence.api.EZBExtendedEntityManager;
import org.ow2.easybeans.rpc.JEJBResponse;
import org.ow2.easybeans.rpc.api.EJBLocalRequest;
import org.ow2.easybeans.rpc.api.EJBResponse;
import org.ow2.easybeans.rpc.api.RPCException;
import org.ow2.util.auditreport.api.IAuditID;
import org.ow2.util.log.Log;
import org.ow2.util.log.LogFactory;
import org.ow2.util.pool.api.PoolException;
import org.ow2.util.pool.impl.JPool;
import org.ow2.util.pool.impl.enhanced.EnhancedCluePool;
import org.ow2.util.pool.impl.enhanced.api.clue.basiccluemanager.IClueAccessor;
import org.ow2.util.pool.impl.enhanced.impl.clue.basiccluemanager.BasicClueManager;
import org.ow2.util.pool.impl.enhanced.manager.clue.optional.IPoolItemRemoveClueManager;

/**
 * This class manages the stateless session bean and its creation/lifecycle.
 * @author Florent Benoit
 */
public class StatefulSessionFactory extends SessionFactory<EasyBeansSFSB> implements
        EZBStatefulSessionFactory<EasyBeansSFSB, Long>, IPoolItemRemoveClueManager<EasyBeansSFSB, Long>,
        IClueAccessor<EasyBeansSFSB, Long> {

    /**
     * Logger.
     */
    private static Log logger = LogFactory.getLog(StatefulSessionFactory.class);

    /**
     * Id generator.
     */
    private long idCount = 0L;

    /**
     * Manager of clue.
     */
    private BasicClueManager<EasyBeansSFSB, Long> basicClueManager;


    /**
     * Locks by beans.
     */
    private Map<Long, Lock> locks;

    /**
     * Extended Persistence contexts for a given bean.
     */
    private Map<EasyBeansSFSB, List<EZBExtendedEntityManager>> extendedPersistenceContexts = null;


    /**
     * Manages the current bean ID being invoked.
     */
    private InheritableThreadLocal<Long> currentBeanId = null;

    /**
     * Synchronization Listener which will receive event of the transaction manager.
     */
    private Map<Transaction, Synchronization> sessionSynchronizationListeners = null;

    /**
     * Builds a new factory with a given name and its container.
     * @param className name of this factory (name of class that is managed)
     * @param container the root component of this factory.
     * @throws FactoryException if class can't be loaded.
     */
    public StatefulSessionFactory(final String className, final EZBContainer container) throws FactoryException {
        this(className, container, false);
    }

    /**
     * Builds a new factory with a given name and its container.
     * @param className name of this factory (name of class that is managed)
     * @param container the root component of this factory.
     * @param useExtendedPersistenceContext if the SFSB is using an extended Persistence context
     * @throws FactoryException if class can't be loaded.
     */
    public StatefulSessionFactory(final String className, final EZBContainer container,
            final boolean useExtendedPersistenceContext) throws FactoryException {
        super(className, container);

        this.locks = new HashMap<Long, Lock>();
        this.currentBeanId = new InheritableThreadLocal<Long>();

        // Init Stateful session synchronization
        this.sessionSynchronizationListeners = new WeakHashMap<Transaction, Synchronization>();

        // Extended persistence contexts
        this.extendedPersistenceContexts = new HashMap<EasyBeansSFSB, List<EZBExtendedEntityManager>>();

        // Use of the old pool ?
        // If we're using extended persistence context, we need to use lazy
        // initialization so that we can track creation of new stateful beans
        // from a stateful session bean
        if (Boolean.getBoolean(OLD_POOL) || useExtendedPersistenceContext) {
            setPool(new JPool<EasyBeansSFSB, Long>(new JPoolWrapperFactory<EasyBeansSFSB, Long>(this)));
        } else {
            // new pool
            EnhancedCluePool<EasyBeansSFSB, Long> enhancedPool = getManagementPool().getEnhancedCluePoolFactory()
                    .createEnhancedCluePool(this);
            this.basicClueManager = new BasicClueManager<EasyBeansSFSB, Long>(this, false, false);
            // stateful = only one client at a given time.
            enhancedPool.setAllowSharedInstance(false);
            setPool(enhancedPool);
        }
    }

    /**
     * Gets a new ID or a null value.
     * @param beanId given id.
     * @return new id
     */
    protected synchronized Long getId(final Long beanId) {
        Long newId = beanId;
        // no Id, compute a new one
        if (newId == null) {
            this.idCount++;
            newId = Long.valueOf(this.idCount);
        }
        return newId;
    }

    /**
     * Gets a bean for the given id.
     * @param beanId id of the expected bean.
     * @return a Stateless bean.
     * @throws IllegalArgumentException if bean is not found.
     */
    @Override
    protected synchronized EasyBeansSFSB getBean(final Long beanId) throws IllegalArgumentException {
        EasyBeansSFSB bean = null;
        try {
            bean = getPool().get(beanId);
        } catch (PoolException e) {
            throw new IllegalArgumentException("Cannot get element in the pool", e);
        }
        logger.debug("Set for bean {0} the Id = {1}", bean, beanId);
        bean.setEasyBeansStatefulID(beanId);
        return bean;
    }

    /**
     * Callback called when object is gonna be removed.
     * @param instance that is being removed from the pool.
     */
    @Override
    public void remove(final EasyBeansSFSB instance) {

        // Close any extended persistence context that are associated to the current instance
        List<EZBExtendedEntityManager> extendedEntityManagers = getExtendedPersistenceContexts(instance);
        try {
            if (extendedEntityManagers != null) {
                for (EZBExtendedEntityManager extendedEntityManager : extendedEntityManagers) {
                    extendedEntityManager.close();
                }
            }
        } finally {

            super.remove(instance);
            instance.setEasyBeansRemoved(true);

            // Remove lock for this instance
            synchronized (this.locks) {
                this.locks.remove(instance.getEasyBeansStatefulID());
            }
        }
    }


    /**
     * before set for the current container the extended persistence contexts.
     * Injects Resources into the Bean.
     * @param instance The Bean instance to be injected.
     * @throws PoolException if resources cannot be injected.
     */
    @Override
    protected void injectResources(final EasyBeansSFSB instance) throws PoolException {

        // Get current Map of Extended Persistence context for this current
        // instance and linked instance
        Map<String, EZBExtendedEntityManager> oldExtendedPersistenceContexts = getContainer()
                .getCurrentExtendedPersistenceContexts();
        if (oldExtendedPersistenceContexts == null) {
            // It means that we're the first stateful in the chain
            Map<String, EZBExtendedEntityManager> extendedPersistenceContexts = new HashMap<String, EZBExtendedEntityManager>();
            getContainer().setCurrentExtendedPersistenceContexts(extendedPersistenceContexts);
        }
        try {
            super.injectResources(instance);
        } finally {
            // Reset with the old persistence contexts (if any)
            getContainer().setCurrentExtendedPersistenceContexts(oldExtendedPersistenceContexts);
        }

    }


    /**
     * Do a local call on a method of this factory.
     * @param localCallRequest the given request
     * @return response with the value of the call and the bean ID (if any)
     */
    @Override
    public EJBResponse localCall(final EJBLocalRequest localCallRequest) {
        Long id = getId(localCallRequest.getBeanId());
        Long methodHash = localCallRequest.getMethodHash();

        // build EJB Response and set the id
        EJBResponse ejbResponse = new JEJBResponse();
        ejbResponse.setBeanId(id);

        IMethodInfo methodInfo = getMethodInfoHashes().get(methodHash);
        IAccessTimeoutInfo accessTimeout = null;
        if (methodInfo != null) {
            accessTimeout = methodInfo.getAccessTimeout();
        }

        // existing lock ?
        Lock lock = null;
        synchronized (this.locks) {
            lock = this.locks.get(id);
            if (lock == null) {
                lock = new ReentrantLock();
                this.locks.put(id, lock);
            }
        }

        // getAccess is used if accessTimeout with value >=0 is used
        boolean getAccess = true;
        if (accessTimeout != null) {
            // Infinite wait
            if (accessTimeout.value() == -1) {
                lock.lock();
            } else if (accessTimeout.value() >= 0) {
                try {
                    logger.debug("Trying to lock bean with id ''{0}'' with value ''{1}'' and timeunit ''{2}''", id, Long
                            .valueOf(accessTimeout.value()), accessTimeout.unit());
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
                    rpcException = new RPCException(ConcurrentBuilderException.buildConcurrentTimeoutException(
                            "Unable to get a concurrent access with an accessTimeout of '" + accessTimeout + "' on bean '"
                                    + getClassName() + "' and method '" + getHashes().get(methodHash) + "'."));
                }
                ejbResponse.setRPCException(rpcException);
                return ejbResponse;
            }
        }

        // If we're here, it means that we've got the lock. so don't forget to unlock
        try {

            EasyBeansSFSB bean = null;
            try {
                bean = getBean(id);
            } catch (IllegalArgumentException e) {
                ejbResponse.setRPCException(new RPCException("Cannot get element in the pool", e));
                return ejbResponse;
            } catch (NoSuchEJBException e) {
                ejbResponse.setRPCException(new RPCException("Bean has been removed", e));
                return ejbResponse;
            }

            Method m = getHashes().get(methodHash);

            if (m == null) {
                ejbResponse.setRPCException(new RPCException("Cannot find method called on the bean '" + getClassName() + "'.",
                        new NoSuchMethodException("The method is not found on the bean")));
                return ejbResponse;
            }



            Object value = null;

            // set ClassLoader
            ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader(getContainer().getClassLoader());
            // Set InvokedBusiness Interface
            String oldInvokedBusinessInterface = getInvokedBusinessInterfaceNameThreadLocal().get();
            getInvokedBusinessInterfaceNameThreadLocal().set(localCallRequest.getInvokedBusinessInterfaceName());

            // Operation state
            OperationState oldState = getOperationState();
            getOperationStateThreadLocal().set(BUSINESS_METHOD);

            // bean ID being invoked
            Long oldBeanId = getCurrentBeanIDThreadLocal().get();
            getCurrentBeanIDThreadLocal().set(id);

            // Dispatch the bean invocation begin event.
            String methodEventProviderId = getJ2EEManagedObjectId() + "/" + J2EEManagedObjectNamingHelper.getMethodSignature(m)
            + "@Local";
            boolean enabledEvent = !localCallRequest.isCalledFromRemoteRequest();

            EZBEventBeanInvocation event = null;
            long number = 0;

            // Invocation ID
            IAuditID previousID = null;
            // Compute and send begin event only if required
            if (enabledEvent) {
                if (getCurrentInvocationID() != null) {
                    previousID = getCurrentInvocationID().newInvocation();
                }
                event = getInvocationEventBegin(methodEventProviderId, localCallRequest.getMethodArgs());
                number = event.getInvocationNumber();
                getEventDispatcher().dispatch(event);
            }

            try {
                value = m.invoke(bean, localCallRequest.getMethodArgs());
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
                Thread.currentThread().setContextClassLoader(oldClassLoader);
                getInvokedBusinessInterfaceNameThreadLocal().set(oldInvokedBusinessInterface);
                getOperationStateThreadLocal().set(oldState);
                getCurrentBeanIDThreadLocal().set(oldBeanId);

                // send events only if not called remotely
                if (enabledEvent) {
                    getEventDispatcher().dispatch(new EventBeanInvocationEnd(methodEventProviderId, number, value));
                    // Restore previous ID
                    if (getCurrentInvocationID() != null) {
                        getCurrentInvocationID().setAuditID(previousID);
                    }
                }

                // push back into the pool
                try {
                    getPool().release(bean);
                } catch (PoolException e) {
                    ejbResponse.setRPCException(new RPCException("cannot release bean", e));
                }

                // If the bean has been removed (stateful), flag it as a removed
                // bean, so the client won't call again any methods.
                if (bean.getEasyBeansRemoved()) {
                    ejbResponse.setRemoved(true);
                }
            }
            ejbResponse.setValue(value);
        } finally {
            // Release the lock
            lock.unlock();
        }

        return ejbResponse;

    }

    /**
     * Notified when the timer service send a Timer object.
     * It has to call the Timed method.
     * @param timer the given timer object that will be given to the timer method.
     */
    public void notifyTimeout(final Timer timer) {
        throw new EJBException("Stateful bean cannot receive Timer objects");
    }

    /**
     * Try to find if there is a matching instance by using the given instance and the given clue.
     * @param easyBeansSFSB the given stateful instance
     * @param clue the given clue
     * @return true if the instance is matching
     */
    public boolean tryMatch(final EasyBeansSFSB easyBeansSFSB, final Long clue) {
        return this.basicClueManager.tryMatch(easyBeansSFSB, clue);
    }

    /**
     * Unmatch the given instance.
     * @param easyBeansSFSB the given stateful instance
     */
    public void unMatch(final EasyBeansSFSB easyBeansSFSB) {
        this.basicClueManager.unMatch(easyBeansSFSB);
    }

    /**
     * @param easyBeansSFSB the given instance
     * @return the clue for the given instance.
     */
    public Long getClue(final EasyBeansSFSB easyBeansSFSB) {
        return easyBeansSFSB.getEasyBeansStatefulID();
    }

    /**
     * Sets the given clue on the given instance.
     * @param easyBeansSFSB the given instance
     * @param clue the given clue
     */
    public void setClue(final EasyBeansSFSB easyBeansSFSB, final Long clue) {
        easyBeansSFSB.setEasyBeansStatefulID(clue);
    }

    /**
     * @return the current beanID thread local.
     */
    public InheritableThreadLocal<Long> getCurrentBeanIDThreadLocal() {
        return this.currentBeanId;
    }


    /**
     * Gets the current session synchronization listener on the given transaction if any.
     * @param tx the given transaction
     * @return the current session synchronization listener
     */
    public Synchronization getSessionSynchronizationListener(final Transaction tx) {
        return this.sessionSynchronizationListeners.get(tx);
    }

    /**
     * Sets the current session synchronization listener on the given transaction.
     * @param tx the given transaction
     * @param sessionSynchronizationListener the session synchronization listener
     */
    public void setSessionSynchronizationListener(final Transaction tx, final Synchronization sessionSynchronizationListener) {
        this.sessionSynchronizationListeners.put(tx, sessionSynchronizationListener);
    }

    /**
     * Unsets the current session synchronization listener on the given transaction.
     * @param tx the given transaction
     */
    public void unsetSessionSynchronizationListener(final Transaction tx) {
        this.sessionSynchronizationListeners.remove(tx);
    }

    /**
     * Gets the list of extended persistence context for the given stateful session bean.
     * @param statefulSessionBean the given bean
     * @return the list of extended persistence contexts
     */
    public List<EZBExtendedEntityManager> getExtendedPersistenceContexts(final EasyBeansSFSB statefulSessionBean) {
        return this.extendedPersistenceContexts.get(statefulSessionBean);
    }

    /**
     * Adds the given extended persistence context for the given stateful
     * session bean.
     * @param statefulSessionBean the given bean
     * @param extendedEntityManager the given persistence context
     */
    public void addExtendedPersistenceContext(final EasyBeansSFSB statefulSessionBean,
            final EZBExtendedEntityManager extendedEntityManager) {
        List<EZBExtendedEntityManager> list = this.extendedPersistenceContexts.get(statefulSessionBean);
        if (list == null) {
            list = new ArrayList<EZBExtendedEntityManager>();
            this.extendedPersistenceContexts.put(statefulSessionBean, list);
        }
        list.add(extendedEntityManager);
    }


    /**
     * Stops the factory.
     */
    @Override
    public void stop() {
        super.stop();

        // Cleanup extended persistence contexts
        this.extendedPersistenceContexts.clear();

    }

}
