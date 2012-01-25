/**
 * EasyBeans
 * Copyright (C) 2009 Bull S.A.S.
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
 * $Id: EventLifeCycleClusteredBean.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.event.lifecycle;

import java.util.List;

import org.ow2.easybeans.api.binding.EZBRef;
import org.ow2.easybeans.api.event.bean.EZBClusteredBeanEvent;
import org.ow2.util.event.api.IEventListener;

/**
 * @author Florent Benoit
 */
public class EventLifeCycleClusteredBean implements EZBClusteredBeanEvent {

    /**
     * The next event number.
     */
    private static long nextNumber = 1;

    /**
     * The event number.
     */
    private long number;

    /**
     * The event time.
     */
    private long time;

    /**
     * The event source.
     */
    private String source;

    /**
     * <code>STARTING or STOPPING</code>.
     */
    private String state = null;

    /**
     * Beans references.
     */
    private List<EZBRef> references = null;

    /**
     * @return the state.
     */
    public String getState() {
        return this.state;
    }

    /**
     * Sets the state.
     * @param state the given state
     */
    public void setState(final String state) {
        this.state = state;
    }

    /**
     * default contructor.
     */
    public EventLifeCycleClusteredBean() {
        this.number = EventLifeCycleClusteredBean.nextNumber++;
        this.time = System.currentTimeMillis();
    }

    /**
     * Set beans references.
     * @param references the JNDI references
     */
    public void setReferences(final List<EZBRef> references) {
        this.references = references;
    }

    /**
     * Constructor using fields.
     * @param source source of the event
     * @param state the given state
     * @param references the Bean references
     */
    public EventLifeCycleClusteredBean(final String source, final String state, final List<EZBRef> references) {
        this();
        this.state = state;
        this.source = source;
        this.references = references;
    }

    /**
     * @return EasyBeans references.
     */
    public List<EZBRef> getReferences() {
        return this.references;
    }

    /**
     * @return the id of event provider.
     */
    public String getEventProviderId() {
        return this.source;
    }

    /**
     * @return events number.
     */
    public long getNumber() {
        return this.number;
    }

    /**
     * @return time.
     */
    public long getTime() {
        return this.time;
    }

    /**
     * Check permissions.
     * @param arg0 the given permission
     * @return true
     */
    public boolean checkPermission(final IEventListener arg0) {
        return true;
    }

}
