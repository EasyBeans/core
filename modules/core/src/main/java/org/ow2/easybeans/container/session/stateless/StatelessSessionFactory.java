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
 * $Id: StatelessSessionFactory.java 5518 2010-05-31 12:34:03Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.container.session.stateless;

import static org.ow2.easybeans.api.OperationState.BUSINESS_METHOD;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;

import javax.ejb.NoSuchEJBException;
import javax.ejb.Timer;

import org.ow2.easybeans.api.EZBContainer;
import org.ow2.easybeans.api.FactoryException;
import org.ow2.easybeans.api.OperationState;
import org.ow2.easybeans.api.bean.EasyBeansSLSB;
import org.ow2.easybeans.api.bean.info.IApplicationExceptionInfo;
import org.ow2.easybeans.api.event.bean.EZBEventBeanInvocation;
import org.ow2.easybeans.container.session.JPoolWrapperFactory;
import org.ow2.easybeans.container.session.PoolWrapper;
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
import org.ow2.util.pool.impl.enhanced.EnhancedPool;
import org.ow2.util.pool.impl.enhanced.manager.optional.IPoolItemRemoveManager;



/**
 * This class manages the stateless session bean and its creation/lifecycle.
 * @author Florent Benoit
 */
public class StatelessSessionFactory extends SessionFactory<EasyBeansSLSB> implements IPoolItemRemoveManager<EasyBeansSLSB> {

    /**
     * Logger.
     */
    private static Log logger = LogFactory.getLog(StatelessSessionFactory.class);

    /**
     * Builds a new factory with a given name and its container.
     * @param className name of this factory (name of class that is managed)
     * @param container the root component of this factory.
     * @throws FactoryException if class can't be loaded.
     */
    public StatelessSessionFactory(final String className, final EZBContainer container) throws FactoryException {
        super(className, container);

        // Use of the old pool ?
        if (Boolean.getBoolean(OLD_POOL)) {
            setPool(new JPool<EasyBeansSLSB, Long>(new JPoolWrapperFactory<EasyBeansSLSB, Long>(this)));
        } else {
            // new pool
            EnhancedPool<EasyBeansSLSB> statelessPool = getManagementPool().getEnhancedPoolFactory().createEnhancedPool(this);
            setPool(new PoolWrapper<EasyBeansSLSB>(statelessPool));
        }

    }


    /**
     * Gets a bean for the given id.
     * @param beanId id of the expected bean.
     * @return a Stateless bean.
     * @throws IllegalArgumentException if bean is not found.
     */
    @Override
    protected EasyBeansSLSB getBean(final Long beanId) throws IllegalArgumentException {
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

        // build EJB Response and set the id
        EJBResponse ejbResponse = new JEJBResponse();

        EasyBeansSLSB bean = null;
        try {
            bean = getBean(null);
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

        // Invoke method
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
            // send events only if not called remotely
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

            // push back into the pool
            try {
                getPool().release(bean);
            } catch (PoolException e) {
                ejbResponse.setRPCException(new RPCException("cannot release bean", e));
            }

        }
        ejbResponse.setValue(value);
        return ejbResponse;
    }

    /**
     * Callback called when object is gonna be removed.
     * @param instance that is being removed from the pool.
     */
    @Override
    public void remove(final EasyBeansSLSB instance) {
        super.remove(instance);
        instance.setEasyBeansRemoved(true);
    }

    /**
     * Start the factory.
     * @throws FactoryException if the startup fails.
     */
    @Override
    public void start() throws FactoryException {
        super.start();


        // Not yet instantiated ?
        if (getSessionBeanInfo().getTimersInfo().size() > 0) {
            try {
                EasyBeansSLSB bean = getBean(null);
                try {
                    getPool().release(bean);
                } catch (PoolException e) {
                    throw new FactoryException("Cannot initialize a Stateless Session bean required by schedule timers", e);
                }
            } catch (RuntimeException e) {
                throw new FactoryException("Cannot initialize a Stateless Session bean required by schedule timers", e);
            }
        }


    }

    /**
     * Stops the factory.
     */
    @Override
    public void stop() {
        try {
            // Stop all timers
            Collection<Timer> timers = getTimerService().getTimers();
            for (Timer timer : timers) {
                timer.cancel();
            }
        } finally {
            // And then stop factory
            super.stop();
        }

    }

}
