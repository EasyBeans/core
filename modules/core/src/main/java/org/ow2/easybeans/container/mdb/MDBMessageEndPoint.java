/**
 * EasyBeans
 * Copyright (C) 2006-2007 Bull S.A.S.
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
 * $Id:MDBMessageEndPoint.java 1477 2007-06-16 16:50:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.container.mdb;

import java.lang.reflect.Method;

import javax.ejb.Timer;
import javax.resource.ResourceException;
import javax.resource.spi.ApplicationServerInternalException;
import javax.resource.spi.UnavailableException;
import javax.resource.spi.endpoint.MessageEndpoint;

import org.ow2.easybeans.api.Factory;
import org.ow2.easybeans.api.bean.EasyBeansMDB;

/**
 * Implementation of the MessageEndPoint interface.<br/> These methods will be
 * called by the Resource Adapter.
 * @author Florent Benoit
 */
public class MDBMessageEndPoint implements MessageEndpoint, EZBMessageEndPoint {

    /**
     * MDB object Wrapped by this message end point.
     */
    private EasyBeansMDB easyBeansMDB = null;

    /**
     * Reference to the message end point factory.
     */
    private MDBMessageEndPointFactory mdbMessageEndPointFactory = null;

    /**
     * Constructor : Build an endpoint with a reference to the message end point
     * factory.
     * @param mdbMessageEndPointFactory the message end point factory.
     * @param easyBeansMDB the message driven bean object that is wrapped.
     */
    public MDBMessageEndPoint(final MDBMessageEndPointFactory mdbMessageEndPointFactory, final EasyBeansMDB easyBeansMDB) {
        this.mdbMessageEndPointFactory = mdbMessageEndPointFactory;
        this.easyBeansMDB = easyBeansMDB;
    }

    /**
     * This is called by a resource adapter before a message is delivered.
     * @param method description of a target method. This information about the
     *        intended target method allows an application server to decide
     *        whether to start a transaction during this method call, depending
     *        on the transaction preferences of the target method. The
     *        processing (by the application server) of the actual message
     *        delivery method call on the endpoint must be independent of the
     *        class loader associated with this descriptive method object.
     * @throws NoSuchMethodException - indicates that the specified method does
     *         not exist on the target endpoint.
     * @throws ResourceException - generic exception.
     * @throws ApplicationServerInternalException - indicates an error condition
     *         in the application server.
     * @throws IllegalStateException - indicates that the endpoint is in an
     *         illegal state for the method invocation. For example, this occurs
     *         when beforeDelivery and afterDelivery method calls are not
     *         paired.
     * @throws UnavailableException - indicates that the endpoint is not
     *         available.
     */
    public void beforeDelivery(final Method method) throws ApplicationServerInternalException, UnavailableException,
            NoSuchMethodException, IllegalStateException, ResourceException {
    }

    /**
     * This is called by a resource adapter after a message is delivered.
     * @throws ResourceException - generic exception.
     * @throws ApplicationServerInternalException - indicates an error condition
     *         in the application server.
     * @throws IllegalStateException - indicates that the endpoint is in an
     *         illegal state for the method invocation. For example, this occurs
     *         when beforeDelivery and afterDelivery method calls are not
     *         paired.
     * @throws UnavailableException - indicates that the endpoint is not
     *         available.
     */
    public void afterDelivery() throws ApplicationServerInternalException, IllegalStateException, UnavailableException,
            ResourceException {
    }

    /**
     * This method may be called by the resource adapter to indicate that it no
     * longer needs a proxy endpoint instance. This hint may be used by the
     * application server for endpoint pooling decisions.
     */
    public void release() {
        this.mdbMessageEndPointFactory.releaseEndPoint(this);
    }

    /**
     * Gets the factory of the bean.
     * @return factory of the bean.
     */
    public Factory getEasyBeansFactory() {
        return this.mdbMessageEndPointFactory;
    }

    /**
     * Gets the factory of the bean.
     * @return factory of the bean.
     */
    public MDBMessageEndPointFactory getMDBMessageEndPointFactory() {
        return this.mdbMessageEndPointFactory;
    }

    /**
     * Gets the wrapped Message Driven Bean object.
     * @return wrapped Message Driven Bean object.
     */
    public EasyBeansMDB getEasyBeansMDB() {
        return this.easyBeansMDB;
    }

    /**
     * Invokes the timeout method on the bean.
     * @param timer the given EJB timer
     */
    public void notifyTimeout(final Timer timer) {
        // set classloader to EJB classloader
        ClassLoader oldCL = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(getEasyBeansFactory().getContainer().getClassLoader());
        // Call the timeout method
        try {
            getEasyBeansMDB().timeoutCallByEasyBeans(timer);
        } finally {
            Thread.currentThread().setContextClassLoader(oldCL);
        }
    }
}
