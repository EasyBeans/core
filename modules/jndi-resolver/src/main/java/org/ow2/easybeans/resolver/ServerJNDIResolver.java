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
 * $Id: ServerJNDIResolver.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.resolver;

import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import org.ow2.easybeans.resolver.api.EZBContainerJNDIResolver;
import org.ow2.easybeans.resolver.api.EZBJNDIBeanData;
import org.ow2.easybeans.resolver.api.EZBJNDIData;
import org.ow2.easybeans.resolver.api.EZBServerJNDIResolver;
import org.ow2.util.log.Log;
import org.ow2.util.log.LogFactory;

/**
 * Allows to find JNDI Names by asking all containers.
 * @author Florent Benoit
 */
public class ServerJNDIResolver extends CommonJNDIResolver implements EZBServerJNDIResolver {

    /**
     * Logger.
     */
    private Log logger = LogFactory.getLog(ServerJNDIResolver.class);

    /**
     * List of available JNDI resolver of containers.
     */
    private static Map<URL, WeakReference<EZBContainerJNDIResolver>> containerResolvers
        = new WeakHashMap<URL, WeakReference<EZBContainerJNDIResolver>>();

    /**
     * Remove a container JNDI resolver.
     * @param resolver the given resolver to remove.
     */
    public synchronized void removeContainerResolver(final EZBContainerJNDIResolver resolver) {
        URL containerURL = resolver.getURL();
        WeakReference<EZBContainerJNDIResolver> weakRef = containerResolvers.get(containerURL);
        if (weakRef != null) {
            containerResolvers.remove(containerURL);
        } else {
             this.logger.warn("Cannot remove the given container resolver as it is not present.");
        }
    }

    /**
     * Add a new container JNDI resolver.
     * @param resolver the given resolver to add.
     */
    public synchronized void addContainerResolver(final EZBContainerJNDIResolver resolver) {
        // build reference (weak)
        WeakReference<EZBContainerJNDIResolver> weakRef = new WeakReference<EZBContainerJNDIResolver>(resolver);

        // add reference
        containerResolvers.put(resolver.getURL(), weakRef);
    }

    /**
     * Allows to find EJB JNDI name.
     * @return a list of matching JNDI objects for the given interface and bean name.
     * @param interfaceName the name of the interface that EJBs are implementing.
     */
    public synchronized List<EZBJNDIBeanData> getEJBJNDINames(final String interfaceName) {
        return  getEJBJNDINames(interfaceName, null);
    }

    /**
     * Allows to find EJB JNDI name.
     * @return a list of matching JNDI objects for the given interface and bean name.
     * @param interfaceName the name of the interface that EJBs are implementing.
     * @param beanName the name of the bean on which we need to find JNDI name.
     */
    public synchronized List<EZBJNDIBeanData> getEJBJNDINames(final String interfaceName, final String beanName) {
        // Get iterator on the resolvers
        Iterator<URL> itContainerURL = containerResolvers.keySet().iterator();

        // Create list of results
        List<EZBJNDIBeanData> foundJNDINames = new ArrayList<EZBJNDIBeanData>();

        // For each URL
        while (itContainerURL.hasNext()) {
            URL url = itContainerURL.next();
            // Get the resolver
            WeakReference<EZBContainerJNDIResolver> weakRef = containerResolvers.get(url);

            if (weakRef != null) {
                // Get resolver
                EZBContainerJNDIResolver containerResolver = weakRef.get();

                // Now, ask resolver without asking its parent
                List<EZBJNDIBeanData> containerJNDINames = containerResolver.getEJBJNDINames(interfaceName, beanName, false);

                // Add the results
                foundJNDINames.addAll(containerJNDINames);
            }
        }

        // return result
        return foundJNDINames;
    }

    /**
     * Allows to find Message Destination JNDI name.
     * @return a list of matching JNDI objects for the given message destination name.
     * @param messageDestinationName the name of the message destination.
     */
    public List<EZBJNDIData> getMessageDestinationJNDINames(final String messageDestinationName) {

        // Get iterator on the resolvers
        Iterator<URL> itContainerURL = containerResolvers.keySet().iterator();

        // Create list of results
        List<EZBJNDIData> foundJNDINames = new ArrayList<EZBJNDIData>();

        // For each URL
        while (itContainerURL.hasNext()) {
            URL url = itContainerURL.next();
            // Get the resolver
            WeakReference<EZBContainerJNDIResolver> weakRef = containerResolvers.get(url);

            if (weakRef != null) {
                // Get resolver
                EZBContainerJNDIResolver containerResolver = weakRef.get();

                // Now, ask resolver without asking its parent
                List<EZBJNDIData> containerJNDINames = containerResolver.getMessageDestinationJNDINames(messageDestinationName,
                        false);

                // Add the results
                foundJNDINames.addAll(containerJNDINames);
            }
        }

        // return result
        return foundJNDINames;

    }

}
