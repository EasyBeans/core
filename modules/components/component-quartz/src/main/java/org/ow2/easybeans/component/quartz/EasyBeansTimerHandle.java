/**
 * EasyBeans
 * Copyright (C) 2007-2012 Bull S.A.S.
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
 * $Id: EasyBeansTimerHandle.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.component.quartz;

import static org.ow2.easybeans.api.OperationState.AFTER_COMPLETION;
import static org.ow2.easybeans.api.OperationState.DEPENDENCY_INJECTION;
import static org.ow2.easybeans.api.OperationState.LIFECYCLE_CALLBACK_INTERCEPTOR;

import java.util.List;

import javax.ejb.EJBException;
import javax.ejb.NoSuchObjectLocalException;
import javax.ejb.Timer;
import javax.ejb.TimerHandle;

import org.ow2.easybeans.api.EZBContainer;
import org.ow2.easybeans.api.EZBServer;
import org.ow2.easybeans.api.EmbeddedManager;
import org.ow2.easybeans.api.Factory;
import org.ow2.easybeans.api.OperationState;
import org.ow2.easybeans.api.components.EZBComponentRegistry;
import org.ow2.easybeans.component.itf.TimerComponent;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;

/**
 * Implementation of the Timer handle interface.
 * @author Florent Benoit
 */
public class EasyBeansTimerHandle implements TimerHandle {

    /**
     * Serial version UID for serializable classes.
     */
    private static final long serialVersionUID = 5391559452078385341L;

    /**
     * JobDetail used to get parameters.
     */
    private JobDetail jobDetail;

    /**
     * Constructor. Build an handle for this timer.
     * @param jobDetail the job detail.
     */
    public EasyBeansTimerHandle(final JobDetail jobDetail) {
        this.jobDetail = jobDetail;
    }

    /**
     * Obtain a reference to the timer represented by this handle.
     * @return a reference to the timer represented by this handle.
     * @throws IllegalStateException If this method is invoked while the
     *         instance is in a state that does not allow access to this method.
     * @throws NoSuchObjectLocalException If invoked on a handle whose
     *         associated timer has expired or has been cancelled.
     * @throws EJBException If this method could not complete due to a
     *         system-level failure.
     */
    public Timer getTimer() throws IllegalStateException, NoSuchObjectLocalException, EJBException {
        Timer timer = null;

        // Get data from the jobDetail
        EasyBeansJobDetailData easyBeansJobDetailData = (EasyBeansJobDetailData) this.jobDetail.getJobDataMap().get("data");

        Integer easyBeansServerID = easyBeansJobDetailData.getEasyBeansServerID();
        String containerID = easyBeansJobDetailData.getContainerId();
        String factoryName = easyBeansJobDetailData.getFactoryName();

        // Get the EasyBeans embedded instance
        EZBServer embedded = EmbeddedManager.getEmbedded(easyBeansServerID);

        // Get the container object
        EZBContainer container = embedded.getContainer(containerID);
        // get the factory
        Factory<?, ?> factory = container.getFactory(factoryName);

        // Ensure operation state is valid
        OperationState operationState = factory.getOperationState();
        if (DEPENDENCY_INJECTION == operationState || LIFECYCLE_CALLBACK_INTERCEPTOR == operationState
                || AFTER_COMPLETION == operationState) {
            throw new IllegalStateException("The getTimer() method cannot be called within the operation state '"
                    + operationState + "'.");
        }

        // Get the components registry
        EZBComponentRegistry registry = embedded.getComponentManager().getComponentRegistry();

        // Get the timer components
        List<TimerComponent> timerComponents = registry.getComponents(TimerComponent.class);

        // Find the quartz component in this list
        if (timerComponents == null || timerComponents.size() == 0) {
            throw new EJBException("Cannot get the timer object as no timer component have been found on the EasyBeans server");
        }
        // Check the first one
        TimerComponent timerComponent = timerComponents.get(0);
        QuartzComponent quartzComponent = null;
        if (timerComponent instanceof QuartzComponent) {
            quartzComponent = (QuartzComponent) timerComponent;
        } else {
            throw new EJBException("The timer component found is not a Quartz Timer Component ('" + timerComponent + "').");
        }

        // Get Scheduler on the quartz component
        Scheduler scheduler = quartzComponent.getScheduler();


        // Get triggers
        List<? extends Trigger> triggers = null;
        try {
            triggers = scheduler.getTriggersOfJob(this.jobDetail.getKey());
        } catch (SchedulerException e) {
            throw new EJBException("Cannot get triggers for the job named '" + this.jobDetail.getKey().getName() + "'.", e);
        }

        if (triggers.size() == 0) {
            throw new NoSuchObjectLocalException("The associated timer of the handle has been cancelled.");
        }

        // Should be only once trigger per job
        if (triggers == null || triggers.size() > 1) {
            throw new EJBException("Invalid numbers of triggers found for the job named '" + this.jobDetail.getKey().getName()
                    + "'.");
        }

        // Build a timer object and return it
        timer = new EasyBeansTimer(this.jobDetail, triggers.get(0), scheduler, factory);

        // Return the timer instance
        return timer;
    }

}
