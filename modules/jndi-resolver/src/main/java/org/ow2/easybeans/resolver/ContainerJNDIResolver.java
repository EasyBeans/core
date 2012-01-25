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
 * $Id: ContainerJNDIResolver.java 5369 2010-02-24 14:58:19Z benoitf $
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
import org.ow2.util.archive.api.ArchiveException;
import org.ow2.util.archive.api.IArchive;
import org.ow2.util.log.Log;
import org.ow2.util.log.LogFactory;

/**
 * JNDI resolver for a container. It can answer to any request about beans of a given container.
 * @author Florent Benoit
 */
public class ContainerJNDIResolver extends CommonJNDIResolver implements EZBContainerJNDIResolver {

    /**
     * Logger.
     */
    private static Log logger = LogFactory.getLog(ContainerJNDIResolver.class);

    /**
     * URL of this container resolver.
     */
    private URL containerURL = null;

    /**
     * Link to an Application JNDI Resolver.
     */
    private EZBApplicationJNDIResolver applicationJNDIResolver = null;

    /**
     * Map beween interface name and a map between bean name and the JNDI data.
     */
    private Map<String, Map<String, EZBJNDIBeanData>> interfacesMap;

    /**
     * Map beween message destination name and the JNDI data.
     */
    private Map<String, EZBJNDIData> messageDestinationMap;

    /**
     * Default constructor.
     * @param archive the archive of the given container.
     */
    public ContainerJNDIResolver(final IArchive archive) {
        try {
            this.containerURL = archive.getURL();
        } catch (ArchiveException e) {
            throw new IllegalArgumentException("Cannot get URL from archive '" + archive + "'.");
        }
        this.interfacesMap = new HashMap<String, Map<String, EZBJNDIBeanData>>();
        this.messageDestinationMap = new HashMap<String, EZBJNDIData>();
    }

