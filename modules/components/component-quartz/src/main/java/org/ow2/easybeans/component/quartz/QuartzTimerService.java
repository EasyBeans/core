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
 * $Id: QuartzTimerService.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.component.quartz;

import static org.ow2.easybeans.api.OperationState.AFTER_COMPLETION;
import static org.ow2.easybeans.api.OperationState.DEPENDENCY_INJECTION;
import static org.ow2.easybeans.api.OperationState.LIFECYCLE_CALLBACK_INTERCEPTOR;
import static org.quartz.SimpleTrigger.REPEAT_INDEFINITELY;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import javax.ejb.EJBException;
import javax.ejb.Timer;
import javax.ejb.TimerService;

import org.ow2.easybeans.api.Factory;
import org.ow2.easybeans.api.IStatefulSessionFactory;
import org.ow2.easybeans.api.OperationState;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;

/**
 * Implementation of the EJB Timer service that is based on the Quartz
 * framework.
 * @author Florent Benoit
 */
public class QuartzTimerService implements TimerService {

    /**
     * An EJB Timer service is linked to an EasyBeans factory.
     */
    private Factory<?, ?> factory = null;

    /**
     * The timer service is also linked to a Quartz scheduler.
     */
    private Scheduler scheduler = null;

    /**
     * Name of the group for triggers.
     */
    private String triggerGroupName = null;

    /**
     * Trigger name ID.
     */
    private long triggerId = 0;

    /**
     * JobDetail id.
     */
    private long jobDetailId = 0;

    /**
     * Build a new instance of the EJB Timer service for the given factory and
     * the given scheduler.
     * @param factory the given factory
     * @param scheduler the given scheduler
     */
    public QuartzTimerService(final Factory<?, ?> factory, final Scheduler scheduler) {
        this.factory = factory;
        this.scheduler = scheduler;
        this.triggerGroupName = factory.getId();
    }

    /**
     * Create a single-action timer that expires after a specified duration.
     * @param duration The number of milliseconds that must elapse before the
     *        timer expires.
     * @param info Application information to be delivered along with the timer
     *        expiration notification. This can be null.
     * @return The newly created Timer.
     * @throws IllegalArgumentException If duration is negative
     * @throws IllegalStateException If this method is invoked while the
     *         instance is in a state that does not allow access to this method.
     * @throws EJBException If this method fails due to a system-level failure.
     */
    public Timer createTimer(final long duration, final Serializable info) throws IllegalArgumentException,
            IllegalStateException, EJBException {

        // Disallowed from stateful
        if (this.factory instanceof IStatefulSessionFactory) {
            throw new IllegalStateException("The createTimer() method cannot be called from a stateful session bean.");
        }

        OperationState operationState = this.factory.getOperationState();
        if (DEPENDENCY_INJECTION == operationState || LIFECYCLE_CALLBACK_INTERCEPTOR == operationState
                || AFTER_COMPLETION == operationState) {
            throw new IllegalStateException(
                    "The createTimer(duration, info) method cannot be called within the operation state '" + operationState
                            + "'.");
        }

        // Use other method
        return createTimer(new Date(System.currentTimeMillis() + duration), info);
    }

    /**
     * Create an interval timer whose first expiration occurs after a specified
     * duration, and whose subsequent expirations occur after a specified
     * interval.
     * @param initialDuration The number of milliseconds that must elapse before
     *        the first timer expiration notification.
     * @param intervalDuration The number of milliseconds that must elapse
     *        between timer expiration notifications. Expiration notifications
     *        are scheduled relative to the time of the first expiration. If
     *        expiration is delayed(e.g. due to the interleaving of other method
     *        calls on the bean) two or more expiration notifications may occur
     *        in close succession to "catch up".
     * @param info Application information to be delivered along with the timer
     *        expiration. This can be null.
     * @return The newly created Timer.
     * @throws IllegalArgumentException If initialDuration is negative, or
     *         intervalDuration is negative.
     * @throws IllegalStateException If this method is invoked while the
     *         instance is in a state that does not allow access to this method.
     * @throws EJBException If this method could not complete due to a
     *         system-level failure.
     */
    public Timer createTimer(final long initialDuration, final long intervalDuration, final Serializable info)
            throws IllegalArgumentException, IllegalStateException, EJBException {

        // Disallowed from stateful
        if (this.factory instanceof IStatefulSessionFactory) {
            throw new IllegalStateException("The createTimer() method cannot be called from a stateful session bean.");
        }

        OperationState operationState = this.factory.getOperationState();
        if (DEPENDENCY_INJECTION == operationState || LIFECYCLE_CALLBACK_INTERCEPTOR == operationState
                || AFTER_COMPLETION == operationState) {
            throw new IllegalStateException("The createTimer(duratinitialDurationion, intervalDuration, info) "
                    + " method cannot be called within the operation state '" + operationState + "'.");
        }

        // Compute start date for initial expiration
        Date initialExpiration = new Date(System.currentTimeMillis() + initialDuration);

        // Use the method using Date parameter
        return createTimer(initialExpiration, intervalDuration, info);
    }

