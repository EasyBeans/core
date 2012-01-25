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
 * $Id: EZBManagementIdentifier.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.api.jmx;

/**
 * JMX Identifier, used to create a JMX ObjectName from an Object.
 * @author Guillaume Sauthier
 * @param <ManagedType> Managed resource Type
 */
public interface EZBManagementIdentifier<ManagedType> {

    /**
     * @return Returns the JMX Domain name of the MBean.
     */
    String getDomain();

    /**
     * May differ if JSR77 MBean or "normal" MBean.<br/>
     * A JSR77 MBean has j2eeType=XX, but a "normal" MBean has type=YY.
     * Will probably be implemented by an abstract class.
     * @return Returns the type=type_name couple.
     */
    String getTypeProperty();

    /**
     * @return Returns the 'type' property name : <code>j2eeType</code>
     *         for JSR 77 and <code>type</code> for others.
     */
    String getTypeName();

    /**
     * This method has to be implemented by each {@link EZBManagementIdentifier}.
     * @return Returns the type value. (example : <code>J2EEServer</code>)
     */
    String getTypeValue();

    /**
     * @param instance Managed instance from which the name will be extracted.
     * @return Returns the ObjectName 'name' property value.
     */
    String getNamePropertyValue(final ManagedType instance);

    /**
     * @param instance Managed instance from which the additionnal
     *        properties will be extracted.
     * @return Returns a comma separated(,) list of properties (name=value)
     */
    String getAdditionnalProperties(final ManagedType instance);

    /**
     * Sets the domain for this identifier.
     * @param domainName the JMX Domain name of the MBean.
     */
    void setDomain(final String domainName);

    /**
     * Sets the Server name for this identifier.
     * @param serverName the JMX Server name of this MBean.
     */
    void setServerName(final String serverName);

}
