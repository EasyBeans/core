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
 * $Id: EasyBeansComponentTracker.java 6142 2012-01-25 14:11:48Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.osgi.agent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;
import org.ow2.easybeans.component.api.EZBComponent;
import org.ow2.util.log.Log;
import org.ow2.util.log.LogFactory;

/**
 * Tracks the EasyBeans component.
 * @author Florent Benoit
 */
public class EasyBeansComponentTracker extends ServiceTracker {

    /**
     * Logger.
     */
    private static final Log LOGGER = LogFactory.getLog(EasyBeansComponentTracker.class);

    /**
     * List of required instances.
     */
    private List<String> componentInstancesRequired = null;

    /**
     * List of running instances.
     */
    private List<String> componentInstancesRunning = null;

    /**
     * Server is ready (all components have been started ?).
     */
    private boolean isReady = false;


    /**
     * Service registration.
     */
    private ServiceRegistration registration = null;

    /**
     * Build a new component tracker.
     * @param context the bundle context
     * @param componentInstancesRequired instances that we need to wait
     */
    public EasyBeansComponentTracker(final BundleContext context, final List<String> componentInstancesRequired) {
        super(context, EZBComponent.class.getName(), null);
        this.componentInstancesRequired = componentInstancesRequired;
        this.componentInstancesRunning = new ArrayList<String>();

        LOGGER.debug("componentInstancesRequired=''{0}''", this.componentInstancesRequired);

    }

    /**
     * Adding a new service.
     */
    @Override
    public Object addingService(final ServiceReference serviceReference) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Adding service {0}", serviceReference);
            LOGGER.debug("properties = ''{0}''", Arrays.asList(serviceReference.getPropertyKeys()));
            LOGGER.debug("service.id=''{0}''", serviceReference.getProperty("service.id"));
            LOGGER.debug("service.pid=''{0}''", serviceReference.getProperty("service.pid"));
            LOGGER.debug("factory pid=''{0}''", serviceReference.getProperty("service.factoryPid"));
            LOGGER.debug("xmlconfig=''{0}''", serviceReference.getProperty("xmlconfig"));
        }
        String factoryPid = (String) serviceReference.getProperty("service.factoryPid");

        // This component is a component that we're waiting for
        if (this.componentInstancesRequired.contains(factoryPid)) {
            LOGGER.debug("{0} is contained in ''{1}''", factoryPid, this.componentInstancesRequired);
            this.componentInstancesRunning.add(factoryPid);
        }

        LOGGER.debug("this.componentInstancesRunning.size()=''{0}'', componentInstancesRequired.size()=''{1}''",
                this.componentInstancesRunning.size(), this.componentInstancesRequired.size());
        LOGGER.debug("this.componentInstancesRunning=''{0}''", this.componentInstancesRunning);

        // Do we have the required instances ?
        if (this.componentInstancesRunning.size() >= this.componentInstancesRequired.size()) {
            // do it only once
            if (!this.isReady) {
                // Register a "ready" service
                this.registration = this.context.registerService(EZBAgentReady.class.getName(), new AgentReady(), null);
                this.isReady = true;
                LOGGER.info("Agent becomes ready, all required components are here");

            }
        }

        return super.addingService(serviceReference);
    }

    @Override
    public void modifiedService(final ServiceReference serviceReference, final Object o) {
        LOGGER.debug("modifiedService service {0}", serviceReference);
        super.modifiedService(serviceReference, o);
    }

    @Override
    public void removedService(final ServiceReference serviceReference, final Object service) {
        LOGGER.debug("removedService service {0}", serviceReference);

        String factoryPid = (String) serviceReference.getProperty("service.factoryPid");
        // This component is a component that we're waiting for, remove it
        if (this.componentInstancesRequired.contains(factoryPid)) {
            this.componentInstancesRunning.remove(factoryPid);
        }

        // Do we have the required instances ?
        if (this.componentInstancesRunning.size() < this.componentInstancesRequired.size()) {
            if (this.isReady) {
                // Unregister "ready" service
                this.isReady = false;
                if (this.registration != null) {
                    this.registration.unregister();
                    LOGGER.info("Agent becomes unavailable, missing some required components");

                }
            }
        }

        super.removedService(serviceReference, service);
    }

}
