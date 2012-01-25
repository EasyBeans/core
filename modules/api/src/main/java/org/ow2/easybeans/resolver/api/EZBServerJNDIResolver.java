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
 * $Id: EZBServerJNDIResolver.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.resolver.api;

/**
 * JNDI Resolver for EJB at the server level. This allows to find a JNDIName for
 * a given interface name. It can find many possible cases for a given
 * interface. This case has to be handled by the client of this interface.
 * @author Florent Benoit
 */
public interface EZBServerJNDIResolver extends EZBJNDIResolver {

       /**
     * Remove a container JNDI resolver.
     * @param resolver the given resolver to remove.
     */
    void removeContainerResolver(final EZBContainerJNDIResolver resolver);

    /**
     * Add a new container JNDI resolver.
     * @param resolver the given resolver to add.
     */
    void addContainerResolver(final EZBContainerJNDIResolver resolver);

}
