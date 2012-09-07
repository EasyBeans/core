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

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Job executing a call to {@link org.ow2.easybeans.component.util.TimerCallback#execute(java.util.Map)} using information
 * found in {@link CallbackJobDetailData}.
 *
 * @author Loic Albertin
 */
public class CallbackJob implements Job {

    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        CallbackJobDetailData jobDetailData =
                (CallbackJobDetailData) jobExecutionContext.getJobDetail().getJobDataMap().get(CallbackJobDetailData.CALLBACK_DATA_KEY);
        jobDetailData.getCallback().execute(jobDetailData.getCallbackProperties());
    }
}
