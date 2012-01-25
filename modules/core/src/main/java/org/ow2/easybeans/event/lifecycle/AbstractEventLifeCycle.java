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
 * $Id: AbstractEventLifeCycle.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.event.lifecycle;

import org.ow2.easybeans.api.event.lifecycle.EZBEventLifeCycle;
import org.ow2.easybeans.event.AbstractEvent;

/**
 * Abstract parent class for all EasyBeans lifecycle events.
 * @author missonng
 */
public abstract class AbstractEventLifeCycle extends AbstractEvent implements EZBEventLifeCycle {
    /**
     * The default constructor.
     * @param source The event source.
     */
    public AbstractEventLifeCycle(final String source) {
        super(source);
    }
}
