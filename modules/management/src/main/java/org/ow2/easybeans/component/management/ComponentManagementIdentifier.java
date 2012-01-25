/**
 * EasyBeans
 * Copyright (C) 2008 Bull S.A.S.
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
 * $Id: ComponentManagementIdentifier.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.component.management;

import org.ow2.easybeans.api.jmx.EZBManagementIdentifier;
import org.ow2.easybeans.component.api.EZBComponent;

/**
 * Generic {@link org.ow2.easybeans.api.jmx.EZBManagementIdentifier} for EZBComponents.
 * @author missonng
 * @param <T> Managed Type.
 */
public abstract class ComponentManagementIdentifier<T extends EZBComponent> implements EZBManagementIdentifier<T> {

    /**
     * Default domain name.
     */
    private static final String DEFAULT_DOMAIN_NAME = "";

    /**
     * Domain name.
     */
    private String domainName = null;

    /**
     * Server name.
     */
    private String serverName = null;

    /**
     * {@inheritDoc}
     */
    public String getDomain() {
        if (this.domainName == null) {
            return DEFAULT_DOMAIN_NAME;
        }
        return this.domainName;
    }

    /**
     * {@inheritDoc}
     */
    public void setDomain(final String domainName) {
        this.domainName = domainName;
    }

    /**
     * @return the JMX Server name of the MBean.
     */
    public String getServerName() {
        return this.serverName;
    }

    /**
     * {@inheritDoc}
     */
    public void setServerName(final String serverName) {
        this.serverName = serverName;
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
    public String getTypeValue() {
        return "EZBComponent";
    }

    /**
     * {@inheritDoc}
     */
    public String getTypeProperty() {
        return getTypeName() + "=" + getTypeValue();
    }

    /**
     * {@inheritDoc}
     */
    public String getNamePropertyValue(final T instance) {
        return instance.getClass().getName();
    }

    /**
     * {@inheritDoc}
     */
    public String getAdditionnalProperties(final T instance) {
        return null;
    }
}
