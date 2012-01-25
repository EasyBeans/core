/**
 * EasyBeans
 * Copyright (C) 2008 Bull S.A.S.
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
 * $Id: ManagedServiceUtils.java 5371 2010-02-24 15:02:00Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.osgi.handler;

import java.util.Hashtable;

import org.osgi.framework.Constants;
import org.ow2.easybeans.api.Factory;

/**
 * Utilities for ManagedService registration.
 * @author Guillaume Sauthier
 */
public final class ManagedServiceUtils {

    /**
     * Default empty constructor for Utils class.
     */
    private ManagedServiceUtils() {}

    /**
     * Provides the initial properties associated with that service.
     * @param factory the Bean factory
     * @param interfaceName Bean's business interface
     * @return initial properties
     */
    public static Hashtable<String, String> getDefaults(final Factory<?, ?> factory,
                                                        final String interfaceName) {

        Hashtable<String, String> defaults = new Hashtable<String, String>();
        defaults.put(Constants.SERVICE_PID,
                     interfaceName + ":" + factory.getBeanInfo().getName());
        defaults.put("ejb.interface", interfaceName);
        defaults.put("ejb.name", factory.getBeanInfo().getName());
        defaults.put("ejb.classname", factory.getClassName());
        defaults.put("ejb.id", factory.getId());
        defaults.put("ejb.container.name", factory.getContainer().getName());

        return defaults;

    }

}
