/**
 * EasyBeans
 * Copyright (C) 2008 Bull S.A.S.
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
 * $Id: AbstractStatistic.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.statistic;

import java.util.LinkedList;
import java.util.List;

import org.ow2.easybeans.api.event.EZBEventListener;
import org.ow2.easybeans.api.event.EZBEventManager;
import org.ow2.easybeans.api.jmx.EZBMBeanAttribute;
import org.ow2.easybeans.api.jmx.EZBMBeanOperation;
import org.ow2.easybeans.api.statistic.EZBStatistic;
import org.ow2.easybeans.component.itf.EZBEventComponent;
import org.ow2.easybeans.component.itf.EZBJmxComponent;

/**
 * Abstract implementation of the EZBStatistic class.
 * @author missonng
 */
public abstract class AbstractStatistic implements EZBStatistic {
    /**
     * The AbstractStatistic name.
     */
    private String name;

    /**
     * The AbstractStatistic description.
     */
    private String description;

    /**
     * The AbstractStatistic unit.
     */
    private String unit;

    /**
     * The AbstractStatistic start time.
     */
    private long startTime;

    /**
     * The AbstractStatistic last sample time.
     */
    private long lastTime;

    /**
     * The AbstractStatistic availability.
     */
    private boolean available;

    /**
     * The statistic factory id.
     */
    private String statisticFactoryId;

    /**
     * The statistic provider id.
     */
    private String statisticProviderId;

    /**
     * The jmx component.
     */
    private EZBJmxComponent jmxComponent;

    /**
     * The event manager.
     */

    private EZBEventManager eventManager = null;

    /**
     * The MBean attributes.
     */
    private List<EZBMBeanAttribute> mbeanAttributes;

    /**
     * The MBean operations.
     */
    private List<EZBMBeanOperation> mbeanOperations;

    /**
     * The AbstractStatistic constructor.
     * @param name The AbstractStatistic name.
     * @param description The AbstractStatistic description.
     * @param unit The AbstractStatistic unit.
     * @param statisticFactoryId The statistic provider id.
     * @param statisticProviderId The statistic factory id.
     * @param eventComponent The event component.
     * @param jmxComponent The jmx component.
     */
    public AbstractStatistic(final String name, final String description, final String unit,
            final String statisticFactoryId, final String statisticProviderId,
            final EZBEventComponent eventComponent, final EZBJmxComponent jmxComponent) {
        this.name = name;
        this.description = description;
        this.unit = unit;
        this.startTime = System.currentTimeMillis();
        this.lastTime = System.currentTimeMillis();
        this.available = false;
        this.statisticFactoryId = statisticFactoryId;
        this.statisticProviderId = statisticProviderId;
        this.jmxComponent = jmxComponent;
        this.eventManager = new StatisticEventManager(eventComponent);
        this.mbeanAttributes = new LinkedList<EZBMBeanAttribute>();
        this.mbeanOperations = new LinkedList<EZBMBeanOperation>();
    }

    /**
     * Get the statistic name.
     * @return The statistic name.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Get the statistic description.
     * @return The statistic description.
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * Get the statistic unit.
     * @return The statistic unit.
     */
    public String getUnit() {
        return this.unit;
    }

    /**
     * Get the statistic start time.
     * @return The statistic start time.
     */
    public long getStartTime() {
        return this.startTime;
    }

    /**
     * Get the statistic last sample time.
     * @return The statistic last sample time.
     */
    public long getLastSampleTime() {
        return this.lastTime;
    }

    /**
     * Set the statistic last sample time.
     * @param lastTime The statistic last sample time.
     */
    public void setLastSampleTime(final long lastTime) {
        this.lastTime = lastTime;
    }

    /**
     * Get the statistic id.
     * @return The statistic factory id.
     */
    public String getStatisticId() {
        return getStatisticProviderId().replace(":", "\\:") + "::" + getStatisticFactoryId().replace(":", "\\:");
    }

    /**
     * Get the statistic factory id.
     * @return The statistic factory id.
     */
    public String getStatisticFactoryId() {
        return this.statisticFactoryId;
    }

    /**
     * Get the statistic provider id.
     * @return The statistic provider id.
     */
    public String getStatisticProviderId() {
        return this.statisticProviderId;
    }

