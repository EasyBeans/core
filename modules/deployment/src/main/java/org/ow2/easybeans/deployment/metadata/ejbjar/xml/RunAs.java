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
 * $Id: RunAs.java 4431 2009-01-13 14:28:30Z fornacif $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.deployment.metadata.ejbjar.xml;

/**
 * A RunAs represents the XML <code>run-as</code> element.
 * @author Francois Fornaciari
 */
public class RunAs {

    /**
     * The principal name of this run-as.
     */
    private String principalName;

    /**
     * @return the principal name of this run-as
     */
    public String getPrincipalName() {
        return this.principalName;
    }

    /**
     * Set the principal name of this run-as.
     * @param principalName principalName to be used
     */
    public void setPrincipalName(final String principalName) {
        this.principalName = principalName;
    }

}
