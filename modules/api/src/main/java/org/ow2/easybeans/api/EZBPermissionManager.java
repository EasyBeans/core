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
 * $Id: EZBPermissionManager.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.api;

/**
 * Class that is linked to an EasyBeans factory and manages the check of the
 * security.
 * @author Florent Benoit
 */
public interface EZBPermissionManager {

    /**
     * Checks the security for the given invocation context.
     * @param invocationContext the context to check.
     * @param runAsBean if true, the bean is a run-as bean.
     * @return true if the access has been granted, else false.
     */
    boolean checkSecurity(final EasyBeansInvocationContext invocationContext, boolean runAsBean);

    /**
     * Test if the caller has a given role. EJBRoleRefPermission object must be
     * created with ejbName and actions equal to roleName<br/>
     * See section 4.3.2 of JACC
     * @param ejbName The name of the EJB on wich look role
     * @param roleName The name of the security role. The role must be one of
     *        the security-role-ref that is defined in the deployment
     *        descriptor.
     * @param inRunAs bean calling this method is running in run-as mode or not ?
     * @return True if the caller has the specified role.
     */
    boolean isCallerInRole(final String ejbName, final String roleName, final boolean inRunAs);

    /**
     * 3.1.5 Translating EJB Deployment Descriptors<br>
     * A reference to a PolicyConfiguration object must be obtained by calling
     * the getPolicyConfiguration method on the PolicyConfigurationFactory
     * implementation class of the provider configured into the container. The
     * policy context identifier used in the call to getPolicyConfiguration must
     * be a String that satisfies the requirements described in Section 3.1.4,
     * EJB Policy Context Identifiers, on page 28. The value true must be passed
     * as the second parameter in the call to getPolicyConfiguration to ensure
     * that any and all policy statements are removed from the policy context
     * associated with the returned PolicyConfiguration. The method-permission,
     * exclude-list, and security-role-ref elements appearing in the deployment
     * descriptor must be translated into permissions and added to the
     * PolicyConfiguration object to yield an equivalent translation as that
     * defined in the following sections and such that every EJB method for
     * which the container performs pre-dispatch access decisions is implied by
     * at least one permission resulting from the translation.
     * @throws PermissionManagerException if permissions can't be set
     */
    void translateMetadata() throws PermissionManagerException;


    /**
     * Commit the Policy Configuration.
     * @throws PermissionManagerException if commit can't be done
     */
    void commit() throws PermissionManagerException;

}
