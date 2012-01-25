/**
 * EasyBeans
 * Copyright (C) 2006,2007 Bull S.A.S.
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
 * $Id: EZBContainerLifeCycleCallback.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.api;

import org.ow2.easybeans.api.binding.EZBRef;


/**
 * This interface should be implemented by users which want to be notified
 * at some lifecycle step of the container.
 * @author Florent Benoit
 */
public interface EZBContainerLifeCycleCallback {

    /**
     * Called when container is starting.
     * @param info some information on the container which is starting.
     * @throws LifeCycleCallbackException if the invocation of the callback failed
     */
    void start(EZBContainerCallbackInfo info) throws LifeCycleCallbackException;


    /**
     * Called when container is stopping.
     * @param info some information on the container which is stopping.
     * @throws LifeCycleCallbackException if the invocation of the callback failed
    */
    void stop(EZBContainerCallbackInfo info) throws LifeCycleCallbackException;

    /**
     * Called before binding a reference into the registry.
     * @param info some information on the container which is running.
     * @param reference a reference on the bean that will be bound
     * @throws LifeCycleCallbackException if the invocation of the callback failed
     */
    void beforeBind(EZBContainerCallbackInfo info, EZBRef reference) throws LifeCycleCallbackException;

}
