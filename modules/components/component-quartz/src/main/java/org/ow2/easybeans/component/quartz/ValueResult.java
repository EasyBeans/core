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

package org.ow2.easybeans.component.quartz;

/**
 * Result value.
 * @author Florent Benoit
 */
public class ValueResult {

    /**
     * Default result is 0.
     */
    private int result = 0;

    /**
     * By default, do not need to increment the next field.
     */
    private boolean needsIncrement = false;

    /**
     * @return the value
     */
    public int getResult() {
        return this.result;
    }

    /**
     * Sets the result value.
     * @param result the value
     */
    public void setResult(final int result) {
        this.result = result;
    }

    /**
     * @return true if we needs to increment the next field
     */
    public boolean needsIncrement() {
        return this.needsIncrement;
    }

    /**
     * Sets the value for the increment flag.
     * @param needsIncrement true/false
     */
    public void setNeedsIncrement(final boolean needsIncrement) {
        this.needsIncrement = needsIncrement;
    }




}
