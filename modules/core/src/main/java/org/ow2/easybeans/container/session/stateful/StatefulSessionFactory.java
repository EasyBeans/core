/**
 * EasyBeans
 * Copyright (C) 2006-2009 Bull S.A.S.
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

import javax.ejb.ApplicationException;
import javax.ejb.EJBException;
import javax.ejb.NoSuchEJBException;
import javax.ejb.Timer;

import org.ow2.easybeans.api.EZBContainer;
import org.ow2.easybeans.api.FactoryException;
import org.ow2.easybeans.api.OperationState;
import org.ow2.easybeans.api.bean.EasyBeansSFSB;
import org.ow2.easybeans.api.event.bean.EZBEventBeanInvocation;
import org.ow2.easybeans.container.session.JPoolWrapperFactory;
import org.ow2.easybeans.container.session.SessionFactory;
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
import org.ow2.util.pool.impl.enhanced.EnhancedCluePool;
import org.ow2.util.pool.impl.enhanced.api.clue.basiccluemanager.IClueAccessor;
import org.ow2.util.pool.impl.enhanced.impl.clue.basiccluemanager.BasicClueManager;
import org.ow2.util.pool.impl.enhanced.manager.clue.optional.IPoolItemRemoveClueManager;

/**
 * This class manages the stateless session bean and its creation/lifecycle.
 * @author Florent Benoit
 */
public class StatefulSessionFactory extends SessionFactory<EasyBeansSFSB> implements
        IPoolItemRemoveClueManager<EasyBeansSFSB, Long>, IClueAccessor<EasyBeansSFSB, Long> {

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
     * Builds a new factory with a given name and its container.
     * @param className name of this factory (name of class that is managed)
     * @param container the root component of this factory.
     * @throws FactoryException if class can't be loaded.
     */
    public StatefulSessionFactory(final String className, final EZBContainer container) throws FactoryException {
        super(className, container);

        // Use of the old pool ?
        if (Boolean.getBoolean(OLD_POOL)) {
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
        super.remove(instance);
        instance.setEasyBeansRemoved(true);
    }

    /**
     * Do a local call on a method of this factory.
     * @param localCallRequest the given request
     * @return response with the value of the call and the bean ID (if any)
     */
    @Override
    public EJBResponse localCall(final EJBLocalRequest localCallRequest) {
        Long id = getId(localCallRequest.getBeanId());

        // build EJB Response and set the id
        EJBResponse ejbResponse = new JEJBResponse();
        ejbResponse.setBeanId(id);

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

        Method m = getHashes().get(localCallRequest.getMethodHash());

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

        synchronized (bean) {
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
                ApplicationException applicationException = getBeanInfo().getApplicationExceptions().get(
                        cause.getClass().getName());
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
        }
        ejbResponse.setValue(value);
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
}
