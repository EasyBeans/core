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
 * $Id: ScanningMonitor.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.component.depmonitor;

import java.util.Arrays;

/**
 * This monitor is deploying application when it is launched and then scan the
 * directory for each period.
 * @author Florent Benoit
 */
public class ScanningMonitor extends AbsMonitor implements EZBMonitor {

    /**
     * Builds a new monitor.
     * @throws EZBMonitorException if there is an exception for this monitor.
     */
    public ScanningMonitor() throws EZBMonitorException {
        super();
    }

    /**
     * Start this monitor.
     * @throws EZBMonitorException if it can't be started
     */
    @Override
    public void start() throws EZBMonitorException {
        getLogger().info("Monitoring deployables of the directories ''{0}'' with period of ''{1}'' ms",
                Arrays.asList(getDirectory()), Long.valueOf(getWaitTime()));
        super.start();

    }
}
