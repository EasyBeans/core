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
 * $Id: WebserviceEndpoint.java 4703 2009-02-25 10:04:21Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.deployment.metadata.ejbjar.xml;

/**
 * The WebserviceEndpoint represents the EZB specific
 * info relative to a port-component.
 *
 * @author Guillaume Sauthier
 */
public class WebserviceEndpoint {

    /**
     * WSDL publication directory.
     */
    private String wsdlPublicationDirectory;

    /**
     * Name of the port-component to be complemented.
     */
    private String portComponentName;

    public String getPortComponentName() {
        return this.portComponentName;
    }

    public void setPortComponentName(final String portComponentName) {
        this.portComponentName = portComponentName;
    }

    public String getWsdlPublicationDirectory() {
        return this.wsdlPublicationDirectory;
    }

    public void setWsdlPublicationDirectory(final String wsdlPublicationDirectory) {
        this.wsdlPublicationDirectory = wsdlPublicationDirectory;
    }
}
