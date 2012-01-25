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
 * $Id: JPolicyConfiguration.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.security.jacc.provider;

import java.security.Permission;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.security.Principal;
import java.security.SecurityPermission;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.security.jacc.PolicyConfiguration;
import javax.security.jacc.PolicyContextException;

import org.ow2.util.log.Log;
import org.ow2.util.log.LogFactory;

/**
 * Defines the PolicyConfiguration implementation class of JACC.
 * @author Florent Benoit
 */
public class JPolicyConfiguration implements PolicyConfiguration {

    /**
     * Available states.
     */
    private enum State {
        /**
         * Open state for the Policy Context Life Cycle Section 3.1.1.1.
         */
        OPEN,

        /**
         * inService state for the Policy Context Life Cycle Section 3.1.1.1.
         */
        IN_SERVICE,

        /**
         * Deleted state for the Policy Context Life Cycle Section 3.1.1.1.
         */
        DELETED
    }

    /**
     * Current state.
     */
    private State state;

    /**
     * ContextID string which differentiate all instances.
     */
    private String contextID = null;

    /**
     * Logger.
     */
    private static Log logger = LogFactory.getLog(JPolicyConfiguration.class);

    /**
     * Excluded permissions.
     */
    private PermissionCollection excludedPermissions = null;

    /**
     * Unchecked permissions.
     */
    private PermissionCollection uncheckedPermissions = null;

    /**
     * Role permissions.
     */
    private Map<String, PermissionCollection> rolePermissions = null;

    /**
     * Constructor of a new PolicyConfiguration object.
     * @param contextID Identifier of this PolicyConfiguration object
     */
    public JPolicyConfiguration(final String contextID) {
        this.contextID = contextID;

        // initial state is open
        resetState();

        // init permissions
        excludedPermissions = new Permissions();
        uncheckedPermissions = new Permissions();
        rolePermissions = new HashMap<String, PermissionCollection>();
    }

    /**
     * Used to add a single excluded policy statement to this
     * PolicyConfiguration.
     * @param permission the permission to be added to the excluded policy
     *        statements.
     * @throws SecurityException if called by an AccessControlContext that has
     *         not been granted the "setPolicy" SecurityPermission.
     * @throws UnsupportedOperationException if the state of the policy context
     *         whose interface is this PolicyConfiguration Object is "deleted"
     *         or "inService" when this method is called.
     * @throws PolicyContextException if the implementation throws a checked
     *         exception that has not been accounted for by the
     *         addToExcludedPolicy method signature. The exception thrown by the
     *         implementation class will be encapsulated (during construction)
     *         in the thrown PolicyContextException.
     */
    public void addToExcludedPolicy(final Permission permission) throws PolicyContextException, SecurityException,
            UnsupportedOperationException {

        logger.debug("Adding permission ''{0}'' as excluded policy.", permission);

        // Section 3.3 - Check permissions
        checkSetPolicy();

        // Open state required
        checkCurrentStateIsInState(State.OPEN);

        // Add permission
        if (permission != null) {
            excludedPermissions.add(permission);
        }

    }

    /**
     * Used to add excluded policy statements to this PolicyConfiguration.
     * @param permissions the collection of permissions to be added to the
     *        excluded policy statements. The collection may be either a
     *        homogenous or heterogenous collection.
     * @throws SecurityException if called by an AccessControlContext that has
     *         not been granted the "setPolicy" SecurityPermission.
     * @throws UnsupportedOperationException if the state of the policy context
     *         whose interface is this PolicyConfiguration Object is "deleted"
     *         or "inService" when this method is called.
     * @throws PolicyContextException if the implementation throws a checked
     *         exception that has not been accounted for by the
     *         addToExcludedPolicy method signature. The exception thrown by the
     *         implementation class will be encapsulated (during construction)
     *         in the thrown PolicyContextException.
     */
    public void addToExcludedPolicy(final PermissionCollection permissions) throws PolicyContextException, SecurityException,
            UnsupportedOperationException {

        logger.debug("Adding permissions ''{0}'' as excluded policy.", permissions);

        // Section 3.3 - Check permissions
        checkSetPolicy();

        // Open state required
        checkCurrentStateIsInState(State.OPEN);

        // Add permissions
        if (permissions != null) {
            for (Enumeration e = permissions.elements(); e.hasMoreElements();) {
                excludedPermissions.add((Permission) e.nextElement());
            }
        }

    }

