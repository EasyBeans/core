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
 * $Id: RMIServerRPCImpl.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.rpc.rmi.server;

import java.rmi.RemoteException;

import javax.rmi.PortableRemoteObject;

import org.ow2.easybeans.api.EZBContainer;
import org.ow2.easybeans.api.Factory;
import org.ow2.easybeans.rpc.api.EJBRemoteRequest;
import org.ow2.easybeans.rpc.api.EJBResponse;
import org.ow2.easybeans.rpc.api.RMIServerRPC;
import org.ow2.easybeans.server.Embedded;

/**
 * Server side object which handle the EJB requests.
 * @author Florent Benoit
 */
public class RMIServerRPCImpl extends PortableRemoteObject implements RMIServerRPC {

    /**
     * Server on which it depends.
     */
    private Embedded ejb3server;


    /**
     * Retry time when container is not available.
     */
    private static final int WAIT_TIME = 1000;

    /**
     * This invoker will discuss with the embedded server when receiving requests.
     * @param ejb3server the server on which send requests.
     * @throws RemoteException if RPC fails
     */
    public RMIServerRPCImpl(final Embedded ejb3server) throws RemoteException {
        super();
        this.ejb3server = ejb3server;
    }

    /**
     * Handle a request and send back a response.<br>
     * It finds the right container and its factory and send the request to the factory.
     * @param request the ejb request to handle.
     * @return a response.
     * @throws RemoteException if there are errors on the prococol.
     */
    public EJBResponse getEJBResponse(final EJBRemoteRequest request) throws RemoteException {

        String id = request.getContainerId();
        if (id == null) {
            throw new RemoteException("No valid container id");
        }
        // Get the container
        EZBContainer container = this.ejb3server.getContainer(id);
        if (container == null) {
            throw new RemoteException("Cannot find the container with id '" + id + "'.");
        }

        // Once the container is found, get the factory
        String factoryName = request.getFactory();


        // while container is not available, stop the current request
        while (!container.isAvailable()) {
            //TODO: change it to a semaphore ?
            try {
                Thread.sleep(WAIT_TIME);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // Get the container
        Factory factory = container.getFactory(factoryName);
        if (factory == null) {
            throw new RemoteException("Cannot find the factory with name '" + factoryName + "'.");
        }

        // Now, need to invoke the bean
        return factory.rpcInvoke(request);
    }

}
