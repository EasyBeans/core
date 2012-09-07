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
 * $Id: EasyBeansTimer.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.component.quartz;

import static org.ow2.easybeans.api.OperationState.AFTER_COMPLETION;
import static org.ow2.easybeans.api.OperationState.DEPENDENCY_INJECTION;

import java.io.Serializable;
import java.util.Date;

import javax.ejb.EJBException;
import javax.ejb.NoSuchObjectLocalException;
import javax.ejb.ScheduleExpression;
import javax.ejb.Timer;
import javax.ejb.TimerHandle;

import org.ow2.easybeans.api.Factory;
import org.ow2.easybeans.api.OperationState;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;

/**
 * Implementation of the Timer interface of EJB specification.
 * @author Florent Benoit
 */
public class EasyBeansTimer implements Timer {

    /**
     * Job Detail for this timer.
     */
    private JobDetail jobDetail = null;

    /**
     * Trigger used by this timer.
     */
    private Trigger trigger = null;

    /**
     * Scheduler used for this timer.
     */
    private Scheduler scheduler = null;

    /**
     * Factory that has created the instance.
     */
    private Factory<?, ?> factory = null;


    /**
     * This timer has been cancelled ?
     */
    private boolean cancelled = false;

    /**
     * Create a new Timer object with the given objects (job, trigger and
     * scheduler).
     * @param jobDetail the given job used to cancel the timer or in order to
     *        get Serializable info
     * @param trigger the trigger used to get the next fire
     * @param scheduler for canceling jobs
     * @param factory optional factory that is creating this timer
     */
    public EasyBeansTimer(final JobDetail jobDetail, final Trigger trigger, final Scheduler scheduler,
            final Factory<?, ?> factory) {
        this.jobDetail = jobDetail;
        this.trigger = trigger;
        this.scheduler = scheduler;
        this.factory = factory;
    }

    /**
     * Cause the timer and all its associated expiration notifications to be
     * cancelled.
     * @throws IllegalStateException If this method is invoked while the
     *         instance is in a state that does not allow access to this method.
     * @throws NoSuchObjectLocalException If invoked on a timer that has expired
     *         or has been cancelled.
     * @throws EJBException If this method could not complete due to a
     *         system-level failure.
     */
    public void cancel() throws IllegalStateException, NoSuchObjectLocalException, EJBException {
        OperationState operationState = this.factory.getOperationState();
        if (DEPENDENCY_INJECTION == operationState || AFTER_COMPLETION == operationState) {
            throw new IllegalStateException("The cancel() method cannot be called within the operation state '"
                    + operationState + "'.");
        }

        checkNotCancelled();

        // Delete job that has been registered by timer service
        try {
            this.scheduler.deleteJob(this.jobDetail.getKey());
        } catch (SchedulerException e) {
            throw new EJBException("Cannot cancel job with name '" + this.jobDetail.getKey().getName() + "'.", e);
        }

        // Timer is cancelled
        this.cancelled = true;
    }

    /**
     * Get the number of milliseconds that will elapse before the next scheduled
     * timer expiration.
     * @return the number of milliseconds that will elapse before the next
     *         scheduled timer expiration.
     * @throws IllegalStateException If this method is invoked while the
     *         instance is in a state that does not allow access to this method.
     * @throws NoSuchObjectLocalException If invoked on a timer that has expired
     *         or has been cancelled.
     * @throws EJBException If this method could not complete due to a
     *         system-level failure.
     */
    public long getTimeRemaining() throws IllegalStateException, NoSuchObjectLocalException, EJBException {
        OperationState operationState = this.factory.getOperationState();
        if (DEPENDENCY_INJECTION == operationState || AFTER_COMPLETION == operationState) {
            throw new IllegalStateException("The getTimeRemaining() method cannot be called within the operation state '"
                    + operationState + "'.");
        }

        checkNotCancelled();


        // If there is no more next timeout, getNextTimeout() method will throw
        // IllegalStateException
        return getNextTimeout().getTime() - System.currentTimeMillis();

    }