    /**
     * Create a single-action timer that expires at a given point in time.
     * @param expiration The point in time at which the timer must expire.
     * @param info Application information to be delivered along with the timer
     *        expiration notification. This can be null.
     * @return The newly created Timer.
     * @throws IllegalArgumentException If expiration is null, or
     *         expiration.getTime() is negative.
     * @throws IllegalStateException If this method is invoked while the
     *         instance is in a state that does not allow access to this method.
     * @throws EJBException If this method could not complete due to a
     *         system-level failure.
     */
    public Timer createTimer(final Date expiration, final Serializable info) throws IllegalArgumentException,
            IllegalStateException, EJBException {
        // Disallowed from stateful
        if (this.factory instanceof IStatefulSessionFactory) {
            throw new IllegalStateException("The createTimer() method cannot be called from a stateful session bean.");
        }
        OperationState operationState = this.factory.getOperationState();
        if (DEPENDENCY_INJECTION == operationState || LIFECYCLE_CALLBACK_INTERCEPTOR == operationState
                || AFTER_COMPLETION == operationState) {
            throw new IllegalStateException(
                    "The createTimer(expiration, info) method cannot be called within the operation state '" + operationState
                            + "'.");
        }

        // Create the trigger that won't repeat
        Trigger trigger = new SimpleTrigger(getTriggerName(), getTriggerGroupName(), expiration);

        // Get timer
        return internalTimer(trigger, info);
    }

    /**
     * Create an interval timer whose first expiration occurs at a given point
     * in time and whose subsequent expirations occur after a specified
     * interval.
     * @param initialExpiration The point in time at which the first timer
     *        expiration must occur.
     * @param intervalDuration The number of milliseconds that must elapse
     *        between timer expiration notifications. Expiration notifications
     *        are scheduled relative to the time of the first expiration. If
     *        expiration is delayed(e.g. due to the interleaving of other method
     *        calls on the bean) two or more expiration notifications may occur
     *        in close succession to "catch up".
     * @param info Application information to be delivered along with the timer
     *        expiration. This can be null.
     * @return The newly created Timer.
     * @throws IllegalArgumentException If initialExpiration is null, or
     *         initialExpiration.getTime() is negative, or intervalDuration is
     *         negative.
     * @throws IllegalStateException If this method is invoked while the
     *         instance is in a state that does not allow access to this method.
     * @throws EJBException If this method could not complete due to a
     *         system-level failure.
     */
    public Timer createTimer(final Date initialExpiration, final long intervalDuration, final Serializable info)
            throws IllegalArgumentException, IllegalStateException, EJBException {
        // Disallowed from stateful
        if (this.factory instanceof IStatefulSessionFactory) {
            throw new IllegalStateException("The createTimer() method cannot be called from a stateful session bean.");
        }
        OperationState operationState = this.factory.getOperationState();
        if (DEPENDENCY_INJECTION == operationState || LIFECYCLE_CALLBACK_INTERCEPTOR == operationState
                || AFTER_COMPLETION == operationState) {
            throw new IllegalStateException("The createTimer(initialExpiration, intervalDuration, info) "
                    + "method cannot be called within the operation state '" + operationState + "'.");
        }

        // Create the trigger (repeat indefinitely)
        Trigger trigger = new SimpleTrigger(getTriggerName(), getTriggerGroupName(), initialExpiration, null,
                REPEAT_INDEFINITELY, intervalDuration);

        // Get timer
        return internalTimer(trigger, info);
    }

