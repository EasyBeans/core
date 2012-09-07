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
 * $Id: QuartzComponent.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.component.quartz;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.ow2.easybeans.api.EZBTimerService;
import org.ow2.easybeans.api.Factory;
import org.ow2.easybeans.component.api.EZBComponentException;
import org.ow2.easybeans.component.itf.TimerComponent;
import org.ow2.easybeans.component.util.Property;
import org.ow2.easybeans.component.util.TimerCallback;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.matchers.GroupMatcher;

/**
 * This component starts the Quartz framework and configure it.
 * It is also providing the Scheduler that EJB timer will use for their use.
 *
 * @author Florent Benoit
 */
public class QuartzComponent implements TimerComponent {

    /**
     * Quartz scheduler shared by all Timer services.
     */
    private Scheduler scheduler = null;

    /**
     * Properties for the scheduler. These properties should have been set before the init of the scheduler.
     */
    private List<Property> quartzProperties = null;


    /**
     * Quartz scheduler Factory.
     */
    private SchedulerFactory schedulerFactory = null;

    /**
     * Init method.<br/>
     * This method is called before the start method.
     *
     * @throws EZBComponentException if the initialization has failed.
     */
    public void init() throws EZBComponentException {

        // Get properties
        Properties schedulerProperties = new Properties();
        if (this.quartzProperties != null) {
            for (Property property : this.quartzProperties) {
                schedulerProperties.put(property.getName(), property.getValue());
            }
        }


        // Initialize the Quartz scheduler Factory
        this.schedulerFactory = null;
        try {
            this.schedulerFactory  = new StdSchedulerFactory(schedulerProperties);
        } catch (SchedulerException e) {
            throw new EZBComponentException("Cannot initialize the Scheduler factory", e);
        }
    }


    /**
     * Start method.<br/>
     * This method is called after the init method.
     *
     * @throws EZBComponentException if the start has failed.
     */
    public void start() throws EZBComponentException {


        // Build a Scheduler
        try {
            this.scheduler = this.schedulerFactory.getScheduler();
        } catch (SchedulerException e) {
            throw new EZBComponentException("Cannot get a scheduler from the factory", e);
        }

        // Start the scheduler
        try {
            this.scheduler.start();
        } catch (SchedulerException e) {
            throw new EZBComponentException("Cannot start the scheduler", e);
        }
    }


    /**
     * Stop method.<br/>
     * This method is called when component needs to be stopped.
     *
     * @throws EZBComponentException if the stop is failing.
     */
    public void stop() throws EZBComponentException {
        // Stop the scheduler
        try {
            this.scheduler.shutdown();
        } catch (SchedulerException e) {
            throw new EZBComponentException("Cannot stop the scheduler", e);
        }
    }


    /**
     * Gets an EJB timer service through this component.
     *
     * @param factory an EasyBeans factory providing timeout notification.
     *
     * @return an EJB timer service
     */
    public EZBTimerService getTimerService(final Factory<?, ?> factory) {
        return new QuartzTimerService(factory, this.scheduler);
    }

    /**
     * Schedule a recurring call to a {@link TimerCallback} starting immediately.
     *
     * @param id                 if there is already a Callback scheduled with the same id then an exception is thrown
     * @param interval           Recurrence interval in milliseconds
     * @param callback           The {@link TimerCallback} to notify
     * @param callbackProperties Properties to be send to the {@link TimerCallback#execute(java.util.Map)} method
     */
    public void schedule(String id, long interval, TimerCallback callback, Map<String, Object> callbackProperties)
            throws EZBComponentException {
        try {
            if (this.getScheduler().getTriggerKeys(GroupMatcher.triggerGroupContains(CallbackJobDetailData.JOB_GROUP_NAME)).size() != 0) {
                throw new EZBComponentException("An existing TimerCallback with the same id is already registered");
            }
            JobDataMap jobDataMap = new JobDataMap();
            jobDataMap.put(CallbackJobDetailData.CALLBACK_DATA_KEY, new CallbackJobDetailData(callback, callbackProperties));
            JobDetail jobDetail = JobBuilder.newJob(CallbackJob.class).usingJobData(jobDataMap).
                    withIdentity(JobKey.jobKey(id, CallbackJobDetailData.JOB_GROUP_NAME)).build();

            Trigger trigger = TriggerBuilder.newTrigger().withIdentity(TriggerKey.triggerKey(id, CallbackJobDetailData.JOB_GROUP_NAME))
                    .forJob(jobDetail).startNow()
                    .withSchedule(SimpleScheduleBuilder.simpleSchedule().withIntervalInMilliseconds(interval).repeatForever()).build();

            this.getScheduler().scheduleJob(jobDetail, trigger);
        } catch (SchedulerException e) {
            throw new EZBComponentException("Unable to schedule TimeCallback " + id + " with a repeat interval of " + interval, e);
        }
    }

    /**
     * Unschedule a callback timer for a given id
     *
     * @param id the scheduled timer id
     */
    public void unschedule(String id) throws EZBComponentException {
        try {
            this.getScheduler().deleteJob(JobKey.jobKey(id, CallbackJobDetailData.JOB_GROUP_NAME));
        } catch (SchedulerException e) {
            throw new EZBComponentException("Unable to unschedule job " + id, e);
        }
    }

    /**
     * Gets the list of properties.
     *
     * @return the list of properties.
     */
    public List<Property> getProperties() {
        return this.quartzProperties;
    }

    /**
     * Set the list of properties.
     *
     * @param quartzProperties the list of properties.
     */
    public void setProperties(final List<Property> quartzProperties) {
        this.quartzProperties = quartzProperties;
    }

    /**
     * Gets the Quartz scheduler.
     *
     * @return the Quartz scheduler.
     */
    public Scheduler getScheduler() {
        return this.scheduler;
    }


}
