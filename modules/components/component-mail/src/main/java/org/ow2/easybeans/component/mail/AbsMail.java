/**
 * EasyBeans
 * Copyright (C) 2007 Bull S.A.S.
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
 * $Id: AbsMail.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.component.mail;

import java.util.List;
import java.util.Properties;

import org.ow2.easybeans.component.util.Property;

/**
 * Default stuff common to common objects.
 * @author Florent BENOIT
 */
public class AbsMail implements MailItf {

    /**
     * Properties for the factories.
     */
    private List<Property> mailProperties = null;

    /**
     * Name of the factory.
     */
    private String name = null;

    /**
     * JNDI name of the factory.
     */
    private String jndiName = null;

    /**
     * Auth properties for this factory.
     */
    private Auth auth = null;

    /**
     * @return the mail properties.
     */
    public Properties getMailSessionProperties() {
        // Get properties
        Properties mailSessionProperties = new Properties();
        if (mailProperties != null) {
            for (Property property : mailProperties) {
                mailSessionProperties.put(property.getName(), property.getValue());
            }
        }
        return mailSessionProperties;
    }

    /**
     * @return the name of the factory.
     */
    public String getName() {
        return name;
    }

    /**
     * Set the name of this factory.
     * @param name the name of this factory
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * @return the JNDI name of the factory.
     */
    public String getJNDIName() {
        return jndiName;
    }

    /**
     * Set the JNDI name of this factory.
     * @param jndiName the JNDI name of this factory
     */
    public void setJNDIName(final String jndiName) {
        this.jndiName = jndiName;
    }

    /**
     * Gets the list of properties.
     * @return the list of properties.
     */
    public List<Property> getProperties() {
        return this.mailProperties;
    }

    /**
     * Set the list of properties.
     * @param mailProperties the list of properties.
     */
    public void setProperties(final List<Property> mailProperties) {
        this.mailProperties = mailProperties;
    }

    /**
     * Set the authenticator.
     * @param auth the given authenticator
     */
    public void setAuth(final Auth auth) {
        this.auth = auth;
    }

    /**
     * @return the authenticator.
     */
    public Auth getAuth() {
        return this.auth;
    }
}
