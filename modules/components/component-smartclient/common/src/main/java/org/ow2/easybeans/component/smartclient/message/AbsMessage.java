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
 * $Id: AbsMessage.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.component.smartclient.message;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

import org.ow2.easybeans.component.smartclient.api.Message;
import org.ow2.easybeans.component.smartclient.api.ProtocolConstants;

/**
 * Abstract class that needs to be used for every message that are exchanged between client and endpoint.
 * @author Florent Benoit
 *
 */
public abstract class AbsMessage implements Message {

    /**
     * Gets a message to send.
     * @return the bytebuffer to send
     */
    public ByteBuffer getMessage() {
        ByteBuffer subMessage = getSubMessage();

        // compute length
        int length = HEADER_SIZE;
        if (subMessage != null) {
            length += subMessage.capacity();
        }

        // Create ByteBuffer
        ByteBuffer byteBuffer = ByteBuffer.allocate(length);

        // Append header
        byteBuffer.put(ProtocolConstants.PROTOCOL_VERSION);
        byteBuffer.put(getOpCode());
        if (subMessage != null) {
            byteBuffer.putInt(subMessage.capacity());
        }

        // append inner message (go to position 0 first)
        if (subMessage != null) {
            subMessage.position(0);
            byteBuffer.put(subMessage);
        }

        // reset our position
        byteBuffer.position(0);

        // return buffer
        return byteBuffer;
    }

    /**
     * Gets the OpCode of this message.
     * @return the operation code.
     */
    public abstract byte getOpCode();

    /**
     * Gets the content of this message (only this part, not the header).
     * @return the content of this message.
     */
    public abstract ByteBuffer getSubMessage();


    /**
     * Encode the given string into a bytebuffer.
     * @param str the given string
     * @return a bytebuffer with UTF-8 encoded string
     */
    protected ByteBuffer encode(final String str) {
        byte[] bytes = null;
        try {
            bytes = str.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException("Invalid Encoding scheme", e);
        }
        ByteBuffer buffer = ByteBuffer.allocate(bytes.length);
        buffer.put(bytes);
        return buffer;

    }

    /**
     * Decode the string encoded in the bytebuffer in UTF-8 format.
     * @param buffer the given buffer to analyze.
     * @return the decoded string
     */
    protected String decode(final ByteBuffer buffer) {
        Charset charset = Charset.forName("UTF-8");
        CharsetDecoder charsetDecoder = charset.newDecoder();

        CharBuffer charBuffer = null;
        try {
            charBuffer = charsetDecoder.decode(buffer);
        } catch (CharacterCodingException e) {
           throw new IllegalStateException("Invalid characted encoding", e);
        }
        return charBuffer.toString();
    }

}
