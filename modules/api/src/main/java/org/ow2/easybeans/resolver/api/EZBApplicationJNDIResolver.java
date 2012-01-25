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
 * $Id: EZBApplicationJNDIResolver.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.resolver.api;

import java.net.URL;
import java.util.List;

/**
 * Resolver allowing to find matching JNDI names for containers of the same EAR.
 * @author Florent Benoit
 */
public interface EZBApplicationJNDIResolver extends EZBJNDIResolver {

    /**
     * Allows to find EJB JNDI name.
     * @return a list of matching JNDI objects for the given interface.
     * @param interfaceName the name of the interface that EJBs are
     *        implementing.
     * @param beanName the name of the bean on which we need to find JNDI name.
     * @param ejbLinkURL the optional URL for the container that should include
     *        the bean name.
     */
    List<EZBJNDIBeanData> getEJBJNDINames(String interfaceName, String beanName, URL ejbLinkURL);


    /**
     * Add a child container JNDI Resolver.
     * @param containerJNDIResolver the child resolver to add
     */
    void addContainerJNDIResolver(EZBContainerJNDIResolver containerJNDIResolver);

}
