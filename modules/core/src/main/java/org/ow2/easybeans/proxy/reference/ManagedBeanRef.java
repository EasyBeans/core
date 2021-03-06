/*
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
 * $Id:$
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.proxy.reference;

import org.ow2.easybeans.proxy.factory.ManagedBeanObjectFactory;

/**
 * Define the {@link javax.naming.Referenceable} object used to create ManagedBean.
 * This is the object that is bind in the registry.
 *
 * @author Loic Albertin
 */
public class ManagedBeanRef extends LocalCallRef {


    /**
     * Constructor : build a reference.
     *
     * @param itfClassName the name of the interface.
     * @param embeddedID   the ID of the embedded server.
     * @param containerId  the ID of the container.
     * @param factoryName  the name of the factory
     * @param useID        true if all instance build with this ref are unique
     *                     (stateful), false if it references the same object (stateless)
     */
    public ManagedBeanRef(final String itfClassName, final Integer embeddedID, final String containerId, final String factoryName,
            final boolean useID) {
        super(itfClassName, embeddedID, containerId, factoryName, useID);
    }

    /**
     * Gets the name of the factory (can be used by subclasses to change the name).
     *
     * @return the name of the factory used by this reference.
     */
    protected String getFactoryClassName() {
        return ManagedBeanObjectFactory.class.getName();
    }
}
