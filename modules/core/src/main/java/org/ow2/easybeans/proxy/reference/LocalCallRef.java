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
 * $Id: LocalCallRef.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.proxy.reference;

import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.StringRefAddr;

import org.ow2.easybeans.proxy.factory.LocalCallFactory;

/**
 * Define the Referenceable objectd used by local EJB.
 * This is the object that is bind in the registry.
 * @author Florent Benoit
 */
public class LocalCallRef extends AbsCallRef {

    /**
     * Property referencing the embedded server's ID.
     */
    public static final String EMBEDDED_ID = "embeddedID";

    /**
     * Embedded server ID.
     */
    private Integer embeddedID = null;


    /**
     * Constructor : build a reference.
     * @param itfClassName the name of the interface.
     * @param embeddedID the ID of the embedded server.
     * @param containerId the ID of the container.
     * @param factoryName the name of the factory
     * @param useID true if all instance build with this ref are unique
     *        (stateful), false if it references the same object (stateless)
     */
    public LocalCallRef(final String itfClassName, final Integer embeddedID, final String containerId,
            final String factoryName, final boolean useID) {
        super(itfClassName, containerId, factoryName, useID);
        this.embeddedID = embeddedID;
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
        Reference reference = new Reference(getItfClassName(), getFactoryClassName(), null);
        reference.add(new StringRefAddr(EMBEDDED_ID, this.embeddedID.toString()));
        updateRefAddr(reference);
        return reference;
    }

    /**
     * Gets the name of the factory (can be used by subclasses to change the name).
     * @return the name of the factory used by this reference.
     */
    protected String getFactoryClassName() {
        return LocalCallFactory.class.getName();
    }
}
