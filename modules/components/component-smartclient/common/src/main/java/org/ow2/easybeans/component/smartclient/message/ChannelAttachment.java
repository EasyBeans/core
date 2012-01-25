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
 * $Id: ChannelAttachment.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.component.smartclient.message;

import java.nio.ByteBuffer;

/**
 * Class used as attachment for client's channel (to store the bytecode that is read).
 * @author Florent Benoit
 */
public class ChannelAttachment {

    /**
     * Default size.
     */
    private static final int DEFAULT_SIZE = 1024;

    /**
     * Init bytebuffer with a default size.
     */
    private ByteBuffer byteBuffer = ByteBuffer.allocate(DEFAULT_SIZE);


    /**
     * Gets the buffer of this object.
     * @return the object's buffer.
     */
    public ByteBuffer getByteBuffer() {
        return byteBuffer;
    }
}