    /**
     * Get the point in time at which the next timer expiration is scheduled to
     * occur.
     * @return the point in time at which the next timer expiration is scheduled
     *         to occur.
     * @throws IllegalStateException If this method is invoked while the
     *         instance is in a state that does not allow access to this method.
     * @throws NoSuchObjectLocalException If invoked on a timer that has expired
     *         or has been cancelled.
     * @throws EJBException If this method could not complete due to a
     *         system-level failure.
     */
    public Date getNextTimeout() throws IllegalStateException, NoSuchObjectLocalException, EJBException {
        OperationState operationState = this.factory.getOperationState();
        if (DEPENDENCY_INJECTION == operationState || AFTER_COMPLETION == operationState) {
            throw new IllegalStateException("The getNextTimeout() method cannot be called within the operation state '"
                    + operationState + "'.");
        }

        checkNotCancelled();


        // Get next timeout
        boolean noNextTimeout = false;
        Date nextFireTime = this.trigger.getNextFireTime();
        Date now = new Date();

        // do we have a date ?
        if (nextFireTime != null) {
            // if we've a date in the past or equals to now, check for a future date
            if (now.after(nextFireTime) || now.equals(nextFireTime)) {
                Date newTime = this.trigger.getFireTimeAfter(now);
                if (newTime != null && nextFireTime.equals(newTime)) {
                    // Well this has been ended
                    noNextTimeout = true;
                } else {
                    return newTime;
                }
            }
        }

        // May be null (trigger will not fire again)
        if (nextFireTime == null) {
            noNextTimeout = true;
        }

        if (noNextTimeout) {
            throw new NoSuchObjectLocalException("No next timeout for this timer");

        }

        // return the date
        return nextFireTime;
    }

    /**
     * Get the information associated with the timer at the time of creation.
     * @return The Serializable object that was passed in at timer creation, or
     *         null if the info argument passed in at timer creation was null.
     * @throws IllegalStateException If this method is invoked while the
     *         instance is in a state that does not allow access to this method.
     * @throws NoSuchObjectLocalException If invoked on a timer that has expired
     *         or has been cancelled.
     * @throws EJBException If this method could not complete due to a
     *         system-level failure.
     */
    public Serializable getInfo() throws IllegalStateException, NoSuchObjectLocalException, EJBException {
        OperationState operationState = this.factory.getOperationState();
        if (DEPENDENCY_INJECTION == operationState || AFTER_COMPLETION == operationState) {
            throw new IllegalStateException("The getInfo() method cannot be called within the operation state '"
                    + operationState + "'.");
        }

        checkNotCancelled();


        EasyBeansJobDetailData data = (EasyBeansJobDetailData) this.jobDetail.getJobDataMap().get("data");

        // Get info from the data of the job detail
        return data.getInfo();
    }

    /**
     * Get a serializable handle to the timer. This handle can be used at a
     * later time to re-obtain the timer reference.
     * @return a serializable handle to the timer.
     * @throws IllegalStateException If this method is invoked while the
     *         instance is in a state that does not allow access to this method.
     * @throws NoSuchObjectLocalException If invoked on a timer that has expired
     *         or has been cancelled.
     * @throws EJBException If this method could not complete due to a
     *         system-level failure.
     */
    public TimerHandle getHandle() throws IllegalStateException, NoSuchObjectLocalException, EJBException {
        OperationState operationState = this.factory.getOperationState();
        if (DEPENDENCY_INJECTION == operationState || AFTER_COMPLETION == operationState) {
            throw new IllegalStateException("The getHandle() method cannot be called within the operation state '"
                    + operationState + "'.");
        }

        checkNotCancelled();

        if (!isPersistent()) {
            throw new IllegalStateException("Cannot call getHandle on a non-persistent timer");
        }

        return new EasyBeansTimerHandle(this.jobDetail);
    }

