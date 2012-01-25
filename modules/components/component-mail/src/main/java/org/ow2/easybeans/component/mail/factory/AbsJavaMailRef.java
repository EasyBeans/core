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
 * $Id: AbsJavaMailRef.java 5650 2010-11-04 14:50:58Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.component.mail.factory;

import java.io.IOException;
import java.io.Serializable;
import java.util.Properties;

import javax.naming.BinaryRefAddr;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.Referenceable;
import javax.naming.StringRefAddr;

import org.ow2.util.marshalling.Serialization;

/**
 * Abstract referenceable object used to bind JavaMail factories.
 * @author Florent BENOIT
 */
public abstract class AbsJavaMailRef implements Referenceable {

    /**
     * Name of the factory.
     */
    private String name = null;

    /**
     * JNDI name of the factory.
     */
    private String jndiName = null;

    /**
     * Mail Properties.
     */
    private Properties properties = null;

    /**
     * Username for authentication.
     */
    private String authName = null;

    /**
     * Password for authentication.
     */
    private String authPass = null;

    /**
     * Build a new javamail reference.
     */
    public AbsJavaMailRef() {
    }

    /**
     * @return the name of the factory.
     */
    public String getName() {
        return this.name;
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
        return this.jndiName;
    }

    /**
     * Set the JNDI name of this factory.
     * @param jndiName the JNDI name of this factory
     */
    public void setJNDIName(final String jndiName) {
        this.jndiName = jndiName;
    }

    /**
     * @return the authentication name of the factory.
     */
    public String getAuthName() {
        return this.authName;
    }

    /**
     * Set the authentication name of this factory.
     * @param authName the authentication name of this factory
     */
    public void setAuthName(final String authName) {
        this.authName = authName;
    }

    /**
     * @return the authentication password of the factory.
     */
    public String getAuthPass() {
        return this.authPass;
    }

    /**
     * Set the authentication password of this factory.
     * @param authPass the authentication password of this factory
     */
    public void setAuthPass(final String authPass) {
        this.authPass = authPass;
    }

    /**
     * @return the properties used by the factory.
     */
    public Properties getProperties() {
        return this.properties;
    }

    /**
     * Sets the session properties.
     * @param properties the session properties.
     */
    public void setProperties(final Properties properties) {
        this.properties = properties;
    }

    /**
     * Adds some settings to the reference.
     * @param reference the reference to configure
     * @throws NamingException if something goes wrong
     */
    protected void updateRefAddr(final Reference reference) throws NamingException {
        // Add the mail properties
        putObject(reference, AbsJavaMailFactory.MAIL_PROPERTIES, this.properties);

        // And then the authentication
        reference.add(new StringRefAddr(AbsJavaMailFactory.AUTHENTICATION_USERNAME, this.authName));
        reference.add(new StringRefAddr(AbsJavaMailFactory.AUTHENTICATION_PASSWORD, this.authPass));
    }

    /**
     * Put the given object in the reference with the given property name.
     * @param reference the given reference
     * @param propertyName the given property
     * @param o the object to add
     * @throws NamingException if object is not put.
     */
    public void putObject(final Reference reference, final String propertyName, final Serializable o) throws NamingException {
        try {
            reference.add(new BinaryRefAddr(propertyName, Serialization.storeObject(o)));
        } catch (IOException e) {
            NamingException ne = new NamingException("Cannot get bytes from the to recipients object");
            ne.initCause(e);
            throw ne;
        }
    }

    /**
     * Retrieves the Reference of this object.
     * @return The non-null Reference of this object.
     * @exception NamingException If a naming exception was encountered while
     *            retrieving the reference.
     */
    public abstract Reference getReference() throws NamingException;

    /**
     * Return the type of the factory.
     * @return the type of the mail factory
     */
    public abstract String getType();

}
