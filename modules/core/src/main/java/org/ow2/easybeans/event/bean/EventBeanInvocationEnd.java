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
 * $Id: EventBeanInvocationEnd.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.event.bean;

import org.ow2.easybeans.api.event.bean.EZBEventBeanInvocationEnd;

/**
 * EasyBeans bean invocation begin events.
 * @author missonng
 */
public class EventBeanInvocationEnd extends AbstractEventBeanInvocation implements EZBEventBeanInvocationEnd {
    /**
     * The bean invocation result.
     */
    private Object result;

    /**
     * The default constructor.
     * @param source The event source.
     * @param number The invocation number.
     * @param result The invocation result.
     */
    public EventBeanInvocationEnd(final String source, final long number, final Object result) {
        super(source, number);
        this.result = result;
    }

    /**
     * Get the bean invocation result.
     * @return The bean invocation result.
     */
    public Object getResult() {
        return this.result;
    }
}
