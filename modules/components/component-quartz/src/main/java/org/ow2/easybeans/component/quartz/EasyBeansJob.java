/**
 * EasyBeans
 * Copyright (C) 2007 Bull S.A.S.
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
 * $Id: EasyBeansJob.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.component.quartz;

import javax.ejb.Timer;

import org.ow2.easybeans.api.EZBContainer;
import org.ow2.easybeans.api.EZBServer;
import org.ow2.easybeans.api.EmbeddedManager;
import org.ow2.easybeans.api.Factory;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * This class will receive a job execution context and then call the bean
 * method. The parameters are stored in the Job Execution Context
 * @author Florent Benoit
 */
public class EasyBeansJob implements Job {

    /**
     * Invoke the factory with the timer object.
     * @param context the context containing the stuff in order to call the Bean's factory and the associated timeout method.
     * @throws JobExecutionException if there is an exception while executing
     *         the job.
     */
    public void execute(final JobExecutionContext context) throws JobExecutionException {
        // Get the Job Detail data from the context
        EasyBeansJobDetailData data = ((EasyBeansJobDetail) context.getJobDetail()).getJobDetailData();


        // Find the Embedded instance
        EZBServer server = EmbeddedManager.getEmbedded(data.getEasyBeansServerID());
        if (server == null) {
            throw new JobExecutionException("Cannot find the embedded server with the id '" + data.getEasyBeansServerID() + "'.");
        }

        // Get the container
        EZBContainer container = server.getContainer(data.getContainerId());
        if (container == null) {
            throw new JobExecutionException("Cannot find the container with the id '" + data.getContainerId() + "'.");
        }

        // Get the factory
        Factory factory = container.getFactory(data.getFactoryName());
        if (factory == null) {
            throw new JobExecutionException("Cannot find the factory with the name '" + data.getFactoryName() + "'.");
        }

        // Get the timer
        Timer timer = data.getTimer();
        if (timer == null) {
            throw new JobExecutionException("No timer found in the given JobExecutionContext.");
        }

        // Invoke the timer method
        factory.notifyTimeout(timer);

    }
}
