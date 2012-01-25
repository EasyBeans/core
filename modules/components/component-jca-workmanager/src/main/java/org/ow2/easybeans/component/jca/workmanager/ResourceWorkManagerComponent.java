/**
 * EasyBeans
 * Copyright (C) 2006-2010 Bull S.A.S.
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
 * $Id: ResourceWorkManagerComponent.java 5479 2010-04-28 14:57:16Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.component.jca.workmanager;

import java.util.LinkedList;

import javax.resource.spi.work.ExecutionContext;
import javax.resource.spi.work.Work;
import javax.resource.spi.work.WorkCompletedException;
import javax.resource.spi.work.WorkEvent;
import javax.resource.spi.work.WorkException;
import javax.resource.spi.work.WorkListener;
import javax.resource.spi.work.WorkManager;
import javax.resource.spi.work.WorkRejectedException;
import javax.transaction.NotSupportedException;
import javax.transaction.SystemException;
import javax.transaction.xa.Xid;

import org.ow2.easybeans.component.api.EZBComponentException;
import org.ow2.easybeans.component.itf.EZBWorkManagerComponent;
import org.ow2.easybeans.component.itf.TMComponent;
import org.ow2.util.log.Log;
import org.ow2.util.log.LogFactory;

/**
 * Implementation of the Resource Work Manager API.
 * @author Philippe Durieux (JOnAS)
 * @author Florent Benoit (EasyBeans)
 */
public class ResourceWorkManagerComponent implements EZBWorkManagerComponent, WorkManager {

    /**
     * MilliSeconds value.
     */
    public static final long ONE_SECOND = 1000L;

    /**
     * Default waiting time.
     */
    private static final long DEFAULT_WAIT_TIME = 60 * ONE_SECOND;

    /**
     * Default min threads.
     */
    private static final int DEFAULT_MIN = 5;

    /**
     * Default max threads.
     */
    private static final int DEFAULT_MAX = 100;

    /**
     * Logger.
     */
    private static Log logger = LogFactory.getLog(ResourceWorkManagerComponent.class);

    /**
     * List of ResourceWork (which wrap Work object).
     */
    private LinkedList<ResourceWork> workList = new LinkedList<ResourceWork>();

    /**
     * Identifier of this pool.
     */
    private static int poolnumber = 0;

    /**
     * Thread number (when building ResourceWorkThread, it assigns a new thread
     * number).
     */
    private static int threadnumber = 0;

    /**
     * The maximum size of the pool.
     */
    private int maxpoolsz = DEFAULT_MAX;

    /**
     * The minimum size of the pool.
     */
    private int minpoolsz = DEFAULT_MIN;

    /**
     * The current size of thread pool.
     */
    private int poolsz;

    /**
     * Threads that are ready to work.
     */
    private int freeThreads = 0;

    /**
     * The time to wait (in millisec).
     */
    private long waitingTime = DEFAULT_WAIT_TIME;

    /**
     * Pool status : by default, it is not stopped.
     */
    private boolean stopped = false;

    /**
     * Wait few more seconds when waiting.
     */
    private static final long FEW_MORE_SECONDS = 3000;

    /**
     * Transaction component.
     */
    private TMComponent transactionComponent = null;

    /**
     * Default Constructor.
     */
    public ResourceWorkManagerComponent() {
        // new identifier
        poolnumber++;
    }

    /**
     * @return current pool size
     */
    public int getCurrentPoolSize() {
        return this.poolsz;
    }

    /**
     * @return min pool size
     */
    public int getMinPoolSize() {
        return this.minpoolsz;
    }

    /**
     * @return max pool size
     */
    public int getMaxPoolSize() {
        return this.maxpoolsz;
    }

    /**
     * Sets the min pool size.
     * @param minsz the min pool size.
     */
    public void setMinPoolSize(final int minsz) {
        this.minpoolsz = minsz;
    }

    /**
     * Sets the max pool size.
     * @param maxsz the max pool size.
     */
    public void setMaxPoolSize(final int maxsz) {
        this.maxpoolsz = maxsz;
    }

    /**
     * Accepts a Work instance for processing. This call blocks until the Work
     * instance completes execution. There is no guarantee on when the accepted
     * Work instance would start execution ie., there is no time constraint to
     * start execution.
     * @param work The unit of work to be done. Could be long or short-lived.
     * @throws WorkRejectedException a Work instance has been rejected from
     *         further processing.
     * @throws WorkCompletedException a Work instance has completed execution
     *         with an exception.
     * @throws WorkException if work is not done
     */
    public void doWork(final Work work) throws WorkRejectedException, WorkCompletedException, WorkException {
        doMyWork(work, INDEFINITE, null, null, 0);
    }