    /**
     * Get the schedule expression corresponding to this timer.  The timer
     * must be a calendar-based timer.  It may have been created automatically
     * or programmatically.
     * @return schedule expression for the timer.
     * @throws IllegalStateException If this method is
     * invoked while the instance is in a state that does not allow access
     * to this method.  Also thrown if invoked on a timer that is not a
     * calendar-based timer.
     * @throws NoSuchObjectLocalException If invoked on a timer
     * that has expired or has been cancelled.
     * @throws EJBException If this method could not complete due
     * to a system-level failure.
     * @since EJB 3.1 version.
     */
    public ScheduleExpression getSchedule() throws IllegalStateException, NoSuchObjectLocalException, EJBException {

        checkNotCancelled();


        EasyBeansJobDetailData data = (EasyBeansJobDetailData) this.jobDetail.getJobDataMap().get("data");
        ScheduleExpression scheduleExpression = data.getScheduleExpression();

        if (scheduleExpression == null) {
            throw new IllegalStateException("Not a calendar based timer");
        }
        return scheduleExpression;
    }

    /**
     * Return whether this timer is a calendar-based timer.
     * @return boolean indicating whether the timer is calendar-based.
     * @throws java.lang.IllegalStateException If this method is invoked while the instance is in a state that does not allow
     * access to this method.
     * @throws javax.ejb.NoSuchObjectLocalException If invoked on a timer that has expired or has been cancelled.
     * @throws javax.ejb.EJBException If this method could not complete due to a system-level failure.
     * @since EJB 3.1 version.
     */
    public boolean isCalendarTimer() throws IllegalStateException, NoSuchObjectLocalException, EJBException {
        OperationState operationState = this.factory.getOperationState();
        if (DEPENDENCY_INJECTION == operationState || AFTER_COMPLETION == operationState) {
            throw new IllegalStateException("The getHandle() method cannot be called within the operation state '"
                    + operationState + "'.");
        }

        checkNotCancelled();


        EasyBeansJobDetailData data = (EasyBeansJobDetailData) this.jobDetail.getJobDataMap().get("data");
        return data.getScheduleExpression() != null;
        }

    /**
     * Return whether this timer has persistent semantics.
     * @return boolean indicating whether the timer is persistent.
     * @throws IllegalStateException If this method is invoked while the instance is in a state that does not allow access to this
     * method.
     * @throws NoSuchObjectLocalException If invoked on a timer that has expired or has been cancelled.
     * @throws EJBException If this method could not complete due to a system-level failure.
     * @since EJB 3.1 version.
     */
    public boolean isPersistent() throws IllegalStateException, NoSuchObjectLocalException, EJBException {
        checkNotCancelled();

        EasyBeansJobDetailData data = (EasyBeansJobDetailData) this.jobDetail.getJobDataMap().get("data");
        return data.isPersistent();
    }

    /**
     * Gets the trigger.
     * @return trigger
     */
    protected Trigger getTrigger() {
        return this.trigger;
    }

    /**
     * Checks that the current timer has not be cancelled else throws an exception.
     * @throws NoSuchObjectLocalException if the timer has been cancelled.
     */
    protected void checkNotCancelled() throws NoSuchObjectLocalException {
        if (this.cancelled) {
            throw new NoSuchObjectLocalException("This timer has been cancelled");
        }
    }


    /**
     * Flag this timer as being invalid from now.
     */
    public void setInvalid() {
        this.cancelled = true;
    }


    @Override
    public boolean equals(final Object other) {
        if (other == null || !(other instanceof EasyBeansTimer)) {
            return false;
        }

        EasyBeansTimer otherTimer = (EasyBeansTimer) other;
        return this.jobDetail.getKey().equals(otherTimer.jobDetail.getKey());

    }

    @Override
    public int hashCode() {
        return this.jobDetail.hashCode();
    }

}
