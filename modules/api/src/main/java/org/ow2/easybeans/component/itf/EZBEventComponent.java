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
 * $Id: EZBEventComponent.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.component.itf;

import org.ow2.easybeans.api.EZBJ2EEManagedObject;
import org.ow2.easybeans.api.event.EZBEventListener;
import org.ow2.easybeans.component.api.EZBComponent;
import org.ow2.util.event.api.IEventDispatcher;
import org.ow2.util.event.api.IEventService;

/**
 * Interface of the EasyBeans event component.
 * @author missonng
 */
public interface EZBEventComponent extends EZBComponent {
    /**
     * Register a new J2EE managed object.<br>
     * If a J2EE managed object with the same id is already registered, it will be unregistered first.
     * @param object The J2EE managed object to register.
     * @param dispatcher The event dispatcher for this J2EE managed object.
     */
    void registerJ2EEManagedObject(EZBJ2EEManagedObject object, IEventDispatcher dispatcher);

    /**
     * Unregister a J2EE managed object.
     * @param object The J2EE managed object to unregister.
     */
    void unregisterJ2EEManagedObject(EZBJ2EEManagedObject object);

    /**
     * Register a new event listener.<br>
     * The listener will automatically be register with each dispatcher matching his filter.
     * @param eventListener The listener to register.
     */
    void registerEventListener(EZBEventListener eventListener);

    /**
     * Unregister an event listener.
     * @param eventListener The listener to unregister.
     */
    void unregisterEventListener(EZBEventListener eventListener);

    /**
     * @return the event service used to associate Dispatcher and Listener.
     */
    IEventService getEventService();

    /**
     * Creates a new IEventDispatcher.
     * @return a new IEventDispatcher.
     */
    IEventDispatcher createEventDispatcher();
}
