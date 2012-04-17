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
 * $Id: LocalCallFactory.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.proxy.factory;

import java.lang.reflect.Proxy;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.RefAddr;
import javax.naming.Reference;
import javax.naming.spi.ObjectFactory;

import org.ow2.easybeans.api.EZBContainerException;
import org.ow2.easybeans.proxy.client.LocalCallInvocationHandler;
import org.ow2.easybeans.proxy.reference.AbsCallRef;
import org.ow2.easybeans.proxy.reference.LocalCallRef;

/**
 * Factory creating an EJB proxy for local calls.
 * @author Florent Benoit.
 */
public class LocalCallFactory implements ObjectFactory {

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
    public Object getObjectInstance(final Object obj, final Name name, final Context nameCtx, final Hashtable<?, ?> environment)
            throws Exception {
        if (obj instanceof Reference) {
            Reference ref = (Reference) obj;

            // get the embeddedID, getContainerId(), getFactoryName()
            RefAddr embeddedIDAddr = ref.get(LocalCallRef.EMBEDDED_ID);
            RefAddr containerIDAddr = ref.get(AbsCallRef.CONTAINER_ID);
            RefAddr factoryNameAddr = ref.get(AbsCallRef.FACTORY_NAME);
            RefAddr itfClassNameAddr = ref.get(AbsCallRef.INTERFACE_NAME);
            RefAddr useIDAddr = ref.get(AbsCallRef.USE_ID);

            Integer embeddedID = Integer.valueOf((String) embeddedIDAddr.getContent());
            String containerID = (String) containerIDAddr.getContent();
            String factoryName = (String) factoryNameAddr.getContent();
            String itfClassName = (String) itfClassNameAddr.getContent();
            boolean useID = Boolean.valueOf((String) useIDAddr.getContent()).booleanValue();

            // Build new Handler
            LocalCallInvocationHandler handler = buildLocalHandler(embeddedID, containerID, factoryName, useID);

            // Get current classloader
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

            // load class
            Class<?> clz = null;
            try {
                clz = classLoader.loadClass(itfClassName);
            } catch (ClassNotFoundException e) {
                throw new EZBContainerException("Cannot find the class '" + itfClassName + "' in Classloader '"
                        + classLoader + "'.", e);
            }

            // set the interface class
            handler.setInterfaceClass(clz);

            // build the proxy
            Object proxy = Proxy.newProxyInstance(classLoader, new Class[] {clz}, handler);


            // Stateful case ? needs to invoke a method in order to
            // initialize the ID as the ID needs to be present when the client
            // performs the lookup.
            if (useID) {
                proxy.toString();
            }

            // return the object built.
            return proxy;
        }
        throw new IllegalStateException("Can only build object with a reference");
    }

    /**
     * Build a new Invocation handler.
     * @param embeddedID the Embedded server ID.
     * @param containerId the id of the container that will be called on the
     *        remote side.
     * @param factoryName the name of the remote factory.
     * @param useID true if all instance build with this ref are unique
     *        (stateful), false if it references the same object (stateless)
     * @return an instance of a local handler
     */
    protected LocalCallInvocationHandler buildLocalHandler(final Integer embeddedID, final String containerId,
            final String factoryName, final boolean useID) {
        return new LocalCallInvocationHandler(embeddedID, containerId, factoryName, useID);
    }
}
