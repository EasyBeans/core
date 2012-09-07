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
 * $Id: QuartzTimerService.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.component.quartz;

import static org.ow2.easybeans.api.OperationState.AFTER_COMPLETION;
import static org.ow2.easybeans.api.OperationState.DEPENDENCY_INJECTION;
import static org.ow2.easybeans.api.OperationState.LIFECYCLE_CALLBACK_INTERCEPTOR;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.ejb.EJBException;
import javax.ejb.ScheduleExpression;
import javax.ejb.Timer;
import javax.ejb.TimerConfig;

import org.ow2.easybeans.api.EZBTimerService;
import org.ow2.easybeans.api.Factory;
import org.ow2.easybeans.api.IStatefulSessionFactory;
import org.ow2.easybeans.api.OperationState;
import org.ow2.easybeans.api.bean.info.IBeanInfo;
import org.ow2.easybeans.api.bean.info.IMethodInfo;
import org.ow2.util.log.Log;
import org.ow2.util.log.LogFactory;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.matchers.GroupMatcher;

/**
 * Implementation of the EJB Timer service that is based on the Quartz
 * framework.
 * @author Florent Benoit
 */
public class QuartzTimerService implements EZBTimerService {

    /**
     * Logger.
     */
    private static final Log LOGGER = LogFactory.getLog(QuartzTimerService.class);

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
        Trigger trigger = TriggerBuilder.newTrigger().withIdentity(getTriggerName(), getTriggerGroupName()).startAt(
                expiration).build();

        // Get timer
        return internalTimer(trigger, info, false, null, null);
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

        // Create the trigger (repeat for ever)
        Trigger trigger = TriggerBuilder.newTrigger().withIdentity(getTriggerName(), getTriggerGroupName()).startAt(
                initialExpiration).withSchedule(
                SimpleScheduleBuilder.simpleSchedule().withIntervalInMilliseconds(intervalDuration).repeatForever()).build();

