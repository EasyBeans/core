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

package org.ow2.easybeans.api;

import javax.ejb.EJBException;
import javax.ejb.ScheduleExpression;
import javax.ejb.Timer;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;

import org.ow2.easybeans.api.bean.info.IMethodInfo;

/**
 * Defines the EasyBeans timer service.
 * It has some methods used to create timers for @Schedule annotated methods
 * @author Florent Benoit
 */
public interface EZBTimerService extends TimerService {

    /**
     * Create a calendar-based timer based on the input schedule expression.
     * @param schedule a schedule expression describing the timeouts for this timer.
     * @param timerConfig timer configuration.
     * @return the newly created Timer.
     * @throws IllegalArgumentException If Schedule represents an invalid schedule expression.
     * @throws IllegalStateException If this method is invoked while the instance is in a state that does not allow access to this
     * method.
     * @throws EJBException If this method could not complete due to a system-level failure.
     * @since EJB 3.1 version.
     */
    public Timer createCalendarTimer(final ScheduleExpression schedule, final TimerConfig timerConfig, final IMethodInfo methodInfo)
            throws IllegalArgumentException, IllegalStateException, EJBException;
}
