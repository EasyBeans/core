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
 * $Id: EZBContainerJNDIResolver.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.resolver.api;

import java.net.URL;
import java.util.List;


/**
 * Resolver allowing to find matching JNDI names for beans within the same container.
 * @author Florent Benoit
 */
public interface EZBContainerJNDIResolver extends EZBJNDIResolver {

    /**
     * @return URL used by this resolver.
     */
    URL getURL();

    /**
     * Allows to find EJB JNDI name.
     * @return a list of matching JNDI objects for the given interface and bean
     *         name.
     * @param interfaceName the name of the interface that EJBs are
     *        implementing.
     * @param beanName the name of the bean on which we need to find JNDI name.
     * @param askParent allow to disallow request to parent.
     */
    List<EZBJNDIBeanData> getEJBJNDINames(final String interfaceName, final String beanName, final boolean askParent);

    /**
     * Adds a new JNDI name for the given interface name / bean name.
     * @param interfaceName the given interface
     * @param jndiData data for the JNDI Name entry
     */
    void addEJBJNDIName(final String interfaceName, final EZBJNDIBeanData jndiData);

    /**
     * Adds a new JNDI name for the given message destination name.
     * @param messageDestinationName the given message destination name.
     * @param jndiData data for the JNDI Name entry
     */
    void addMessageDestinationJNDIName(final String messageDestinationName, final EZBJNDIData jndiData);

    /**
     * Allows to find Message Destination JNDI name.
     * @param messageDestinationName the name of the message destination.
     * @param askParent allow to disallow request to parent.
     * @return a list of matching JNDI objects for the given message destination
     *         name.
     */
    List<EZBJNDIData> getMessageDestinationJNDINames(final String messageDestinationName, final boolean askParent);

    /**
     * @return the application JNDI Resolver.
     */
    EZBApplicationJNDIResolver getApplicationJNDIResolver();

    /**
     * Sets the application resolver.
     * @param applicationJNDIResolver the application resolver
     */
    void setApplicationJNDIResolver(final EZBApplicationJNDIResolver applicationJNDIResolver);
}
