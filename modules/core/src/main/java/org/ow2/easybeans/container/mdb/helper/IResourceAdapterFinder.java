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
 * $Id: IResourceAdapterFinder.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.container.mdb.helper;

import javax.resource.ResourceException;
import javax.resource.spi.ResourceAdapter;

/**
 * Interface that may be used to find the Resource Adapter object.
 * @author Florent BENOIT
 */
public interface IResourceAdapterFinder {

    /**
     * Gets the resource adapter object for the given jndi name (activation
     * spec) and the given embedded object.
     * @param jndiName the name of the activation spec bound in the registry
     * @return an instance of the resource adapter that provides the MDB
     *         activation spec.
     * @throws ResourceException if an error occurs while trying to get the
     *         resource adapter.
     */
    ResourceAdapter getResourceAdapter(final String jndiName) throws ResourceException;

}
