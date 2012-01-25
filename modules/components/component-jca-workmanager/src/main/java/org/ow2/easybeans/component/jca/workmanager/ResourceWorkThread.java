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
 * $Id: ResourceWorkThread.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.component.jca.workmanager;

import javax.resource.spi.work.WorkException;

import org.ow2.util.log.Log;
import org.ow2.util.log.LogFactory;

/**
 * Thread executing works for the work manager.
 * @author Philippe Durieux (JOnAS)
 * @author Florent Benoit (EasyBeans)
 */
public class ResourceWorkThread extends Thread {

    /**
     * Logger.
     */
    private Log logger = LogFactory.getLog(ResourceWorkThread.class);

    /**
     * Resource WorkManager used by this worker thread.
     */
    private ResourceWorkManagerComponent workManager;

    /**
     * Build a thread doing the work.
     * @param workManager The implementation of WorkManager API.
     * @param threadNumber the thread number for this thread (debug info)
     * @param workManagerNumber identifier of the work manager instance
     */
    ResourceWorkThread(final ResourceWorkManagerComponent workManager, final  int workManagerNumber, final int threadNumber) {
        this.workManager = workManager;
        setName(this.getClass().getName() + "- wm number (" + workManagerNumber + "), Thread Number (" + threadNumber + ")");
    }

    /**
     * Start the thread work.
     */
    @Override
    public void run() {

        while (true) {
            try {
                this.workManager.nextWork();
            } catch (InterruptedException e) {
                this.logger.error("Exception while waiting during a work", e);
                return;
            } catch (WorkException e) {
                this.logger.error("Exception during work run", e);
            } catch (ResourceWorkManagerStoppedException e) {
                this.logger.debug("Manager has been stopped", e);
                return;
            }
        }
    }
}