    /**
     * Used to add a single permission to a named role in this
     * PolicyConfiguration.
     * @param roleName the name of the Role to which the permission is to be
     *        added.
     * @param permission the permission to be added to the role.
     * @throws SecurityException if called by an AccessControlContext that has
     *         not been granted the "setPolicy" SecurityPermission.
     * @throws UnsupportedOperationException if the state of the policy context
     *         whose interface is this PolicyConfiguration Object is "deleted"
     *         or "inService" when this method is called.
     * @throws PolicyContextException - if the implementation throws a checked
     *         exception that has not been accounted for by the addToRole method
     *         signature. The exception thrown by the implementation class will
     *         be encapsulated (during construction) in the thrown
     *         PolicyContextException.
     */
    public void addToRole(final String roleName, final Permission permission) throws PolicyContextException, SecurityException,
            UnsupportedOperationException {

        logger.debug("Adding permission ''{0}'' to role ''{1}''.", permission, roleName);

        // Section 3.3 - Check permissions
        checkSetPolicy();

        // Open state required
        checkCurrentStateIsInState(State.OPEN);

        // Fail if roleName is null
        if (roleName == null) {
            throw new PolicyContextException(logger.getI18n().getMessage("JPolicyConfiguration.addToRole"));
        }

        // Break if permission is null
        if (permission == null) {
            return;
        }
        PermissionCollection permissionsOfRole = rolePermissions.get(roleName);

        // create permission object if no previous permission for the given role
        if (permissionsOfRole == null) {
            permissionsOfRole = new Permissions();
        }
        permissionsOfRole.add(permission);

        // add to the list
        rolePermissions.put(roleName, permissionsOfRole);

    }

    /**
     * Used to add permissions to a named role in this PolicyConfiguration.
     * @param roleName the name of the Role to which the permissions are to be
     *        added.
     * @param permissions the collection of permissions to be added to the role.
     *        The collection may be either a homogenous or heterogenous
     *        collection.
     * @throws SecurityException if called by an AccessControlContext that has
     *         not been granted the "setPolicy" SecurityPermission.
     * @throws UnsupportedOperationException if the state of the policy context
     *         whose interface is this PolicyConfiguration Object is "deleted"
     *         or inService" when this method is called.
     * @throws PolicyContextException - if the implementation throws a checked
     *         exception that has not been accounted for by the addToRole method
     *         signature. The exception thrown by the implementation class will
     *         be encapsulated (during construction) in the thrown
     *         PolicyContextException.
     */
    public void addToRole(final String roleName, final PermissionCollection permissions) throws PolicyContextException,
            SecurityException, UnsupportedOperationException {

        logger.debug("Adding permissions ''{0}'' to role ''{1}''.", permissions, roleName);

        // Section 3.3 - Check permissions
        checkSetPolicy();

        // Open state required
        checkCurrentStateIsInState(State.OPEN);

        // Fail if roleName is null
        if (roleName == null) {
            throw new PolicyContextException(logger.getI18n().getMessage("JPolicyConfiguration.addToRole"));
        }

        // Break if permission is null
        if (permissions == null) {
            return;
        }
        PermissionCollection permissionsOfRole = rolePermissions.get(roleName);

        // create permission object if no previous permission for the given role
        if (permissionsOfRole == null) {
            permissionsOfRole = new Permissions();
        }

        for (Enumeration e = permissions.elements(); e.hasMoreElements();) {
            permissionsOfRole.add((Permission) e.nextElement());
        }

        // add to the list
        rolePermissions.put(roleName, permissionsOfRole);

    }

