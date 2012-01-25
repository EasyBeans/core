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
 * $Id: EZBStatisticFactory.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.api.statistic;



/**
 * Interface for all EasyBeans statistic factory.
 * @author missonng
 */
public interface EZBStatisticFactory {
    /**
     * Get the statistic factory id.
     * @return The statistic factory id.
     */
    String getStatisticFactoryId();

    /**
     * Get the statistic provider filter.<br>
     * The statistic provider filter is a regular expression that define for which provider a statistic should be created.
     * @return The statistic provider filter.
     */
    String getStatisticProviderFilter();

    /**
     * Create a statistic for the given statistic provider.
     * @param statisticProviderId The statistic provider id.
     * @return The created statistic.
     */
    EZBStatistic createStatistic(String statisticProviderId);
}
