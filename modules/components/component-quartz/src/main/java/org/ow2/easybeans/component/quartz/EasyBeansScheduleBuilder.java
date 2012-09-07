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

import javax.ejb.ScheduleExpression;

import org.quartz.ScheduleBuilder;
import org.quartz.spi.MutableTrigger;

/**
 * Defines a schedule builder for building timer based on a Schedule Expression.
 * @author Florent Benoit
 */
public class EasyBeansScheduleBuilder extends ScheduleBuilder<EasyBeansScheduleTrigger> {

    /**
     * Schedule Expression.
     */
    private ScheduleExpression scheduleExpression;

    /**
     * Build a new builder for the given schedule expression.
     * @param scheduleExpression the given expression
     */
    public EasyBeansScheduleBuilder(final ScheduleExpression scheduleExpression) {
        if (scheduleExpression == null) {
            throw new IllegalArgumentException("scheduleExpression cannot be null");
        }
        this.scheduleExpression = scheduleExpression;
    }

    /**
     * @return a trigger that has been built by this builder
     */
    @Override
    protected MutableTrigger build() {
        return new EasyBeansScheduleTrigger(this.scheduleExpression);
    }


}
