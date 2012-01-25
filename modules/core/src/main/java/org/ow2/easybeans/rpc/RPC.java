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
 * $Id: RPC.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.rpc;

import java.util.Hashtable;

import org.ow2.easybeans.rpc.api.ClientRPC;
import org.ow2.easybeans.rpc.rmi.client.RMIClientRPC;

/**
 * Gets a RPC client.<br>
 * By default it will use RMI RPC.<br>
 * @author Florent Benoit
 */
public final class RPC {

    /**
     * Utility class, no public constructor.
     */
    private RPC() {

    }

    /**
     * TODO : manage a pool and different kind of implementation. For now only RMI exists
     * @param rmiClientEnvironment the RMI environment.
     * @return an RPC client
     */
    public static ClientRPC getClient(final Hashtable<?, ?> rmiClientEnvironment) {
        return new RMIClientRPC(rmiClientEnvironment);
    }
}
