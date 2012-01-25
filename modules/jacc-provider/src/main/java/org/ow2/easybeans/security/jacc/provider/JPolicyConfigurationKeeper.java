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
 * $Id: JPolicyConfigurationKeeper.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.security.jacc.provider;

import java.util.HashMap;
import java.util.Map;

import javax.security.jacc.PolicyContextException;

/**
 * This class keep the JPolicyConfiguration. This is used when
 * JPolicyConfigurationFactory is used as a delegate factory.
 * @author Florent Benoit
 */
public final class JPolicyConfigurationKeeper {

    /**
     * Internal list of JPolicyConfiguration objects.
     */
    private static Map<String, JPolicyConfiguration> policyConfigurations = new HashMap<String, JPolicyConfiguration>();

    /**
     * Utility class, no constructor.
     */
    private JPolicyConfigurationKeeper() {
    }

    /**
     * Gets the policy configuration map.
     * @return a policy configuration map.
     */
    private static Map<String, JPolicyConfiguration> getPolicyConfigurations() {
        if (policyConfigurations == null) {
            policyConfigurations = new HashMap<String, JPolicyConfiguration>();
        }
        return policyConfigurations;
    }

    /**
     * Add a EasyBeans policy configuration.
     * @param config policy configuration object to add
     */
    public static void addConfiguration(final JPolicyConfiguration config) {
        try {
            getPolicyConfigurations().put(config.getContextID(), config);
        } catch (PolicyContextException pce) {
            throw new RuntimeException("Cannot add the policy configuration object '" + config + "'");
        }
    }

    /**
     * Remove a EasyBeans policy configuration.
     * @param config policy configuration object to remove
     */
    public static void removeConfiguration(final JPolicyConfiguration config) {
        try {
            if (getPolicyConfigurations().containsKey(config.getContextID())) {
                getPolicyConfigurations().remove(config.getContextID());
            }
        } catch (PolicyContextException pce) {
            throw new RuntimeException("Cannot remove the policy configuration object '" + config + "'");
        }
    }

    /**
     * Gets the EasyBeans policy configuration by its contextId.
     * @param contextId given ID to retrieve policy configuration object
     * @return the EasyBeans policy configuration specified by its contextId
     */
    public static JPolicyConfiguration getConfiguration(final String contextId) {
        return getPolicyConfigurations().get(contextId);
    }
}
