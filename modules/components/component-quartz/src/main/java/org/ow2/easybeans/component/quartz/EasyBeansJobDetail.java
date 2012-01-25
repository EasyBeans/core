/**
 * EasyBeans
 * Copyright (C) 2007 Bull S.A.S.
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
 * $Id: EasyBeansJobDetail.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.component.quartz;

import org.quartz.JobDataMap;
import org.quartz.JobDetail;

/**
 * This class is used to give more parameters to the Invoker Job. It extends the
 * basic class and then add some info in the JobDataMap.
 * @author Florent Benoit
 */
public class EasyBeansJobDetail extends JobDetail {

    /**
     * Serial version UID for serializable classes.
     */
    private static final long serialVersionUID = 5194296209118376082L;

    /**
     * Key for the data that are stored in the Job Data Map.
     */
    public static final String DATA_KEY = "data";

    /**
     * Create an EasyBeans Job Detail by specifying a given name, group and data.
     * The EasyBeansJob class will be used as Job.
     * @param name the name of this job detail
     * @param group the group of this job detail
     * @param jobDetailData The data that are stored in the job detail. It allows to get the
     * serializable info object that can be given by the user and to retrieve
     * the right bean.
     */
    public EasyBeansJobDetail(final String name, final String group, final EasyBeansJobDetailData jobDetailData) {
        super(name, group, EasyBeansJob.class);

        // Get the data map (that is built if null)
        JobDataMap jobDataMap = getJobDataMap();

        // Add the data for this job
        jobDataMap.put(DATA_KEY, jobDetailData);

    }

    /**
     * Gets the data for this Job Detail.
     * @return the data for this job detail.
     */
    public EasyBeansJobDetailData getJobDetailData() {
        return (EasyBeansJobDetailData) getJobDataMap().get(DATA_KEY);
    }

}
