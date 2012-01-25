/**
 * EasyBeans
 * Copyright (C) 2009 Bull S.A.S.
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
 * $Id: ProxyHelper.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.proxy.helper;

import java.lang.reflect.Proxy;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.ow2.easybeans.api.EZBContainer;
import org.ow2.easybeans.api.EZBServer;
import org.ow2.easybeans.api.Factory;
import org.ow2.easybeans.container.session.stateful.StatefulSessionFactory;
import org.ow2.easybeans.proxy.client.AbsInvocationHandler;
import org.ow2.easybeans.proxy.client.ClientRPCInvocationHandler;
import org.ow2.easybeans.proxy.client.LocalCallInvocationHandler;

/**
 * Allow to create local or remote proxies.
 * @author Florent Benoit
 */
public final class ProxyHelper {

    /**
     * Utility class.
     */
    private ProxyHelper() {

    }

    /**
     * Build a proxy for the given EasyBeans factory allowing to call methods on the bean/factory.
     * @param <T> the interface expected
     * @param factory the given EasyBeans factory
     * @param interfaceClass the interface of the bean class
     * @param isLocalProxy use a local proxy or remote proxy ?
     * @return a local or remote proxy ready-to-use
     */
    @SuppressWarnings("unchecked")
    public static <T> T getProxy(final Factory<?, ?> factory, final Class<T> interfaceClass, final boolean isLocalProxy) {

        // Get elements used to create a proxy
        EZBContainer container = factory.getContainer();
        EZBServer server = container.getConfiguration().getEZBServer();
        String containerID = container.getId();
        String factoryName = factory.getBeanInfo().getName();

        // If factory is a stateful factory, add the stateful flag
        boolean isStateful = false;
        if (factory instanceof StatefulSessionFactory) {
            isStateful = true;
        }
        Integer serverID = server.getID();

        // Build the handler
        AbsInvocationHandler handler = null;
        if (isLocalProxy) {
            // local handler
            handler = new LocalCallInvocationHandler(serverID, containerID, factoryName, isStateful);
        } else {
            // remote handler
            handler = new ClientRPCInvocationHandler(containerID, factoryName, isStateful);
            try {
                ((ClientRPCInvocationHandler) handler).setRMIEnv(new InitialContext().getEnvironment());
            } catch (NamingException e) {
                throw new IllegalArgumentException("Unable to get environment", e);
            }
        }

        // handler used for getBusinessObject() method
        handler.setBusinessObjectMode(true);

        // Load the interface with the classloader of the thread
        // Get current classloader
        ClassLoader classLoader = interfaceClass.getClassLoader();

        // set the interface class
        handler.setInterfaceClass(interfaceClass);

        // return the proxy
        return (T) Proxy.newProxyInstance(classLoader, new Class[] {interfaceClass}, handler);
    }

}
