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
 * $Id: EasyBeansJobDetailData.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.component.quartz;

import java.io.Serializable;

import javax.ejb.Timer;

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
     * @return the serializable info used for the timer expiration notification.
     */
    public Serializable getInfo() {
        return info;
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
        return containerId;
    }

    /**
     * @return the name of the factory.
     */
    public String getFactoryName() {
        return factoryName;
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
        return easyBeansServerID;
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
        return timer;
    }

}
