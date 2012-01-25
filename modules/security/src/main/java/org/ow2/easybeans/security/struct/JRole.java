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
 * $Id: JRole.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.security.struct;

/**
 * This class defines a Role.
 * It use the Principal class in order to add roles to
 * the java.security.acl.Group class
 * @author Florent Benoit
 */
public class JRole extends JPrincipal {

    /**
     * UID for serialization.
     */
    private static final long serialVersionUID = 7698441696763650989L;

    /**
     * Constructor (use the super constructor).
     * @param roleName the name of this role
     */
    public JRole(final String roleName) {
        super(roleName);
    }

}
