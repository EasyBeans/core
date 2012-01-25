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
 * $Id: ServiceRef.java 4708 2009-02-25 13:29:59Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.deployment.metadata.ejbjar.xml;

import java.util.List;

/**
 * A ServiceRef represents the XML <code>service-ref</code> element.
 * @author Guillaume Sauthier
 */
public class ServiceRef {

    /**
     * Reference's name used to match objects.
     */
    private String name;

    /**
     * The overriding location URL of the WSDL.
     */
    private String wsdlLocation;

    /**
     * Port component references.
     */
    private List<PortComponentRef> portComponentRefs = null;

    /**
     * @return the name of this reference
     */
    public String getName() {
        return this.name;
    }

    /**
     * Set the name of this reference.
     * @param name name to be used
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * @return the location (stringified URL) of the WSDL to be used
     */
    public String getWsdlLocation() {
        return this.wsdlLocation;
    }

    /**
     * Set the WSDL location of the web service reference.
     * @param wsdlLocation location as a stringified URL.
     */
    public void setWsdlLocation(final String wsdlLocation) {
        this.wsdlLocation = wsdlLocation;
    }

    /**
     * @return the list of port component references.
     */
    public List<PortComponentRef> getPortComponentRefs() {
        return this.portComponentRefs;
    }

    /**
     * Set the list of port component references.
     * @param portComponentRefs port component references
     */
    public void setPortComponentRefs(final List<PortComponentRef> portComponentRefs) {
        this.portComponentRefs = portComponentRefs;
    }

}
