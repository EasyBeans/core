/**
 * EasyBeans
 * Copyright (C) 2012 Bull S.A.S.
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
 * $Id: LocalCallFactory.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.proxy.factory;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.RefAddr;
import javax.naming.Reference;
import javax.naming.spi.ObjectFactory;

import org.ow2.easybeans.api.EZBContainerException;
import org.ow2.easybeans.api.bean.proxy.EasyBeansNoInterfaceProxyBean;
import org.ow2.easybeans.proxy.client.LocalCallInvocationHandler;
import org.ow2.easybeans.proxy.reference.AbsCallRef;
import org.ow2.easybeans.proxy.reference.LocalCallRef;
import org.ow2.easybeans.proxy.reference.NoInterfaceLocalCallRef;

/**
 * Factory creating a no-interface EJB proxy for local calls.
 * @author Florent Benoit.
 */
public class NoInterfaceLocalCallFactory extends LocalCallFactory implements ObjectFactory {

    /**
     * @return an instance of a proxy (an EJB) that handle local calls.
     * @param obj the reference containing data to build instance
     * @param name Name of context, relative to ctx, or null.
     * @param nameCtx Context relative to which 'name' is named.
     * @param environment Environment to use when creating the context *
     * @throws Exception if this object factory encountered an exception while
     *         attempting to create an object, and no other object factories are
     *         to be tried.
     */
    @Override
    public Object getObjectInstance(final Object obj, final Name name, final Context nameCtx, final Hashtable<?, ?> environment)
            throws Exception {
        if (obj instanceof Reference) {
            Reference ref = (Reference) obj;

            // get the embeddedID, getContainerId(), getFactoryName()
            RefAddr embeddedIDAddr = ref.get(LocalCallRef.EMBEDDED_ID);
            RefAddr containerIDAddr = ref.get(AbsCallRef.CONTAINER_ID);
            RefAddr factoryNameAddr = ref.get(AbsCallRef.FACTORY_NAME);
            RefAddr itfClassNameAddr = ref.get(AbsCallRef.INTERFACE_NAME);
            RefAddr proxyClassNameAddr = ref.get(NoInterfaceLocalCallRef.PROXY_CLASSNAME);

            RefAddr useIDAddr = ref.get(AbsCallRef.USE_ID);

            Integer embeddedID = Integer.valueOf((String) embeddedIDAddr.getContent());
            String containerID = (String) containerIDAddr.getContent();
            String factoryName = (String) factoryNameAddr.getContent();
            String beanClassName = (String) itfClassNameAddr.getContent();
            String proxyClassName = (String) proxyClassNameAddr.getContent();

            boolean useID = Boolean.valueOf((String) useIDAddr.getContent()).booleanValue();

            // Build new Handler
            LocalCallInvocationHandler handler = buildLocalHandler(embeddedID, containerID, factoryName, useID);

            // Get current classloader
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

            // load proxy class
            Class<EasyBeansNoInterfaceProxyBean> clz = null;
            try {
                clz = (Class<EasyBeansNoInterfaceProxyBean>) classLoader.loadClass(proxyClassName);
            } catch (ClassNotFoundException e) {
                throw new EZBContainerException("Cannot find the class '" + proxyClassName + "' in Classloader '"
                        + classLoader + "'.", e);
            }


            // load bean class
            Class<?> beanClazz = null;
            try {
                beanClazz = classLoader.loadClass(beanClassName);
            } catch (ClassNotFoundException e) {
                throw new EZBContainerException("Cannot find the class '" + beanClassName + "' in Classloader '"
                        + classLoader + "'.", e);
            }

            // set the interface class
            handler.setInterfaceClass(beanClazz);

            // Build a new instance of the class
            EasyBeansNoInterfaceProxyBean proxyBean = clz.newInstance();
            proxyBean.setInvocationHandler(handler);


            // Stateful case ? needs to invoke a method in order to
            // initialize the ID as the ID needs to be present when the client
            // performs the lookup.
            if (useID) {
                proxyBean.toString();
            }

            return proxyBean;
        }
        throw new IllegalStateException("Can only build object with a reference");
    }

}
