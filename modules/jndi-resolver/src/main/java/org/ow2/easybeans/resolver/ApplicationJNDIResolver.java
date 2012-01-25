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
 * $Id: ApplicationJNDIResolver.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.resolver;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ow2.easybeans.resolver.api.EZBApplicationJNDIResolver;
import org.ow2.easybeans.resolver.api.EZBContainerJNDIResolver;
import org.ow2.easybeans.resolver.api.EZBJNDIBeanData;
import org.ow2.easybeans.resolver.api.EZBJNDIData;

/**
 * Manage JNDI names for all EJBs of an application.
 * @author Florent Benoit
 */
public class ApplicationJNDIResolver extends CommonJNDIResolver implements EZBApplicationJNDIResolver {

    /**
     * URL of this resolver.
     */
    //private URL earURL = null;


    /**
     * Map beween URL of a container and the container resolver.
     */
    private Map<URL, EZBContainerJNDIResolver> containersMap;

    /**
     * Default constructor.
     */
    public ApplicationJNDIResolver() {
        this.containersMap = new HashMap<URL, EZBContainerJNDIResolver>();
    }


    /**
     * Add a child container JNDI Resolver.
     * @param containerJNDIResolver the child resolver to add
     */
    public void addContainerJNDIResolver(final EZBContainerJNDIResolver containerJNDIResolver) {
        this.containersMap.put(containerJNDIResolver.getURL(), containerJNDIResolver);
    }


    /**
     * Allows to find EJB JNDI name.
     * @return a list of matching JNDI objects for the given interface and bean
     *         name.
     * @param interfaceName the name of the interface that EJBs are
     *        implementing.
     */
    public List<EZBJNDIBeanData> getEJBJNDINames(final String interfaceName) {
        // Return a list with all the beans available for this interface
        return getEJBJNDINames(interfaceName, null);
    }

    /**
     * Allows to find EJB JNDI name.
     * @return a list of matching JNDI objects for the given interface and bean
     *         name.
     * @param interfaceName the name of the interface that EJBs are
     *        implementing.
     * @param beanName the name of the bean on which we need to find JNDI name.
     */
    public List<EZBJNDIBeanData> getEJBJNDINames(final String interfaceName, final String beanName) {
        // Ask also EAR if present
        return getEJBJNDINames(interfaceName, beanName, null);
    }

    /**
     * Allows to find EJB JNDI name.
     * @return a list of matching JNDI objects for the given interface.
     * @param interfaceName the name of the interface that EJBs are
     *        implementing.
     * @param beanName the name of the bean on which we need to find JNDI name.
     * @param ejbLinkURL the optional URL for the container that should include
     *        the bean name.
     */
    public List<EZBJNDIBeanData> getEJBJNDINames(final String interfaceName, final String beanName, final URL ejbLinkURL) {

        // Get the container for the given URL if present
        if (ejbLinkURL != null) {
            EZBContainerJNDIResolver containerJNDIResolver = this.containersMap.get(ejbLinkURL);

            // found one ?
            if (containerJNDIResolver != null) {
                return containerJNDIResolver.getEJBJNDINames(interfaceName, beanName, false);
            }

            // No container for this URL
            return Collections.emptyList();
        }

        // Need to ask all containers as we don't know the right URL
        Collection<EZBContainerJNDIResolver> containerJNDIResolvers = this.containersMap.values();

        // No containers, do nothing
        if (containerJNDIResolvers == null) {
            return Collections.emptyList();
        }

        // Build list that will be returned
        List<EZBJNDIBeanData> beanDataList = new ArrayList<EZBJNDIBeanData>();

        // Ask all containers and add the return value
        for (EZBContainerJNDIResolver containerJNDIResolver : containerJNDIResolvers) {
            // Ask without asking ourself again else it ends it recursive loop
            beanDataList.addAll(containerJNDIResolver.getEJBJNDINames(interfaceName, beanName, false));
        }

        // Return value
        return beanDataList;
    }

    /**
     * Allows to find Message Destination JNDI name.
     * @return a list of matching JNDI objects for the given message destination name.
     * @param messageDestinationName the name of the message destination.
     */
    public List<EZBJNDIData> getMessageDestinationJNDINames(final String messageDestinationName) {
        // Need to ask all containers
        Collection<EZBContainerJNDIResolver> containerJNDIResolvers = this.containersMap.values();

        // No containers, do nothing
        if (containerJNDIResolvers == null) {
            return Collections.emptyList();
        }

        // Build list that will be returned
        List<EZBJNDIData> dataList = new ArrayList<EZBJNDIData>();

        // Ask all containers and add the return value
        for (EZBContainerJNDIResolver containerJNDIResolver : containerJNDIResolvers) {
            // Ask without asking ourself again else it ends it recursive loop
            dataList.addAll(containerJNDIResolver.getMessageDestinationJNDINames(messageDestinationName, false));
        }

        // Return value
        return dataList;
    }

}
