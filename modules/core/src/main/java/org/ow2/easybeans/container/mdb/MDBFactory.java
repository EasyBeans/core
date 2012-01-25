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
 * $Id: MDBFactory.java 5747 2011-02-28 17:12:27Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.container.mdb;

import org.ow2.easybeans.api.EZBContainer;
import org.ow2.easybeans.api.FactoryException;
import org.ow2.easybeans.api.bean.EasyBeansMDB;
import org.ow2.easybeans.api.bean.info.IBeanInfo;
import org.ow2.easybeans.container.AbsFactory;
import org.ow2.easybeans.container.info.MessageDrivenInfo;
import org.ow2.easybeans.container.session.JPoolWrapperFactory;
import org.ow2.easybeans.container.session.PoolWrapper;
import org.ow2.easybeans.rpc.api.EJBLocalRequest;
import org.ow2.easybeans.rpc.api.EJBRemoteRequest;
import org.ow2.easybeans.rpc.api.EJBResponse;
import org.ow2.util.log.Log;
import org.ow2.util.log.LogFactory;
import org.ow2.util.pool.api.PoolException;
import org.ow2.util.pool.impl.JPool;
import org.ow2.util.pool.impl.enhanced.EnhancedPool;
import org.ow2.util.pool.impl.enhanced.api.basic.CreatePoolItemException;
import org.ow2.util.pool.impl.enhanced.manager.optional.IPoolItemRemoveManager;

/**
 * This classes is reponsible to manage the MDB objects.<br />
 * Each MDB object (EasyBeansMDB) has a link to a MessageEndPoint object. The
 * internal message endpoint object is used by the resource adapter with the
 * help of the message end point factory.
 * @author Florent Benoit
 */
public abstract class MDBFactory extends AbsFactory<EasyBeansMDB> implements IPoolItemRemoveManager<EasyBeansMDB> {

    /**
     * Logger.
     */
    private static Log logger = LogFactory.getLog(MDBFactory.class);

    /**
     * Runtime information about the MDB.
     */
    private MessageDrivenInfo messageDrivenInfo = null;

    /**
     * Builds a new MDB factory with a given name and its container.
     * @param className name of this factory (name of class that is managed)
     * @param container the root component of this factory.
     * @throws FactoryException if class can't be loaded.
     */
    public MDBFactory(final String className, final EZBContainer container) throws FactoryException {
        super(className, container);

        // Use of the old pool ?
        if (Boolean.getBoolean(OLD_POOL)) {
            setPool(new JPool<EasyBeansMDB, Long>(new JPoolWrapperFactory<EasyBeansMDB, Long>(this)));
        } else {
            // new pool
            EnhancedPool<EasyBeansMDB> mdbPool = getManagementPool().getEnhancedPoolFactory().createEnhancedPool(this);
            setPool(new PoolWrapper<EasyBeansMDB>(mdbPool));
        }

    }

    /**
     * A request comes to the bean factory and needs to be handled.<br>
     * A response is done which contains the answer.
     * @param request the EJB request.
     * @return a response that have been processed by the factory.
     */
    @Override
    public EJBResponse rpcInvoke(final EJBRemoteRequest request) {
        // Not used by MDB : Invocation is done by Resource Adapter on Message
        // End Point
        return null;
    }

    /**
     * Do a local call on a method of this factory.
     * @param localCallRequest the given request
     * @return response with the value of the call and the bean ID (if any)
     */
    public EJBResponse localCall(final EJBLocalRequest localCallRequest) {
        // Not used by MDB : Invocation is done by Resource Adapter on Message
        // End Point
        return null;
    }

    /**
     * Stops the factory.
     */
    @Override
    public void stop() {
        super.stop();

        // remove pool
        try {
            getPool().stop();
        } catch (PoolException e) {
            logger.error("Problem when stopping the factory", e);
        }
    }

    /**
     * @return information of the current bean.
     */
    public IBeanInfo getBeanInfo() {
        return this.messageDrivenInfo;
    }

    /**
     * @return information of the current bean.
     */
    public MessageDrivenInfo getMessageDrivenInfo() {
        return this.messageDrivenInfo;
    }

    /**
     * Sets the information object for a session bean.
     * @param messageDrivenInfo information on the bean.
     */
    public void setMessageDrivenInfo(final MessageDrivenInfo messageDrivenInfo) {
        this.messageDrivenInfo = messageDrivenInfo;
    }

    /**
     * Creates an instance.
     * @throws CreatePoolItemException if instance cannot be created.
     * @return the created instance.
     */
    public EasyBeansMDB createPoolItem() throws CreatePoolItemException {
        EasyBeansMDB instance = null;
        try {
            instance = getBeanClass().newInstance();
        } catch (InstantiationException e) {
            throw new CreatePoolItemException(WAITING_TIME_BEFORE_CREATION, "Cannot create a new instance", e);
        } catch (IllegalAccessException e) {
            throw new CreatePoolItemException(WAITING_TIME_BEFORE_CREATION, "Cannot create a new instance", e);
        } catch (Error e) {
            logger.error("Unable to create a new instance of the class ''{0}''", getBeanClass().getName(), e);
            // null as factory is broken
            throw new CreatePoolItemException(null, "Cannot create a new instance", e);
        }

        // Set the factory
        instance.setEasyBeansFactory(this);
        instance.setEasyBeansInvocationContextFactory(getInvocationContextFactory());

        // Init the MDB context
        EasyBeansMDBContext mdbContext = new EasyBeansMDBContext(this);
        instance.setEasyBeansContext(mdbContext);

        ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(getContainer().getClassLoader());
        try {
            // Call injection
            try {
                injectResources(instance);
            } catch (PoolException e) {
                throw new CreatePoolItemException(WAITING_TIME_BEFORE_CREATION, "Cannot inject resource in the instance", e);
            }


            // post construct callback
            instance.postConstructEasyBeansLifeCycle();
        } finally {
            Thread.currentThread().setContextClassLoader(oldClassLoader);
        }

        return instance;
    }

    /**
     * Callback used when the given element will be removed.
     * @param mdb the given instance to be removed
     */
    public void poolItemRemoved(final EasyBeansMDB mdb) {
        super.remove(mdb);
    }


}
