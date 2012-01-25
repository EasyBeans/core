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
 * $Id: AbsNameMessage.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.component.smartclient.message;

import java.nio.ByteBuffer;

/**
 * Abstract class that can be used to exchange a message with a name inside.
 * @author Florent Benoit
 */
public abstract class AbsNameMessage extends AbsMessage {

    /**
     * Name used for this message.
     */
    private String name = null;


    /**
     * Builds a new message with the given name.
     * @param name the given name
     */
    public AbsNameMessage(final String name) {
        super();
        this.name = name;
    }

    /**
     * Builds a message by using the data contains in the bytebuffer.
     * @param dataBuffer the data of the message to extract.
     */
    public AbsNameMessage(final ByteBuffer dataBuffer) {
        super();

        // rest of bytes = className
        this.name = decode(dataBuffer);

    }

    /**
     * Gets the name of this message.
     * @return the name of this message.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the OpCode of this message.
     * @return the operation code.
     */
    @Override
    public abstract byte getOpCode();

    /**
     * Gets the content of this message (only this part, not the header).
     * @return the content of this message.
     */
    @Override
    public ByteBuffer getSubMessage() {
        // Needs to send the name
        ByteBuffer nameBuffer = encode(name);

        return nameBuffer;
    }



}
