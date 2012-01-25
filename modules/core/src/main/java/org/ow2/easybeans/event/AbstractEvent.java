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
 * $Id: AbstractEvent.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.event;

import org.ow2.easybeans.api.event.EZBEvent;
import org.ow2.util.event.api.IEventListener;

/**
 * Abstract parent class for all EasyBeans events.
 * @author missonng
 */
public abstract class AbstractEvent implements EZBEvent {
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
     * The default constructor.
     * @param source The event source.
     */
    public AbstractEvent(final String source) {
        this.number = AbstractEvent.nextNumber++;
        this.time = System.currentTimeMillis();
        this.source = source;
    }

    /**
     * Get the event number.
     * @return The event number.
     */
    public long getNumber() {
        return this.number;
    }

    /**
     * Get the event time (ms).
     * @return The event time (ms).
     */
    public long getTime() {
        return this.time;
    }

    /**
     * Get the event provider id.
     * @return The event provider id.
     */
    public String getEventProviderId() {
        return this.source;
    }

    /**
     * Check whether the event listener has permission to receive this event.
     * @param eventListener The event listener to check.
     * @return True if the listener has the permission, false otherwise.
     */
    public boolean checkPermission(final IEventListener eventListener) {
        return true;
    }
}
