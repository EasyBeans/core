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
 * $Id: ClassAnswer.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.component.smartclient.message;

import java.nio.ByteBuffer;

import org.ow2.easybeans.component.smartclient.api.ProtocolConstants;

/**
 * Class used to send a class to the client.
 * @author Florent Benoit
 */
public class ClassAnswer extends AbsNameBytesMessage {

    /**
     * Builds a new answer with the given name and the array of bytes.
     * @param name the given name
     * @param bytes the array of bytes to store.
     */
    public ClassAnswer(final String name, final byte[] bytes) {
        super(name, bytes);
    }

    /**
     * Builds an answer with the given bytebuffer.
     * @param buffer buffer containing the data to extract.
     */
    public ClassAnswer(final ByteBuffer buffer) {
        super(buffer);
    }

    /**
     * Gets the OpCode of this answer.
     * @return the operation code.
     */
    @Override
    public byte getOpCode() {
        return ProtocolConstants.CLASS_ANSWER;
    }

    /**
     * Gets the name of this class.
     * @return the name of this class.
     */
    public String getClassName() {
        return getName();
    }

    /**
     * Gets bytecode of the class.
     * @return te bytecode of the class
     */
    public byte[] getByteCode() {
        return getBytes();
    }
}
