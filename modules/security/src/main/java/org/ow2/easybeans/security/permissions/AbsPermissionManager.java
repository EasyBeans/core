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
 * $Id: AbsPermissionManager.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.security.permissions;

import java.net.URL;
import java.security.Policy;

import javax.security.jacc.PolicyConfiguration;
import javax.security.jacc.PolicyConfigurationFactory;
import javax.security.jacc.PolicyContextException;

import org.ow2.easybeans.api.PermissionManagerException;

/**
 * Manages the permission for EasyBeans EJB3 container.
 * @author Florent Benoit
 */
public abstract class AbsPermissionManager {

    /**
     * JACC Policy configuration.
     */
    private PolicyConfiguration policyConfiguration = null;

    /**
     * Context ID (URL).
     */
    private URL contextIdURL = null;

    /**
     * Context ID.
     */
    private String contextId = null;

    /**
     * Policy to use.
     */
    private static Policy policy = null;

    /**
     * Default Constructor.
     * @param contextIdURL context ID URL used for PolicyContext
     * @throws PermissionManagerException if permissions can't be set
     */
    public AbsPermissionManager(final URL contextIdURL) throws PermissionManagerException {
        this(contextIdURL, true);
    }

    /**
     * Default Constructor.
     * @param contextIdURL context ID URL used for PolicyContext
     * @param remove - if true, the policy configuration will be removed.
     * @throws PermissionManagerException if permissions can't be set
     */
    public AbsPermissionManager(final URL contextIdURL, final boolean remove) throws PermissionManagerException {
        this.contextIdURL = contextIdURL;
        this.contextId = contextIdURL.toString();

        PolicyConfigurationFactory policyConfigurationFactory = null;
        // Init JACC
        try {
            policyConfigurationFactory = PolicyConfigurationFactory.getPolicyConfigurationFactory();
        } catch (ClassNotFoundException e) {
            throw new PermissionManagerException("Error when trying to get the PolicyConfigurationFactory object", e);
        } catch (PolicyContextException e) {
            throw new PermissionManagerException("Error when trying to get the PolicyConfigurationFactory object", e);
        }
        try {
            this.policyConfiguration = policyConfigurationFactory.getPolicyConfiguration(this.contextId, remove);
        } catch (PolicyContextException pce) {
            throw new PermissionManagerException("Error when trying to get the PolicyConfiguration object with contextId '"
                    + this.contextId + "'.'", pce);
        }

        // Policy to use
        setPolicy(Policy.getPolicy());
    }

    /**
     * Sets the policy.
     * @param policy the given policy
     */
    private static void setPolicy(final Policy policy) {
        AbsPermissionManager.policy = policy;
    }

    /**
     * Delete this object.
     * @throws PermissionManagerException if the configuration can't be deleted
     */
    public void delete() throws PermissionManagerException {

        try {
            this.policyConfiguration.delete();
        } catch (PolicyContextException pce) {
            throw new PermissionManagerException("Cannot delete policyConfiguration object", pce);
        }
        this.policyConfiguration = null;

        // Policy need to be refresh
        policy.refresh();
    }

    /**
     * Commit the Policy Configuration.
     * @throws PermissionManagerException if commit can't be done
     */
    public void commit() throws PermissionManagerException {
        try {
            this.policyConfiguration.commit();
            policy.refresh();
        } catch (PolicyContextException pce) {
            throw new PermissionManagerException("Cannot commit configuration", pce);
        }
    }

    /**
     * @return Returns the policy.
     */
    protected static Policy getPolicy() {
        return policy;
    }

    /**
     * @return Returns the contextId.
     */
    protected String getContextId() {
        return this.contextId;
    }

    /**
     * @param contextId The contextId to set.
     */
    protected void setContextId(final String contextId) {
        this.contextId = contextId;
    }

    /**
     * @return Returns the policyConfiguration.
     */
    protected PolicyConfiguration getPolicyConfiguration() {
        return this.policyConfiguration;
    }

    /**
     * @param policyConfiguration The policyConfiguration to set.
     */
    protected void setPolicyConfiguration(final PolicyConfiguration policyConfiguration) {
        this.policyConfiguration = policyConfiguration;
    }

    /**
     * @return Returns the contextId URL.
     */
    protected URL getContextIdURL() {
        return this.contextIdURL;
    }
}
