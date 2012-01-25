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
 * $Id: EJBLocalHomeCallFactory.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.proxy.factory;

import org.ow2.easybeans.proxy.client.EJBLocalHomeInvocationHandler;
import org.ow2.easybeans.proxy.client.LocalCallInvocationHandler;


/**
 * Factory creating an EJB proxy for EJB local home calls.
 * @author Florent Benoit.
 */
public class EJBLocalHomeCallFactory extends LocalCallFactory {

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
    @Override
    protected LocalCallInvocationHandler buildLocalHandler(final Integer embeddedID, final String containerId,
            final String factoryName, final boolean useID) {
        return new EJBLocalHomeInvocationHandler(embeddedID, containerId, factoryName, useID);
    }
}
