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
 * $Id: Factory.java 5488 2010-05-04 16:06:31Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.api;

import java.util.Map;

import javax.ejb.Timer;
import javax.ejb.TimerService;
import javax.naming.Context;

import org.ow2.easybeans.api.bean.info.IBeanInfo;
import org.ow2.easybeans.api.interceptor.EZBInvocationContextFactory;
import org.ow2.easybeans.rpc.api.EJBLocalRequest;
import org.ow2.easybeans.rpc.api.EJBRemoteRequest;
import org.ow2.easybeans.rpc.api.EJBResponse;
import org.ow2.util.pool.api.Pool;

/**
 * This interface is used by all bean factories of EJB 3 container.
 * @param <PoolType> The type of the pool.
 * @param <Clue> The clue used by the pool.
 * @author Florent Benoit
 *
 */
public interface Factory<PoolType, Clue> extends EZBJ2EEManagedObject {

    /**
     * A remote request comes to the bean factory and needs to be handled.<br>
     * A response is done which contains the answer.
     * @param request the EJB request.
     * @return a response that have been processed by the factory.
     */
    EJBResponse rpcInvoke(final EJBRemoteRequest request);

    /**
     * Do a local call on a method of this factory.
     * @param localCallRequest the given request
     * @return response with the value of the call and the bean ID (if any)
     */
    EJBResponse localCall(final EJBLocalRequest localCallRequest);

    /**
     * Notified when the timer service send a Timer object.
     * It has to call the Timed method.
     * @param timer the given timer object that will be given to the timer method.
     */
    void notifyTimeout(final Timer timer);

    /**
     * Init the factory.
     * @throws FactoryException if the initialization fails.
     */
    void init() throws FactoryException;

    /**
     * Starts the factory.
     * @throws FactoryException if factory doesn't start
     */
    void start() throws FactoryException;

    /**
     * Stops the factory.
     */
    void stop();

    /**
     * Gets the className used by this factory.
     * @return classname that will be instantiated to build bean instance.
     */
    String getClassName();

    /**
     * Gets the container used by this factory.
     * @return container of this factory
     */
    EZBContainer getContainer();

    /**
     * Gets the java: context.
     * @return java: context.
     */
     Context getJavaContext();

     /**
      * Sets the java: context.
      * @param javaContext the java: context.
      */
     void setJavaContext(final Context javaContext);

     /**
      * Gets the bean information.
      * @return bean information
      */
      IBeanInfo getBeanInfo();

      /**
       * Gets the pool used by this factory.
       * @return pool.
       */
      Pool<PoolType, Clue> getPool();

      /**
       * Gets an id for this factory.
       * @return string id.
       */
      String getId();

      /**
       * Gets the timer service of this factory.
       * @return the timer service.
       */
      TimerService getTimerService();

      /**
       * Defines the invocation context factory (for dynamic mode).
       * @param invocationContextFactory the given invocation context factory
       */
      void setInvocationContextFactory(EZBInvocationContextFactory invocationContextFactory);

      /**
       * @return the invocation context factory (for dynamic mode).
       */
      EZBInvocationContextFactory getInvocationContextFactory();

      /**
       * @return the current operation state.
       */
      OperationState getOperationState();

      /**
       * @return the current operation state thread local.
       */
      InheritableThreadLocal<OperationState> getOperationStateThreadLocal();

      /**
       * @return the current context map of the current invocation
       */
      InheritableThreadLocal<Map<String, Object>> getContextDataThreadLocal();
}
