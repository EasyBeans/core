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
 * $Id: ResourceAnswer.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

 package org.ow2.easybeans.component.smartclient.message;

import java.nio.ByteBuffer;

import org.ow2.easybeans.component.smartclient.api.ProtocolConstants;

/**
 * Answer to the client with the content of the resource.
 * @author Florent Benoit
 */
public class ResourceAnswer extends AbsNameBytesMessage {

    /**
     * Builds a new answer with the given name and the array of bytes.
     * @param name the given name
     * @param bytes the array of bytes to store.
     */
    public ResourceAnswer(final String name, final byte[] bytes) {
        super(name, bytes);
    }

    /**
     * Builds an answer with the given bytebuffer.
     * @param buffer buffer containing the data to extract.
     */
    public ResourceAnswer(final ByteBuffer buffer) {
        super(buffer);
    }

    /**
     * Gets the OpCode of this answer.
     * @return the operation code.
     */
    @Override
    public byte getOpCode() {
        return ProtocolConstants.RESOURCE_ANSWER;
    }

    /**
     * Gets the name of the resource.
     * @return the name of this resource.
     */
    public String getResourceName() {
        return getName();
    }

}
