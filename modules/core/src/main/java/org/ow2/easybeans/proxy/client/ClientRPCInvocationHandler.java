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
 * $Id: ClientRPCInvocationHandler.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.proxy.client;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.rmi.NoSuchObjectException;
import java.util.HashMap;
import java.util.Hashtable;

import javax.ejb.EJBException;
import javax.ejb.EJBObject;
import javax.ejb.EJBTransactionRequiredException;
import javax.ejb.Handle;
import javax.ejb.NoSuchEJBException;
import javax.transaction.TransactionRequiredException;

import org.ow2.easybeans.container.svc.EasyBeansHandle;
import org.ow2.easybeans.rpc.EJBRemoteRequestImpl;
import org.ow2.easybeans.rpc.RPC;
import org.ow2.easybeans.rpc.api.ClientRPC;
import org.ow2.easybeans.rpc.api.EJBRemoteRequest;
import org.ow2.easybeans.rpc.api.EJBResponse;
import org.ow2.easybeans.rpc.api.RPCException;
import org.ow2.easybeans.rpc.util.Hash;



/**
 * This class sends an EJB request to the server and send back to the client the
 * response.
 * @author Florent Benoit
 */
public class ClientRPCInvocationHandler extends AbsInvocationHandler {

    /**
     * Id for serializable class.
     */
    private static final long serialVersionUID = 1852625501781836250L;

    /**
     * Flag used to recreate EasyBeans Proxy on the client side as with IIOP/JacORB the dynamic proxy serialization is failing.
     */
    private static final boolean RECREATE_DYNAMIC_PROXY = Boolean.getBoolean("easybeans.recreate.dynamic.proxy");

    /**
     * Environment used by the client when lookup on proxy object has be done.
     * This will be set by the Remote Factory.
     */
    private Hashtable<?, ?> rmiClientEnvironment = null;

    /**
     * Build a new Invocation handler.
     * @param containerId the id of the container that will be called on the
     *        remote side.
     * @param factoryName the name of the remote factory.
     * @param useID true if all instance build with this ref are unique
     *        (stateful), false if it references the same object (stateless)
     */
    public ClientRPCInvocationHandler(final String containerId, final String factoryName, final boolean useID) {
        super(containerId, factoryName, useID);
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
        return invoke(proxy, method, args, null, null);

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
     * @param methodName the name of the method
     * @param hashMethod the hash of the method
     * @return the value to return from the method invocation on the proxy
     *         instance.
     * @throws Exception the exception to throw from the method invocation on
     *         the proxy instance.
     */
    protected Object invoke(final Object proxy, final Method method, final Object[] args, final String methodName,
            final Long hashMethod) throws Exception {

        // Bean removed, no methods are allowed
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

        // getHandle method (ejb 2.1) view
        if ("getHandle".equals(method.getName()) && Handle.class.equals(method.getReturnType())) {
            // In this case, return an handle based on the proxy
            return new EasyBeansHandle((EJBObject) proxy);
        }


        ClientRPC client = RPC.getClient(this.rmiClientEnvironment);

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

        // Get or reuse method name
        String mName = null;
        if (methodName != null) {
            mName = methodName;
        } else {
            mName = method.getName();
        }

        EJBRemoteRequest request = new EJBRemoteRequestImpl(mName, hashLong, args, getContainerId(), getFactoryName(),
                getBeanId(), getInterfaceClassName());

        // send response
        EJBResponse response;
        try {
            response = client.sendEJBRequest(request);
        } catch (RuntimeException e) {
            // Exception due to protocol
            throw new EJBException("Error while sending a request", e);
        }
        // Sets the bean ID
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

        // Handle exception
        RPCException rpcException = response.getRPCException();
        if (rpcException != null) {
            handleThrowable(convertThrowable(rpcException.getCause()), rpcException.isApplicationException(), method,
                    rpcException);
        }

        // Needs to rebuild the proxy as we only have the invocation handler
        if (RECREATE_DYNAMIC_PROXY) {
            Object value = response.getValue();
            if (value != null) {
                if (value instanceof ClientRPCInvocationHandler) {
                    ClientRPCInvocationHandler invocationHandler = (ClientRPCInvocationHandler) value;
                    if (invocationHandler.isBusinessObjectMode()) {
                        Class<?> interfaceClass = Thread.currentThread().getContextClassLoader().loadClass(
                                invocationHandler.getInterfaceClassName());
                        return Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                                new Class[] {interfaceClass}, invocationHandler);
                    }
                }
            }
        }


        return response.getValue();
    }

    /**
     * Convert the received exception to the correct type for the remote case.
     * @param throwable the exception to analyze.
     * @return the converted exception or the original exception
     */
    protected Throwable convertThrowable(final Throwable throwable) {
        // see 14.3.9 chapter EJB3
        if (isExtendingRmiRemote() && throwable instanceof NoSuchEJBException) {
            NoSuchObjectException ne = new NoSuchObjectException(throwable.getMessage());
            ne.detail = throwable;
            return ne;
        }

        // see 14.4.2.2 chapter EJB3
        if (throwable instanceof javax.ejb.TransactionRequiredLocalException) {
            if (isExtendingRmiRemote()) {
                TransactionRequiredException tre = new TransactionRequiredException(throwable.getMessage());
                tre.detail = throwable;
                return tre;
            }
            // else
            EJBTransactionRequiredException ejbTransRequiredException = new EJBTransactionRequiredException(throwable
                    .getMessage());
            ejbTransRequiredException.initCause(throwable);
            return ejbTransRequiredException;
        }
        return throwable;
    }

    /**
     * Set the RMI environment used by the client when the lookup has been done.
     * @param rmiClientEnvironment the given environment.
     */
    public void setRMIEnv(final Hashtable<?, ?> rmiClientEnvironment) {
        this.rmiClientEnvironment = rmiClientEnvironment;
    }

    /**
     * Get the RMI environment used by the client when the lookup has been done.
     * @return RMI env.
     */
    public Hashtable<?, ?> getRMIEnv() {
        return  this.rmiClientEnvironment;
    }

}
