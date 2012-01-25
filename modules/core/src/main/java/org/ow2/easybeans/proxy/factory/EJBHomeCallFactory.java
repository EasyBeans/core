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
 * $Id: EJBHomeCallFactory.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.proxy.factory;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.RefAddr;
import javax.naming.Reference;

import org.ow2.easybeans.proxy.client.ClientRPCInvocationHandler;
import org.ow2.easybeans.proxy.client.EJBHomeRPCInvocationHandler;
import org.ow2.easybeans.proxy.reference.EJBHomeCallRef;


/**
 * Factory creating an EJB Remote Home proxy for remote calls.
 * @author Florent Benoit.
 */
public class EJBHomeCallFactory extends RemoteCallFactory {

    /**
     * Name of the remote interface.
     */
    private String remoteInterface = null;

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

            // get the properties
            RefAddr remoteInterfaceAddr = ref.get(EJBHomeCallRef.REMOTE_INTERFACE);
            this.remoteInterface =  (String) remoteInterfaceAddr.getContent();
        }

        // Now call the super method.
        return super.getObjectInstance(obj, name, nameCtx, environment);
    }

    /**
     * Build an instance of a remote RPC handler. Can be used by subclasses to change the object.
     * @param containerID the id of the container that will be called on the
     *        remote side.
     * @param factoryName the name of the remote factory.
     * @param useID true if all instance build with this ref are unique
     *        (stateful), false if it references the same object (stateless)
     * @return an instance of a remote handler.
     */
    @Override
    protected ClientRPCInvocationHandler buildRemoteHandler(final String containerID, final String factoryName,
            final boolean useID) {
        return new EJBHomeRPCInvocationHandler(containerID, factoryName, useID, this.remoteInterface);
    }
}