    /**
     * Used to add a single unchecked policy statement to this
     * PolicyConfiguration.
     * @param permission the permission to be added to the unchecked policy
     *        statements.
     * @throws SecurityException if called by an AccessControlContext that has
     *         not been granted the "setPolicy" SecurityPermission.
     * @throws UnsupportedOperationException if the state of the policy context
     *         whose interface is this PolicyConfiguration Object is "deleted"
     *         or "inService" when this method is called.
     * @throws PolicyContextException if the implementation throws a checked
     *         exception that has not been accounted for by the
     *         addToUncheckedPolicy method signature. The exception thrown by
     *         the implementation class will be encapsulated (during
     *         construction) in the thrown PolicyContextException.
     */
    public void addToUncheckedPolicy(final Permission permission) throws PolicyContextException, SecurityException,
            UnsupportedOperationException {

        logger.debug("Adding permission ''{0}'' as unchecked policy.", permission);

        // Section 3.3 - Check permissions
        checkSetPolicy();

        // Open state required
        checkCurrentStateIsInState(State.OPEN);

        // Add permission
        if (permission != null) {
            uncheckedPermissions.add(permission);
        }

    }

    /**
     * Used to add unchecked policy statements to this PolicyConfiguration.
     * @param permissions the collection of permissions to be added as unchecked
     *        policy statements. The collection may be either a homogenous or
     *        heterogenous collection.
     * @throws SecurityException if called by an AccessControlContext that has
     *         not been granted the "setPolicy" SecurityPermission.
     * @throws UnsupportedOperationException if the state of the policy context
     *         whose interface is this PolicyConfiguration Object is "deleted"
     *         or "inService" when this method is called.
     * @throws PolicyContextException if the implementation throws a checked
     *         exception that has not been accounted for by the
     *         addToUncheckedPolicy method signature. The exception thrown by
     *         the implementation class will be encapsulated (during
     *         construction) in the thrown PolicyContextException.
     */
    public void addToUncheckedPolicy(final PermissionCollection permissions) throws PolicyContextException, SecurityException,
            UnsupportedOperationException {

        logger.debug("Adding permissions ''{0}'' as unchecked policy.", permissions);

        // Section 3.3 - Check permissions
        checkSetPolicy();

        // Open state required
        checkCurrentStateIsInState(State.OPEN);

        // Add permissions
        if (permissions != null) {
            for (Enumeration e = permissions.elements(); e.hasMoreElements();) {
                uncheckedPermissions.add((Permission) e.nextElement());
            }
        }

    }

    /**
     * This method is used to set to "inService" the state of the policy context
     * whose interface is this PolicyConfiguration Object. Only those policy
     * contexts whose state is "inService" will be included in the policy
     * contexts processed by the Policy.refresh method. A policy context whose
     * state is "inService" may be returned to the "open" state by calling the
     * getPolicyConfiguration method of the PolicyConfiguration factory with the
     * policy context identifier of the policy context. When the state of a
     * policy context is "inService", calling any method other than commit,
     * delete, getContextID, or inService on its PolicyConfiguration Object will
     * cause an UnsupportedOperationException to be thrown.
     * @throws SecurityException if called by an AccessControlContext that has
     *         not been granted the "setPolicy" SecurityPermission.
     * @throws UnsupportedOperationException if the state of the policy context
     *         whose interface is this PolicyConfiguration Object is "deleted"
     *         when this method is called.
     * @throws PolicyContextException if the implementation throws a checked
     *         exception that has not been accounted for by the commit method
     *         signature. The exception thrown by the implementation class will
     *         be encapsulated (during construction) in the thrown
     *         PolicyContextException.
     */
    public void commit() throws PolicyContextException, SecurityException, UnsupportedOperationException {

        // Section 3.3 - Check permissions
        checkSetPolicy();

        // Deleted state refused
        checkCurrentStateNotInState(State.DELETED);

        // Now state is in service
        state = State.IN_SERVICE;

        // add the configuration of this object
        JPolicyConfigurationKeeper.addConfiguration(this);
    }