    /**
     * Accepts a Work instance for processing. This call blocks until the Work
     * instance completes execution.
     * @param work The unit of work to be done. Could be long or short-lived.
     * @param timeout a time duration (in milliseconds) within which the
     *        execution of the Work instance must start. Otherwise, the Work
     *        instance is rejected with a WorkRejectedException set to an
     *        appropriate error code (WorkRejectedException.TIMED_OUT).
     * @param executionContext an object containing the execution context with
     *        which the submitted Work instance must be executed.
     * @param workListener an object which would be notified when the various
     *        Work processing events (work accepted, work rejected, work
     *        started, work completed) occur.
     * @throws WorkRejectedException a Work instance has been rejected from
     *         further processing.
     * @throws WorkCompletedException a Work instance has completed execution
     *         with an exception.
     * @throws WorkException if work is not done
     */
    public void doWork(final Work work, final long timeout, final ExecutionContext executionContext,
            final WorkListener workListener) throws WorkRejectedException, WorkCompletedException, WorkException {
        if (workListener != null) {
            workListener.workAccepted(new WorkEvent(this, WorkEvent.WORK_ACCEPTED, work, null));
        }
        doMyWork(work, timeout, executionContext, workListener, System.currentTimeMillis());
    }

    /**
     * Accepts a Work instance for processing. This call blocks until the Work
     * instance starts execution but not until its completion. There is no
     * guarantee on when the accepted Work instance would start execution ie.,
     * there is no time constraint to start execution.
     * @param work The unit of work to be done. Could be long or short-lived.
     * @return the time elapsed (in milliseconds) from Work acceptance until
     *         start of execution. Note, this does not offer real-time
     *         guarantees. It is valid to return -1, if the actual start delay
     *         duration is unknown.
     * @throws WorkRejectedException a Work instance has been rejected from
     *         further processing.
     * @throws WorkException if work is not started
     */
    public long startWork(final Work work) throws WorkRejectedException, WorkException {
        return startWork(work, INDEFINITE, null, null);
    }

    /**
     * Accepts a Work instance for processing. This call blocks until the Work
     * instance starts execution but not until its completion. There is no
     * guarantee on when the accepted Work instance would start execution ie.,
     * there is no time constraint to start execution.
     * @param work The unit of work to be done. Could be long or short-lived.
     * @param timeout a time duration (in milliseconds) within which the
     *        execution of the Work instance must start. Otherwise, the Work
     *        instance is rejected with a WorkRejectedException set to an
     *        appropriate error code (WorkRejectedException.TIMED_OUT).
     * @param executionContext an object containing the execution context with
     *        which the submitted Work instance must be executed.
     * @param workListener an object which would be notified when the various
     *        Work processing events (work accepted, work rejected, work
     *        started, work completed) occur.
     * @return the time elapsed (in milliseconds) from Work acceptance until
     *         start of execution. Note, this does not offer real-time
     *         guarantees. It is valid to return -1, if the actual start delay
     *         duration is unknown.
     * @throws WorkRejectedException a Work instance has been rejected from
     *         further processing.
     * @throws WorkException if work is not started
     */
    public long startWork(final Work work, final long timeout, final ExecutionContext executionContext,
            final WorkListener workListener) throws WorkRejectedException, WorkException {

        ResourceWork resourceWork = new ResourceWork(work, timeout, executionContext, workListener);
        if (workListener != null) {
            workListener.workAccepted(new WorkEvent(this, WorkEvent.WORK_ACCEPTED, work, null));
        }
        long starttime = System.currentTimeMillis();
        long duration = 0;
        synchronized (this.workList) {
            this.workList.add(resourceWork);
            if (this.poolsz < this.maxpoolsz && this.workList.size() > this.freeThreads) {
                // We need one more thread.
                this.poolsz++;
                ResourceWorkThread resourceWorkThread = new ResourceWorkThread(this, threadnumber++, poolnumber);
                resourceWorkThread.start();
            } else {
                this.workList.notify();
            }
        }
        // Wait until my work is started.
        boolean started = false;
        synchronized (resourceWork) {
            if (!resourceWork.isStarted()) {
                try {
                    // No need to wait after timeout is elapsed
                    long waittime = this.waitingTime;
                    if (timeout < waittime) {
                        waittime = timeout + FEW_MORE_SECONDS;
                    }
                    resourceWork.wait(waittime);
                } catch (InterruptedException e) {
                    throw new WorkRejectedException("Interrupted");
                }
            }
            started = resourceWork.isStarted();
        }
        duration = System.currentTimeMillis() - starttime;
        if (!started) {
            synchronized (this.workList) {
                // Remove the work in the list
                if (!this.workList.remove(resourceWork)) {
                    logger.debug("Cannot remove the work");
                }
                throw new WorkRejectedException(WorkException.START_TIMED_OUT);
            }
        }
        return duration;
    }

