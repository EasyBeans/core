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
 * $Id: StatisticEventManager.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.statistic;

import java.util.LinkedList;
import java.util.List;

import org.ow2.easybeans.api.event.EZBEventListener;
import org.ow2.easybeans.api.event.EZBEventManager;
import org.ow2.easybeans.component.itf.EZBEventComponent;

/**
 * Event manager for statistics.
 * @author eyindanga
 */
public class StatisticEventManager implements EZBEventManager {

    /**
     * The event listeners.
     */
    private List<EZBEventListener> eventListeners;

    /**
     * The event component.
     */
    private EZBEventComponent eventComponent = null;

    /**
     * Default constructor.
     */
    public StatisticEventManager() {
        this.eventListeners = new LinkedList<EZBEventListener>();
    }

    /**
     * Constructor.
     * @param eventComponent an event component.
     */
    public StatisticEventManager(final EZBEventComponent eventComponent) {
        this.eventComponent = eventComponent;
        this.eventListeners = new LinkedList<EZBEventListener>();
    }

    /**
     * Constructor.
     * @param eventComponent an event component.
     * @param eventListeners event listeners.
     */
    public StatisticEventManager(final EZBEventComponent eventComponent, final List<EZBEventListener> eventListeners) {
        this.eventComponent = eventComponent;
        this.eventListeners = eventListeners;
    }

    /**
     * Adds an event listener.
     * @param eventListener the evnt listener to add.
     */
    public void addEventListener(final EZBEventListener eventListener) {
        this.eventListeners.add(eventListener);
    }

    /**
     * Gets the event component.
     * @return the event component.
     */
    public EZBEventComponent getEventComponent() {
        return this.eventComponent;
    }

    /**
     * Gets all the event listeners.
     * @return the event listeners.
     */
    public List<EZBEventListener> getEventListeners() {
        return this.eventListeners;
    }

    /**
     * Sets the event component.
     * @param eventComponent the event component to set
     */
    public void setEventComponent(final EZBEventComponent eventComponent) {
        this.eventComponent = eventComponent;
    }

}
