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
 * $Id: TimerComponent.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.component.itf;

import java.util.Map;

import org.ow2.easybeans.api.EZBTimerService;
import org.ow2.easybeans.api.Factory;
import org.ow2.easybeans.component.api.EZBComponent;
import org.ow2.easybeans.component.api.EZBComponentException;
import org.ow2.easybeans.component.util.TimerCallback;

/**
 * Interface for the component that provides the EJB timer service.
 * @author Florent Benoit
 */
public interface TimerComponent extends EZBComponent {


    /**
     * Gets an EJB timer service through this component.
     * @param factory an EasyBeans factory providing timeout notification.
     * @return an EJB timer service
     */
    EZBTimerService getTimerService(final Factory<?, ?> factory);

    /**
     * Schedule a recurring call to a {@link TimerCallback} starting immediately.
     *
     * @param id                 if there is already a Callback scheduled with the same id then an exception is thrown
     * @param interval           Recurrence interval in milliseconds
     * @param callback           The {@link TimerCallback} to notify
     * @param callbackProperties Properties to be send to the {@link TimerCallback#execute(java.util.Map)} method
     */
    void schedule(String id, long interval, TimerCallback callback, Map<String, Object> callbackProperties) throws EZBComponentException;

    /**
     * Unschedule a callback timer for a given id
     *
     * @param id the scheduled timer id
     */
    void unschedule(String id) throws EZBComponentException;

}