    /**
     * Accepts a Work instance for processing. This call does not block and
     * returns immediately once a Work instance has been accepted for
     * processing. There is no guarantee on when the submitted Work instance
     * would start execution ie., there is no time constraint to start
     * execution.
     * @param work The unit of work to be done. Could be long or short-lived.
     * @throws WorkRejectedException - indicates that a Work instance has been
     *         rejected from further processing. This can occur due to internal
     *         factors.
     * @throws WorkException if work is not scheduled.
     */
    public void scheduleWork(final Work work) throws WorkRejectedException, WorkException {
        scheduleWork(work, INDEFINITE, null, null);
    }

    /**
     * Accepts a Work instance for processing. This call does not block and
     * returns immediately once a Work instance has been accepted for
     * processing. There is no guarantee on when the submitted Work instance
     * would start execution ie., there is no time constraint to start
     * execution.
     * @param work The unit of work to be done. Could be long or short-lived.
     * @param timeout a time duration (in milliseconds) within which the
     *        execution of the Work instance must start. Otherwise, the Work
     *        instance is rejected with a WorkRejectedException set to an
     *        appropriate error code (WorkRejectedException.TIMED_OUT).
     * @param executionContext an object containing the execution context with
     *        which the submitted Work instance must be executed.
     * @param workListener an object which would be notified when the various
     *        Work processing events (work accepted, work rejected, work
     *        started, work completed) occur.
     * @throws WorkRejectedException a Work instance has been rejected from
     *         further processing.
     * @throws WorkException if work is not scheduled.
     */
    public void scheduleWork(final Work work, final long timeout, final ExecutionContext executionContext,
            final WorkListener workListener) throws WorkRejectedException, WorkException {

        ResourceWork resourceWork = new ResourceWork(work, timeout, executionContext, workListener);
        if (workListener != null) {
            workListener.workAccepted(new WorkEvent(this, WorkEvent.WORK_ACCEPTED, work, null));
        }
        synchronized (this.workList) {
            this.workList.add(resourceWork);
            if (this.poolsz < this.maxpoolsz && this.workList.size() > this.freeThreads) {
                // We need one more thread.
                this.poolsz++;
                ResourceWorkThread resourceWorkThread = new ResourceWorkThread(this, threadnumber++, poolnumber);
                resourceWorkThread.start();
            } else {
                // Just wake up a thread waiting for work.
                this.workList.notify();
            }
        }
    }

    /**
     * Internal method doing the work.
     * @param work The unit of work to be done. Could be long or short-lived.
     * @param timeout a time duration (in milliseconds) within which the
     *        execution of the Work instance must start. Otherwise, the Work
     *        instance is rejected with a WorkRejectedException set to an
     *        appropriate error code (WorkRejectedException.TIMED_OUT).
     * @param executionContext an object containing the execution context with
     *        which the submitted Work instance must be executed.
     * @param workListener an object which would be notified when the various
     *        Work processing events (work accepted, work rejected, work
     *        started, work completed) occur.
     * @param creationTime the date of the creation of the work
     * @throws WorkException if work is not performed.
     */
    @SuppressWarnings("boxing")
    private void doMyWork(final Work work, final long timeout, final ExecutionContext executionContext,
            final WorkListener workListener, final long creationTime) throws WorkException {

        // Notify the listener that the work is started or rejected by timeout.
        if (workListener != null) {
            long duration = System.currentTimeMillis() - creationTime;
            if (duration > timeout) {
                // This can occur only in case of scheduleWork
                logger.warn("REJECTED: duration= {0}", duration);
                workListener.workRejected(new WorkEvent(this, WorkEvent.WORK_REJECTED, work, null));
                return;
            }
            workListener.workStarted(new WorkEvent(this, WorkEvent.WORK_STARTED, work, null));
        }

        // Setup ExecutionContext
        Xid xid = null;
        if (executionContext != null) {
            xid = executionContext.getXid();
            if (xid != null) {
                long txtimeout = executionContext.getTransactionTimeout();
                try {
                    if (txtimeout != WorkManager.UNKNOWN) {
                        this.transactionComponent.begin(xid, txtimeout);
                    } else {
                        this.transactionComponent.begin(xid);
                    }
                } catch (NotSupportedException e) {
                    throw new WorkException("Error starting a new transaction", e);
                } catch (SystemException e) {
                    throw new WorkException("Error starting a new transaction", e);
                }
            }
        }

        try {
            work.run();
            // Notify the listener that the work is completed.
            if (workListener != null) {
                workListener.workCompleted(new WorkEvent(this, WorkEvent.WORK_COMPLETED, work, null));
            }
        } catch (Exception e) {
            if (workListener != null) {
                workListener.workCompleted(new WorkEvent(this, WorkEvent.WORK_COMPLETED, work, null));
            }
            throw new WorkCompletedException(e);
        } finally {
            if (xid != null) {
                this.transactionComponent.clearThreadTx();
            }
        }
    }

