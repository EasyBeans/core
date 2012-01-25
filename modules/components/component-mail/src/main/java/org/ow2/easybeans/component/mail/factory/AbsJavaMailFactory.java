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
 * $Id: AbsJavaMailFactory.java 5650 2010-11-04 14:50:58Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.component.mail.factory;

import java.io.IOException;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.naming.NamingException;
import javax.naming.RefAddr;
import javax.naming.Reference;
import javax.naming.StringRefAddr;
import javax.naming.spi.ObjectFactory;

import org.ow2.util.marshalling.Serialization;

/**
 * Abstract JNDI factory for Mail factories.
 * @author Florent BENOIT
 */
public abstract class AbsJavaMailFactory implements ObjectFactory {

    /**
     * User name for authentication.
     */
    public static final String AUTHENTICATION_USERNAME = "auth.name";

    /**
     * Password for authentication.
     */
    public static final String AUTHENTICATION_PASSWORD = "auth.pass";

    /**
     * Properties used for mail factory.
     */
    public static final String MAIL_PROPERTIES = "mail.properties";

    /**
     * Gets an authenticator for the given reference.
     * @param reference the given reference
     * @return an authenticator or null if there is none.
     */
    protected Authenticator getAuthenticator(final Reference reference) {
        // Get auth name
        String authName = getString(reference, AUTHENTICATION_USERNAME);

        // Get auth password
        String authPass = getString(reference, AUTHENTICATION_PASSWORD);

        // Build an authenticator if the properties have been set.
        SimpleAuthenticator authenticator = null;
        if (authName != null && authPass != null) {
            authenticator = new SimpleAuthenticator(authName, authPass);
        }
        return authenticator;
    }

    /**
     * Gets the session properties.
     * @param reference the given reference
     * @return session properties
     * @throws NamingException if object is not read.
     */
    public Properties getSessionProperties(final Reference reference) throws NamingException {
        return getObject(reference, MAIL_PROPERTIES);
    }

    /**
     * Get the given string from the reference.
     * @param reference the given reference
     * @param propertyName the given property
     * @return the given string
     */
    public String getString(final Reference reference, final String propertyName) {
        RefAddr tmpRefAddr = reference.get(propertyName);
        String tmpValue = null;
        if (tmpRefAddr instanceof StringRefAddr) {
            tmpValue = (String) ((StringRefAddr) tmpRefAddr).getContent();
        }
        return tmpValue;
    }

    /**
     * Load a given object for the given property name.
     * @param reference the given reference
     * @param propertyName the given property
     * @return the loaded object
     * @throws NamingException if object is not read.
     * @param <T> the type of the object to load
     */
    @SuppressWarnings("unchecked")
    public <T> T getObject(final Reference reference, final String propertyName) throws NamingException {
        RefAddr refAddr = reference.get(propertyName);
        T obj = null;
        if (refAddr != null) {
            try {
                obj = (T) Serialization.loadObject((byte[]) refAddr.getContent());
            } catch (IOException e) {
                NamingException ne = new NamingException("Cannot load mail session properties object");
                ne.initCause(e);
                throw ne;
            } catch (ClassNotFoundException e) {
                NamingException ne = new NamingException("Cannot load mail session properties object");
                ne.initCause(e);
                throw ne;
            }
        }
        return obj;
    }


}
