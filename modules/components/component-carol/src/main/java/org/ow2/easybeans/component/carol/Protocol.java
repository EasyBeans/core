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
 * $Id: Protocol.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.component.carol;

/**
 * Protocol class which describe the RMI protocol configuration.
 * @author Florent Benoit
 */
public class Protocol {

    /**
     * Default hostname.
     */
    private static final String DEFAULT_HOSTNAME = "localhost";

    /**
     * Default port number.
     */
    private static final int DEFAULT_PORTNUMBER = 1099;

    /**
     * Name.
     */
    private String name = null;

    /**
     * URL (contains host and port).
     */
    private String url = null;

    /**
     * Hostname (other way to configure the URL).
     */
    private String hostname = DEFAULT_HOSTNAME;

    /**
     * Port number (other way to configure the URL).
     */
    private int portNumber = DEFAULT_PORTNUMBER;

    /**
     * @return the hostname for this protocol
     */
    public String getHostname() {
        return hostname;
    }

    /**
     * Sets the hostname of this protocol.
     * @param hostname the host for listening
     */
    public void setHostname(final String hostname) {
        this.hostname = hostname;
    }

    /**
     * @return the name for this protocol
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of this protocol.
     * @param name the given name
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * @return the port number for this protocol
     */
    public int getPortNumber() {
        return portNumber;
    }

    /**
     * Sets the port number of this protocol.
     * @param portNumber the port for listening
     */
    public void setPortNumber(final int portNumber) {
        this.portNumber = portNumber;
    }

    /**
     * @return the URL for this protocol
     */
    public String getUrl() {
        return url;
    }

    /**
     * Sets the URL used as PROVIDER_URL.
     * @param url the url which define host + port.
     */
    public void setUrl(final String url) {
        this.url = url;
    }

}