    /**
     * @return the waiting time.
     */
    public long getWaitingTime() {
        return this.waitingTime;
    }

    /**
     * Sets the waiting time.
     * @param waitingTime the time to wait
     */
    public void setWaitingTime(final long waitingTime) {
        this.waitingTime = waitingTime;
    }

    /**
     * Do the next JWork object to be run.
     * @throws WorkException if work is not done
     * @throws InterruptedException if one object can't wait.
     * @throws ResourceWorkManagerStoppedException if the manager is stopped.
     */
    public void nextWork() throws WorkException, InterruptedException, ResourceWorkManagerStoppedException {
        ResourceWork run = null;
        boolean haswait = false;
        synchronized (this.workList) {
            while (this.workList.isEmpty()) {
                if ((haswait && this.freeThreads > this.minpoolsz) || this.stopped) {
                    this.poolsz--;
                    throw new ResourceWorkManagerStoppedException("Manager is stopped");
                }
                try {
                    this.freeThreads++;
                    this.workList.wait(this.waitingTime);
                    this.freeThreads--;
                    haswait = true;
                } catch (InterruptedException e) {
                    this.freeThreads--;
                    this.poolsz--;
                    throw e;
                }
            }
            run = this.workList.removeFirst();
            // In case startWork() was called
            synchronized (run) {
                logger.debug("Starting a new work");
                run.setStarted();
                run.notify();
            }
        }
        doMyWork(run.getWork(), run.getTimeout(), run.getExecutionContext(), run.getWorkListener(), run
                .getCreationTime());
    }

    /**
     * Remove this WorkManager : Stop all threads.
     */
    public synchronized void stopThreads() {
        this.stopped = true;
        notifyAll();
        poolnumber--;
    }

    /**
     * Init method.<br/>
     * This method is called before the start method.
     * @throws EZBComponentException if the initialization has failed.
     */
    public void init() throws EZBComponentException {
        // init
    }

    /**
     * Start method.<br/>
     * This method is called after the init method.
     * @throws EZBComponentException if the start has failed.
     */
    public void start() throws EZBComponentException {

        if (this.transactionComponent == null) {
            logger.error("Component disabled as there is no transaction manager available");
            this.minpoolsz = 0;
            this.maxpoolsz = 0;
            return;
        }

        // Build threads for work.
        for (this.poolsz = 0; this.poolsz < this.minpoolsz; this.poolsz++) {
            ResourceWorkThread resourceWorkThread = new ResourceWorkThread(this, poolnumber, threadnumber++);
            resourceWorkThread.setDaemon(true);
            resourceWorkThread.start();
        }
        logger.info("Settings: minThreads={0},maxThreads={1},txTimeout={2}s", Integer.valueOf(this.minpoolsz), Integer
                .valueOf(this.maxpoolsz), Long.valueOf(this.waitingTime / ONE_SECOND));
    }

    /**
     * Stop method.<br/>
     * This method is called when component needs to be stopped.
     * @throws EZBComponentException if the stop is failing.
     */
    public void stop() throws EZBComponentException {
        // Stop threads of the workmanager
        stopThreads();
        logger.info("WorkManager Stopped");
    }

    /**
     * @return transaction component.
     */
    public TMComponent getTransactionComponent() {
        return this.transactionComponent;
    }

    /**
     * Sets the transaction component.
     * @param transactionComponent the given transaction component.
     */
    public void setTransactionComponent(final TMComponent transactionComponent) {
        this.transactionComponent = transactionComponent;
    }


}