    /**
     * @return URL used by this resolver.
     */
    public URL getURL() {
        return this.containerURL;
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
     * @return the application JNDI Resolver.
     */
    public EZBApplicationJNDIResolver getApplicationJNDIResolver() {
        return this.applicationJNDIResolver;
    }

    /**
     * Sets the application resolver.
     * @param applicationJNDIResolver the application resolver
     */
    public void setApplicationJNDIResolver(final EZBApplicationJNDIResolver applicationJNDIResolver) {
        this.applicationJNDIResolver = applicationJNDIResolver;
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
        return getEJBJNDINames(interfaceName, beanName, true);
    }

    /**
     * Allows to find EJB JNDI name.
     * @return a list of matching JNDI objects for the given interface and bean
     *         name.
     * @param interfaceName the name of the interface that EJBs are
     *        implementing.
     * @param beanName the name of the bean on which we need to find JNDI name.
     * @param askParent allow to disallow request to parent.
     */
    public List<EZBJNDIBeanData> getEJBJNDINames(final String interfaceName, final String beanName, final boolean askParent) {

        // Get available beans for the given interface name
        Map<String, EZBJNDIBeanData> beansMap = this.interfacesMap.get(interfaceName);

        // No beans for the given interface
        if (beansMap == null) {
            // Not found, check in the other containers of the EAR (if any)
            return getEJBJNDINameInEAR(interfaceName, beanName, askParent);
        }

        // No bean name, use all values
        if (beanName == null || "".equals(beanName)) {
            Collection<EZBJNDIBeanData> beanValues = beansMap.values();

            // Not found, check in the other containers of the EAR
            if (beanValues == null || beanValues.isEmpty()) {
                // Not found, check in the other containers of the EAR (if any)
                return getEJBJNDINameInEAR(interfaceName, beanName, askParent);
            }

            // Return a list with all the beans available for this interface
            return new ArrayList<EZBJNDIBeanData>(beanValues);
        }

        // Is that the bean name exists and there is an ejb-link in this name ?
        String newBeanname = beanName;
        if (beanName.indexOf("#") > 0) {
            // extract only the bean name
            newBeanname = beanName.split("#")[1];
            logger.debug("EJB-LINK not fully supported for interface '" + interfaceName
                    + "', and bean name '" + beanName + "' in container '" + this.containerURL + "'.");
        }

        // Build list for the return value
        List<EZBJNDIBeanData> beanDataList = new ArrayList<EZBJNDIBeanData>();

        // Bean name is here, use it
        EZBJNDIBeanData beanData = beansMap.get(newBeanname);

        // Found a value, add it
        if (beanData == null) {
            // Not found, check in the other containers of the EAR (if any)
            return getEJBJNDINameInEAR(interfaceName, newBeanname, askParent);
        }
        // Add the local instance found
        beanDataList.add(beanData);

        // Return value
        return beanDataList;
    }

    /**
     * Allows to find EJB JNDI name in the EAR if the container is packaged
     * within an EAR.
     * @return a list of matching JNDI objects for the given interface and bean
     *         name.
     * @param interfaceName the name of the interface that EJBs are
     *        implementing.
     * @param beanName the name of the bean on which we need to find JNDI name.
     * @param enabled allow to disallow request to parent.
     */
    protected List<EZBJNDIBeanData> getEJBJNDINameInEAR(final String interfaceName,
            final String beanName, final boolean enabled) {
        // If container is not inside an EAR or that recursion is turned off
        // stop here
        if (!enabled || this.applicationJNDIResolver == null) {
            return Collections.emptyList();
        }

        // Ask the resolver of the EAR
        return this.applicationJNDIResolver.getEJBJNDINames(interfaceName, beanName);
    }


    /**
     * Adds a new JNDI name for the given interface name / bean name.
     * @param interfaceName the given interface
     * @param jndiData data for the JNDI Name entry
     */
    public void addEJBJNDIName(final String interfaceName, final EZBJNDIBeanData jndiData) {
        // Get beans map
        Map<String, EZBJNDIBeanData> beansMap = this.interfacesMap.get(interfaceName);

        // Null ? create a new one
        if (beansMap == null) {
            beansMap = new HashMap<String, EZBJNDIBeanData>();
            this.interfacesMap.put(interfaceName, beansMap);
        }

        String beanName = jndiData.getBeanName();

        // Existing info about the bean ?
        EZBJNDIData existingData = beansMap.get(beanName);
        if (existingData != null) {
            logger.warn("Data already set for '" + jndiData + "' for the container URL '" + this.containerURL + "'.");
        }

        // Put info
        beansMap.put(beanName, jndiData);

    }

    /**
     * Adds a new JNDI name for the given message destination name.
     * @param messageDestinationName the given message destination name.
     * @param jndiData data for the JNDI Name entry
     */
    public void addMessageDestinationJNDIName(final String messageDestinationName, final EZBJNDIData jndiData) {
        EZBJNDIData existingData = this.messageDestinationMap.get(messageDestinationName);
        if (existingData != null) {
            logger.warn("JNDI Name for Message destination name ''{0}'' was already set with value ''{1}}'",
                    messageDestinationName, jndiData.getName());
        }
        this.messageDestinationMap.put(messageDestinationName, jndiData);
    }


    /**
     * Allows to find Message Destination JNDI name.
     * @return a list of matching JNDI objects for the given message destination name.
     * @param messageDestinationName the name of the message destination.
     */
    public List<EZBJNDIData> getMessageDestinationJNDINames(final String messageDestinationName) {
        // Ask EAR if not found
        return getMessageDestinationJNDINames(messageDestinationName, true);
    }


    /**
     * Allows to find Message Destination JNDI name.
     * @param messageDestinationName the name of the message destination.
    * @param askParent allow to disallow request to parent.
     * @return a list of matching JNDI objects for the given message destination name.
     */
    public List<EZBJNDIData> getMessageDestinationJNDINames(final String messageDestinationName, final boolean askParent) {
     // Build list for the return value
        List<EZBJNDIData> dataList = new ArrayList<EZBJNDIData>();

        // Name of a jar in the destination link ?
        String newMessageDestinationName = messageDestinationName;
        if (newMessageDestinationName.indexOf("#") > 0) {
            // extract only the destination name
            newMessageDestinationName = messageDestinationName.split("#")[1];
        }

        // get metadata
        EZBJNDIData data = this.messageDestinationMap.get(newMessageDestinationName);

        // Found a value, add it
        if (data == null) {
            // Not found, check in the other containers of the EAR (if any)
            return getMessageDestinationJNDINameInEAR(newMessageDestinationName, askParent);
        }

        // Add the local instance found
        dataList.add(data);

        return dataList;
    }

    /**
     * Allows to find MessageDestination JNDI name in the EAR if the container is packaged
     * within an EAR.
     * @return a list of matching JNDI objects for the message destination name
     * @param messageDestinationName the name of the message destination
     * @param enabled allow to disallow request to parent.
     */
    protected List<EZBJNDIData> getMessageDestinationJNDINameInEAR(final String messageDestinationName, final boolean enabled) {
        // If container is not inside an EAR or that recursion is turned off
        // stop here
        if (!enabled || this.applicationJNDIResolver == null) {
            return Collections.emptyList();
        }

        // Ask the resolver of the EAR
        return this.applicationJNDIResolver.getMessageDestinationJNDINames(messageDestinationName);
    }
}
