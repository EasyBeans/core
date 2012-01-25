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
 * $Id: ProtocolConstants.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.component.smartclient.api;

/**
 * Describes all protocol's value used to exchange messages.
 * @author Florent Benoit
 */
public interface ProtocolConstants {

    /**
     * Gets protocol's version.
     * @return the version of the protocol.
     */
    byte version();


    /**
     * Version of the protocol.
     */
    byte PROTOCOL_VERSION = 0x01;

    /**
     * Client asking for a Class.
     */
    byte CLASS_REQUEST = (byte) 0x01;

    /**
     * Server answering for a class.
     */
    byte CLASS_ANSWER = (byte) 0x02;

    /**
     * Server answering for a class that was not found.
     */
    byte CLASS_NOT_FOUND = (byte) 0x03;

    /**
     * Client asking for a resource.
     */
    byte RESOURCE_REQUEST = (byte) 0x04;

    /**
     * Server answering for a resource.
     */
    byte RESOURCE_ANSWER = (byte) 0x05;

    /**
     * Server answering for a resource that was not found.
     */
    byte RESOURCE_NOT_FOUND = (byte) 0x06;

    /**
     * Ask for the PROVIDER_URL.
     */
    byte PROVIDER_URL_REQUEST = (byte) 0x07;

    /**
     * Gets the PROVIDER_URL.
     */
    byte PROVIDER_URL_ANSWER = (byte) 0x08;


}
