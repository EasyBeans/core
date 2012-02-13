/**
 * EasyBeans
 * Copyright (C) 2012 Bull S.A.S.
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
 * $Id: LocalCallRef.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.proxy.reference;

import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.StringRefAddr;

import org.ow2.easybeans.proxy.factory.NoInterfaceLocalCallFactory;


/**
 * Define the Referenceable objectd used by a no-interface view local EJB.
 * This is the object that is bind in the registry.
 * @author Florent Benoit
 */
public class NoInterfaceLocalCallRef extends LocalCallRef {

    /**
     * Property referencing the name of the proxy class.
     */
    public static final String PROXY_CLASSNAME = "proxyclassname";

    /**
     * Proxy class Name.
     */
    private String proxyClassName = null;

    /**
     * Constructor : build a reference.
     * @param itfName the name of the bean class.
     * @param embeddedID the ID of the embedded server.
     * @param containerId the ID of the container.
     * @param factoryName the name of the factory
     * @param useID true if all instance build with this ref are unique
     *        (stateful), false if it references the same object (stateless)
     * @param proxyClassName the name of the proxy class.
     */
    public NoInterfaceLocalCallRef(final String itfName, final Integer embeddedID, final String containerId,
            final String factoryName, final boolean useID, final String proxyClassName) {
        super(itfName, embeddedID, containerId, factoryName, useID);
        this.proxyClassName = proxyClassName;
    }

    /**
     * Retrieves the Reference of this object.
     * @return The non-null Reference of this object.
     * @exception NamingException If a naming exception was encountered while
     *            retrieving the reference.
     */
    @Override
    public Reference getReference() throws NamingException {

        // Build the reference to the factory
        Reference reference = super.getReference();
        reference.add(new StringRefAddr(PROXY_CLASSNAME, this.proxyClassName));
        updateRefAddr(reference);
        return reference;
    }

    /**
     * Gets the name of the factory (can be used by subclasses to change the name).
     * @return the name of the factory used by this reference.
     */
    @Override
    protected String getFactoryClassName() {
        return NoInterfaceLocalCallFactory.class.getName();
    }
}
