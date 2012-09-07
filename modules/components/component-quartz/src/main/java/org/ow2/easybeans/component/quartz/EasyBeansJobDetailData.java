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
 * $Id: EasyBeansJobDetailData.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.component.quartz;

import java.io.Serializable;

import javax.ejb.ScheduleExpression;
import javax.ejb.Timer;

import org.ow2.easybeans.api.bean.info.IMethodInfo;

/**
 * Data that are stored in the JobDetail. The data needs to be serializable.
 * @author Florent Benoit
 */
public class EasyBeansJobDetailData implements Serializable {

    /**
     * Serial version UID for serializable classes.
     */
    private static final long serialVersionUID = 8707678125021056156L;

    /**
     * Reference on the id of the EasyBeans server. This will allow to get back
     * the Quartz scheduler.
     */
    private Integer easyBeansServerID;

    /**
     * Container id.
     */
    private String containerId = null;

    /**
     * Factory name.
     */
    private String factoryName = null;

    /**
     * Application information to be delivered along with the timer expiration
     * notification.
     */
    private Serializable info = null;

    /**
     * The timer object (that is transient).
     * It is used only on the same JVM.
     */
    private transient Timer timer = null;

    /**
     * Callback method for the timeout.
     */
    private transient IMethodInfo methodInfo = null;

    /**
     * Schedule Expression for calendar based timer.
     */
    private ScheduleExpression scheduleExpression = null;

    /**
     * Persistent flag for the timers.
     */
    private boolean isPersistent = false;


    /**
     * @return the serializable info used for the timer expiration notification.
     */
    public Serializable getInfo() {
        return this.info;
    }

    /**
     * Sets the serializable info used for the timer expiration notification.
     * @param info the given info
     */
    public void setInfo(final Serializable info) {
        this.info = info;
    }

    /**
     * Sets the container ID.
     * @param containerId the identifier of the container.
     */
    public void setContainerId(final String containerId) {
        this.containerId = containerId;
    }

    /**
     * @return the container id.
     */
    protected String getContainerId() {
        return this.containerId;
    }

    /**
     * @return the name of the factory.
     */
    public String getFactoryName() {
        return this.factoryName;
    }

    /**
     * Sets the factory's name.
     * @param factoryName the name of the factory.
     */
    public void setFactoryName(final String factoryName) {
        this.factoryName = factoryName;
    }

    /**
     * Sets the Server ID of the EasyBeans instance.
     * @param easyBeansServerID the ID of the EasyBeans server
     */
    public void setEasyBeansServerID(final Integer easyBeansServerID) {
        this.easyBeansServerID = easyBeansServerID;
    }

    /**
     * Gets the Server ID of the EasyBeans instance.
     * @return the ID of the EasyBeans server
     */
    public Integer getEasyBeansServerID() {
        return this.easyBeansServerID;
    }


    /**
     * Sets the timer object.
     * @param timer the given timer
     */
    public void setTimer(final Timer timer) {
        this.timer = timer;
    }

    /**
     * Gets the timer object.
     * @return the timer object
     */
    public Timer getTimer() {
        return this.timer;
    }

    /**
     * Sets the callback method info.
     * @param methodInfo the callback method
     */
    public void setMethodInfo(final IMethodInfo methodInfo) {
        this.methodInfo = methodInfo;
    }


    /**
     * @return callback method
     */
    public IMethodInfo getMethodInfo() {

        return this.methodInfo;
    }

    /**
     * Sets the sechedule expression if it's a calendar based timer.
     * @param scheduleExpression the expression
     */
    public void setScheduleExpression(final ScheduleExpression scheduleExpression) {
        this.scheduleExpression = scheduleExpression;
    }

    /**
     * @return calendar based timer expression or null
     */
    public ScheduleExpression getScheduleExpression() {
        return this.scheduleExpression;
    }

    /**
     * Sets the persistent mode flag.
     * @param isPersistent true/false
     */
    public void setPersistent(final boolean isPersistent) {
        this.isPersistent = isPersistent;
    }

    /**
     * @return true if this timer is a persistent timer.
     */
    public boolean isPersistent() {
        return this.isPersistent;
    }

}
