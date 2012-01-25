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
 * $Id: ResourceWork.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.component.jca.workmanager;

import javax.resource.spi.work.ExecutionContext;
import javax.resource.spi.work.Work;
import javax.resource.spi.work.WorkListener;

import org.ow2.util.log.Log;
import org.ow2.util.log.LogFactory;

/**
 * This class defines a work of the JCA API by adding some properties around
 * this work.
 * @author Philippe Durieux (JOnAS)
 * @author Florent Benoit (EasyBeans)
 */
public class ResourceWork {

    /**
     * Logger.
     */
    private static Log logger = LogFactory.getLog(ResourceWorkThread.class);

    /**
     * Work object that is wrapped.
     */
    private Work work;

    /**
     * Timeout for the given work.
     */
    private long timeout;

    /**
     * JCA Execution context (contains information about transactions).
     */
    private ExecutionContext executionContext;

    /**
     * Listener that is notified when work are
     * accepted/rejected/started/completed.
     */
    private WorkListener workListener;

    /**
     * Creation of this object.
     */
    private long creationTime;

    /**
     * This work has been started or not ? (default = false).
     */
    private boolean started = false;

    /**
     * Default constructor : build a wrapper around the given work.
     * @param work the given work
     * @param timeout the timeout of this work
     * @param executionContext the context for the given work
     * @param workListener the listener on this work object
     */
    @SuppressWarnings("boxing")
    public ResourceWork(final Work work, final long timeout, final ExecutionContext executionContext,
            final WorkListener workListener) {
        this.work = work;
        this.timeout = timeout;
        this.executionContext = executionContext;
        this.workListener = workListener;
        this.creationTime = System.currentTimeMillis();
        if (logger.isDebugEnabled()) {
            logger.debug("Timeout value is {0}", timeout);
        }
    }

    /**
     * @return the work object
     */
    public Work getWork() {
        return this.work;
    }

    /**
     * @return the timeout of this object
     */
    public long getTimeout() {
        return this.timeout;
    }

    /**
     * @return the execution context of this work
     */
    public ExecutionContext getExecutionContext() {
        return this.executionContext;
    }

    /**
     * @return the listener of this work.
     */
    public WorkListener getWorkListener() {
        return this.workListener;
    }

    /**
     * @return the creation time of this object.
     */
    public long getCreationTime() {
        return this.creationTime;
    }

    /**
     * @return true if the work has been started, else false.
     */
    public boolean isStarted() {
        return this.started;
    }

    /**
     * Sets the started mode to true.
     */
    public void setStarted() {
        this.started = true;
    }
}