    /**
     * Causes all policy statements to be deleted from this PolicyConfiguration
     * and sets its internal state such that calling any method, other than
     * delete, getContextID, or inService on the PolicyConfiguration will be
     * rejected and cause an UnsupportedOperationException to be thrown. This
     * operation has no affect on any linked PolicyConfigurations other than
     * removing any links involving the deleted PolicyConfiguration.
     * @throws SecurityException if called by an AccessControlContext that has
     *         not been granted the "setPolicy" SecurityPermission.
     * @throws PolicyContextException if the implementation throws a checked
     *         exception that has not been accounted for by the delete method
     *         signature. The exception thrown by the implementation class will
     *         be encapsulated (during construction) in the thrown
     *         PolicyContextException.
     */
    public void delete() throws PolicyContextException, SecurityException {

        // Section 3.3 - Check permissions
        checkSetPolicy();

        // all policy statements are deleted
        excludedPermissions = new Permissions();
        uncheckedPermissions = new Permissions();
        rolePermissions = new HashMap<String, PermissionCollection>();

        // change state to DELETED
        state = State.DELETED;

        // remove the configuration of this object
        JPolicyConfigurationKeeper.removeConfiguration(this);

    }

    /**
     * This method returns this object's policy context identifier.
     * @return this object's policy context identifier.
     * @throws SecurityException if called by an AccessControlContext that has
     *         not been granted the "setPolicy" SecurityPermission.
     * @throws PolicyContextException if the implementation throws a checked
     *         exception that has not been accounted for by the getContextID
     *         method signature. The exception thrown by the implementation
     *         class will be encapsulated (during construction) in the thrown
     *         PolicyContextException.
     */
    public String getContextID() throws PolicyContextException, SecurityException {

        // Section 3.3 - Check permissions
        checkSetPolicy();

        return contextID;
    }

    /**
     * This method is used to determine if the policy context whose interface is
     * this PolicyConfiguration Object is in the "inService" state.
     * @return true if the state of the associated policy context is
     *         "inService"; false otherwise.
     * @throws SecurityException if called by an AccessControlContext that has
     *         not been granted the "setPolicy" SecurityPermission.
     * @throws PolicyContextException if the implementation throws a checked
     *         exception that has not been accounted for by the inService method
     *         signature. The exception thrown by the implementation class will
     *         be encapsulated (during construction) in the thrown
     *         PolicyContextException.
     */
    public boolean inService() throws PolicyContextException, SecurityException {

        // Section 3.3 - Check permissions
        checkSetPolicy();

        return (state == State.IN_SERVICE);
    }

