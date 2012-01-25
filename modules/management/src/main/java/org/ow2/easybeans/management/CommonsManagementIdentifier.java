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
 * $Id: CommonsManagementIdentifier.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.management;

import org.ow2.easybeans.api.jmx.EZBManagementIdentifier;

/**
 * Commons {@link org.ow2.easybeans.api.jmx.EZBManagementIdentifier}.
 *
 * @author Guillaume Sauthier
 * @param <T> Managed Type
 */
public abstract class CommonsManagementIdentifier<T> implements EZBManagementIdentifier<T> {

    /**
     * Domain name.
     */
    private String domainName = "";

    /**
     * Server name.
     */
    private String serverName = null;

    /**
     * Sets the domain for this identifier.
     * @param domainName the JMX Domain name of the MBean.
     */
    public void setDomain(final String domainName) {
        this.domainName = domainName;
    }

    /**
     * @return the JMX Server name of the MBean.
     */
    public String getServerName() {
        return serverName;
    }

    /**
     * Sets the Server name for this identifier.
     * @param serverName the JMX Server name of this MBean.
     */
    public void setServerName(final String serverName) {
        this.serverName = serverName;
    }

    /**
     * @return Returns the JMX Domain name of the MBean.
     */
    public String getDomain() {
        return domainName;
    }

    /**
     * {@inheritDoc}
     */
    public String getTypeName() {
        return "type";
    }

    /**
     * {@inheritDoc}
     */
    public String getTypeProperty() {
        StringBuilder sb = new StringBuilder();
        sb.append(getTypeName());
        sb.append("=");
        sb.append(getTypeValue());
        return sb.toString();
    }

}
