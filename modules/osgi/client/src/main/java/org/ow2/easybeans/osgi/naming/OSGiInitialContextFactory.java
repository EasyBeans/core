/**
 * EasyBeans
 * Copyright (C) 2006 Bull S.A.S.
 * Contact: easybeans@objectweb.org
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
 * $Id: OSGiInitialContextFactory.java 3054 2008-04-30 15:41:13Z sauthieg $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.osgi.naming;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;

/**
 * Add support for OSGi bundle URL protocols and delegates to a Carol <code>MultiOrbInitialContextFactory</code>.
 * @author Guillaume Sauthier
 */
public class OSGiInitialContextFactory implements InitialContextFactory {

    /**
     * 'handler.pkgs' property name.
     */
    private static final String HANDLER_PKGS_PROP = "java.protocol.handler.pkgs";

    /**
     * Carol Multi InitialContextFactory (ObjectWeb).
     */
    private static final String CAROL_OBJECTWEB_MULTI_ICF = "org.objectweb.carol.jndi.spi.MultiOrbInitialContextFactory";

    /**
     * Carol Multi InitialContextFactory (OW2).
     */
    private static final String CAROL_OW2_MULTI_ICF = "org.ow2.carol.jndi.spi.MultiOrbInitialContextFactory";

    /**
     * Carol OW2 class for a test.
     */
    private static final String CAROL_OW2_TEST_CLASS = "org.ow2.carol.util.configuration.ProtocolConfiguration";

    /**
     * Default constructor for the factory.
     * Adds the support for <code>bundle</code> (Felix) and <code>bundleentry</code> (Equinox) protocols.
     */
    public OSGiInitialContextFactory() {

        // Get the old property value
        String handlerPkgs = System.getProperty(HANDLER_PKGS_PROP);

        StringBuilder sb = new StringBuilder();

        // Add the new package for additionnal support
        if (handlerPkgs != null) {
            sb.append(handlerPkgs);
            sb.append("|");
        }

        sb.append("org.ow2.easybeans.osgi.protocol");

        // Set back the property with the new value
        System.setProperty(HANDLER_PKGS_PROP, sb.toString());

    }

    /**
     * Delegates to a Carol <code>MultiOrbInitialContextFactory</code>.
     * {@inheritDoc}
     * @see javax.naming.spi.InitialContextFactory#getInitialContext(java.util.Hashtable)
     */
    @SuppressWarnings("unchecked")
    public Context getInitialContext(final Hashtable environment)
            throws NamingException {

        // Try to see if Carol OW2 is present
        boolean carolV3Present = false;
        try {
            this.getClass().getClassLoader().loadClass(CAROL_OW2_TEST_CLASS);
            carolV3Present = true;
        } catch (ClassNotFoundException e) {
            carolV3Present = false;
        } catch (NoClassDefFoundError e) {
            carolV3Present = false;
        }

        if (carolV3Present) {
            environment.put(Context.INITIAL_CONTEXT_FACTORY, CAROL_OW2_MULTI_ICF);
        } else {
            environment.put(Context.INITIAL_CONTEXT_FACTORY, CAROL_OBJECTWEB_MULTI_ICF);
        }
        return new InitialContext(environment);
    }

}