    /**
     * Creates a relationship between this configuration and another such that
     * they share the same principal-to-role mappings. PolicyConfigurations are
     * linked to apply a common principal-to-role mapping to multiple seperately
     * manageable PolicyConfigurations, as is required when an application is
     * composed of multiple modules. Note that the policy statements which
     * comprise a role, or comprise the excluded or unchecked policy collections
     * in a PolicyConfiguration are unaffected by the configuration being linked
     * to another.
     * @param link a reference to a different PolicyConfiguration than this
     *        PolicyConfiguration. The relationship formed by this method is
     *        symetric, transitive and idempotent. If the argument
     *        PolicyConfiguration does not have a different Policy context
     *        identifier than this PolicyConfiguration no relationship is
     *        formed, and an exception, as described below, is thrown.
     * @throws SecurityException if called by an AccessControlContext that has
     *         not been granted the "setPolicy" SecurityPermission.
     * @throws UnsupportedOperationException if the state of the policy context
     *         whose interface is this PolicyConfiguration Object is "deleted"
     *         or "inService" when this method is called.
     * @throws IllegalArgumentException if called with an argument
     *         PolicyConfiguration whose Policy context is equivalent to that of
     *         this PolicyConfiguration.
     * @throws PolicyContextException if the implementation throws a checked
     *         exception that has not been accounted for by the
     *         linkConfiguration method signature. The exception thrown by the
     *         implementation class will be encapsulated (during construction)
     *         in the thrown PolicyContextException.
     */
    public void linkConfiguration(final PolicyConfiguration link) throws IllegalArgumentException, PolicyContextException,
            SecurityException, UnsupportedOperationException {

        // Section 3.3 - Check permissions
        checkSetPolicy();

        // Open state required
        checkCurrentStateIsInState(State.OPEN);

        // Equivalent to this PolicyConfiguration object ?
        if (this.equals(link)) {
            throw new IllegalArgumentException(logger.getI18n().getMessage("JPolicyConfiguration.linkConfiguration.equivalent",
                    this, link));
        }

        // TODO : link objects together.

    }

    /**
     * Used to remove any excluded policy statements from this
     * PolicyConfiguration.
     * @throws SecurityException if called by an AccessControlContext that has
     *         not been granted the "setPolicy" SecurityPermission.
     * @throws UnsupportedOperationException if the state of the policy context
     *         whose interface is this PolicyConfiguration Object is "deleted"
     *         or "inService" when this method is called.
     * @throws PolicyContextException if the implementation throws a checked
     *         exception that has not been accounted for by the
     *         removeExcludedPolicy method signature. The exception thrown by
     *         the implementation class will be encapsulated (during
     *         construction) in the thrown PolicyContextException.
     */
    public void removeExcludedPolicy() throws PolicyContextException, SecurityException, UnsupportedOperationException {

        // Section 3.3 - Check permissions
        checkSetPolicy();

        // Open state required
        checkCurrentStateIsInState(State.OPEN);

        // reinit
        excludedPermissions = new Permissions();
    }

    /**
     * Used to remove a role and all its permissions from this
     * PolicyConfiguration.
     * @param roleName the name of the Role to remove from this
     *        PolicyConfiguration.
     * @throws SecurityException if called by an AccessControlContext that has
     *         not been granted the "setPolicy" SecurityPermission.
     * @throws UnsupportedOperationException if the state of the policy context
     *         whose interface is this PolicyConfiguration Object is "deleted"
     *         or "inService" when this method is called.
     * @throws PolicyContextException if the implementation throws a checked
     *         exception that has not been accounted for by the removeRole
     *         method signature. The exception thrown by the implementation
     *         class will be encapsulated (during construction) in the thrown
     *         PolicyContextException.
     */
    public void removeRole(final String roleName)
        throws PolicyContextException, SecurityException, UnsupportedOperationException {

        // Section 3.3 - Check permissions
        checkSetPolicy();

        // Open state required
        checkCurrentStateIsInState(State.OPEN);

        // Remove role permissions
        rolePermissions.remove(roleName);
    }

    /**
     * Used to remove any unchecked policy statements from this
     * PolicyConfiguration.
     * @throws SecurityException if called by an AccessControlContext that has
     *         not been granted the "setPolicy" SecurityPermission.
     * @throws UnsupportedOperationException if the state of the policy context
     *         whose interface is this PolicyConfiguration Object is "deleted"
     *         or "inService" when this method is called.
     * @throws PolicyContextException if the implementation throws a checked
     *         exception that has not been accounted for by the
     *         removeUncheckedPolicy method signature. The exception thrown by
     *         the implementation class will be encapsulated (during
     *         construction) in the thrown PolicyContextException.
     */
    public void removeUncheckedPolicy() throws PolicyContextException, SecurityException, UnsupportedOperationException {

        // Section 3.3 - Check permissions
        checkSetPolicy();

        // Open state required
        checkCurrentStateIsInState(State.OPEN);

        // Remove unckecked policy
        uncheckedPermissions = new Permissions();
    }

