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
 * $Id: MethodSecurityInfo.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.container.info.security;

import java.security.Permission;
import java.util.ArrayList;
import java.util.List;

import org.ow2.easybeans.api.bean.info.IMethodSecurityInfo;

/**
 * Used to describe permission information for a given method.
 * @author Florent Benoit
 */
public class MethodSecurityInfo implements IMethodSecurityInfo {

    /**
     * Excluded method ?.
     */
    private boolean excluded = false;

    /**
     * Unchecked method ?.
     */
    private boolean unchecked = false;

    /**
     * List of roles for this method.
     */
    private List<String> roles = null;

    /**
     * Permission for this method.
     */
    private Permission permission;

    /**
     * Default constructor.
     */
    public MethodSecurityInfo() {
        this.roles = new ArrayList<String>();
    }


    /**
     * This method is excluded (no call allowed if true).
     * @param excluded boolean true/false.
     */
    public void setExcluded(final boolean excluded) {
        this.excluded = excluded;
    }

    /**
     * @return true if the method is excluded.
     */
    public boolean isExcluded() {
        return excluded;
    }

    /**
     * This method is unchecked (if true, all calls are allowed to this method).
     * @param unchecked boolean true/false.
     */
    public void setUnchecked(final boolean unchecked) {
        this.unchecked = unchecked;
    }

    /**
     * @return true if the method is unchecked.
     */
    public boolean isUnchecked() {
        return unchecked;
    }

    /**
     * Add the given role to the list of roles allowed to call this method.
     * @param roleName the name of the role.
     */
    public void addRole(final String roleName) {
        this.roles.add(roleName);
    }

    /**
     * @return list of roles allowed to call this method.
     */
    public List<String> getRoles() {
        return roles;
    }

    /**
     * Sets the permission.
     * @param permission the permission to set.
     */
    public void setPermission(final Permission permission) {
        this.permission = permission;
    }

    /**
     * @return permissions for this method.
     */
    public Permission getPermission() {
        return permission;
    }

}
