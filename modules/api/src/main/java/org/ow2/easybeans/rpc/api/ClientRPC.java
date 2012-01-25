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
 * $Id: ClientRPC.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.rpc.api;

/**
 * All RPC implementation need to implement this interface which send a request
 * to the remote side and receive a response forwarded back to the client.
 * @author Florent Benoit
 */
public interface ClientRPC {

    /**
     * Sends a request comes to the remote side.<br>
     * A response is done by the remote side and it sends back a response.
     * @param request the EJB request.
     * @return a response that have been processed by the server.
     */
    EJBResponse sendEJBRequest(EJBRemoteRequest request);
}
