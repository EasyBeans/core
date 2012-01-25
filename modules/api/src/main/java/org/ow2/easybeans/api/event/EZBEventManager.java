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
 * $Id: EZBEventManager.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.api.event;

import java.util.List;

import org.ow2.easybeans.component.itf.EZBEventComponent;

/**
 * Interface for events managers.
 * @author eyindanga
 */
public interface EZBEventManager {
    /**
     * Set the event component.
     * @param eventComponent the event component to set
     */
    void setEventComponent(EZBEventComponent eventComponent);

    /**
     * gets the event component.
     * @return the event component.
     */
    EZBEventComponent getEventComponent();
    /**
     * Add an event listener.
     * @param eventListener the evnt listener to add.
     */
    void addEventListener(EZBEventListener eventListener);
    /**
     * Gets all the event listeners.
     * @return the event listeners.
     */
    List<EZBEventListener> getEventListeners();

}
