/**
 * EasyBeans
 * Copyright (C) 2006 Bull S.A.S.
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
 * $Id: MDBResourceAdapterHelper.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.container.mdb.helper;

import javax.resource.ResourceException;
import javax.resource.spi.ResourceAdapter;

import org.ow2.easybeans.component.api.EZBComponent;
import org.ow2.easybeans.component.itf.JMSComponent;
import org.ow2.easybeans.server.Embedded;

/**
 * This class allow to get the ResourceAdapter object for a given destination
 * (activation-spec object).
 * @author Florent Benoit
 */
public final class MDBResourceAdapterHelper {

    /**
     * Implementation of a finder.
     */
    private static IResourceAdapterFinder resourceAdapterFinder = null;

    /**
     * Utility class, no public constructor.
     */
    private MDBResourceAdapterHelper() {

    }

    /**
     * Gets the resource adapter object for the given jndi name (activation
     * spec) and the given embedded object.
     * @param jndiName the nameof the activation spec bound in the registry
     * @param embedded the embedded server
     * @return an instance of the resource adapter that provides the MDB
     *         activation spec.
     * @throws ResourceException if an error occurs while trying to get the
     *         resource adapter.
     */
    public static ResourceAdapter getResourceAdapter(final String jndiName, final Embedded embedded)
            throws ResourceException {

        // Use the delegate if set
        if (resourceAdapterFinder != null) {
            return resourceAdapterFinder.getResourceAdapter(jndiName);
        }

        // try to see if JMS mini-resource adapter service was started.
        EZBComponent component = embedded.getComponent("org.ow2.easybeans.component.joram.JoramComponent");
        if (component != null) {
            // get ResourceAdapter from the service
            if (component instanceof JMSComponent) {
                return ((JMSComponent) component).getResourceAdapter();
            }
            throw new IllegalArgumentException("The 'jms' component doesn't implement JMSComponent interface.");
        }

        throw new ResourceException("MDB is used but no resource service was started.");

    }

    /**
     * Sets the finder object that can be used to get the resource adapter object.
     * @param finder the given instance.
     */
    public static void setResourceAdapterFinder(final IResourceAdapterFinder finder) {
        if (resourceAdapterFinder != null) {
            throw new IllegalStateException("Unable to set the finder. It is already set");
        }
        resourceAdapterFinder = finder;
    }


}
