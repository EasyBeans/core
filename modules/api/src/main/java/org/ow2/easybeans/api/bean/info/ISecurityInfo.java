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
 * $Id: ISecurityInfo.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.api.bean.info;

import java.util.List;

import javax.security.auth.Subject;

import org.ow2.util.ee.metadata.common.api.xml.struct.ISecurityRoleRef;

/**
 * Runtime info about security.
 * @author Florent Benoit
 */
public interface ISecurityInfo {

    /**
     * Adds a role for this bean (for isCallerInRole).
     * @param roleName the name of a role.
     */
    void addDeclaredRole(String roleName);

    /**
     * Sets the list of declared roles.
     * @param declaredRoles list of declared roles.
     */
    void setDeclaredRole(List<String> declaredRoles);

    /**
     * @return list of roles declared for this bean.
     */
    List<String> getDeclaredRoles();

    /**
     * Adds a method containing security.
     * @param methodSecurityInfo the info about security.
     */
    void addMethodSecurityInfo(IMethodSecurityInfo methodSecurityInfo);

    /**
     * @return list of security infos on all methods.
     */
    List<IMethodSecurityInfo> getMethodSecurityInfos();

    /**
     * Sets the name of the run-as security role.
     * @param runAsRole the name of the role.
     */
    void setRunAsRole(String runAsRole);


    /**
     * Gets run-as name.
     * @return the name of the security role for the run-as.
     */
    String getRunAsRole();


    /**
     * Gets run-as role subject.
     * @return a subject with run-as role as role.
     */
    Subject getRunAsSubject();

    /**
     * Gets the list of &lt;security-role-ref&gt; elements.
     * @return list of &lt;security-role-ref&gt; elements.
     */
    List<ISecurityRoleRef> getSecurityRoleRefList();

}
