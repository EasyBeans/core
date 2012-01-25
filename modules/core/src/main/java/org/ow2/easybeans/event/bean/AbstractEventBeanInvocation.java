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
 * $Id: AbstractEventBeanInvocation.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.event.bean;

import org.ow2.easybeans.api.event.bean.EZBEventBeanInvocation;
import org.ow2.easybeans.event.AbstractEvent;

/**
 * Abstract parent class for all EasyBeans bean invocation events.
 * @author missonng
 */
public abstract class AbstractEventBeanInvocation extends AbstractEvent implements EZBEventBeanInvocation {
    /**
     * The invocation number.
     */
    private long number;

    /**
     * The default constructor.
     * @param source The event source.
     * @param number The invocation number.
     */
    public AbstractEventBeanInvocation(final String source, final long number) {
        super(source);
        this.number = number;
    }

    /**
     * Get the invocation number.
     * @return The invocation number.
     */
    public long getInvocationNumber() {
        return this.number;
    }
}
