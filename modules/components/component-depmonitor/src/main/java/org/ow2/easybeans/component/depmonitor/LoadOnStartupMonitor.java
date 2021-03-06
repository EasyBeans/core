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
 * $Id: LoadOnStartupMonitor.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.component.depmonitor;

import java.util.Arrays;

/**
 * This monitor is deploying application only the first time that it is
 * launched, there is no further scan.
 * @author Florent Benoit
 */
public class LoadOnStartupMonitor extends AbsMonitor implements EZBMonitor {

    /**
     * Builds a new monitor.
     * @throws EZBMonitorException if there is an exception for this monitor.
     */
    public LoadOnStartupMonitor() throws EZBMonitorException {
        super();
        setWaitTime(0);
    }

    /**
     * Start this monitor.
     * @throws EZBMonitorException if it can't be started
     */
    @Override
    public void start() throws EZBMonitorException {
        getLogger().info("Loading at startup the deployables of the directories ''{0}''", Arrays.asList(getDirectory()));
        super.start();
    }
}
