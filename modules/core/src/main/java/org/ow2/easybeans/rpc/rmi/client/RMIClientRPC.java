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
 * $Id: RMIClientRPC.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.rpc.rmi.client;

import java.rmi.RemoteException;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;

import org.ow2.easybeans.rpc.api.ClientRPC;
import org.ow2.easybeans.rpc.api.EJBRemoteRequest;
import org.ow2.easybeans.rpc.api.EJBResponse;
import org.ow2.easybeans.rpc.api.RMIServerRPC;

/**
 * RMI implementation of the RPC mechanism of EJB requests/responses.
 * @author Florent Benoit
 */
public class RMIClientRPC implements ClientRPC {

    /**
     * EasyBeans factory.
     */
    public static final String EASYBEANS_RMI_FACTORY = "easybeans.rpc.rmi.factory";

    /**
     * Initial context factory (delegate ?).
     */
    private static final String EASYBEANS_INITIAL_FACTORY = System.getProperty(EASYBEANS_RMI_FACTORY);

    /**
     * Environment to use to get the remote RPC object.
     */
    private Hashtable rmiClientEnvironment = null;

    /**
     * RPC remote object.
     */
    private RMIServerRPC rmiServerRPC = null;

    /**
     * Builds a new RMI client RPC with the given rmi environment.
     * @param rmiClientEnvironment the RMI environment.
     */
    public RMIClientRPC(final Hashtable<?, ?> rmiClientEnvironment) {
        this.rmiClientEnvironment = rmiClientEnvironment;
        initRemoteConnection();
    }


    /**
     * Sends a request comes to the remote side.<br>
     * A response is done by the remote side and it sends back a response.
     * @param request the EJB request.
     * @return a response that have been processed by the server.
     */
    @Override
    @SuppressWarnings("unchecked")
    public EJBResponse sendEJBRequest(final EJBRemoteRequest request) {

        // Send Request and get the answer
        try {
            return rmiServerRPC.getEJBResponse(request);
        } catch (RemoteException re) {
            throw new RuntimeException("Error while handling answer on the remote side ", re);
        }

    }

    /**
     * Initialize the connection to the remote side.
     */
    protected void initRemoteConnection() {
        // initial context factory ?
        if (EASYBEANS_INITIAL_FACTORY != null) {
            this.rmiClientEnvironment.put(Context.INITIAL_CONTEXT_FACTORY, EASYBEANS_INITIAL_FACTORY);
        }

        Context ictx = null;
        try {
            ictx = new InitialContext(this.rmiClientEnvironment);
        } catch (NamingException ne) {
            throw new IllegalStateException(ne);
        }

        // Lookup server object
        Object serverObject = null;
        try {
            serverObject = ictx.lookup(RMIServerRPC.RPC_JNDI_NAME);
        } catch (NamingException ne) {
            throw new IllegalStateException(ne);
        }
        // Get a connection to the RPC server
        rmiServerRPC = (RMIServerRPC) PortableRemoteObject.narrow(serverObject, RMIServerRPC.class);

    }

}
