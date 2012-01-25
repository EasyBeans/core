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
 * $Id: LocalCallInvocationHandler.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.proxy.client;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.Method;
import java.util.HashMap;

import javax.ejb.EJBLocalObject;
import javax.ejb.NoSuchEJBException;
import javax.ejb.NoSuchObjectLocalException;

import org.ow2.easybeans.api.EZBContainer;
import org.ow2.easybeans.api.EZBServer;
import org.ow2.easybeans.api.EmbeddedManager;
import org.ow2.easybeans.api.Factory;
import org.ow2.easybeans.rpc.EJBLocalRequestImpl;
import org.ow2.easybeans.rpc.api.EJBLocalRequest;
import org.ow2.easybeans.rpc.api.EJBResponse;
import org.ow2.easybeans.rpc.api.RPCException;
import org.ow2.easybeans.rpc.util.Hash;

/**
 * Object acting as the proxy for local interfaces calls.
 * @author Florent Benoit
 */
public class LocalCallInvocationHandler extends AbsInvocationHandler implements Externalizable {

    /**
     * UID for serialization.
     */
    private static final long serialVersionUID = -4327634481654235615L;

    /**
     * Embedded server ID.
     */
    private Integer embeddedID = null;

    /**
     * Factory for sending requests (build in constructor or when serialization
     * occurs).
     */
    private transient Factory<?, ?> factory = null;

    /**
     * Boolean used to know if this class extends javax.ejb.EJBLocalObject class.
     */
    private boolean isExtendingEJBLocalObject = false;

    /**
     * Build a new Invocation handler.
     * @param embeddedID the Embedded server ID.
     * @param containerId the id of the container that will be called on the
     *        remote side.
     * @param factoryName the name of the remote factory.
     * @param useID true if all instance build with this ref are unique
     *        (stateful), false if it references the same object (stateless)
     */
    public LocalCallInvocationHandler(final Integer embeddedID, final String containerId, final String factoryName,
            final boolean useID) {
        super(containerId, factoryName, useID);

        // Server ID
        this.embeddedID = embeddedID;

        // Init the factory
        initFactory();
    }

    /**
     * Default constructor (used for serialization).
     */
    public LocalCallInvocationHandler() {
        super(null, null, false);
    }

    /**
     * Initialize the factory object with the given infos.
     */
    private void initFactory() {
        // Get Embedded server
        EZBServer ejb3Server = EmbeddedManager.getEmbedded(this.embeddedID);
        if (ejb3Server == null) {
            throw new IllegalStateException("Cannot find the server with id '" + this.embeddedID + "'.");
        }

        // Get the container
        EZBContainer container = ejb3Server.getContainer(getContainerId());
        if (container == null) {
            throw new IllegalStateException("Cannot find the container with id '" + getContainerId() + "'.");
        }

        this.factory = container.getFactory(getFactoryName());
        if (this.factory == null) {
            throw new IllegalStateException("Cannot find the factory with name '" + getFactoryName() + "'.");
        }
    }

    /**
     * Processes a method invocation on a proxy instance and returns the result.
     * This method will be invoked on an invocation handler when a method is
     * invoked on a proxy instance that it is associated with.
     * @param proxy the proxy instance that the method was invoked on
     * @param method the <code>Method</code> instance corresponding to the
     *        interface method invoked on the proxy instance. The declaring
     *        class of the <code>Method</code> object will be the interface
     *        that the method was declared in, which may be a superinterface of
     *        the proxy interface that the proxy class inherits the method
     *        through.
     * @param args an array of objects containing the values of the arguments
     *        passed in the method invocation on the proxy instance, or
     *        <code>null</code> if interface method takes no arguments.
     *        Arguments of primitive types are wrapped in instances of the
     *        appropriate primitive wrapper class, such as
     *        <code>java.lang.Integer</code> or <code>java.lang.Boolean</code>.
     * @return the value to return from the method invocation on the proxy
     *         instance.
     * @throws Exception the exception to throw from the method invocation on
     *         the proxy instance.
     */
     public Object invoke(final Object proxy, final Method method, final Object[] args) throws Exception {
            return invoke(proxy, method, args, null);
    }

