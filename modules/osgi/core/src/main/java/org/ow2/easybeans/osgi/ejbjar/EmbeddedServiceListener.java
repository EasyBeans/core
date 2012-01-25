/**
 * EasyBeans
 * Copyright (C) 2006 Bull S.A.S.
 * Contact: easybeans@objectweb.org
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
 * $Id: EmbeddedServiceListener.java 3054 2008-04-30 15:41:13Z sauthieg $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.osgi.ejbjar;

import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;

/**
 * Listen for arrival and departure of Embedded Services.
 * @author Guillaume Sauthier
 */
public class EmbeddedServiceListener implements ServiceListener {

    /**
     * The instance providing callbacks.
     */
    private Activator activator = null;

    /**
     * Constructor.
     * @param activator Provides the callbacks.
     */
    public EmbeddedServiceListener(final Activator activator) {
        this.activator = activator;
    }

    /**
     * When an Embedded instance gets registered, the activator starts the Container.
     * When an Embedded instance gets unregistered, the activator stops the Container.
     * @param se ServiceEvent wrapping the service instance.
     * @see org.osgi.framework.ServiceListener#serviceChanged(org.osgi.framework.ServiceEvent)
     */
    public void serviceChanged(final ServiceEvent se) {
        // store the ref
        ServiceReference sr = se.getServiceReference();

        // Switch over the event type
        switch (se.getType()) {
        case ServiceEvent.MODIFIED:
            // ??
            break;
        case ServiceEvent.REGISTERED:
            this.activator.startContainer(sr);
            break;
        case ServiceEvent.UNREGISTERING:
            this.activator.stopContainer();
            break;
        default:
            break;
        }
    }

}
