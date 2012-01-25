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
 * $Id: IMethodSecurityInfo.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.api.bean.info;

import java.security.Permission;
import java.util.List;

/**
 * Used to describe permission information for a given method.
 * @author Florent Benoit
 */
public interface IMethodSecurityInfo {

    /**
     * This method is excluded (no call allowed if true).
     * @param excluded boolean true/false.
     */
    void setExcluded(boolean excluded);

    /**
     * @return true if the method is excluded.
     */
    boolean isExcluded();

    /**
     * This method is unchecked (if true, all calls are allowed to this method).
     * @param unchecked boolean true/false.
     */
    void setUnchecked(boolean unchecked);

    /**
     * @return true if the method is unchecked.
     */
    boolean isUnchecked();

    /**
     * Add the given role to the list of roles allowed to call this method.
     * @param roleName the name of the role.
     */
    void addRole(String roleName);

    /**
     * @return list of roles allowed to call this method.
     */
    List<String> getRoles();

    /**
     * Sets the permission.
     * @param permission the permission to set.
     */
    void setPermission(Permission permission);

    /**
     * @return permissions for this method.
     */
    Permission getPermission();

}
