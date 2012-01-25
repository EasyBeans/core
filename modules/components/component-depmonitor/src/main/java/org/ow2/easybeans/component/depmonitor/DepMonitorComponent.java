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
 * $Id: DepMonitorComponent.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.component.depmonitor;

import java.util.ArrayList;
import java.util.List;

import org.ow2.easybeans.component.api.EZBComponentException;
import org.ow2.easybeans.component.itf.EZBDepMonitorComponent;
import org.ow2.util.log.Log;
import org.ow2.util.log.LogFactory;

/**
 * Manage the monitoring of deployables.
 * @author Florent Benoit
 */
public class DepMonitorComponent implements EZBDepMonitorComponent {

    /**
     * Logger.
     */
    private static Log logger = LogFactory.getLog(DepMonitorComponent.class);

    /**
     * List of monitors.
     */
    private List<EZBMonitor> monitors = null;


    /**
     * Default constructor.<br>
     */
    public DepMonitorComponent() {
        this.monitors = new ArrayList<EZBMonitor>();
    }

    /**
     * Init method.<br/> This method is called before the start method.
     * @throws EZBComponentException if the initialization has failed.
     */
    public void init() throws EZBComponentException {
    }

    /**
     * Start method.<br/> This method is called after the init method.
     * @throws EZBComponentException if the start has failed.
     */
    public void start() throws EZBComponentException {

    }

    /**
     * Callback by the server.
     */
    public void enable() throws EZBComponentException {
        for (EZBMonitor monitor : this.monitors) {
            try {
                monitor.start();
            } catch (EZBMonitorException e) {
                throw new EZBComponentException("Cannot start monitor '" + monitor + "'", e);
            }
        }
    }

    /**
     * Gets the list of monitors.
     * @return the list of monitors.
     */
    public List<EZBMonitor> getEZBMonitors() {
        return this.monitors;
    }

    /**
     * Set the list of monitors.
     * @param monitors the list of monitors.
     */
    public void setEZBMonitors(final List<EZBMonitor> monitors) {
        this.monitors = monitors;
    }

    /**
     * Stop method.<br/> This method is called when component needs to be
     * stopped.
     * @throws EZBComponentException if the stop is failing.
     */
    public void stop() throws EZBComponentException {
        for (EZBMonitor monitor : this.monitors) {
            try {
                monitor.stop();
            } catch (EZBMonitorException e) {
                logger.error("Unable to stop a monitor '" + monitor + "'", e);
            }
        }

    }


}
