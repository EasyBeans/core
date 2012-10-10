/*
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
 * $Id:$
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.proxy.factory;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.RefAddr;
import javax.naming.Reference;
import javax.naming.spi.ObjectFactory;

import org.ow2.easybeans.api.EZBContainer;
import org.ow2.easybeans.api.EZBServer;
import org.ow2.easybeans.api.EmbeddedManager;
import org.ow2.easybeans.api.Factory;
import org.ow2.easybeans.container.managedbean.ManagedBeanFactory;
import org.ow2.easybeans.proxy.reference.AbsCallRef;
import org.ow2.easybeans.proxy.reference.LocalCallRef;

/**
 * An ObjectFactory used to create ManagedBean based on JNDI references
 *
 * @author Loic Albertin
 */
public class ManagedBeanObjectFactory implements ObjectFactory {

    public Object getObjectInstance(Object obj, Name name, Context nameCtx, Hashtable<?, ?> environment) throws Exception {
        if (obj instanceof Reference) {
            Reference ref = (Reference) obj;
            // get the embeddedID, getContainerId(), getFactoryName()
            RefAddr embeddedIDAddr = ref.get(LocalCallRef.EMBEDDED_ID);
            RefAddr containerIDAddr = ref.get(AbsCallRef.CONTAINER_ID);
            RefAddr factoryNameAddr = ref.get(AbsCallRef.FACTORY_NAME);
            Integer embeddedID = Integer.valueOf((String) embeddedIDAddr.getContent());
            String containerID = (String) containerIDAddr.getContent();
            String factoryName = (String) factoryNameAddr.getContent();

            return getFactory(embeddedID, containerID, factoryName).getObjectInstance();

        }
        throw new IllegalStateException("Can only build object with a reference");
    }


    /**
     * Initialize the factory object with the given infos.
     */
    private ManagedBeanFactory getFactory(final Integer embeddedID, final String containerId, final String factoryName) {
        // Get Embedded server
        EZBServer ejb3Server = EmbeddedManager.getEmbedded(embeddedID);
        if (ejb3Server == null) {
            throw new IllegalStateException("Cannot find the server with id '" + embeddedID + "'.");
        }

        // Get the container
        EZBContainer container = ejb3Server.getContainer(containerId);
        if (container == null) {
            throw new IllegalStateException("Cannot find the container with id '" + containerId + "'.");
        }

        Factory factory =  container.getFactory(factoryName);
        if (factory == null) {
            throw new IllegalStateException("Cannot find the factory with name '" + factoryName + "'.");
        }
        if (! (factory instanceof ManagedBeanFactory)) {
            throw new IllegalStateException("'" + factoryName + "' is not a ManagedBeanFactory as expected.");
        }
        return (ManagedBeanFactory) factory;
    }
}