    /**
     * Create a timer object that is sent to the client. Also, create a new job
     * and send it to the Quartz Scheduler.
     * @param trigger the object containing the data for the scheduling.
     * @param info the optional serializable object given by the developer.
     * @return a Timer object.
     */
    private Timer internalTimer(final Trigger trigger, final Serializable info) {

        // Add stuff into the data of the job detail
        EasyBeansJobDetailData beansJobDetailData = new EasyBeansJobDetailData();
        beansJobDetailData.setInfo(info);

        // Options for finding the factory again
        Integer easyBeansServerID = this.factory.getContainer().getConfiguration().getEZBServer().getID();
        beansJobDetailData.setEasyBeansServerID(easyBeansServerID);
        beansJobDetailData.setContainerId(this.factory.getContainer().getId());
        beansJobDetailData.setFactoryName(this.factory.getBeanInfo().getName());

        // Build the Job Detail
        EasyBeansJobDetail jobDetail = new EasyBeansJobDetail(getNewJobDetailName(), getJobDetailGroupName(), beansJobDetailData);

        // Build a new timer object
        Timer timer = new EasyBeansTimer(jobDetail, trigger, this.scheduler, this.factory);

        // Add it as a data
        beansJobDetailData.setTimer(timer);

        // Schedule the job
        try {
            this.scheduler.scheduleJob(jobDetail, trigger);
        } catch (SchedulerException e) {
            throw new EJBException("Cannot schedule the given job '" + jobDetail + "'.", e);
        }

        // ...and return the timer
        return timer;
    }

    /**
     * Get all the active timers associated with this bean.
     * @return A collection of javax.ejb.Timer objects.
     * @throws IllegalStateException If this method is invoked while the
     *         instance is in a state that does not allow access to this method.
     * @throws EJBException If this method could not complete due to a
     *         system-level failure.
     */
    public Collection<Timer> getTimers() throws IllegalStateException, EJBException {
        // Disallowed from stateful
        if (this.factory instanceof IStatefulSessionFactory) {
            throw new IllegalStateException("The getTimers() method cannot be called from a stateful session bean.");
        }

        OperationState operationState = this.factory.getOperationState();
        if (DEPENDENCY_INJECTION == operationState || LIFECYCLE_CALLBACK_INTERCEPTOR == operationState
                || AFTER_COMPLETION == operationState) {
            throw new IllegalStateException(
                    "The createTimer(duration, info) method cannot be called within the operation state '" + operationState
                            + "'.");
        }

        Collection<Timer> timers = new ArrayList<Timer>();

        // Get the list of job names for this group
        String[] jobNames = null;
        try {
            jobNames = this.scheduler.getJobNames(getJobDetailGroupName());
        } catch (SchedulerException e) {
            throw new EJBException("Unable to get the job names from the scheduler for the group named '"
                    + getJobDetailGroupName() + "'.", e);
        }

        // If there are jobs, get the detail and trigger
        if (jobNames != null) {
            // For each job name
            for (String jobName : jobNames) {

                // Get detail
                JobDetail jobDetail = null;
                try {
                    jobDetail = this.scheduler.getJobDetail(jobName, getJobDetailGroupName());
                } catch (SchedulerException e) {
                    throw new EJBException("Cannot get the jobDetail for the jobName '" + jobName + "'.", e);
                }

                // Cast to correct object
                EasyBeansJobDetail easyBeansJobDetail = null;
                if (jobDetail instanceof EasyBeansJobDetail) {
                    easyBeansJobDetail = (EasyBeansJobDetail) jobDetail;
                } else {
                    throw new EJBException("JobDetail found for the job named '" + jobName
                            + "' is not an EasyBeansJobDetail object");
                }

                // Get triggers
                Trigger[] triggers = null;
                try {
                    triggers = this.scheduler.getTriggersOfJob(jobName, getJobDetailGroupName());
                } catch (SchedulerException e) {
                   throw new EJBException("Cannot get triggers for the job named '" +  jobName + "'.", e);
                }

                // Should be only once trigger per job
                if (triggers == null || triggers.length > 1) {
                    throw new EJBException("Invalid numbers of triggers found for the job named '" +  jobName + "'.");
                }

                // Build a timer object and return it
                timers.add(new EasyBeansTimer(easyBeansJobDetail, triggers[0], this.scheduler, this.factory));
            }
        }

        // Return the list of the timers for this timer service.
        return timers;
    }

    /**
     * Get an unique identifier for a Trigger name.
     * @return a new trigger name
     */
    private synchronized String getTriggerName() {
        return "triggerTimer" + (this.triggerId++);
    }

    /**
     * Get an unique identifier for a JobDetail name.
     * @return a new job detail name
     */
    private synchronized String getNewJobDetailName() {
        return "jobDetail" + (this.jobDetailId++);
    }

    /**
     * Get the group name for each Trigger.
     * @return the group name
     */
    private String getTriggerGroupName() {
        return this.triggerGroupName;
    }

    /**
     * Get the group name for each job detail.
     * @return the group name
     */
    private String getJobDetailGroupName() {
        return "jobDetailGroup" + this.factory.getClassName();
    }

    /**
     * Gets the Scheduler.
     * @return the scheduler.
     */
    public Scheduler getScheduler() {
        return this.scheduler;
    }
}
