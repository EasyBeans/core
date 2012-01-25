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
 * $Id: JPolicyConfigurationFactory.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.security.jacc.provider;

import java.security.SecurityPermission;
import java.util.HashMap;
import java.util.Map;

import javax.security.jacc.PolicyConfiguration;
import javax.security.jacc.PolicyConfigurationFactory;
import javax.security.jacc.PolicyContextException;

import org.ow2.util.log.Log;
import org.ow2.util.log.LogFactory;

/**
 * Defines the PolicyConfigurationFactory implementation class of JACC.
 * @author Florent Benoit
 */
public class JPolicyConfigurationFactory extends PolicyConfigurationFactory {

    /**
     * List of PolicyConfiguration objects. Manage all configurations available
     */
    private Map<String, PolicyConfiguration> policyConfigurations = null;

    /**
     * Logger.
     */
    private static Log logger = LogFactory.getLog(JPolicyConfigurationFactory.class);

    /**
     * Constructor.
     */
    public JPolicyConfigurationFactory() {
        policyConfigurations = new HashMap<String, PolicyConfiguration>();

    }

    /**
     * This method is used to obtain an instance of the provider specific class
     * that implements the PolicyConfiguration interface that corresponds to the
     * identified policy context within the provider.
     * @param contextID A String identifying the policy context whose
     *        PolicyConfiguration interface is to be returned. The value passed
     *        to this parameter must not be null.
     * @param remove A boolean value that establishes whether or not the policy
     *        statements of an existing policy context are to be removed before
     *        its PolicyConfiguration object is returned. If the value passed to
     *        this parameter is true, the policy statements of an existing
     *        policy context will be removed. If the value is false, they will
     *        not be removed.
     * @return an Object that implements the PolicyConfiguration Interface
     *         matched to the Policy provider and corresponding to the
     *         identified policy context.
     * @throws SecurityException when called by an AccessControlContext that has
     *         not been granted the "setPolicy" SecurityPermission.
     * @throws PolicyContextException if the implementation throws a checked
     *         exception that has not been accounted for by the
     *         getPolicyConfiguration method signature. The exception thrown by
     *         the implementation class will be encapsulated (during
     *         construction) in the thrown PolicyContextException.
     */
    @Override
    public PolicyConfiguration getPolicyConfiguration(final String contextID, final boolean remove)
            throws PolicyContextException, SecurityException {

        // Section 3.3 - Check permissions
        checkSetPolicy();

        // Get in cache
        PolicyConfiguration policyConfiguration = getInternalPolicyConfiguration(contextID);

        // Is there an existing configuration ?
        if (policyConfiguration != null) {
            // Need to be removed ?
            if (remove) {
                // Delete permissions
                policyConfiguration.delete();
                ((JPolicyConfiguration) policyConfiguration).resetState();
            }
            // return cache
            return policyConfiguration;
        }

        // No previous PolicyConfiguration for the specific contextID
        // need to build a new PolicyConfiguration
        policyConfiguration = new JPolicyConfiguration(contextID);

        // Add in cache for future use and return it.
        policyConfigurations.put(contextID, policyConfiguration);

        return policyConfiguration;

    }

    /**
     * This method is used to check if there the PolicyConfiguration is in cache
     * and return it if it is in the cache.
     * @param contextID A String identifying the policy context whose
     *        PolicyConfiguration interface is to be returned. The value passed
     *        to this parameter must not be null.
     * @return an Object that implements the PolicyConfiguration Interface
     *         matched to the Policy provider and corresponding to the
     *         identified policy context.
     */
    private synchronized PolicyConfiguration getInternalPolicyConfiguration(final String contextID) {
        // Get in cache
        return policyConfigurations.get(contextID);
    }

    /**
     * This method determines if the identified policy context exists with state
     * "inService" in the Policy provider associated with the factory.
     * @param contextID A string identifying a policy context
     * @return true if the identified policy context exists within the provider
     *         and its state is "inService", false otherwise.
     * @throws SecurityException when called by an AccessControlContext that has
     *         not been granted the "setPolicy" SecurityPermission.
     * @throws PolicyContextException if the implementation throws a checked
     *         exception that has not been accounted for by the inService method
     *         signature. The exception thrown by the implementation class will
     *         be encapsulated (during construction) in the thrown
     *         PolicyContextException.
     */
    @Override
    public boolean inService(final String contextID) throws PolicyContextException, SecurityException {

        // Section 3.3 - Check permissions
        logger.debug("Check setpolicy...");
        checkSetPolicy();

        // Context exists ?
        if (policyConfigurations.containsKey(contextID)) {
            logger.debug("Existing config for contextID ''{0}'', gets internal config...", contextID);
            return getInternalPolicyConfiguration(contextID).inService();
        }
        // false otherwise (see javaDoc)
        logger.debug("Config for contextID ''{0}'' not found, return false", contextID);
        return false;
    }

    /**
     * Method which check setPolicy access Section 3.3.<br/>
     * getPolicyConfiguration and inService must throw a SecurityException when
     * called by an AccessControlContext that has not been granted the
     * "setPolicy" SecurityPermission
     * @throws SecurityException when called by an AccessControlContext that has
     *         not been granted the "setPolicy" SecurityPermission.
     */
    private void checkSetPolicy() throws SecurityException {
        SecurityManager securityManager = System.getSecurityManager();
        if (securityManager != null) {
            securityManager.checkPermission(new SecurityPermission("setPolicy"));
        }
    }

}
