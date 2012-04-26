/**
 * EasyBeans
 * Copyright (C) 2012 Bull S.A.S.
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
 * $Id$
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.embeddable;

import java.util.Map;

import javax.ejb.EJBException;
import javax.ejb.embeddable.EJBContainer;
import javax.ejb.spi.EJBContainerProvider;

import org.ow2.easybeans.server.Embedded;

/**
 * Defines the EasyBeans EJB Container provider.
 * @author Florent Benoit
 */
public class EasyBeansEJBContainerProvider implements EJBContainerProvider {

    /**
     * Called by the embeddable container bootstrap process to find a suitable
     * embeddable container implementation. An embeddable container provider may
     * deem itself as appropriate for the embeddable application if any of the
     * following are true : The javax.ejb.embeddable.initial property was
     * included in the Map passed to createEJBContainer and the value of the
     * property is the provider's implementation class. No
     * javax.ejb.embeddable.initial property was specified. If a provider does
     * not qualify as the provider for the embeddable application, it must
     * return null.
     * @param properties the given properties
     * @return EJBContainer instance or null
     * @throws EJBException if container is not created.
     */
    public EJBContainer createEJBContainer(final Map<?, ?> properties) throws EJBException {

        // Check if there is a provider property
        if (properties != null) {
            Object o = properties.get(EJBContainer.PROVIDER);
            // Only manage the EasyBeans class
            if (!EasyBeansEJBContainer.class.getName().equals(o)) {
                return null;
            }
        }

        // Create an Embedded version of EasyBeans
        Embedded embedded = new Embedded();

        // Wrap the Embedded module
        EasyBeansEJBContainer ejbContainer = new EasyBeansEJBContainer(embedded, properties);

        // Start the container and return it
        ejbContainer.start();

        return ejbContainer;

    }

}
