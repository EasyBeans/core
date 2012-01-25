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
 * $Id: AbsNameBytesMessage.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.component.smartclient.message;

import java.nio.ByteBuffer;

/**
 * Abstract class used for exchanging a name and an array of bytes.
 * @author Florent Benoit
 */
public abstract class AbsNameBytesMessage extends AbsMessage {

    /**
     * Name contained in the message.
     */
    private String name = null;

    /**
     * Array of bytes stored in the message.
     */
    private byte[] bytes = null;

    /**
     * Builds a new message with the given name and the array of bytes.
     * @param name the given name
     * @param bytes the array of bytes to store.
     */
    public AbsNameBytesMessage(final String name, final byte[] bytes) {
        super();
        this.name = name;
        this.bytes = bytes.clone();
    }

    /**
     * Builds an message with the given bytebuffer.
     * @param dataBuffer buffer containing the data to extract.
     */
    public AbsNameBytesMessage(final ByteBuffer dataBuffer) {
        super();
        // Get length of the name
        int lengthName = dataBuffer.getInt();


        // allocate buffer with the size of the name
        ByteBuffer nameBuffer = ByteBuffer.allocate(lengthName);
        for (int l = 0; l < lengthName; l++) {
            byte b = dataBuffer.get();
            nameBuffer.put(b);
        }
        // decode the name
        nameBuffer.position(0);
        // set it
        this.name = decode(nameBuffer);

        // rest of bytes = bytecode
        this.bytes = new byte[dataBuffer.limit() - dataBuffer.position()];
        int k = 0;
        for (int i = dataBuffer.position(); i < dataBuffer.limit(); i++) {
            this.bytes[k++] = dataBuffer.get(i);
        }

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
        // Encode the classname
        ByteBuffer nameBuffer = encode(this.name);
        nameBuffer.position(0);

        // create buffer : length's name(int) + name + bytecode
        ByteBuffer messageBuffer = ByteBuffer.allocate(INT_BYTE_SIZE + nameBuffer.capacity() + this.bytes.length);

        // appends length
        messageBuffer.putInt(nameBuffer.capacity());

        // Needs to append the name
        messageBuffer.put(nameBuffer);

        // Bytecode
        messageBuffer.put(this.bytes);

        return messageBuffer;
    }

    /**
     * Gets the name of this message.
     * @return the name of this message.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Gets the bytes of this message.
     * @return the bytes of this message.
     */
    public byte[] getBytes() {
        if (this.bytes != null) {
            return this.bytes.clone();
        }
        return null;
    }
}
