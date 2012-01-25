/**
 * EasyBeans
 * Copyright (C) 2006 Bull S.A.S.
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
 * $Id: EJBRemoteRequestImpl.java 5650 2010-11-04 14:50:58Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.rpc;

import static org.ow2.util.marshalling.Serialization.loadObject;
import static org.ow2.util.marshalling.Serialization.storeObject;

import java.io.IOException;

import org.ow2.easybeans.rpc.api.EJBRemoteRequest;
import org.ow2.easybeans.rpc.api.RPCException;
/**
 * Implementation of the EJBRequest interface.
 * @author Florent Benoit
 */
public class EJBRemoteRequestImpl implements EJBRemoteRequest {

    /**
     * Id for serializable class.
     */
    private static final long serialVersionUID = -3588466669344863787L;

    /**
     * Name of the method.
     */
    private String methodName = null;

    /**
     * Hashing of the method.
      * @see <a href="http://java.sun.com/j2se/1.5.0/docs/guide/rmi/spec/rmi-stubs24.html">Method hashing of RMI</a>
     */
    private Long methodHash;

    /**
     * Arguments of the method.
     */
    private byte[] byteArgs;

    /**
     * Arguments of the method (not serializable).
     */
    private transient Object[] args = null;

    /**
     * Id of the container that will be used on the remote side.
     */
    private String containerId = null;

    /**
     * Name of the factory for which is dedicated this request.
     */
    private String factoryName = null;

    /**
     * Id of the bean (ie, for stateful).
     */
    private Long beanId = null;

    /**
     * Invoked business interface name (if any).
     */
    private String invokedBusinessInterfaceName = null;


    /**
     * Builds a new request that will be sent on remote side.
     * @param methodName the name of the method.
     * @param methodHash the hash of the method.
     * @param args the arguments of the method.
     * @param containerId id of the remote container.
     * @param factoryName the name of the remote factory.
     * @param beanId the bean identifier.
     * @param invokedBusinessInterfaceName the name of the invoked business
     *        interface (may be null)
     * @throws RPCException if the request cannot be built.
     */
    public EJBRemoteRequestImpl(final String methodName, final Long methodHash, final Object[] args, final String containerId,
            final String factoryName, final Long beanId, final String invokedBusinessInterfaceName) throws RPCException {
        this.methodHash = methodHash;
        this.methodName = methodName;

        try {
            this.byteArgs = storeObject(args);
        } catch (IOException e) {
            throw new RPCException("Cannot serialize the arguments of the request.", e);
        }
        this.args = args;
        this.containerId = containerId;
        this.factoryName = factoryName;
        this.beanId = beanId;
        this.invokedBusinessInterfaceName = invokedBusinessInterfaceName;
    }

    /**
     * @return name of the method
     */
    public String getMethodName() {
        return this.methodName;
    }

    /**
     * @see <a
     *      href="http://java.sun.com/j2se/1.5.0/docs/guide/rmi/spec/rmi-stubs24.html">Method
     *      hashing of RMI</a>
     * @return the hash of this method
     */
    public Long getMethodHash() {
        return this.methodHash;
    }

    /**
     * @return the argument of the request (send by the client).
     * @throws IllegalStateException if arguments were serialized and not available.
     */
    public Object[] getMethodArgs() throws IllegalStateException {
        // try to read object from array of bytes
        try {
            this.args = (Object[]) loadObject(this.byteArgs);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot get arguments of the request", e);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Cannot get arguments of the request", e);
        }
        return this.args;
    }

    /**
     * @return the container id of this request. It will be used to know the
     *         container for which this request is sent.
     */
    public String getContainerId() {
        return this.containerId;
    }

    /**
     * @return the factory name of the container.
     */
    public String getFactory() {
        return this.factoryName;
    }

    /**
     * @return the id of the bean.
     */
    public Long getBeanId() {
        return this.beanId;
    }

    /**
     * @return name of the business interface used for invoking the method (if any).
     */
    public String getInvokedBusinessInterfaceName() {
        return this.invokedBusinessInterfaceName;
    }

}
