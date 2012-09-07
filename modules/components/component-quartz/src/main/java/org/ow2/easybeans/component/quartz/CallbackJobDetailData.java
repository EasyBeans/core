/*
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
 * $Id:$
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.component.quartz;

import java.io.Serializable;
import java.util.Map;

import org.ow2.easybeans.component.util.TimerCallback;
import org.quartz.JobDetail;

/**
 * Helper class to retrieve the specified {@link TimerCallback} and its properties.
 *
 * @author Loic Albertin
 */
public class CallbackJobDetailData implements Serializable {
    private static final long serialVersionUID = -5009350982358540834L;

    public static final String CALLBACK_DATA_KEY = "callback-job-detail-data";

    public static final String JOB_GROUP_NAME = "TimerCallback";

    private TimerCallback timerCallback;

    private Map<String, Object> callbackProperties;


    public CallbackJobDetailData(TimerCallback callback, Map<String, Object> callbackProperties) {
        this.timerCallback = callback;
        this.callbackProperties = callbackProperties;
    }

    /**
     * @return the associated {@link TimerCallback}
     */
    public TimerCallback getCallback() {
        return timerCallback;
    }

    /**
     * @return the associated {@link TimerCallback} properties
     */
    public Map<String, Object> getCallbackProperties() {
        return callbackProperties;
    }
}
