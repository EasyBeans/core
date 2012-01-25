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
 * $Id: ResourceRequest.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */
package org.ow2.easybeans.component.smartclient.message;

import java.nio.ByteBuffer;

import org.ow2.easybeans.component.smartclient.api.ProtocolConstants;

/**
 * Used to request a resource on the server.
 * @author Florent Benoit
 */
public class ResourceRequest extends AbsNameMessage {

    /**
     * Builds a new message with the given resource name.
     * @param resourceName the given name
     */
    public ResourceRequest(final String resourceName) {
        super(resourceName);
    }

    /**
     * Builds a message by using the data contains in the bytebuffer.
     * @param dataBuffer the data of the message to extract.
     */
    public ResourceRequest(final ByteBuffer dataBuffer) {
        super(dataBuffer);
    }

    /**
     * Gets the OpCode of this message.
     * @return the operation code.
     */
    @Override
    public byte getOpCode() {
        return ProtocolConstants.RESOURCE_REQUEST;
    }

    /**
     * Gets the name of the resource.
     * @return the resource's name
     */
    public String getResourceName() {
        return getName();
    }
}
