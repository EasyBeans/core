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
 * $Id: Message.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.component.smartclient.api;

import java.nio.ByteBuffer;

/**
 * All messages needs to implement this interface.
 * @author Florent Benoit
 */
public interface Message {

    /**
     * Size to store an int in bytes.
     */
    int INT_BYTE_SIZE = 4;

    /**
     * Header.
     * <ul>
     * <li> byte = version</li>
     * <li> byte = opcode</li>
     * <li> int = length</li>
     * </ul>
     */
    int HEADER_SIZE = 1 + 1 + INT_BYTE_SIZE;


    /**
     * Gets the OpCode of this message.
     * @return the operation code.
     */
    byte getOpCode();

    /**
     * Gets a message to send.
     * @return the bytebuffer to send
     */
    ByteBuffer getMessage();
}