    /**
     * Check if the current state is not the given state. Authorized states are
     * described in javadoc of PolicyConfiguration class
     * @param s given state
     * @throws UnsupportedOperationException if the state is not the given state
     */
    private void checkCurrentStateNotInState(final State s) throws UnsupportedOperationException {
        if (this.state == s) {
            String err = logger.getI18n().getMessage("JPolicyConfiguration.checkCurrentStateNotInState.notValidState", s, state);
            throw new UnsupportedOperationException(err);
        }
    }

    /**
     * Check if the current state is in the given state. Authorized states are
     * described in javadoc of PolicyConfiguration class
     * @param s given state
     * @throws UnsupportedOperationException if the state is not in a valid
     *         state
     */
    private void checkCurrentStateIsInState(final State s) throws UnsupportedOperationException {
        if (this.state != s) {
            String err = logger.getI18n().getMessage("JPolicyConfiguration.checkCurrentStateNotInState.notValidState", state, s);
            throw new UnsupportedOperationException(err);
        }
    }

    /**
     * Method which check setPolicy access. Section 3.3 : all public methods
     * must throw a SecurityException when called by an AccessControlContext
     * that has not been granted the "setPolicy" SecurityPermission
     * @throws SecurityException when called by an AccessControlContext that has
     *         not been granted the "setPolicy" SecurityPermission.
     */
    private void checkSetPolicy() throws SecurityException {
        SecurityManager securityManager = System.getSecurityManager();
        if (securityManager != null) {
            securityManager.checkPermission(new SecurityPermission("setPolicy"));
        }
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     * @param obj the reference object with which to compare.
     * @return true if this object is the same as the obj argument; false
     *         otherwise.
     */
    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof PolicyConfiguration)) {
            logger.error("JPolicyConfiguration.equals.notInstanceOf");
            return false;
        }
        // Compare
        try {
            return (this.contextID.equals(((PolicyConfiguration) obj).getContextID()));
        } catch (PolicyContextException pce) {
            logger.error("JPolicyConfiguration.equals.canNotCheck", pce);
            return false;
        }

    }

    /**
     * Gets a hash code value for the object.
     * @return a hash code value for this object.
     */
    @Override
    public int hashCode() {
        return contextID.hashCode();
    }

    /**
     * Reset to OPEN state (Used by PolicyConfigurationFactory).
     */
    protected void resetState() {
        this.state = State.OPEN;
    }

    /**
     * Gets the excluded permission.
     * @return the excluded permission
     */
    public PermissionCollection getExcludedPermissions() {
        // Works only if state is in service
        if (state != State.IN_SERVICE) {
            return new Permissions();
        }
        return excludedPermissions;
    }

    /**
     * Gets the excluded permission.
     * @return the excluded permission
     */
    public PermissionCollection getUncheckedPermissions() {
        // Works only if state is in service
        if (state != State.IN_SERVICE) {
            return new Permissions();
        }
        return uncheckedPermissions;
    }

    /**
     * Gets the permissions for a given principal.
     * @param principal given principal
     * @return the permissions for a given principal
     */
    public PermissionCollection getPermissionsForPrincipal(final Principal principal) {

        logger.debug("principal = ''{0}''", principal);

        // Works only if state is in service and if principal is not null
        if (principal == null || state != State.IN_SERVICE) {
            return new Permissions();
        }

        PermissionCollection permissionsOfRole = rolePermissions.get(principal.getName());

        logger.debug("Permissions found = ''{0}''", permissionsOfRole);

        // create empty permission object if no previous permission for the
        // given role
        if (permissionsOfRole == null) {
            permissionsOfRole = new Permissions();
        }

        return permissionsOfRole;
    }

}