    /**
     * Reset the statistic.
     */
    public synchronized void reset() {
        this.startTime = System.currentTimeMillis();
        this.lastTime = System.currentTimeMillis();
    }

    /**
     * Activate the statistic.
     */
    public synchronized void activate() {
        if (!this.available) {
            this.available = true;

            for (EZBEventListener eventListener : this.eventManager.getEventListeners()) {
                this.eventManager.getEventComponent().registerEventListener(eventListener);
            }

            for (EZBMBeanAttribute mbeanAttribute : this.mbeanAttributes) {
                this.jmxComponent.registerMBeanAttribute(mbeanAttribute);
            }

            for (EZBMBeanOperation mbeanOperation : this.mbeanOperations) {
                this.jmxComponent.registerMBeanOperation(mbeanOperation);
            }
        }
    }

    /**
     * Deactivate the statistic.
     */
    public synchronized void deactivate() {
        if (this.available) {
            this.available = false;

            for (EZBEventListener eventListener : this.eventManager.getEventListeners()) {
                this.eventManager.getEventComponent().unregisterEventListener(eventListener);
            }

            for (EZBMBeanAttribute mbeanAttribute : this.mbeanAttributes) {
                this.jmxComponent.unregisterMBeanAttribute(mbeanAttribute);
            }

            for (EZBMBeanOperation mbeanOperation : this.mbeanOperations) {
                this.jmxComponent.unregisterMBeanOperation(mbeanOperation);
            }
        }
    }

    /**
     * Get the statistic availability.
     * @return The statistic availability.
     */
    public synchronized boolean isAvailable() {
        return this.available;
    }

    /**
     * Get the event component.
     * @return The event component.
     */
    protected EZBEventComponent getEventComponent() {
        return this.eventManager.getEventComponent();
    }

    /**
     * Get the jmx component.
     * @return The jmx component.
     */
    protected EZBJmxComponent getJmxComponent() {
        return this.jmxComponent;
    }

    /**
     * Add an event listener.
     * @param eventListener The event listener to add.
     */
    protected synchronized void addEventListener(final EZBEventListener eventListener) {
        this.eventManager.addEventListener(eventListener);

        if (this.available) {
            this.eventManager.getEventComponent().registerEventListener(eventListener);
        }
    }

    /**
     * Remove an event listener.
     * @param eventListener The event listener to remove.
     */
    protected synchronized void removeEventListener(final EZBEventListener eventListener) {
        this.eventManager.getEventListeners().remove(eventListener);

        if (this.available) {
            this.eventManager.getEventComponent().unregisterEventListener(eventListener);
        }
    }

    /**
     * Add a MBean attribute.
     * @param mbeanAttribute The MBean attribute to add.
     */
    protected synchronized void addMBeanAttribute(final EZBMBeanAttribute mbeanAttribute) {
        this.mbeanAttributes.add(mbeanAttribute);

        if (this.available) {
            this.jmxComponent.registerMBeanAttribute(mbeanAttribute);
        }
    }

    /**
     * Remove a MBean attribute.
     * @param mbeanAttribute The MBean attribute to remove.
     */
    protected synchronized void removeMBeanAttribute(final EZBMBeanAttribute mbeanAttribute) {
        this.mbeanAttributes.remove(mbeanAttribute);

        if (this.available) {
            this.jmxComponent.unregisterMBeanAttribute(mbeanAttribute);
        }
    }

    /**
     * Add a MBean operation.
     * @param mbeanOperation The MBean operation to add.
     */
    protected synchronized void addMBeanOperation(final EZBMBeanOperation mbeanOperation) {
        this.mbeanOperations.add(mbeanOperation);

        if (this.available) {
            this.jmxComponent.registerMBeanOperation(mbeanOperation);
        }
    }

    /**
     * Remove a MBean operation.
     * @param mbeanOperation The MBean operation to remove.
     */
    protected synchronized void removeMBeanOperation(final EZBMBeanOperation mbeanOperation) {
        this.mbeanOperations.remove(mbeanOperation);

        if (this.available) {
            this.jmxComponent.unregisterMBeanOperation(mbeanOperation);
        }
    }

    /**
     * @return the eventManager
     */
    public EZBEventManager getEventManager() {
        return this.eventManager;
    }

    /**
     * @param eventManager the eventManager to set
     */
    public void setEventManager(final EZBEventManager eventManager) {
        this.eventManager = eventManager;
    }
}