    /**
     * Processes a method invocation on a proxy instance and returns the result.
     * This method will be invoked on an invocation handler when a method is
     * invoked on a proxy instance that it is associated with.
     * @param proxy the proxy instance that the method was invoked on
     * @param method the <code>Method</code> instance corresponding to the
     *        interface method invoked on the proxy instance. The declaring
     *        class of the <code>Method</code> object will be the interface
     *        that the method was declared in, which may be a superinterface of
     *        the proxy interface that the proxy class inherits the method
     *        through.
     * @param args an array of objects containing the values of the arguments
     *        passed in the method invocation on the proxy instance, or
     *        <code>null</code> if interface method takes no arguments.
     *        Arguments of primitive types are wrapped in instances of the
     *        appropriate primitive wrapper class, such as
     *        <code>java.lang.Integer</code> or <code>java.lang.Boolean</code>.
     * @param hashMethod the hash of the method
     * @return the value to return from the method invocation on the proxy
     *         instance.
     * @throws Exception the exception to throw from the method invocation on
     *         the proxy instance.
     */
    public Object invoke(final Object proxy, final Method method, final Object[] args, final Long hashMethod) throws Exception {
        // bean removed ?
        if (isRemoved()) {
            handleThrowable(convertThrowable(new NoSuchEJBException("The bean has been removed")), false, method, null);
        }

        // Methods on the Object.class are not send on the remote side
        if (method.getDeclaringClass().getName().equals("java.lang.Object")) {
            // for stateful bean, let the first call to toString go to the remote side in order to initialize the bean ID
            if (!isUsingID() || getBeanId() != null || !method.getName().equals("toString")) {
                return handleObjectMethods(method, args);
            }
        }

        if (getHashedMethods() == null) {
            setHashedMethods(new HashMap<Method, Long>());
        }
        Long hashLong = null;
        if (hashMethod == null) {
            hashLong = getHashedMethods().get(method);
            if (hashLong == null) {
                hashLong = Long.valueOf(Hash.hashMethod(method));
                getHashedMethods().put(method, hashLong);
            }
        } else {
            // Reuse given hashCode
            hashLong = hashMethod;
        }

        // Now, need to invoke the bean
        EJBLocalRequest localRequest = new EJBLocalRequestImpl(hashLong, args, getBeanId(), getInterfaceClassName());
        EJBResponse response = this.factory.localCall(localRequest);
        setBeanId(response.getBeanId());

        // toString() call on the remote side for initializing the bean id is done
        // so we return toString() computing
        if (method.getDeclaringClass().getName().equals("java.lang.Object")) {
            if (getBeanId() != null && method.getName().equals("toString")) {
                return handleObjectMethods(method, args);
            }
        }

        // bean removed ?
        setRemoved(response.isRemoved());

        RPCException rpcException = response.getRPCException();
        if (rpcException != null) {
            handleThrowable(rpcException.getCause(), rpcException.isApplicationException(), method, rpcException);
        }




        return response.getValue();

    }

    /**
     * Save our content.
     * @param out the stream to write the object to
     * @throws IOException Includes any I/O exceptions that may occur
     */
    public void writeExternal(final ObjectOutput out) throws IOException {
        // server ID
        out.writeObject(this.embeddedID);
        // Container ID
        out.writeObject(getContainerId());
        // Factory name
        out.writeObject(getFactoryName());
        // interface class name
        out.writeObject(getInterfaceClassName());
        // boolean (extending java.rmi.remote ?)
        out.writeBoolean(isExtendingRmiRemote());
        // boolean (extending javax.ejb.EJBLocalObject ?)
        out.writeBoolean(this.isExtendingEJBLocalObject);

        // boolean (useID flag)
        out.writeBoolean(isUsingID());

        // boolean (removed)
        out.writeBoolean(isRemoved());
    }

    /**
     * Build our content.
     * @param in the stream to read data from in order to restore the object
     * @exception IOException if I/O errors occur
     * @exception ClassNotFoundException If the class for an object being
     *            restored cannot be found.
     */
    public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
        // read ServerID
        this.embeddedID = (Integer) in.readObject();
        // read Container id
        setContainerId((String) in.readObject());
        // read factory's name
        setFactoryName((String) in.readObject());

        // interface class name
        setInterfaceClassName((String) in.readObject());

        // boolean flag
        setExtendingRmiRemote(in.readBoolean());

        // boolean (extending javax.ejb.EJBLocalObject ?)
        this.isExtendingEJBLocalObject = (in.readBoolean());

        // useID flag
        setUseID(in.readBoolean());

        // removed flag
        setRemoved(in.readBoolean());

        // init Factory object (transient)
        initFactory();

    }

    /**
     * Gets the embedded ID.
     * @return the embedded ID.
     */
    protected Integer getEmbeddedID() {
        return this.embeddedID;
    }

    /**
     * Sets the interface that represents this handler.
     * @param clz the instance of the interface.
     */
    @Override
    public void setInterfaceClass(final Class<?> clz) {
        super.setInterfaceClass(clz);
        if (EJBLocalObject.class.isAssignableFrom(clz)) {
            this.isExtendingEJBLocalObject = true;
        }
    }

    /**
     * Convert the received exception to the correct type for the remote case.
     * @param throwable the exception to analyze.
     * @return the converted exception or the original exception
     */
    protected Throwable convertThrowable(final Throwable throwable) {
        // see 14.4.2.3 chapter EJB3
        if (this.isExtendingEJBLocalObject && throwable instanceof NoSuchEJBException) {
            NoSuchObjectLocalException ne = new NoSuchObjectLocalException(throwable.getMessage(), (Exception) throwable);
            return ne;
        }
        // no check
        return throwable;
    }

    /**
     * This method has to be used only from the OSGi.
     * @param factory the factory to set
     */
    public void setFactory(final Factory<?, ?> factory) {
        this.factory = factory;
    }

    /**
     * @return the Factory used to execute the requests.
     */
    protected Factory<?, ?> getFactory() {
        return this.factory;
    }
}
