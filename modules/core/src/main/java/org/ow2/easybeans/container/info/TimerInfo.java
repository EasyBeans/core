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

import javax.ejb.ScheduleExpression;
import javax.ejb.TimerConfig;

import org.ow2.easybeans.api.bean.info.IMethodInfo;
import org.ow2.easybeans.api.bean.info.ITimerInfo;

/**
 * Defines info about timers.
 * @author Florent Benoit
 */
public class TimerInfo implements ITimerInfo {

    private ScheduleExpression scheduleExpression = null;

    private TimerConfig timerConfig = null;

    private IMethodInfo methodInfo;

    public void setScheduleExpression(final ScheduleExpression scheduleExpression) {
        this.scheduleExpression = scheduleExpression;
    }

    public ScheduleExpression getScheduleExpression() {
        return this.scheduleExpression;
    }

    public void setTimerConfig(final TimerConfig timerConfig) {
        this.timerConfig = timerConfig;
    }

    public TimerConfig getTimerConfig() {
        return this.timerConfig;
    }

    public void setMethodInfo(final IMethodInfo methodInfo) {
        this.methodInfo = methodInfo;
    }

    public IMethodInfo getMethodInfo() {
        return this.methodInfo;
    }

}
