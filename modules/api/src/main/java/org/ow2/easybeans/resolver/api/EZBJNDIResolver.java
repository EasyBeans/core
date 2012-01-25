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
 * $Id: EZBJNDIResolver.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.resolver.api;

import java.util.List;

/**
 * Interface that all JNDI Resolver should use.
 * @author Florent Benoit
 */
public interface EZBJNDIResolver {

    /**
     * Allows to find EJB JNDI name.
     * @return a list of matching JNDI objects for the given interface and bean name.
     * @param interfaceName the name of the interface that EJBs are implementing.
     */
    List<EZBJNDIBeanData> getEJBJNDINames(String interfaceName);


    /**
     * Allows to find EJB JNDI name.
     * @return a list of matching JNDI objects for the given interface and bean name.
     * @param interfaceName the name of the interface that EJBs are implementing.
     * @param beanName the name of the bean on which we need to find JNDI name.
     */
    List<EZBJNDIBeanData> getEJBJNDINames(String interfaceName, String beanName);

    /**
     * Gets jndi name for a given interface and a bean name.
     * @param interfaceName the name of the interface.
     * @param beanName the name of the bean.
     * @return the jndi name
     * @throws EZBJNDIResolverException if not found
     */
    String getEJBJNDIUniqueName(final String interfaceName, final String beanName) throws EZBJNDIResolverException;

    /**
     * Allows to find Message Destination JNDI name.
     * @return a list of matching JNDI objects for the given message destination name.
     * @param messageDestinationName the name of the message destination.
     */
    List<EZBJNDIData> getMessageDestinationJNDINames(String messageDestinationName);

    /**
     * Allows to find Message Destination JNDI name.
     * @return a list of matching JNDI objects for the given message destination name.
     * @param messageDestinationName the name of the message destination.
     * @throws EZBJNDIResolverException if not found
     */
    String getMessageDestinationJNDIUniqueName(String messageDestinationName) throws EZBJNDIResolverException;

}
