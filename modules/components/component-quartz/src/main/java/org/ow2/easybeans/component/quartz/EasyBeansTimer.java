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
 * $Id: EasyBeansTimer.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.component.quartz;

import static org.ow2.easybeans.api.OperationState.AFTER_COMPLETION;
import static org.ow2.easybeans.api.OperationState.DEPENDENCY_INJECTION;
import static org.ow2.easybeans.api.OperationState.LIFECYCLE_CALLBACK_INTERCEPTOR;

import java.io.Serializable;
import java.util.Date;

import javax.ejb.EJBException;
import javax.ejb.NoSuchObjectLocalException;
import javax.ejb.Timer;
import javax.ejb.TimerHandle;

import org.ow2.easybeans.api.Factory;
import org.ow2.easybeans.api.OperationState;
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
    private EasyBeansJobDetail jobDetail = null;

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
     * Create a new Timer object with the given objects (job, trigger and
     * scheduler).
     * @param jobDetail the given job used to cancel the timer or in order to
     *        get Serializable info
     * @param trigger the trigger used to get the next fire
     * @param scheduler for canceling jobs
     * @param factory optional factory that is creating this timer
     */
    public EasyBeansTimer(final EasyBeansJobDetail jobDetail, final Trigger trigger, final Scheduler scheduler,
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
        if (DEPENDENCY_INJECTION == operationState || LIFECYCLE_CALLBACK_INTERCEPTOR == operationState
                || AFTER_COMPLETION == operationState) {
            throw new IllegalStateException("The cancel() method cannot be called within the operation state '"
                    + operationState + "'.");
        }

        // Delete job that has been registered by timer service
        try {
            this.scheduler.deleteJob(this.jobDetail.getName(), this.jobDetail.getGroup());
        } catch (SchedulerException e) {
            throw new EJBException("Cannot cancel job with name '" + this.jobDetail.getName() + "'.", e);
        }
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
        if (DEPENDENCY_INJECTION == operationState || LIFECYCLE_CALLBACK_INTERCEPTOR == operationState
                || AFTER_COMPLETION == operationState) {
            throw new IllegalStateException("The getTimeRemaining() method cannot be called within the operation state '"
                    + operationState + "'.");
        }

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
        if (DEPENDENCY_INJECTION == operationState || LIFECYCLE_CALLBACK_INTERCEPTOR == operationState
                || AFTER_COMPLETION == operationState) {
            throw new IllegalStateException("The getNextTimeout() method cannot be called within the operation state '"
                    + operationState + "'.");
        }

        // Get next timeout
        Date date = this.trigger.getNextFireTime();

        // May be null (trigger will not fire again)
        if (date == null) {
            throw new IllegalStateException("No next timeout for this timer");
        }

        // return the date
        return date;
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
        if (DEPENDENCY_INJECTION == operationState || LIFECYCLE_CALLBACK_INTERCEPTOR == operationState
                || AFTER_COMPLETION == operationState) {
            throw new IllegalStateException("The getInfo() method cannot be called within the operation state '"
                    + operationState + "'.");
        }

        // Get info from the data of the job detail
        return this.jobDetail.getJobDetailData().getInfo();
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
        if (DEPENDENCY_INJECTION == operationState || LIFECYCLE_CALLBACK_INTERCEPTOR == operationState
                || AFTER_COMPLETION == operationState) {
            throw new IllegalStateException("The getHandle() method cannot be called within the operation state '"
                    + operationState + "'.");
        }

        return new EasyBeansTimerHandle(this.jobDetail);
    }

}
