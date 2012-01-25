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
 * $Id: JPolicy.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.security.jacc.provider;

import java.io.FilePermission;
import java.lang.reflect.ReflectPermission;
import java.net.SocketPermission;
import java.security.CodeSource;
import java.security.Permission;
import java.security.PermissionCollection;
import java.security.Policy;
import java.security.Principal;
import java.security.ProtectionDomain;
import java.util.PropertyPermission;

import javax.management.MBeanPermission;
import javax.security.jacc.EJBMethodPermission;
import javax.security.jacc.EJBRoleRefPermission;
import javax.security.jacc.PolicyConfiguration;
import javax.security.jacc.PolicyConfigurationFactory;
import javax.security.jacc.PolicyContext;
import javax.security.jacc.PolicyContextException;
import javax.security.jacc.WebResourcePermission;
import javax.security.jacc.WebRoleRefPermission;
import javax.security.jacc.WebUserDataPermission;

import org.ow2.util.log.Log;
import org.ow2.util.log.LogFactory;

/**
 * Defines the "delegating Policy provider" / JACC 2.5 In J2SE 1.4 new methods
 * can be used for dynamic permissions implies() and getPermissions() methods on
 * Policy class were added. A replacement Policy object may accomplish this by
 * delegating non-javax.security.jacc policy decisions to the corresponding
 * default system Policy implementation class. A replacement Policy object that
 * relies in this way on the corresponding default Policy implementation class
 * must identify itself in its installation instructions as a "delegating Policy
 * provider"<br>
 * EasyBeans uses delegating model
 * @author Florent Benoit
 */
public final class JPolicy extends Policy {

    /**
     * Logger.
     */
    private static Log logger = LogFactory.getLog(JPolicy.class);

    /**
     * Unique instance of JPolicy.
     */
    private static JPolicy unique = null;

    /**
     * Bootstrap Policy provider use for delegating non-jacc decisions.
     */
    private static Policy initialPolicy = null;

    /**
     * Reference to the EasyBeans PolicyConfigurationFactory. Used for retrieve
     * parameters with interfaces not in
     * javax.security.jacc.PolicyConfigurationFactory
     */
    private static PolicyConfigurationFactory policyConfigurationFactory = null;

    /**
     * Constructor : build a policy which manage JACC permissions. The non-jacc
     * permissions are delegated to the initial Policy class
     */
    private JPolicy() {
        // Retrieve existing policy
        initialPolicy = Policy.getPolicy();

    }

    /**
     * Init the PolicyConfiguration factory object used in Policy configuration.
     * @throws JPolicyException if some methods on PolicyConfigurationFactory
     *         fail
     */
    private void initPolicyConfigurationFactory() throws JPolicyException {
        // Check that factory is the EasyBeans policy configuration factory
        try {
            policyConfigurationFactory = PolicyConfigurationFactory.getPolicyConfigurationFactory();
        } catch (ClassNotFoundException cnfe) {
            throw new JPolicyException("PolicyConfigurationFactory class implementation was not found", cnfe);
        } catch (PolicyContextException pce) {
            throw new JPolicyException("PolicyContextException in PolicyConfigurationFactory", pce);
        }

        // Check that the object is initialized
        if (policyConfigurationFactory == null) {
            throw new JPolicyException("policyConfigurationFactory object hasn't be initialized");
        }

    }

    /**
     * Gets the unique instance of the JACC delegating policy provider.
     * @return unique instance of the JACC delegating policy provider
     */
    public static JPolicy getInstance() {
        if (unique == null) {
            unique = new JPolicy();
        }
        return unique;
    }

    // Section 4.8
    // J2EE 1.4 container can call Policy.implies or Policy.getPermissions
    // with an argument ProtectionDomain that was constructed with the
    // principals of the caller.
    // Then the caller must call implies method on the returned
    // PermissionCollection

