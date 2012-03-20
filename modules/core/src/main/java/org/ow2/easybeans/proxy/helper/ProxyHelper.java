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
import org.ow2.easybeans.api.bean.proxy.EasyBeansNoInterfaceProxyBean;
import org.ow2.easybeans.container.session.stateful.StatefulSessionFactory;
import org.ow2.easybeans.enhancer.lib.ProxyClassEncoder;
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
     * @param proxyType the type of proxy to use
     * @return a proxy ready-to-use
     */
    @SuppressWarnings("unchecked")
    public static <T> T getProxy(final Factory<?, ?> factory, final Class<T> interfaceClass, final ProxyType proxyType) {

        // Get elements used to create a proxy
        EZBContainer container = factory.getContainer();
        EZBServer server = container.getConfiguration().getEZBServer();
        String containerID = container.getId();
        String factoryName = factory.getBeanInfo().getName();

        // If factory is a stateful factory, add the stateful flag
        boolean isStateful = false;
        Long beanId = null;
        if (factory instanceof StatefulSessionFactory) {
            isStateful = true;
            beanId = ((StatefulSessionFactory) factory).getCurrentBeanIDThreadLocal().get();
        }
        Integer serverID = server.getID();

        // Load the interface with the classloader of the thread
        // Get current classloader
        ClassLoader classLoader = interfaceClass.getClassLoader();


        // Proxy bean (for type = no interface)
        EasyBeansNoInterfaceProxyBean proxyBean = null;



        // Build the handler
        AbsInvocationHandler handler = null;


        switch (proxyType) {
        case NO_INTERFACE :
            String beanProxyClassName = ProxyClassEncoder.getProxyClassName(interfaceClass.getName().replace(".", "/"))
                    .replace("/", ".");

            // Load the proxy class
            Class<EasyBeansNoInterfaceProxyBean> clz = null;
            try {
                clz = (Class<EasyBeansNoInterfaceProxyBean>) classLoader.loadClass(beanProxyClassName);
            } catch (ClassNotFoundException e) {
                throw new IllegalArgumentException("Cannot find the class '" + beanProxyClassName + "' in Classloader '"
                        + classLoader + "'.", e);
            }

            // Build handler
            handler = new LocalCallInvocationHandler(serverID, containerID, factoryName, isStateful);

            // Build a new instance of the class
            try {
                proxyBean = clz.newInstance();
            } catch (InstantiationException e) {
                throw new IllegalArgumentException("Unable to build an instance of the proxy '" + beanProxyClassName
                        + "' in Classloader '" + classLoader + "'.", e);
            } catch (IllegalAccessException e) {
                throw new IllegalArgumentException("Unable to build an instance of the proxy '" + beanProxyClassName
                        + "' in Classloader '"         + classLoader + "'.", e);
            }

            // Defines handler on the proxy bean
            proxyBean.setInvocationHandler(handler);


        break;
        case LOCAL :
            // local handler
            handler = new LocalCallInvocationHandler(serverID, containerID, factoryName, isStateful);
            break;

        case REMOTE:
            // remote handler
            handler = new ClientRPCInvocationHandler(containerID, factoryName, isStateful);
            try {
                ((ClientRPCInvocationHandler) handler).setRMIEnv(new InitialContext().getEnvironment());
            } catch (NamingException e) {
                throw new IllegalArgumentException("Unable to get environment", e);
            }
            break;
        default: throw new IllegalStateException("No such type of proxy");
        }

        // handler used for getBusinessObject() method
        handler.setBusinessObjectMode(true);



        // set the interface class
        handler.setInterfaceClass(interfaceClass);
        if (beanId != null) {
            handler.setBeanId(beanId);
        }

        // return the proxy
        switch (proxyType) {
        case NO_INTERFACE :
            return (T) proxyBean;
        case LOCAL:
        case REMOTE:
            return (T) Proxy.newProxyInstance(classLoader, new Class[] {interfaceClass}, handler);
        default: throw new IllegalStateException("No such type of proxy");
        }
    }

}
