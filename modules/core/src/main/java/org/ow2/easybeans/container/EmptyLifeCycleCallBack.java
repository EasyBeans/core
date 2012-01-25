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
 * $Id: EmptyLifeCycleCallBack.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.container;

import org.ow2.easybeans.api.EZBContainerCallbackInfo;
import org.ow2.easybeans.api.EZBContainerLifeCycleCallback;
import org.ow2.easybeans.api.LifeCycleCallbackException;
import org.ow2.easybeans.api.binding.EZBRef;

/**
 * Provide callbacks that do nothing.
 * @author WEI Zhouyue & ZHU Ning & BOUZONNET Loris
 */
public abstract class EmptyLifeCycleCallBack implements EZBContainerLifeCycleCallback {

    /**
     * Called when container is starting.
     * @param info some information on the container which is starting.
     * @throws LifeCycleCallbackException if the invocation of the callback failed
     */
    public void start(final EZBContainerCallbackInfo info) throws LifeCycleCallbackException {
        // Do nothing
    }

    /**
     * Called when container is stopping.
     * @param info some information on the container which is stopping.
     * @throws LifeCycleCallbackException if the invocation of the callback failed
    */
    public void stop(final EZBContainerCallbackInfo info) throws LifeCycleCallbackException {
        // Do nothing
    }

    /**
     * Called before binding a reference into the registry.
     * @param info some information on the container which is running.
     * @param reference a reference on the bean that will be bound
     * @throws LifeCycleCallbackException if the invocation of the callback failed
     */
    public void beforeBind(final EZBContainerCallbackInfo info, final EZBRef reference) throws LifeCycleCallbackException {
        // Do nothing
    }



}