    /**
     * Evaluates the global policy for the permissions granted to the
     * ProtectionDomain and tests whether the permission is granted.
     * @param domain the ProtectionDomain to test.
     * @param permission the Permission object to be tested for implication.
     * @return true if "permission" is a proper subset of a permission granted
     *         to this ProtectionDomain.
     */
    @Override
    public boolean implies(final ProtectionDomain domain, final Permission permission) {
        // See 2.5 of JACC. A replacement Policy object may accomplish this
        // by delegating non-javax.security.jacc policy decisions to the
        // corresponding default system Policy implementation class.


        // Something has reset the policy object, avoid NPE
        if (initialPolicy == null) {
            return false;
        }

        if (permission instanceof RuntimePermission || permission instanceof SocketPermission
                || permission instanceof PropertyPermission || permission instanceof FilePermission
                || permission instanceof MBeanPermission || permission instanceof ReflectPermission) {
            return initialPolicy.implies(domain, permission);
        }

        // check with context ID
        String contextID = PolicyContext.getContextID();
        // No context, use existing
        if (contextID == null) {
            return initialPolicy.implies(domain, permission);
        }

        if (!(permission instanceof EJBMethodPermission || permission instanceof EJBRoleRefPermission
                || permission instanceof WebUserDataPermission || permission instanceof WebRoleRefPermission
                || permission instanceof WebResourcePermission)) {
            return initialPolicy.implies(domain, permission);
        }

        logger.debug("Permission being checked = ''{0}''", permission);

        // configuration was committed ?
        try {
            if (policyConfigurationFactory == null) {
                initPolicyConfigurationFactory();
            }

            if (!policyConfigurationFactory.inService(contextID)) {
                logger.debug("Policy configuration factory not in service, return false");
                return false;
            }
        } catch (JPolicyException jpe) {
            logger.error("JPolicy.implies.canNotCheck", jpe);
            return false;
        } catch (PolicyContextException pce) {
            logger.error("JPolicy.implies.canNotCheck", pce);
            return false;
        }

        JPolicyConfiguration jPolicyConfiguration = null;
        try {
            PolicyConfiguration pc = policyConfigurationFactory.getPolicyConfiguration(contextID, false);
            if (pc instanceof JPolicyConfiguration) {
                jPolicyConfiguration = (JPolicyConfiguration) pc;
            } else {
                // Maybe it's a delegating policy configuration and we have a
                // configuration for this object
                jPolicyConfiguration = JPolicyConfigurationKeeper.getConfiguration(contextID);
                if (jPolicyConfiguration == null) {
                    throw new RuntimeException("This policy provider can only manage JPolicyConfiguration objects");
                }
            }
        } catch (PolicyContextException pce) {
            logger.error("JPolicy.implies.canNotRetrieve", contextID, pce);
            return false;
        }

        /*
         * JACC 3.2 The provider must ensure that excluded policy statements
         * take precedence over overlapping unchecked policy statements, and
         * that both excluded and unchecked policy statements take precedence
         * over overlapping role based policy statements.
         */
        PermissionCollection excludedPermissions = jPolicyConfiguration.getExcludedPermissions();
        PermissionCollection uncheckedPermissions = jPolicyConfiguration.getUncheckedPermissions();

        // debug info.
        if (logger.isDebugEnabled()) {
            logger.debug("Check permission");
            logger.debug("Excluded permissions = " + excludedPermissions);
            logger.debug("unchecked permissions = " + uncheckedPermissions);
        }

        // excluded ?
        if (excludedPermissions.implies(permission)) {
            logger.debug("Permission ''{0}'' is excluded, return false", permission);
            return false;
        } else if (uncheckedPermissions.implies(permission)) { // unchecked
            logger.debug("Permission ''{0}'' is unchecked, return true", permission);
            return true;
        } else {
            // per role if any or false
            if (domain.getPrincipals().length > 0) {
                logger.debug("There are principals, checking principals...");
                // check roles
                return isImpliedPermissionForPrincipals(jPolicyConfiguration, permission, domain.getPrincipals());
            }
            // permission not found
            logger.debug("Principals length = 0, there is no principal on this domain");
            logger.debug("Permission ''{0}'' not found, return false", permission);
            return false;
        }
    }

    /**
     * Evaluates the global policy and returns a PermissionCollection object
     * specifying the set of permissions allowed given the characteristics of
     * the protection domain.
     * @param domain the ProtectionDomain associated with the caller.
     * @return the set of permissions allowed for the domain according to the
     *         policy.The returned set of permissions must be a new mutable
     *         instance and it must support heterogeneous Permission types.
     */
    @Override
    public PermissionCollection getPermissions(final ProtectionDomain domain) {

        // Always use delegating model
        return initialPolicy.getPermissions(domain);
    }

    /**
     * Evaluates the global policy and returns a PermissionCollection object
     * specifying the set of permissions allowed for code from the specified
     * code source.
     * @param codeSource the CodeSource associated with the caller. This
     *        encapsulates the original location of the code (where the code
     *        came from) and the public key(s) of its signer.
     * @return the set of permissions allowed for code from codesource according
     *         to the policy.The returned set of permissions must be a new
     *         mutable instance and it must support heterogeneous Permission
     *         types.
     */
    @Override
    public PermissionCollection getPermissions(final CodeSource codeSource) {

        // Always use delegating model
        return initialPolicy.getPermissions(codeSource);
    }

    /**
     * Refreshes/reloads the policy configuration.
     */
    @Override
    public void refresh() {
        initialPolicy.refresh();
    }

    /**
     * Check for each principal permission if the given permission is implied.
     * @param jPolicyConfiguration EasyBeans JACC PolicyConfiguration object
     * @param permission the permission to check
     * @param principals the array of principals on which we must retrieve
     *        permissions
     * @return true if the given permission is implied by a role's permission
     */
    private boolean isImpliedPermissionForPrincipals(final JPolicyConfiguration jPolicyConfiguration,
            final Permission permission, final Principal[] principals) {
        // if (logger.isLoggable(BasicLevel.DEBUG)) {
        // logger.log(BasicLevel.DEBUG, "");
        // }
        PermissionCollection permissions = null;
        int i = 0;
        boolean implied = false;
        // for each principal's permissions check if the given permission is
        // implied
        while (i < principals.length && !implied) {
            if (logger.isDebugEnabled()) {
                logger.debug("Checking permission ''{0}'' with permissions of Principal ''{1}''.", permission, principals[i]
                        .getName());
            }
            permissions = jPolicyConfiguration.getPermissionsForPrincipal(principals[i]);

            if (permissions.implies(permission)) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Permission implied with principal ''{0}''.", principals[i].getName());
                }
                implied = true;
            }
            i++;
        }
        if (!implied) {
            logger.debug("Permission ''{0}'' was not found in each permissions of the given roles, return false", permission);
        }
        return implied;
    }

}
