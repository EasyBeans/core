/**
 * EasyBeans
 * Copyright (C) 2007 Bull S.A.S.
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
 * $Id: LDAPFilter.java 3054 2008-04-30 15:41:13Z sauthieg $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.osgi.util;

import org.osgi.framework.Constants;

/**
 * Utility class for creating LDAP filters.
 * @author Guillaume Sauthier
 */
public final class LDAPFilter {

    /**
     * Empty constructor for utility class.
     */
    private LDAPFilter() { }

    /**
     * Creates a LDAP filter for searching a given interface.
     * @param classname searched interface
     * @return a valid LDAP filter
     */
    public static String createLDAPFilter(final String classname) {
        return "(" + Constants.OBJECTCLASS + "=" + classname + ")";
    }

}
