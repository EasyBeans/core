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
 * $Id: CommonJNDIResolver.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.resolver;

import java.util.List;

import org.ow2.easybeans.resolver.api.EZBJNDIBeanData;
import org.ow2.easybeans.resolver.api.EZBJNDIData;
import org.ow2.easybeans.resolver.api.EZBJNDIResolver;
import org.ow2.easybeans.resolver.api.EZBJNDIResolverException;

/**
 * Defines common stuff for JNDI Resolver.
 * @author Florent Benoit
 */
public abstract class CommonJNDIResolver implements EZBJNDIResolver {

    /**
     * Gets jndi name for a given interface and a bean name.
     * @param itf the name of the interface.
     * @param beanName the name of the bean.
     * @return the jndi name
     * @throws EZBJNDIResolverException if not found
     */
    public String getEJBJNDIUniqueName(final String itf, final String beanName) throws EZBJNDIResolverException {
        List<EZBJNDIBeanData> dataList = getEJBJNDINames(itf, beanName);

        // Not found
        if (dataList == null || dataList.isEmpty()) {
            throw new EZBJNDIResolverException("No JNDI Name found for interface '" + itf + "' and bean name '" + beanName
                    + "'.");
        }

        // Too many entries !
        if (dataList.size() > 1) {
            throw new EZBJNDIResolverException("Found too many entries corresponding to the interface '" + itf
                    + "' and the bean name '" + beanName + "'. Found list = '" + dataList + "'.");
        }

        // Only one item, it's good
        return dataList.get(0).getName();
    }

    /**
     * Allows to find Message Destination JNDI name.
     * @return a list of matching JNDI objects for the given message destination name.
     * @param messageDestinationName the name of the message destination.
     * @throws EZBJNDIResolverException if not found
     */
    public String getMessageDestinationJNDIUniqueName(final String messageDestinationName) throws EZBJNDIResolverException {
        List<EZBJNDIData> dataList = getMessageDestinationJNDINames(messageDestinationName);

        // Not found
        if (dataList == null || dataList.isEmpty()) {
            throw new EZBJNDIResolverException("No JNDI Name found for message destination name '" + messageDestinationName
                    + "'.");
        }

        // Too many entries !
        if (dataList.size() > 1) {
            throw new EZBJNDIResolverException("Found too many entries corresponding  message destination name '"
                    + messageDestinationName + "'. Found list = '" + dataList + "'.");
        }

        // Only one item, it's good
        return dataList.get(0).getName();
    }
}
