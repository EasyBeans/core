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
 * $Id: JPrincipal.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.security.struct;

import java.io.Serializable;
import java.security.Principal;

/**
 * Implementation of Principal class.
 * @author Florent Benoit
 */
public class JPrincipal implements Principal, Serializable {

    /**
     * UID for serialization.
     */
    private static final long serialVersionUID = 5864848835776239991L;

    /**
     * Name of this principal.
     */
    private String name = null;

    /**
     * Constructor.
     * @param name the name of this principal
     */
    public JPrincipal(final String name) {
        this.name = name;
    }

    /**
     * Compares this principal to the specified object. Returns true if the
     * object passed in matches the principal represented by the implementation
     * of this interface.
     * @param another principal to compare with.
     * @return true if the principal passed in is the same as that encapsulated
     *         by this principal, and false otherwise.
     */
    @Override
    public boolean equals(final Object another) {
        if (!(another instanceof Principal)) {
            return false;
        }
        // else
        return name.equals(((Principal) another).getName());
    }

    /**
     * Returns a string representation of this principal.
     * @return a string representation of this principal.
     */
    @Override
    public String toString() {
        return "Principal[" + name + "]";
    }

    /**
     * Returns a hashcode for this principal.
     * @return a hashcode for this principal.
     */
    @Override
    public int hashCode() {
        return name.hashCode();
    }

    /**
     * Returns the name of this principal.
     * @return the name of this principal.
     */
    public String getName() {
        return name;
    }

}
