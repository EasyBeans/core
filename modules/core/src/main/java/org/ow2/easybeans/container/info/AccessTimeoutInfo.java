/**
 * EasyBeans
 * Copyright (C) 2012 Bull S.A.S.
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
 * $Id$
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.container.info;

import java.util.concurrent.TimeUnit;

import org.ow2.easybeans.api.bean.info.IAccessTimeoutInfo;

/**
 * AccessTimeout for a given method (if specified).
 * @author Florent Benoit
 */
public class AccessTimeoutInfo implements IAccessTimeoutInfo {

    /**
     * Value.
     */
    private long value;

    /**
     * Time unit.
     */
    private TimeUnit timeUnit;


    /**
     * Access Timeout with specified values.
     * @param value the given wait time
     * @param timeUnit the unit
     */
    public AccessTimeoutInfo(final long value, final TimeUnit timeUnit) {
        this.value = value;
        this.timeUnit = timeUnit;
    }


    /**
     * @return time unit
     */
    public TimeUnit unit() {
        return this.timeUnit;
    }

    /**
     * @return value
     */
    public long value() {
        return this.value;
    }

    /**
     * @return string representation.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(AccessTimeoutInfo.class.getSimpleName());
        sb.append("[value=");
        sb.append(this.value);
        sb.append(", unit=");
        sb.append(this.timeUnit);
        sb.append("]");
        return sb.toString();
    }



}
