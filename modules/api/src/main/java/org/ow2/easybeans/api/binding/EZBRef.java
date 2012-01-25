/**
 * EasyBeans
 * Copyright (C) 2007-2008 Bull S.A.S.
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
 * $Id: EZBRef.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.api.binding;

import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.Referenceable;

import org.ow2.easybeans.api.Factory;

/**
 * EasyBeans reference used for each factory that produces EJB proxies.
 * This is the root interface.
 * @author Florent BENOIT
 *         Contributors:
 *             S. Ali Tokmen (JNDI naming strategy)
 */
public interface EZBRef extends Referenceable {

    /**
     * Gets the interface class name.
     * @return the name of the interface.
     */
    String getItfClassName();

    /**
     * Gets the JNDI name of this reference.
     * @return JNDI name of this reference.
     */
    String getJNDIName();

    /**
     * Gets the factory of this reference.
     * @return the factory linked to this reference object.
     */
    Factory<?, ?> getFactory();

    /**
     * Sets the factory of this reference.
     * @param factory the given factory.
     */
    void setFactory(final Factory<?, ?> factory);

    /**
     * Retrieves the Reference of this object.
     * @return The non-null Reference of this object.
     * @exception NamingException If a naming exception was encountered while
     *            retrieving the reference.
     */
     Reference getReference() throws NamingException;
}
