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
 * $Id: RMIServerRPC.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.rpc.api;

import java.rmi.Remote;
import java.rmi.RemoteException;


/**
 * Server side interface which need to handle the requests.
 * @author Florent Benoit
 */
public interface RMIServerRPC extends Remote {

    /**
     * JNDI name to use when binding the RPC invoker.
     */
    String RPC_JNDI_NAME = "RMI_SERVER_RPC";

    /**
     * Handle a request and send back a response.
     * @param request the ejb request to handle.
     * @return a response.
     * @throws RemoteException if there are errors on the prococol.
     */
    EJBResponse getEJBResponse(EJBRemoteRequest request) throws RemoteException;
}
