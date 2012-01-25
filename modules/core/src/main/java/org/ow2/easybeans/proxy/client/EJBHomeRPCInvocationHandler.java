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
 * $Id: EJBHomeRPCInvocationHandler.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.proxy.client;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.rmi.RemoteException;

import javax.ejb.EJBHome;
import javax.ejb.EJBMetaData;
import javax.ejb.EJBObject;
import javax.ejb.Handle;
import javax.ejb.HomeHandle;
import javax.ejb.RemoveException;

import org.ow2.easybeans.asm.Type;
import org.ow2.easybeans.container.svc.EasyBeansMetaData;
import org.ow2.easybeans.rpc.util.Hash;

/**
 * This class sends an EJB request to the server and send back to the client the
 * response. It handles the EJBHome calls.
 * @author Florent Benoit
 */
public class EJBHomeRPCInvocationHandler extends ClientRPCInvocationHandler {

    /**
     * Id for serializable class.
     */
    private static final long serialVersionUID = -5766859656696754086L;

    /**
     * Remote interface.
     */
    private String remoteInterface = null;

    /**
     * Build a new Invocation handler.
     * @param containerId the id of the container that will be called on the
     *        remote side.
     * @param factoryName the name of the remote factory.
     * @param useID true if all instance build with this ref are unique
     *        (stateful), false if it references the same object (stateless)
     * @param remoteInterface the name of the remote interface used by the home interface.
     */
    public EJBHomeRPCInvocationHandler(final String containerId, final String factoryName, final boolean useID,
            final String remoteInterface) {
        super(containerId, factoryName, useID);
        this.remoteInterface = remoteInterface;
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
    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Exception {

        // Create ?
        if (method != null) {
            if (method.getName().startsWith("create")) {
                // Return class (interface)
                Class<?> itfClass = method.getReturnType();

                // Build handler
                ClientRPCInvocationHandler handler = new ClientRPCInvocationHandler(getContainerId(), getFactoryName(),
                        isUsingID());
                handler.setRMIEnv(getRMIEnv());
                // set the interface class
                handler.setInterfaceClass(itfClass);

                // Disable interface classname for businessinvokedinterface as it is a component interface
                handler.setInterfaceClassName(null);

                // Get current classloader
                ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

                // Return proxy
                Object beanProxy = Proxy.newProxyInstance(classLoader, new Class[] {itfClass}, handler);

                // create the id for stateful bean
                beanProxy.toString();

                // Bean class not available on the client side, get ejbCreate method from the interface

                // Store the current bean id value
                Long beanId = getBeanId();
                try {
                    Class<?> ejbHomeClass = classLoader.loadClass(getInterfaceClassName());
                    Method ejbCreateMethod = ejbHomeClass.getMethod("create", method.getParameterTypes());

                    // Void method descriptor
                    Type[] argumentTypes = Type.getArgumentTypes(ejbCreateMethod);
                    String methodDescriptor = Type.getMethodDescriptor(Type.VOID_TYPE, argumentTypes);

                    // Get hash
                    long hashCode = Hash.hashMethod("ejbCreate", methodDescriptor);

                    // The current bean id must correspond to the handler bean id
                    setBeanId(handler.getBeanId());
                    // Call the ejbCreate method using the bean proxy
                    super.invoke(beanProxy, ejbCreateMethod, args, "ejbCreate", Long.valueOf(hashCode));
                } catch (NoSuchMethodException e) {
                    // Nothing to do
                } finally {
                    // Set to previous value
                    setBeanId(beanId);
                }

                // return the proxy built
                return beanProxy;
            }
            if (method.getName().equals("remove")) {
                handleRemoveMethod(args);
                // void method
                return null;
            }

            // getEJBMetaData
            if (method.getName().equals("getEJBMetaData")) {
                return handleGetEJBMetadata(proxy);
            }

            // getHomeHandle
            if (method.getName().equals("getHomeHandle")) {
                return handleGetHomeHandle();
            }

        }

        return super.invoke(proxy, method, args);
    }

    /**
     * Handle the remove method for this EJB Home object. If the remove method
     * is not applied with an Handle, it means that it is for the primary key
     * and this is not supported (Only handle session bean Home).
     * @param args the arguments of the remove method. For primary key, it will
     *        throw an error.
     * @throws RemoveException when applying remove method on a primary key.
     * @throws RemoteException if the ejbObject cannot be retrieved from the
     *         handle or if the remove method is failing.
     */
    private void handleRemoveMethod(final Object[] args) throws RemoteException, RemoveException {
        // check if it is an incorrect method (primary key) ?
        // args has a parameter as it uses the Home interface (so no need to
        // check the length of args)
        if (!(args[0] instanceof Handle)) {
            throw new RemoveException("The remove method is not allowed with an object which is not an handle."
                    + "Primary key is only used for entity 2.1x bean.");
        }

        // Ok, now the given arg is an Handle, get it.
        Handle handle = (Handle) args[0];

        // Get the EJBObject from this handle and call the remove method.
        EJBObject ejbObject = handle.getEJBObject();

        // removing
        ejbObject.remove();
    }

    /**
     * Build a metadata object.
     * @param proxy the object on which the method is currently invoked (it's the home proxy).
     * @return an EJB metadata object.
     * @throws RemoteException if metadata object cannot be built.
     */
    private EJBMetaData handleGetEJBMetadata(final Object proxy) throws RemoteException {
        // EJBHome is the proxy object
        EJBHome ejbHome = (EJBHome) proxy;

        // Classloader
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        // Home interface
        Class<?> homeInterfaceClass = null;
        try {
            homeInterfaceClass = classLoader.loadClass(getInterfaceClassName());
        } catch (ClassNotFoundException e) {
            throw new RemoteException("Cannot load the class '" + getInterfaceClassName() + "'.", e);
        }

        // Remote interface
        Class<?> remoteInterfaceClass = null;
        if (this.remoteInterface != null) {
            try {
                remoteInterfaceClass = classLoader.loadClass(this.remoteInterface);
            } catch (ClassNotFoundException e) {
                throw new RemoteException("Cannot load the class '" + this.remoteInterface + "'.", e);
            }
        }

        // stateless ? (using id = stateful)
        boolean isStateless = !isUsingID();

        // build the metadata
        EasyBeansMetaData metadata = new EasyBeansMetaData(ejbHome, homeInterfaceClass, remoteInterfaceClass, isStateless);

        // return it
        return metadata;
    }

    /**
     * Gets an Home handle from this EJB Home.
     * @return an instance of an HomeHandle object.
     */
    private HomeHandle handleGetHomeHandle() {
        return null;
    }

}
