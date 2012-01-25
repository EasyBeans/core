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
 * $Id: EJBLocalHomeInvocationHandler.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.proxy.client;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import javax.ejb.EJBException;
import javax.ejb.RemoveException;

import org.ow2.easybeans.asm.Type;
import org.ow2.easybeans.rpc.util.Hash;
import org.ow2.util.log.Log;
import org.ow2.util.log.LogFactory;

/**
 * Object acting as the proxy for EJB Local Home calls.
 * @author Florent Benoit
 */
public class EJBLocalHomeInvocationHandler extends LocalCallInvocationHandler {

    /**
     * UID for serialization.
     */
    private static final long serialVersionUID = -7113041945135321801L;

    /**
     * UID for serialization.
     */
    private static Log logger = LogFactory.getLog(EJBLocalHomeInvocationHandler.class);

    /**
     * Build a new Invocation handler.
     * @param embeddedID the Embedded server ID.
     * @param containerId the id of the container that will be called on the
     *        remote side.
     * @param factoryName the name of the remote factory.
     * @param useID true if all instance build with this ref are unique
     *        (stateful), false if it references the same object (stateless)
     */
    public EJBLocalHomeInvocationHandler(final Integer embeddedID, final String containerId, final String factoryName,
            final boolean useID) {
        super(embeddedID, containerId, factoryName, useID);
    }

    /**
     * Default constructor (used for serialization).
     */
    public EJBLocalHomeInvocationHandler() {
        super();
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
                LocalCallInvocationHandler handler = new LocalCallInvocationHandler(getEmbeddedID(), getContainerId(),
                        getFactoryName(), isUsingID());
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

                // Bean class not available on the client side, get ejbCreate
                // method from the interface

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

                    // The current bean id must correspond to the handler bean
                    // id
                    setBeanId(handler.getBeanId());

                    // Call the ejbCreate method using the bean proxy
                    super.invoke(beanProxy, ejbCreateMethod, args, Long.valueOf(hashCode));

                } catch (NoSuchMethodException e) {
                    // Nothing to do
                    logger.debug("No create method found", e);
                } catch (EJBException e) {
                    // Method may not exists
                    Throwable t = e.getCause();
                    if (t != null && t instanceof NoSuchMethodException) {
                        logger.debug("Unable to call the ejbCreate method as it is not present on the bean.", e);
                    } else {
                        // else, rethrow exception
                        throw e;
                    }
                } finally {
                    // Set to previous value
                    setBeanId(beanId);
                }

                // return the proxy built
                return beanProxy;
            }
        }
        // remove method ?
        if (method != null && "remove".equals(method.getName())) {
            throw new RemoveException("Only 2.1 entity beans can use the remove(primary key) method.");
        }
        return super.invoke(proxy, method, args);
    }


}
