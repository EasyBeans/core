/**
 * EasyBeans
 * Copyright (C) 2009 Bull S.A.S.
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
 * $Id: PortComponentRef.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.deployment.metadata.ejbjar.xml;

import java.util.Properties;

/**
 * Allow to define specific settings for a port component ref.
 * @author Florent Benoit
 */
public class PortComponentRef {

    /**
     * Service Endpoint Interface of the port-component-ref.
     */
    private String serviceEndpointInterface = null;

    /**
     * Properties.
     */
    private Properties properties = null;

    /**
     * @return the service endpoint interface.
     */
    public String getServiceEndpointInterface() {
        return this.serviceEndpointInterface;
    }

    /**
     * Sets the service endpoint interface.
     * @param serviceEndpointInterface the given SEI
     */
    public void setServiceEndpointInterface(final String serviceEndpointInterface) {
        this.serviceEndpointInterface = serviceEndpointInterface;
    }

    /**
     * Gets the list of properties.
     * @return the set of properties.
     */
    public Properties getProperties() {
        return this.properties;
    }

    /**
     * Set the properties.
     * @param properties the set of properties.
     */
    public void setProperties(final Properties properties) {
        this.properties = properties;
    }

}