        // Get timer
        return internalTimer(trigger, info, false, null, null);
    }


    /**
     * Create a single-action timer that expires after a specified duration.
     * @param duration the number of milliseconds that must elapse before the timer expires.
     * @param timerConfig timer configuration.
     * @return the newly created Timer.
     * @throws IllegalArgumentException If duration is negative
     * @throws IllegalStateException If this method is invoked while the instance is in a state that does not allow
     * access to this method.
     * @throws EJBException If this method fails due to a system-level failure.
     * @since EJB 3.1 version.
     */
    public Timer createSingleActionTimer(final long duration, final TimerConfig timerConfig) throws IllegalArgumentException,
            IllegalStateException, EJBException {

        if (duration < 0) {
            throw new IllegalArgumentException("Invalid duration (negative)");
        }

        // Disallowed from stateful
        if (this.factory instanceof IStatefulSessionFactory) {
            throw new IllegalStateException("The createTimer() method cannot be called from a stateful session bean.");
        }

        return createSingleActionTimer(new Date(System.currentTimeMillis() + duration), timerConfig);
    }

    /**
     * Create a single-action timer that expires at a given point in time.
     * @param expiration the point in time at which the timer must expire.
     * @param timerConfig timer configuration.
     * @return the newly created Timer.
     * @throws IllegalArgumentException If expiration is null or expiration.getTime() is negative.
     * @throws IllegalStateException If this method is invoked while the instance is in a state that does not allow access to this
     * method.
     * @throws EJBException If this method could not complete due to a system-level failure.
     * @since EJB 3.1 version.
     */
    public Timer createSingleActionTimer(final Date expiration, final TimerConfig timerConfig) throws IllegalArgumentException,
            IllegalStateException, EJBException {

        if (expiration == null || expiration.getTime() < 0) {
            throw new IllegalArgumentException("Invalid Expiration date");
        }

        // Disallowed from stateful
        if (this.factory instanceof IStatefulSessionFactory) {
            throw new IllegalStateException("The createTimer() method cannot be called from a stateful session bean.");
        }

        Serializable info = timerConfig.getInfo();
        boolean isPersistent = timerConfig.isPersistent();


        // Create the trigger that won't repeat
        Trigger trigger = TriggerBuilder.newTrigger().withIdentity(getTriggerName(), getTriggerGroupName()).startAt(
                expiration).build();

        // Get timer
        return internalTimer(trigger, info, isPersistent, null, null);
    }

    /**
     * Create an interval timer whose first expiration occurs after a specified duration, and whose subsequent expirations occur
     * after a specified interval.
     * @param initialDuration The number of milliseconds that must elapse before the first timer expiration notification.
     * @param intervalDuration The number of milliseconds that must elapse between timer expiration notifications. Expiration
     * notifications are scheduled relative to the time of the first expiration. If expiration is delayed (e.g. due to the
     * interleaving of other method calls on the bean), two or more expiration notifications may occur in close succession to
     * "catch up".
     * @param timerConfig timer configuration
     * @return the newly created Timer.
     * @throws IllegalArgumentException If initialDuration is negative or intervalDuration is negative.
     * @throws IllegalStateException If this method is invoked while the instance is in a state that does not allow access to this
     * method.
     * @throws EJBException If this method could not complete due to a system-level failure.
     * @since EJB 3.1 version.
     */
    public Timer createIntervalTimer(final long initialDuration, final long intervalDuration, final TimerConfig timerConfig)
            throws IllegalArgumentException, IllegalStateException, EJBException {

        if (initialDuration < 0) {
            throw new IllegalArgumentException("Invalid initial duration (negative");
        }

        if (intervalDuration < 0) {
            throw new IllegalArgumentException("Invalid duration is negative");
        }

        // Disallowed from stateful
        if (this.factory instanceof IStatefulSessionFactory) {
            throw new IllegalStateException("The createTimer() method cannot be called from a stateful session bean.");
        }

        // Compute start date for initial expiration
        Date initialExpiration = new Date(System.currentTimeMillis() + initialDuration);

        // Use the method using Date parameter
        return createIntervalTimer(initialExpiration, intervalDuration, timerConfig);

    }

    /**
     * Create an interval timer whose first expiration occurs at a given point in time and whose subsequent expirations occur
     * after a specified interval.
     * @param initialExpiration the point in time at which the first timer expiration must occur.
     * @param intervalDuration the number of milliseconds that must elapse between timer expiration notifications. Expiration
     * notifications are scheduled relative to the time of the first expiration. If expiration is delayed (e.g. due to the
     * interleaving of other method calls on the bean), two or more expiration notifications may occur in close succession to
     * "catch up".
     * @param timerConfig timer configuration.
     * @return the newly created Timer.
     * @throws IllegalArgumentException If initialExpiration is null, if initialExpiration.getTime() is negative, or if
     * intervalDuration is negative.
     * @throws IllegalStateException If this method is invoked while the instance is in a state that does not allow access to
     * this method.
     * @throws EJBException If this method could not complete due to a system-level failure.
     * @since EJB 3.1 version.
     */
    public Timer createIntervalTimer(final Date initialExpiration, final long intervalDuration, final TimerConfig timerConfig)
            throws IllegalArgumentException, IllegalStateException, EJBException {

        if (initialExpiration == null || initialExpiration.getTime() < 0) {
            throw new IllegalArgumentException("Invalid Expiration date");
        }

        if (intervalDuration < 0) {
            throw new IllegalArgumentException("Invalid duration is negative");
        }

        // Disallowed from stateful
        if (this.factory instanceof IStatefulSessionFactory) {
            throw new IllegalStateException("The createTimer() method cannot be called from a stateful session bean.");
        }

        Serializable info = timerConfig.getInfo();
        boolean isPersistent = timerConfig.isPersistent();

        // Create the trigger (repeat for ever)
        Trigger trigger = TriggerBuilder.newTrigger().withIdentity(getTriggerName(), getTriggerGroupName()).startAt(
                initialExpiration).withSchedule(
                SimpleScheduleBuilder.simpleSchedule().withIntervalInMilliseconds(intervalDuration).repeatForever()).build();

        // Get timer
        return internalTimer(trigger, info, isPersistent, null, null);
    }


    /**
     * Create a calendar-based timer based on the input schedule expression.
     * @param schedule a schedule expression describing the timeouts for this timer.
     * @return the newly created Timer.
     * @throws IllegalArgumentException If Schedule represents an invalid schedule expression.
     * @throws IllegalStateException If this method is invoked while the instance is in a state that does not allow access to this
     * method.
     * @throws EJBException If this method could not complete due to a system-level failure.
     * @since EJB 3.1 version.
     */
    public Timer createCalendarTimer(final ScheduleExpression schedule) throws IllegalArgumentException, IllegalStateException,
            EJBException {
        return createCalendarTimer(schedule, null);
    }

    /**
     * Create a calendar-based timer based on the input schedule expression.
     * @param schedule a schedule expression describing the timeouts for this timer.
     * @param timerConfig timer configuration.
     * @return the newly created Timer.
     * @throws IllegalArgumentException If Schedule represents an invalid schedule expression.
     * @throws IllegalStateException If this method is invoked while the instance is in a state that does not allow access to this
     * method.
     * @throws EJBException If this method could not complete due to a system-level failure.
     * @since EJB 3.1 version.
     */
    public Timer createCalendarTimer(final ScheduleExpression schedule, final TimerConfig timerConfig)
            throws IllegalArgumentException, IllegalStateException, EJBException {
        return createCalendarTimer(schedule, timerConfig, null);

    }

    /**
     * Create a calendar-based timer based on the input schedule expression.
     * @param schedule a schedule expression describing the timeouts for this timer.
     * @param timerConfig timer configuration.
     * @param methodInfo the method used to call when timer has timeout
     * @return the newly created Timer.
     * @throws IllegalArgumentException If Schedule represents an invalid schedule expression.
     * @throws IllegalStateException If this method is invoked while the instance is in a state that does not allow access to this
     * method.
     * @throws EJBException If this method could not complete due to a system-level failure.
     * @since EJB 3.1 version.
     */
    public Timer createCalendarTimer(final ScheduleExpression schedule, final TimerConfig timerConfig,
            final IMethodInfo methodInfo) throws IllegalArgumentException, IllegalStateException, EJBException {

        TriggerBuilder<EasyBeansScheduleTrigger> triggerBuilder = TriggerBuilder.newTrigger()
        .withIdentity(getTriggerName(), getTriggerGroupName())
        .withSchedule(new EasyBeansScheduleBuilder(schedule));

        // Update start and stop
        if (schedule.getStart() != null) {
            triggerBuilder = triggerBuilder.startAt(schedule.getStart());
        }

        if (schedule.getEnd() != null) {
            Date end = schedule.getEnd();
            Date compare = new Date();
            if (schedule.getStart() != null) {
                compare = schedule.getStart();
            }
            if (end.after(compare)) {
                triggerBuilder = triggerBuilder.endAt(schedule.getEnd());
            }
        }

        // build the trigger
        EasyBeansScheduleTrigger trigger = triggerBuilder.build();



        Serializable info = null;
        boolean isPersistent = true;
        if (timerConfig != null) {
            info = timerConfig.getInfo();
            isPersistent = timerConfig.isPersistent();
        }

        if ("".equals(info)) {
            info = null;
        }


        EasyBeansTimer timer =  (EasyBeansTimer) internalTimer(trigger, info, isPersistent, methodInfo, schedule);

        return timer;
    }

    /**
     * Create a timer object that is sent to the client. Also, create a new job
     * and send it to the Quartz Scheduler.
     * @param trigger the object containing the data for the scheduling.
     * @param info the optional serializable object given by the developer.
     * @param isPersistent true if the timer needs to be persistent
     * @param methodInfo the method used as timer callback
     * @param scheduleExpression the EJB Schedule expression
     * @return a Timer object.
     */
    private Timer internalTimer(final Trigger trigger, final Serializable info, final boolean isPersistent,
            final IMethodInfo methodInfo, final ScheduleExpression scheduleExpression) {

        // Add stuff into the data of the job detail
        EasyBeansJobDetailData beansJobDetailData = new EasyBeansJobDetailData();
        beansJobDetailData.setInfo(info);

        // Options for finding the factory again
        Integer easyBeansServerID = this.factory.getContainer().getConfiguration().getEZBServer().getID();
        beansJobDetailData.setEasyBeansServerID(easyBeansServerID);
        beansJobDetailData.setContainerId(this.factory.getContainer().getId());
        if (scheduleExpression != null) {
            beansJobDetailData.setScheduleExpression(scheduleExpression);
        }
        beansJobDetailData.setPersistent(isPersistent);


        String factoryName = null;
        IBeanInfo beanInfo = this.factory.getBeanInfo();
        if (beanInfo == null) {
            factoryName = this.factory.getClassName();
        } else {
            factoryName = this.factory.getBeanInfo().getName();
        }

        beansJobDetailData.setFactoryName(factoryName);

        beansJobDetailData.setMethodInfo(methodInfo);

        // Build the Job Detail
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("data", beansJobDetailData);
        JobDetail jobDetail = JobBuilder.newJob(EasyBeansJob.class)
                .withIdentity(getNewJobDetailName(), getJobDetailGroupName()).usingJobData(jobDataMap).build();

        // Build a new timer object
        EasyBeansTimer timer = new EasyBeansTimer(jobDetail, trigger, this.scheduler, this.factory);
        if (trigger instanceof EasyBeansScheduleTrigger) {
            ((EasyBeansScheduleTrigger) trigger).setEasyBeansTimer(timer);
        }


        // Add it as a data
        beansJobDetailData.setTimer(timer);

        // Schedule the job
        try {
            this.scheduler.scheduleJob(jobDetail, trigger);
        } catch (SchedulerException e) {
            LOGGER.error("Cannot schedule the given job ''{0}''.", jobDetail, e);
            timer.setInvalid();
            return timer;
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
        //maybe was for EJB 3.0 : LIFECYCLE_CALLBACK_INTERCEPTOR == operationState
        if (DEPENDENCY_INJECTION == operationState || AFTER_COMPLETION == operationState) {
            throw new IllegalStateException(
                    "The getTimers() method cannot be called within the operation state '" + operationState
                            + "'.");
        }

        Collection<Timer> timers = new ArrayList<Timer>();

        // Get the list of job names for this group
        Set<JobKey> jobKeys = null;
        try {
            jobKeys = this.scheduler.getJobKeys(GroupMatcher.jobGroupEquals(getJobDetailGroupName()));
        } catch (SchedulerException e) {
            throw new EJBException("Unable to get the job names from the scheduler for the group named '"
                    + getJobDetailGroupName() + "'.", e);
        }

        // If there are jobs, get the detail and trigger
        if (jobKeys != null) {
            // For each job key
            for (JobKey jobKey : jobKeys) {

                // Get detail
                JobDetail jobDetail = null;
                try {
                    jobDetail = this.scheduler.getJobDetail(jobKey);
                } catch (SchedulerException e) {
                    throw new EJBException("Cannot get the jobDetail for the jobKey '" + jobKey + "'.", e);
                }

                // Get triggers
                List<? extends Trigger> triggers = null;

                try {
                    triggers = this.scheduler.getTriggersOfJob(jobKey);
                } catch (SchedulerException e) {
                   throw new EJBException("Cannot get triggers for the job named '" +  jobKey + "'.", e);
                }

                // Should be only once trigger per job
                if (triggers == null || triggers.size() > 1) {
                    throw new EJBException("Invalid numbers of triggers found for the job named '" +  jobKey + "'.");
                }

                // Build a timer object and return it
                timers.add(new EasyBeansTimer(jobDetail, triggers.get(0), this.scheduler, this.factory));
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
