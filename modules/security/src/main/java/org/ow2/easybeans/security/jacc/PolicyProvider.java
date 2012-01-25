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
 * $Id: PolicyProvider.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.security.jacc;

import java.security.Policy;

import org.ow2.easybeans.security.jacc.provider.JPolicy;
import org.ow2.easybeans.security.jacc.provider.JPolicyConfigurationFactory;
import org.ow2.util.log.Log;
import org.ow2.util.log.LogFactory;


/**
 * Helper class for initializing the JACC provider.
 * @author Florent Benoit
 */
public final class PolicyProvider {

    /**
     * JACC Policy Provider property.
     */
    private static final String JACC_POLICY_PROVIDER = "javax.security.jacc.policy.provider";

    /**
     * JACC Policy Configuration Factory Provider property.
     */
    private static final String JACC_POLICY_CONFIG_FACTORY_PROVIDER = "javax.security.jacc.PolicyConfigurationFactory.provider";


    /**
     * Only internal constructor, as it is an utility class.
     */
    private PolicyProvider() {

    }

    /**
     * Initial policy.
     */
    private static Policy initialPolicy = null;

    /**
     * Logger.
     */
    private static Log logger = LogFactory.getLog(PolicyProvider.class);

    /**
     * Init the JACC configuration.
     * Defines in JACC chapter 2
     * @throws SecurityException if JACC policy provider can not be set
     */
    public static void init() throws SecurityException {

        // Check if we have to use an existing policy provider
        // Section 2.7
        String javaPolicy = System.getProperty(JACC_POLICY_PROVIDER);

        if (javaPolicy != null) {
            try {
                java.security.Policy.setPolicy((java.security.Policy) Class.forName(javaPolicy).newInstance());
            } catch (ClassNotFoundException cnfe) {
                // problem with property value of classpath
                throw new SecurityException(cnfe.getMessage());
            } catch (IllegalAccessException iae) {
                // problem with policy class definition
                throw new SecurityException(iae.getMessage());
            } catch (InstantiationException ie) {
                // problem with policy instantiation
                throw new SecurityException(ie.getMessage());
            } catch (ClassCastException cce) {
                // Not instance of java.security.policy
                throw new SecurityException(cce.getMessage());
            }
            logger.info("Using policy provider ''{0}''", javaPolicy);
        } else {
            // keep the previous policy
            initialPolicy = Policy.getPolicy();

            // Sets the EasyBeans delegating policy provider
            logger.debug("Using EasyBeans policy provider ''{0}''.", JPolicy.class.getName());
            java.security.Policy.setPolicy(JPolicy.getInstance());
        }

        // Defines the EasyBeans JACC provider if no provider is already defined
        // Section 2.3
        String jaccFactoryProvider = System.getProperty("JACC_POLICY_CONFIG_FACTORY_PROVIDER");
        if (jaccFactoryProvider == null) {
            // Set JACC provider
            logger.debug("Using EasyBeans PolicyConfigurationFactory provider and EasyBeans Policy provider");
            System.setProperty(JACC_POLICY_CONFIG_FACTORY_PROVIDER, JPolicyConfigurationFactory.class.getName());
        } else {
            logger.info("Using factory ''{0}'' as PolicyConfigurationFactory provider.", jaccFactoryProvider);
        }

    }

    /**
     * Stop the JACC configuration.
     * @throws SecurityException if JACC policy provider cannot be stop
     */
    public static void stop() throws SecurityException {
        if (initialPolicy != null) {
            Policy.setPolicy(initialPolicy);
        }
    }


}
